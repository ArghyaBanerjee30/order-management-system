"""Product model for the inventory service"""
from sqlalchemy import Column, Integer, String, Float, Boolean
from inventory_service.database.config import Base


class Product(Base):
    """
    Product model representing a product in the inventory system.
    """
    __tablename__ = "products"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(100), nullable=False, index=True)
    description = Column(String(500))
    price = Column(Float, nullable=False)
    sku = Column(String(50), unique=True, nullable=False, index=True)
    active = Column(Boolean, default=True, nullable=False)

    def __repr__(self):
        return f"<Product(id={self.id}, name='{self.name}', sku='{self.sku}')>"
