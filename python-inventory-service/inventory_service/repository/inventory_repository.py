"""Inventory repository for data access operations"""
from typing import Optional
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from inventory_service.models.Inventory import Inventory
from inventory_service.models.InventoryHistory import InventoryHistory, InventoryAction
from inventory_service.exceptions import InsufficientStockException


class InventoryRepository:
    """Repository class for Inventory entity operations"""

    def __init__(self, db: Session):
        self.db = db

    def get_by_product_id(self, product_id: int) -> Optional[Inventory]:
        """
        Get inventory by product ID.

        Args:
            product_id: Product ID to search for

        Returns:
            Inventory if found, None otherwise
        """
        return self.db.query(Inventory).filter(Inventory.product_id == product_id).first()

    def create_or_get(self, product_id: int) -> Inventory:
        """
        Get existing inventory or create new one for product.

        Args:
            product_id: Product ID

        Returns:
            Inventory entity
        """
        inventory = self.get_by_product_id(product_id)
        if not inventory:
            inventory = Inventory(
                product_id=product_id,
                available_quantity=0,
                reserved_quantity=0
            )
            self.db.add(inventory)
            self.db.commit()
            self.db.refresh(inventory)
        return inventory

    def reserve_stock(self, product_id: int, quantity: int) -> bool:
        """
        Reserve stock for a product (atomic operation).
        Decreases available quantity and increases reserved quantity.

        Args:
            product_id: Product ID
            quantity: Quantity to reserve

        Returns:
            True if reservation successful

        Raises:
            InsufficientStockException: If not enough available stock
        """
        inventory = self.get_by_product_id(product_id)
        available = inventory.available_quantity if inventory else 0
        if not inventory or available < quantity:
            raise InsufficientStockException(product_id, available, quantity)

        # Update inventory
        inventory.available_quantity -= quantity
        inventory.reserved_quantity += quantity

        # Create history entry
        self._create_history_entry(product_id, InventoryAction.RESERVE, quantity)

        self.db.commit()
        return True

    def release_stock(self, product_id: int, quantity: int) -> bool:
        """
        Release reserved stock for a product (atomic operation).
        Decreases reserved quantity and increases available quantity.

        Args:
            product_id: Product ID
            quantity: Quantity to release

        Returns:
            True if release successful
        """
        inventory = self.get_by_product_id(product_id)
        if not inventory:
            return False

        # Update inventory
        inventory.reserved_quantity -= quantity
        inventory.available_quantity += quantity

        # Create history entry
        self._create_history_entry(product_id, InventoryAction.RELEASE, quantity)

        self.db.commit()
        return True

    def add_stock(self, product_id: int, quantity: int) -> bool:
        """
        Add stock to available quantity.

        Args:
            product_id: Product ID
            quantity: Quantity to add

        Returns:
            True if successful
        """
        inventory = self.create_or_get(product_id)
        inventory.available_quantity += quantity

        # Create history entry
        self._create_history_entry(product_id, InventoryAction.ADD, quantity)

        self.db.commit()
        return True

    def remove_stock(self, product_id: int, quantity: int) -> bool:
        """
        Remove stock from available quantity.

        Args:
            product_id: Product ID
            quantity: Quantity to remove

        Returns:
            True if successful

        Raises:
            InsufficientStockException: If not enough available stock
        """
        inventory = self.get_by_product_id(product_id)
        available = inventory.available_quantity if inventory else 0
        if not inventory or available < quantity:
            raise InsufficientStockException(product_id, available, quantity)

        inventory.available_quantity -= quantity

        # Create history entry
        self._create_history_entry(product_id, InventoryAction.REMOVE, quantity)

        self.db.commit()
        return True

    def _create_history_entry(self, product_id: int, action: InventoryAction, quantity: int):
        """
        Create an inventory history entry.

        Args:
            product_id: Product ID
            action: Inventory action type
            quantity: Quantity involved in the action
        """
        history = InventoryHistory(
            product_id=product_id,
            action=action,
            quantity=quantity
        )
        self.db.add(history)
