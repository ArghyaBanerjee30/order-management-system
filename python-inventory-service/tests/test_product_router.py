"""
Integration tests for Product Router.
"""
import pytest
from fastapi.testclient import TestClient


class TestCreateProduct:
    """Tests for POST /products endpoint."""

    def test_create_product_valid_request(self, client, sample_product_data):
        """Test creating product with valid request."""
        # Act
        response = client.post("/products", json=sample_product_data)

        # Assert
        assert response.status_code == 201
        data = response.json()
        assert data["name"] == sample_product_data["name"]
        assert data["sku"] == sample_product_data["sku"].upper()  # SKU is uppercased
        assert data["price"] == sample_product_data["price"]
        assert "id" in data
        assert data["active"] is True

    def test_create_product_duplicate_sku(self, client, sample_product_data, sample_product):
        """Test creating product with duplicate SKU."""
        # Arrange - sample_product fixture already created a product with this SKU
        duplicate_data = sample_product_data.copy()
        duplicate_data["sku"] = sample_product.sku

        # Act
        response = client.post("/products", json=duplicate_data)

        # Assert
        assert response.status_code == 409
        data = response.json()
        assert "SKU" in data["message"]

    def test_create_product_missing_name(self, client, sample_product_data):
        """Test creating product without name."""
        # Arrange
        invalid_data = sample_product_data.copy()
        del invalid_data["name"]

        # Act
        response = client.post("/products", json=invalid_data)

        # Assert
        assert response.status_code == 422
        data = response.json()
        assert "detail" in data

    def test_create_product_invalid_price(self, client, sample_product_data):
        """Test creating product with invalid price (negative)."""
        # Arrange
        invalid_data = sample_product_data.copy()
        invalid_data["price"] = -10.0

        # Act
        response = client.post("/products", json=invalid_data)

        # Assert
        assert response.status_code == 422
        data = response.json()
        assert "detail" in data

    def test_create_product_invalid_sku_format(self, client, sample_product_data):
        """Test creating product with invalid SKU format."""
        # Arrange
        invalid_data = sample_product_data.copy()
        invalid_data["sku"] = "INVALID SKU!"  # Contains space and special char

        # Act
        response = client.post("/products", json=invalid_data)

        # Assert
        assert response.status_code == 422
        data = response.json()
        assert "detail" in data


class TestGetProducts:
    """Tests for GET /products endpoint."""

    def test_get_all_products(self, client, multiple_products):
        """Test getting all products."""
        # Act
        response = client.get("/products")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        # By default, only active products are returned
        active_count = sum(1 for p in multiple_products if p.active)
        assert len(data) == active_count

    def test_get_all_products_with_inactive(self, client, multiple_products):
        """Test getting all products including inactive."""
        # Act
        response = client.get("/products?active_only=false")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert len(data) == len(multiple_products)

    def test_get_all_products_empty(self, client):
        """Test getting products when none exist."""
        # Act
        response = client.get("/products")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert isinstance(data, list)
        assert len(data) == 0

    def test_get_all_products_with_pagination(self, client, multiple_products):
        """Test getting products with pagination."""
        # Act
        response = client.get("/products?skip=1&limit=1")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert len(data) <= 1


class TestGetProductById:
    """Tests for GET /products/{id} endpoint."""

    def test_get_product_by_id_found(self, client, sample_product):
        """Test getting existing product by ID."""
        # Act
        response = client.get(f"/products/{sample_product.id}")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["id"] == sample_product.id
        assert data["name"] == sample_product.name
        assert data["sku"] == sample_product.sku

    def test_get_product_by_id_not_found(self, client):
        """Test getting non-existent product."""
        # Act
        response = client.get("/products/999")

        # Assert
        assert response.status_code == 404
        data = response.json()
        assert "not found" in data["message"].lower()


class TestUpdateProduct:
    """Tests for PUT /products/{id} endpoint."""

    def test_update_product_full(self, client, sample_product):
        """Test updating product with all fields."""
        # Arrange
        update_data = {
            "name": "Updated Product",
            "description": "Updated Description",
            "price": 149.99,
            "sku": "UPDATED-SKU",
            "active": False
        }

        # Act
        response = client.put(f"/products/{sample_product.id}", json=update_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["id"] == sample_product.id
        assert data["name"] == update_data["name"]
        assert data["price"] == update_data["price"]
        assert data["sku"] == update_data["sku"].upper()
        assert data["active"] == update_data["active"]

    def test_update_product_partial(self, client, sample_product):
        """Test updating product with partial data."""
        # Arrange
        update_data = {
            "price": 79.99
        }

        # Act
        response = client.put(f"/products/{sample_product.id}", json=update_data)

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["id"] == sample_product.id
        assert data["price"] == update_data["price"]
        assert data["name"] == sample_product.name  # Unchanged

    def test_update_product_not_found(self, client):
        """Test updating non-existent product."""
        # Arrange
        update_data = {"price": 99.99}

        # Act
        response = client.put("/products/999", json=update_data)

        # Assert
        assert response.status_code == 404
        data = response.json()
        assert "not found" in data["message"].lower()

    def test_update_product_duplicate_sku(self, client, multiple_products):
        """Test updating product with duplicate SKU."""
        # Arrange
        product1 = multiple_products[0]
        product2 = multiple_products[1]
        update_data = {"sku": product2.sku}

        # Act
        response = client.put(f"/products/{product1.id}", json=update_data)

        # Assert
        assert response.status_code == 409
        data = response.json()
        assert "SKU" in data["message"]

    def test_update_product_invalid_price(self, client, sample_product):
        """Test updating product with invalid price."""
        # Arrange
        update_data = {"price": -50.0}

        # Act
        response = client.put(f"/products/{sample_product.id}", json=update_data)

        # Assert
        assert response.status_code == 422


class TestDeleteProduct:
    """Tests for DELETE /products/{id} endpoint."""

    def test_delete_product_success(self, client, sample_product):
        """Test successful product deletion (soft delete)."""
        # Act
        response = client.delete(f"/products/{sample_product.id}")

        # Assert
        assert response.status_code == 204

        # Verify product is soft deleted (not in active list)
        get_response = client.get("/products")
        data = get_response.json()
        product_ids = [p["id"] for p in data]
        assert sample_product.id not in product_ids

        # Verify product still exists but inactive
        get_by_id_response = client.get(f"/products/{sample_product.id}")
        assert get_by_id_response.status_code == 200
        product_data = get_by_id_response.json()
        assert product_data["active"] is False

    def test_delete_product_not_found(self, client):
        """Test deleting non-existent product."""
        # Act
        response = client.delete("/products/999")

        # Assert
        assert response.status_code == 404
        data = response.json()
        assert "not found" in data["message"].lower()

    def test_delete_product_idempotent(self, client, sample_product):
        """Test deleting already deleted product."""
        # Arrange - Delete once
        client.delete(f"/products/{sample_product.id}")

        # Act - Try to delete again
        response = client.delete(f"/products/{sample_product.id}")

        # Assert - Should still succeed (idempotent)
        assert response.status_code == 204
