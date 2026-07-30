"""InventoryHistory model for tracking inventory changes"""
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Enum as SQLEnum
from sqlalchemy.sql import func
from enum import Enum
from inventory_service.database.config import Base


class InventoryAction(str, Enum):
    """Enum for inventory action types"""
    ADD = "ADD"
    REMOVE = "REMOVE"
    RESERVE = "RESERVE"
    RELEASE = "RELEASE"


class InventoryHistory(Base):
    """
    InventoryHistory model for tracking all inventory changes.
    Provides audit trail for inventory operations.
    """
    __tablename__ = "inventory_history"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    product_id = Column(Integer, ForeignKey("products.id"), nullable=False, index=True)
    action = Column(SQLEnum(InventoryAction), nullable=False)
    quantity = Column(Integer, nullable=False)
    timestamp = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    def __repr__(self):
        return f"<InventoryHistory(id={self.id}, product_id={self.product_id}, action={self.action}, quantity={self.quantity})>"
