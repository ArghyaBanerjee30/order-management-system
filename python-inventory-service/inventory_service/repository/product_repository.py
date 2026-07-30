"""Product repository for data access operations"""
from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from inventory_service.models.Product import Product


class ProductRepository:
    """Repository class for Product entity CRUD operations"""

    def __init__(self, db: Session):
        self.db = db

    def create(self, product: Product) -> Product:
        """
        Create a new product in the database.

        Args:
            product: Product entity to create

        Returns:
            Created product with generated ID

        Raises:
            IntegrityError: If SKU already exists
        """
        self.db.add(product)
        self.db.commit()
        self.db.refresh(product)
        return product

    def get_by_id(self, product_id: int) -> Optional[Product]:
        """
        Get a product by ID.

        Args:
            product_id: Product ID to search for

        Returns:
            Product if found, None otherwise
        """
        return self.db.query(Product).filter(Product.id == product_id).first()

    def get_by_sku(self, sku: str) -> Optional[Product]:
        """
        Get a product by SKU.

        Args:
            sku: Product SKU to search for

        Returns:
            Product if found, None otherwise
        """
        return self.db.query(Product).filter(Product.sku == sku).first()

    def get_all(self, skip: int = 0, limit: int = 100, active_only: bool = True) -> List[Product]:
        """
        Get all products with pagination.

        Args:
            skip: Number of records to skip
            limit: Maximum number of records to return
            active_only: If True, return only active products

        Returns:
            List of products
        """
        query = self.db.query(Product)
        if active_only:
            query = query.filter(Product.active == True)
        return query.offset(skip).limit(limit).all()

    def update(self, product: Product) -> Product:
        """
        Update an existing product.

        Args:
            product: Product entity with updated values

        Returns:
            Updated product
        """
        self.db.commit()
        self.db.refresh(product)
        return product

    def delete(self, product_id: int) -> bool:
        """
        Delete a product (hard delete).

        Args:
            product_id: ID of product to delete

        Returns:
            True if deleted, False if not found
        """
        product = self.get_by_id(product_id)
        if product:
            self.db.delete(product)
            self.db.commit()
            return True
        return False

    def soft_delete(self, product_id: int) -> bool:
        """
        Soft delete a product by setting active=False.

        Args:
            product_id: ID of product to soft delete

        Returns:
            True if soft deleted, False if not found
        """
        product = self.get_by_id(product_id)
        if product:
            product.active = False
            self.db.commit()
            return True
        return False
