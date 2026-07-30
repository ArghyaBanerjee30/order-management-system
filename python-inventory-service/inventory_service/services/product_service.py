"""Product service for business logic"""
from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError

from inventory_service.models.Product import Product
from inventory_service.repository.product_repository import ProductRepository
from inventory_service.schemas.product_schema import ProductCreate, ProductUpdate, ProductResponse
from inventory_service.exceptions import ProductNotFoundException, DuplicateProductException
from inventory_service.logging_config import get_logger

logger = get_logger(__name__)


class ProductService:
    """Service class for Product business logic"""

    def __init__(self, db: Session):
        self.repository = ProductRepository(db)

    def create_product(self, product_data: ProductCreate) -> ProductResponse:
        """
        Create a new product.

        Args:
            product_data: Product creation data

        Returns:
            Created product response

        Raises:
            HTTPException: If SKU already exists
        """
        logger.info(f"Creating product with SKU: {product_data.sku}")

        # Check if SKU already exists
        existing = self.repository.get_by_sku(product_data.sku)
        if existing:
            logger.error(f"Product with SKU {product_data.sku} already exists")
            raise DuplicateProductException(product_data.sku)

        # Create product entity
        product = Product(
            name=product_data.name,
            description=product_data.description,
            price=product_data.price,
            sku=product_data.sku,
            active=product_data.active
        )

        try:
            created_product = self.repository.create(product)
            logger.info(f"Product created successfully with ID: {created_product.id}, SKU: {created_product.sku}")
            return ProductResponse.model_validate(created_product)
        except IntegrityError as e:
            logger.error(f"Database integrity error while creating product: {str(e)}")
            raise DuplicateProductException(product_data.sku)

    def get_product(self, product_id: int) -> ProductResponse:
        """
        Get product by ID.

        Args:
            product_id: Product ID

        Returns:
            Product response

        Raises:
            HTTPException: If product not found
        """
        logger.debug(f"Fetching product with ID: {product_id}")
        product = self.repository.get_by_id(product_id)
        if not product:
            logger.error(f"Product not found with ID: {product_id}")
            raise ProductNotFoundException(product_id)
        logger.debug(f"Product found: {product.sku}")
        return ProductResponse.model_validate(product)

    def get_all_products(self, skip: int = 0, limit: int = 100, active_only: bool = True) -> List[ProductResponse]:
        """
        Get all products with pagination.

        Args:
            skip: Number of records to skip
            limit: Maximum number of records to return
            active_only: If True, return only active products

        Returns:
            List of product responses
        """
        logger.debug(f"Fetching products (skip={skip}, limit={limit}, active_only={active_only})")
        products = self.repository.get_all(skip=skip, limit=limit, active_only=active_only)
        logger.debug(f"Found {len(products)} products")
        return [ProductResponse.model_validate(p) for p in products]

    def update_product(self, product_id: int, product_data: ProductUpdate) -> ProductResponse:
        """
        Update an existing product.

        Args:
            product_id: Product ID
            product_data: Product update data

        Returns:
            Updated product response

        Raises:
            HTTPException: If product not found or SKU already exists
        """
        logger.info(f"Updating product with ID: {product_id}")

        product = self.repository.get_by_id(product_id)
        if not product:
            logger.error(f"Product not found with ID: {product_id}")
            raise ProductNotFoundException(product_id)

        # Check if SKU is being updated and if it already exists
        if product_data.sku and product_data.sku != product.sku:
            existing = self.repository.get_by_sku(product_data.sku)
            if existing:
                logger.error(f"Product with SKU {product_data.sku} already exists")
                raise DuplicateProductException(product_data.sku)
            product.sku = product_data.sku

        # Update fields if provided
        if product_data.name is not None:
            product.name = product_data.name
        if product_data.description is not None:
            product.description = product_data.description
        if product_data.price is not None:
            product.price = product_data.price
        if product_data.active is not None:
            product.active = product_data.active

        try:
            updated_product = self.repository.update(product)
            logger.info(f"Product updated successfully: {product_id}")
            return ProductResponse.model_validate(updated_product)
        except IntegrityError as e:
            logger.error(f"Database integrity error while updating product: {str(e)}")
            raise DuplicateProductException(product_data.sku if product_data.sku else product.sku)

    def delete_product(self, product_id: int) -> bool:
        """
        Delete a product (soft delete preferred).

        Args:
            product_id: Product ID

        Returns:
            True if deleted

        Raises:
            HTTPException: If product not found
        """
        logger.info(f"Deleting product with ID: {product_id}")
        success = self.repository.soft_delete(product_id)
        if not success:
            logger.error(f"Product not found with ID: {product_id}")
            raise ProductNotFoundException(product_id)
        logger.info(f"Product soft-deleted successfully: {product_id}")
        return success
