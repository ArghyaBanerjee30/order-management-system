"""
Unit tests for ProductService.
"""
import pytest
from fastapi import HTTPException
from unittest.mock import Mock, MagicMock
from sqlalchemy.exc import IntegrityError

from models.product import Product
from schemas.product_schema import ProductCreate, ProductUpdate
from services.product_service import ProductService
from exceptions import ProductNotFoundException, DuplicateProductException


@pytest.fixture
def mock_repo():
    """Mock ProductRepository for testing."""
    return Mock()


@pytest.fixture
def product_service(mock_repo):
    """Create ProductService with mocked repository."""
    return ProductService(Mock(), mock_repo)


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


@pytest.fixture
def product_create_data():
    """Sample product create data."""
    return ProductCreate(
        name="New Product",
        description="New Description",
        price=49.99,
        sku="NEW-SKU-001"
    )


@pytest.fixture
def product_update_data():
    """Sample product update data."""
    return ProductUpdate(
        name="Updated Product",
        price=59.99
    )


class TestCreateProduct:
    """Tests for create_product method."""

    def test_create_product_success(self, product_service, mock_repo, product_create_data, sample_product):
        """Test successful product creation."""
        # Arrange
        mock_repo.get_by_sku.return_value = None
        mock_repo.create.return_value = sample_product

        # Act
        result = product_service.create_product(product_create_data)

        # Assert
        assert result is not None
        assert result.id == sample_product.id
        assert result.sku == sample_product.sku
        mock_repo.get_by_sku.assert_called_once_with(product_create_data.sku)
        mock_repo.create.assert_called_once()

    def test_create_product_duplicate_sku(self, product_service, mock_repo, product_create_data, sample_product):
        """Test product creation with duplicate SKU."""
        # Arrange
        mock_repo.get_by_sku.return_value = sample_product

        # Act & Assert
        with pytest.raises(DuplicateProductException) as exc_info:
            product_service.create_product(product_create_data)

        assert "SKU" in str(exc_info.value)
        mock_repo.get_by_sku.assert_called_once()
        mock_repo.create.assert_not_called()

    def test_create_product_database_error(self, product_service, mock_repo, product_create_data):
        """Test product creation with database error."""
        # Arrange
        mock_repo.get_by_sku.return_value = None
        mock_repo.create.side_effect = IntegrityError("", "", "")

        # Act & Assert
        with pytest.raises(HTTPException) as exc_info:
            product_service.create_product(product_create_data)

        assert exc_info.value.status_code == 400


class TestGetProduct:
    """Tests for get_product method."""

    def test_get_product_found(self, product_service, mock_repo, sample_product):
        """Test getting an existing product."""
        # Arrange
        mock_repo.get_by_id.return_value = sample_product

        # Act
        result = product_service.get_product(1)

        # Assert
        assert result is not None
        assert result.id == sample_product.id
        mock_repo.get_by_id.assert_called_once_with(1)

    def test_get_product_not_found(self, product_service, mock_repo):
        """Test getting a non-existent product."""
        # Arrange
        mock_repo.get_by_id.return_value = None

        # Act & Assert
        with pytest.raises(ProductNotFoundException):
            product_service.get_product(999)

        mock_repo.get_by_id.assert_called_once_with(999)


class TestGetAllProducts:
    """Tests for get_all_products method."""

    def test_get_all_products_with_results(self, product_service, mock_repo):
        """Test getting all products with results."""
        # Arrange
        products = [
            Product(id=1, name="Product 1", price=10.00, sku="SKU-1", active=True),
            Product(id=2, name="Product 2", price=20.00, sku="SKU-2", active=True),
        ]
        mock_repo.get_all.return_value = products

        # Act
        result = product_service.get_all_products(skip=0, limit=10, active_only=True)

        # Assert
        assert len(result) == 2
        assert result[0].id == 1
        assert result[1].id == 2
        mock_repo.get_all.assert_called_once_with(skip=0, limit=10, active_only=True)

    def test_get_all_products_empty(self, product_service, mock_repo):
        """Test getting all products when none exist."""
        # Arrange
        mock_repo.get_all.return_value = []

        # Act
        result = product_service.get_all_products()

        # Assert
        assert len(result) == 0
        mock_repo.get_all.assert_called_once()

    def test_get_all_products_with_pagination(self, product_service, mock_repo):
        """Test getting products with pagination."""
        # Arrange
        products = [Product(id=3, name="Product 3", price=30.00, sku="SKU-3", active=True)]
        mock_repo.get_all.return_value = products

        # Act
        result = product_service.get_all_products(skip=2, limit=1)

        # Assert
        assert len(result) == 1
        assert result[0].id == 3
        mock_repo.get_all.assert_called_once_with(skip=2, limit=1, active_only=True)


class TestUpdateProduct:
    """Tests for update_product method."""

    def test_update_product_success(self, product_service, mock_repo, product_update_data, sample_product):
        """Test successful product update."""
        # Arrange
        mock_repo.get_by_id.return_value = sample_product
        mock_repo.update.return_value = sample_product

        # Act
        result = product_service.update_product(1, product_update_data)

        # Assert
        assert result is not None
        assert result.id == sample_product.id
        mock_repo.get_by_id.assert_called_once_with(1)
        mock_repo.update.assert_called_once()

    def test_update_product_not_found(self, product_service, mock_repo, product_update_data):
        """Test updating a non-existent product."""
        # Arrange
        mock_repo.get_by_id.return_value = None

        # Act & Assert
        with pytest.raises(ProductNotFoundException):
            product_service.update_product(999, product_update_data)

        mock_repo.get_by_id.assert_called_once_with(999)
        mock_repo.update.assert_not_called()

    def test_update_product_duplicate_sku(self, product_service, mock_repo, product_update_data, sample_product):
        """Test updating product with duplicate SKU."""
        # Arrange
        product_update_data.sku = "DUPLICATE-SKU"
        other_product = Product(id=2, name="Other", price=10.00, sku="DUPLICATE-SKU", active=True)

        mock_repo.get_by_id.return_value = sample_product
        mock_repo.get_by_sku.return_value = other_product

        # Act & Assert
        with pytest.raises(DuplicateProductException):
            product_service.update_product(1, product_update_data)

        mock_repo.get_by_id.assert_called_once()
        mock_repo.update.assert_not_called()

    def test_update_product_partial_update(self, product_service, mock_repo, sample_product):
        """Test partial product update."""
        # Arrange
        partial_update = ProductUpdate(price=79.99)
        mock_repo.get_by_id.return_value = sample_product
        mock_repo.update.return_value = sample_product

        # Act
        result = product_service.update_product(1, partial_update)

        # Assert
        assert result is not None
        mock_repo.update.assert_called_once()


class TestDeleteProduct:
    """Tests for delete_product method."""

    def test_delete_product_success(self, product_service, mock_repo, sample_product):
        """Test successful product deletion (soft delete)."""
        # Arrange
        mock_repo.get_by_id.return_value = sample_product
        mock_repo.soft_delete.return_value = sample_product

        # Act
        product_service.delete_product(1)

        # Assert
        mock_repo.get_by_id.assert_called_once_with(1)
        mock_repo.soft_delete.assert_called_once_with(1)

    def test_delete_product_not_found(self, product_service, mock_repo):
        """Test deleting a non-existent product."""
        # Arrange
        mock_repo.get_by_id.return_value = None

        # Act & Assert
        with pytest.raises(ProductNotFoundException):
            product_service.delete_product(999)

        mock_repo.get_by_id.assert_called_once_with(999)
        mock_repo.soft_delete.assert_not_called()
