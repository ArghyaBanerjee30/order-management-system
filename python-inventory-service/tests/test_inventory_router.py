"""
Integration tests for Inventory Router.
"""
import pytest
from fastapi.testclient import TestClient


class TestGetInventory:
    """Tests for GET /inventory/{product_id} endpoint."""

    def test_get_inventory_found(self, client, sample_inventory, sample_product):
        """Test getting existing inventory."""
        # Act
        response = client.get(f"/inventory/{sample_product.id}")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["product_id"] == sample_product.id
        assert data["available_quantity"] == 100
        assert data["reserved_quantity"] == 0
        assert "last_updated" in data

    def test_get_inventory_not_found(self, client):
        """Test getting inventory for non-existent product."""
        # Act
        response = client.get("/inventory/999")

        # Assert
        assert response.status_code == 404
        data = response.json()
        assert "not found" in data["message"].lower()


class TestReserveStock:
    """Tests for POST /inventory/reserve endpoint."""

    def test_reserve_stock_success(self, client, sample_inventory, sample_product):
        """Test successful stock reservation."""
        # Arrange
        request_data = {
            "product_id": sample_product.id,
            "quantity": 10
        }

        # Act
        response = client.post("/inventory/reserve", json=request_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "reserved successfully" in data["message"].lower()
        assert data["inventory"]["product_id"] == sample_product.id
        assert data["inventory"]["available_quantity"] == 90
        assert data["inventory"]["reserved_quantity"] == 10

    def test_reserve_stock_insufficient(self, client, sample_inventory, sample_product):
        """Test stock reservation with insufficient stock."""
        # Arrange
        request_data = {
            "product_id": sample_product.id,
            "quantity": 200  # More than available (100)
        }

        # Act
        response = client.post("/inventory/reserve", json=request_data)

        # Assert
        assert response.status_code == 400
        data = response.json()
        assert "insufficient stock" in data["message"].lower()
        assert data["details"]["product_id"] == sample_product.id
        assert data["details"]["available"] == 100
        assert data["details"]["requested"] == 200

    def test_reserve_stock_inventory_not_found(self, client):
        """Test stock reservation for non-existent inventory."""
        # Arrange
        request_data = {
            "product_id": 999,
            "quantity": 10
        }

        # Act
        response = client.post("/inventory/reserve", json=request_data)

        # Assert
        assert response.status_code == 404
        data = response.json()
        assert "not found" in data["message"].lower()

    def test_reserve_stock_invalid_quantity_zero(self, client, sample_product):
        """Test stock reservation with zero quantity."""
        # Arrange
        request_data = {
            "product_id": sample_product.id,
            "quantity": 0
        }

        # Act
        response = client.post("/inventory/reserve", json=request_data)

        # Assert
        assert response.status_code == 422
        data = response.json()
        assert "detail" in data

    def test_reserve_stock_invalid_quantity_negative(self, client, sample_product):
        """Test stock reservation with negative quantity."""
        # Arrange
        request_data = {
            "product_id": sample_product.id,
            "quantity": -10
        }

        # Act
        response = client.post("/inventory/reserve", json=request_data)

        # Assert
        assert response.status_code == 422

    def test_reserve_stock_multiple_times(self, client, sample_inventory, sample_product):
        """Test reserving stock multiple times."""
        # Arrange
        request_data = {
            "product_id": sample_product.id,
            "quantity": 30
        }

        # Act - Reserve first time
        response1 = client.post("/inventory/reserve", json=request_data)
        assert response1.status_code == 200
        data1 = response1.json()
        assert data1["inventory"]["available_quantity"] == 70
        assert data1["inventory"]["reserved_quantity"] == 30

        # Act - Reserve second time
        response2 = client.post("/inventory/reserve", json=request_data)
        assert response2.status_code == 200
        data2 = response2.json()
        assert data2["inventory"]["available_quantity"] == 40
        assert data2["inventory"]["reserved_quantity"] == 60


class TestReleaseStock:
    """Tests for POST /inventory/release endpoint."""

    def test_release_stock_success(self, client, sample_inventory, sample_product, db_session):
        """Test successful stock release."""
        # Arrange - First reserve some stock
        reserve_data = {"product_id": sample_product.id, "quantity": 20}
        client.post("/inventory/reserve", json=reserve_data)

        release_data = {"product_id": sample_product.id, "quantity": 20}

        # Act
        response = client.post("/inventory/release", json=release_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "released successfully" in data["message"].lower()
        assert data["inventory"]["available_quantity"] == 100
        assert data["inventory"]["reserved_quantity"] == 0

    def test_release_stock_partial(self, client, sample_inventory, sample_product):
        """Test partial stock release."""
        # Arrange - Reserve 30, release 10
        reserve_data = {"product_id": sample_product.id, "quantity": 30}
        client.post("/inventory/reserve", json=reserve_data)

        release_data = {"product_id": sample_product.id, "quantity": 10}

        # Act
        response = client.post("/inventory/release", json=release_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["inventory"]["available_quantity"] == 80
        assert data["inventory"]["reserved_quantity"] == 20

    def test_release_stock_inventory_not_found(self, client):
        """Test stock release for non-existent inventory."""
        # Arrange
        request_data = {"product_id": 999, "quantity": 10}

        # Act
        response = client.post("/inventory/release", json=request_data)

        # Assert
        assert response.status_code == 404
        data = response.json()
        assert "not found" in data["message"].lower()

    def test_release_stock_more_than_reserved(self, client, sample_inventory, sample_product):
        """Test releasing more stock than reserved."""
        # Arrange - Reserve 10, try to release 20
        reserve_data = {"product_id": sample_product.id, "quantity": 10}
        client.post("/inventory/reserve", json=reserve_data)

        release_data = {"product_id": sample_product.id, "quantity": 20}

        # Act
        response = client.post("/inventory/release", json=release_data)

        # Assert
        assert response.status_code == 400
        data = response.json()
        assert "insufficient" in data["message"].lower()

    def test_release_stock_invalid_quantity(self, client, sample_product):
        """Test stock release with invalid quantity."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 0}

        # Act
        response = client.post("/inventory/release", json=request_data)

        # Assert
        assert response.status_code == 422


class TestAddStock:
    """Tests for POST /inventory/add endpoint."""

    def test_add_stock_success(self, client, sample_inventory, sample_product):
        """Test successful stock addition."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 50}

        # Act
        response = client.post("/inventory/add", json=request_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "added successfully" in data["message"].lower()
        assert data["inventory"]["available_quantity"] == 150
        assert data["inventory"]["reserved_quantity"] == 0

    def test_add_stock_large_quantity(self, client, sample_inventory, sample_product):
        """Test adding large quantity of stock."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 10000}

        # Act
        response = client.post("/inventory/add", json=request_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["inventory"]["available_quantity"] == 10100

    def test_add_stock_inventory_not_found(self, client):
        """Test stock addition for non-existent inventory."""
        # Arrange
        request_data = {"product_id": 999, "quantity": 50}

        # Act
        response = client.post("/inventory/add", json=request_data)

        # Assert
        assert response.status_code == 404

    def test_add_stock_invalid_quantity(self, client, sample_product):
        """Test stock addition with invalid quantity."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 0}

        # Act
        response = client.post("/inventory/add", json=request_data)

        # Assert
        assert response.status_code == 422

    def test_add_stock_multiple_times(self, client, sample_inventory, sample_product):
        """Test adding stock multiple times."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 25}

        # Act - Add first time
        response1 = client.post("/inventory/add", json=request_data)
        assert response1.status_code == 200
        assert response1.json()["inventory"]["available_quantity"] == 125

        # Act - Add second time
        response2 = client.post("/inventory/add", json=request_data)
        assert response2.status_code == 200
        assert response2.json()["inventory"]["available_quantity"] == 150


class TestRemoveStock:
    """Tests for POST /inventory/remove endpoint."""

    def test_remove_stock_success(self, client, sample_inventory, sample_product):
        """Test successful stock removal."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 20}

        # Act
        response = client.post("/inventory/remove", json=request_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "removed successfully" in data["message"].lower()
        assert data["inventory"]["available_quantity"] == 80
        assert data["inventory"]["reserved_quantity"] == 0

    def test_remove_stock_exact_quantity(self, client, sample_inventory, sample_product):
        """Test removing exact available quantity."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 100}

        # Act
        response = client.post("/inventory/remove", json=request_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["inventory"]["available_quantity"] == 0

    def test_remove_stock_insufficient(self, client, sample_inventory, sample_product):
        """Test removing more stock than available."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": 200}

        # Act
        response = client.post("/inventory/remove", json=request_data)

        # Assert
        assert response.status_code == 400
        data = response.json()
        assert "insufficient" in data["message"].lower()

    def test_remove_stock_inventory_not_found(self, client):
        """Test stock removal for non-existent inventory."""
        # Arrange
        request_data = {"product_id": 999, "quantity": 20}

        # Act
        response = client.post("/inventory/remove", json=request_data)

        # Assert
        assert response.status_code == 404

    def test_remove_stock_invalid_quantity(self, client, sample_product):
        """Test stock removal with invalid quantity."""
        # Arrange
        request_data = {"product_id": sample_product.id, "quantity": -10}

        # Act
        response = client.post("/inventory/remove", json=request_data)

        # Assert
        assert response.status_code == 422


class TestInventoryWorkflow:
    """Tests for complete inventory workflow scenarios."""

    def test_reserve_and_release_workflow(self, client, sample_inventory, sample_product):
        """Test complete reserve and release workflow."""
        # Step 1: Check initial inventory
        response = client.get(f"/inventory/{sample_product.id}")
        assert response.json()["available_quantity"] == 100
        assert response.json()["reserved_quantity"] == 0

        # Step 2: Reserve stock
        reserve_data = {"product_id": sample_product.id, "quantity": 30}
        response = client.post("/inventory/reserve", json=reserve_data)
        assert response.json()["inventory"]["available_quantity"] == 70
        assert response.json()["inventory"]["reserved_quantity"] == 30

        # Step 3: Release stock
        release_data = {"product_id": sample_product.id, "quantity": 30}
        response = client.post("/inventory/release", json=release_data)
        assert response.json()["inventory"]["available_quantity"] == 100
        assert response.json()["inventory"]["reserved_quantity"] == 0

    def test_add_reserve_release_remove_workflow(self, client, sample_inventory, sample_product):
        """Test complex inventory workflow."""
        # Add stock
        client.post("/inventory/add", json={"product_id": sample_product.id, "quantity": 50})

        # Reserve some
        client.post("/inventory/reserve", json={"product_id": sample_product.id, "quantity": 40})

        # Release some
        client.post("/inventory/release", json={"product_id": sample_product.id, "quantity": 20})

        # Remove some
        response = client.post("/inventory/remove", json={"product_id": sample_product.id, "quantity": 30})

        # Final state: 150 + 0 - 40 + 20 - 30 = 100 available, 20 reserved
        data = response.json()
        assert data["inventory"]["available_quantity"] == 100
        assert data["inventory"]["reserved_quantity"] == 20
