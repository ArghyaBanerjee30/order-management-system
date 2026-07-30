"""
Unit tests for InventoryService.
"""
import pytest
from fastapi import HTTPException
from unittest.mock import Mock, MagicMock

from models.inventory import Inventory
from models.product import Product
from schemas.inventory_schema import (
    ReserveStockRequest,
    ReleaseStockRequest,
    AddStockRequest,
    RemoveStockRequest,
    InventoryOperationResponse
)
from services.inventory_service import InventoryService
from exceptions import (
    InventoryNotFoundException,
    InsufficientStockException
)


@pytest.fixture
def mock_repo():
    """Mock InventoryRepository for testing."""
    return Mock()


@pytest.fixture
def inventory_service(mock_repo):
    """Create InventoryService with mocked repository."""
    return InventoryService(Mock(), mock_repo)


@pytest.fixture
def sample_inventory():
    """Sample inventory for testing."""
    inventory = Inventory(
        product_id=1,
        available_quantity=100,
        reserved_quantity=0
    )
    return inventory


@pytest.fixture
def sample_product():
    """Sample product for testing."""
    product = Product(
        id=1,
        name="Test Product",
        description="Test Description",
        price=99.99,
        sku="TEST-SKU-001",
        active=True
    )
    return product


class TestGetInventory:
    """Tests for get_inventory method."""

    def test_get_inventory_found(self, inventory_service, mock_repo, sample_inventory):
        """Test getting existing inventory."""
        # Arrange
        mock_repo.get_by_product_id.return_value = sample_inventory

        # Act
        result = inventory_service.get_inventory(1)

        # Assert
        assert result is not None
        assert result.product_id == 1
        assert result.available_quantity == 100
        assert result.reserved_quantity == 0
        mock_repo.get_by_product_id.assert_called_once_with(1)

    def test_get_inventory_not_found(self, inventory_service, mock_repo):
        """Test getting non-existent inventory."""
        # Arrange
        mock_repo.get_by_product_id.return_value = None

        # Act & Assert
        with pytest.raises(InventoryNotFoundException):
            inventory_service.get_inventory(999)

        mock_repo.get_by_product_id.assert_called_once_with(999)


class TestReserveStock:
    """Tests for reserve_stock method."""

    def test_reserve_stock_success(self, inventory_service, mock_repo, sample_inventory):
        """Test successful stock reservation."""
        # Arrange
        request = ReserveStockRequest(product_id=1, quantity=10)
        updated_inventory = Inventory(
            product_id=1,
            available_quantity=90,
            reserved_quantity=10
        )
        mock_repo.reserve_stock.return_value = updated_inventory

        # Act
        result = inventory_service.reserve_stock(request)

        # Assert
        assert result.success is True
        assert "reserved successfully" in result.message.lower()
        assert result.inventory.available_quantity == 90
        assert result.inventory.reserved_quantity == 10
        mock_repo.reserve_stock.assert_called_once_with(1, 10)

    def test_reserve_stock_insufficient_stock(self, inventory_service, mock_repo):
        """Test stock reservation with insufficient stock."""
        # Arrange
        request = ReserveStockRequest(product_id=1, quantity=200)
        mock_repo.reserve_stock.side_effect = InsufficientStockException(
            product_id=1,
            available=100,
            requested=200
        )

        # Act & Assert
        with pytest.raises(InsufficientStockException) as exc_info:
            inventory_service.reserve_stock(request)

        assert exc_info.value.product_id == 1
        assert exc_info.value.available == 100
        assert exc_info.value.requested == 200
        mock_repo.reserve_stock.assert_called_once_with(1, 200)

    def test_reserve_stock_inventory_not_found(self, inventory_service, mock_repo):
        """Test stock reservation when inventory doesn't exist."""
        # Arrange
        request = ReserveStockRequest(product_id=999, quantity=10)
        mock_repo.reserve_stock.side_effect = InventoryNotFoundException(product_id=999)

        # Act & Assert
        with pytest.raises(InventoryNotFoundException):
            inventory_service.reserve_stock(request)

        mock_repo.reserve_stock.assert_called_once_with(999, 10)


class TestReleaseStock:
    """Tests for release_stock method."""

    def test_release_stock_success(self, inventory_service, mock_repo, sample_inventory):
        """Test successful stock release."""
        # Arrange
        request = ReleaseStockRequest(product_id=1, quantity=10)
        initial_inventory = Inventory(
            product_id=1,
            available_quantity=90,
            reserved_quantity=10
        )
        updated_inventory = Inventory(
            product_id=1,
            available_quantity=100,
            reserved_quantity=0
        )
        mock_repo.release_stock.return_value = updated_inventory

        # Act
        result = inventory_service.release_stock(request)

        # Assert
        assert result.success is True
        assert "released successfully" in result.message.lower()
        assert result.inventory.available_quantity == 100
        assert result.inventory.reserved_quantity == 0
        mock_repo.release_stock.assert_called_once_with(1, 10)

    def test_release_stock_inventory_not_found(self, inventory_service, mock_repo):
        """Test stock release when inventory doesn't exist."""
        # Arrange
        request = ReleaseStockRequest(product_id=999, quantity=10)
        mock_repo.release_stock.side_effect = InventoryNotFoundException(product_id=999)

        # Act & Assert
        with pytest.raises(InventoryNotFoundException):
            inventory_service.release_stock(request)

        mock_repo.release_stock.assert_called_once_with(999, 10)

    def test_release_stock_insufficient_reserved(self, inventory_service, mock_repo):
        """Test releasing more stock than reserved."""
        # Arrange
        request = ReleaseStockRequest(product_id=1, quantity=50)
        mock_repo.release_stock.side_effect = InsufficientStockException(
            product_id=1,
            available=10,  # reserved quantity
            requested=50
        )

        # Act & Assert
        with pytest.raises(InsufficientStockException):
            inventory_service.release_stock(request)

        mock_repo.release_stock.assert_called_once_with(1, 50)


class TestAddStock:
    """Tests for add_stock method."""

    def test_add_stock_success(self, inventory_service, mock_repo, sample_inventory):
        """Test successful stock addition."""
        # Arrange
        request = AddStockRequest(product_id=1, quantity=50)
        updated_inventory = Inventory(
            product_id=1,
            available_quantity=150,
            reserved_quantity=0
        )
        mock_repo.add_stock.return_value = updated_inventory

        # Act
        result = inventory_service.add_stock(request)

        # Assert
        assert result.success is True
        assert "added successfully" in result.message.lower()
        assert result.inventory.available_quantity == 150
        mock_repo.add_stock.assert_called_once_with(1, 50)

    def test_add_stock_inventory_not_found(self, inventory_service, mock_repo):
        """Test stock addition when inventory doesn't exist."""
        # Arrange
        request = AddStockRequest(product_id=999, quantity=50)
        mock_repo.add_stock.side_effect = InventoryNotFoundException(product_id=999)

        # Act & Assert
        with pytest.raises(InventoryNotFoundException):
            inventory_service.add_stock(request)

        mock_repo.add_stock.assert_called_once_with(999, 50)

    def test_add_stock_large_quantity(self, inventory_service, mock_repo):
        """Test adding a large quantity of stock."""
        # Arrange
        request = AddStockRequest(product_id=1, quantity=10000)
        updated_inventory = Inventory(
            product_id=1,
            available_quantity=10100,
            reserved_quantity=0
        )
        mock_repo.add_stock.return_value = updated_inventory

        # Act
        result = inventory_service.add_stock(request)

        # Assert
        assert result.success is True
        assert result.inventory.available_quantity == 10100
        mock_repo.add_stock.assert_called_once_with(1, 10000)


class TestRemoveStock:
    """Tests for remove_stock method."""

    def test_remove_stock_success(self, inventory_service, mock_repo, sample_inventory):
        """Test successful stock removal."""
        # Arrange
        request = RemoveStockRequest(product_id=1, quantity=20)
        updated_inventory = Inventory(
            product_id=1,
            available_quantity=80,
            reserved_quantity=0
        )
        mock_repo.remove_stock.return_value = updated_inventory

        # Act
        result = inventory_service.remove_stock(request)

        # Assert
        assert result.success is True
        assert "removed successfully" in result.message.lower()
        assert result.inventory.available_quantity == 80
        mock_repo.remove_stock.assert_called_once_with(1, 20)

    def test_remove_stock_insufficient_available(self, inventory_service, mock_repo):
        """Test removing more stock than available."""
        # Arrange
        request = RemoveStockRequest(product_id=1, quantity=200)
        mock_repo.remove_stock.side_effect = InsufficientStockException(
            product_id=1,
            available=100,
            requested=200
        )

        # Act & Assert
        with pytest.raises(InsufficientStockException):
            inventory_service.remove_stock(request)

        mock_repo.remove_stock.assert_called_once_with(1, 200)

    def test_remove_stock_inventory_not_found(self, inventory_service, mock_repo):
        """Test stock removal when inventory doesn't exist."""
        # Arrange
        request = RemoveStockRequest(product_id=999, quantity=20)
        mock_repo.remove_stock.side_effect = InventoryNotFoundException(product_id=999)

        # Act & Assert
        with pytest.raises(InventoryNotFoundException):
            inventory_service.remove_stock(request)

        mock_repo.remove_stock.assert_called_once_with(999, 20)

    def test_remove_stock_exact_available_quantity(self, inventory_service, mock_repo):
        """Test removing exact available quantity."""
        # Arrange
        request = RemoveStockRequest(product_id=1, quantity=100)
        updated_inventory = Inventory(
            product_id=1,
            available_quantity=0,
            reserved_quantity=0
        )
        mock_repo.remove_stock.return_value = updated_inventory

        # Act
        result = inventory_service.remove_stock(request)

        # Assert
        assert result.success is True
        assert result.inventory.available_quantity == 0
        mock_repo.remove_stock.assert_called_once_with(1, 100)
