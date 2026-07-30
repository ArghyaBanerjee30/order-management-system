"""Models package - imports all models for Alembic autogenerate"""
from inventory_service.models.Product import Product
from inventory_service.models.Inventory import Inventory
from inventory_service.models.InventoryHistory import InventoryHistory, InventoryAction

__all__ = ["Product", "Inventory", "InventoryHistory", "InventoryAction"]
