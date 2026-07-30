"""Inventory model for the inventory service"""
from sqlalchemy import Column, Integer, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from inventory_service.database.config import Base


class Inventory(Base):
    """
    Inventory model representing stock levels for products.
    """
    __tablename__ = "inventory"

    product_id = Column(Integer, ForeignKey("products.id"), primary_key=True, index=True)
    available_quantity = Column(Integer, nullable=False, default=0)
    reserved_quantity = Column(Integer, nullable=False, default=0)
    last_updated = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    # Relationship to Product
    product = relationship("Product", backref="inventory")

    def __repr__(self):
        return f"<Inventory(product_id={self.product_id}, available={self.available_quantity}, reserved={self.reserved_quantity})>"
