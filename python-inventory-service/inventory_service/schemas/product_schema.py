"""Pydantic schemas for Product"""
from typing import Optional
import re
from pydantic import BaseModel, Field, field_validator


class ProductCreate(BaseModel):
    """Schema for creating a new product"""
    name: str = Field(..., min_length=2, max_length=100, description="Product name")
    description: Optional[str] = Field(None, max_length=500, description="Product description")
    price: float = Field(..., gt=0, le=1000000, description="Product price (must be greater than 0)")
    sku: str = Field(..., min_length=3, max_length=50, description="Stock Keeping Unit (unique)")
    active: bool = Field(default=True, description="Whether the product is active")

    @field_validator('name')
    @classmethod
    def validate_name(cls, v):
        """Ensure name is trimmed and not just whitespace"""
        v = v.strip()
        if not v:
            raise ValueError('Product name cannot be empty or just whitespace')
        if len(v) < 2:
            raise ValueError('Product name must be at least 2 characters long')
        return v

    @field_validator('description')
    @classmethod
    def validate_description(cls, v):
        """Trim description"""
        return v.strip() if v else v

    @field_validator('price')
    @classmethod
    def validate_price(cls, v):
        """Ensure price is positive and reasonable"""
        if v <= 0:
            raise ValueError('Price must be greater than 0')
        if v > 1000000:
            raise ValueError('Price must not exceed 1,000,000')
        return round(v, 2)

    @field_validator('sku')
    @classmethod
    def validate_sku(cls, v):
        """Ensure SKU is uppercase, trimmed, and valid format"""
        v = v.strip().upper()
        if len(v) < 3:
            raise ValueError('SKU must be at least 3 characters long')
        # SKU should contain only alphanumeric characters and hyphens
        if not re.match(r'^[A-Z0-9\-]+$', v):
            raise ValueError('SKU can only contain letters, numbers, and hyphens')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "name": "Laptop",
                "description": "High-performance laptop",
                "price": 999.99,
                "sku": "LAP-001",
                "active": True
            }
        }


class ProductUpdate(BaseModel):
    """Schema for updating a product"""
    name: Optional[str] = Field(None, min_length=2, max_length=100, description="Product name")
    description: Optional[str] = Field(None, max_length=500, description="Product description")
    price: Optional[float] = Field(None, gt=0, le=1000000, description="Product price")
    sku: Optional[str] = Field(None, min_length=3, max_length=50, description="Stock Keeping Unit")
    active: Optional[bool] = Field(None, description="Whether the product is active")

    @field_validator('name')
    @classmethod
    def validate_name(cls, v):
        """Ensure name is trimmed and not just whitespace if provided"""
        if v is not None:
            v = v.strip()
            if not v:
                raise ValueError('Product name cannot be empty or just whitespace')
            if len(v) < 2:
                raise ValueError('Product name must be at least 2 characters long')
        return v

    @field_validator('description')
    @classmethod
    def validate_description(cls, v):
        """Trim description if provided"""
        return v.strip() if v else v

    @field_validator('price')
    @classmethod
    def validate_price(cls, v):
        """Ensure price is positive and reasonable if provided"""
        if v is not None:
            if v <= 0:
                raise ValueError('Price must be greater than 0')
            if v > 1000000:
                raise ValueError('Price must not exceed 1,000,000')
            return round(v, 2)
        return v

    @field_validator('sku')
    @classmethod
    def validate_sku(cls, v):
        """Ensure SKU is uppercase, trimmed, and valid format if provided"""
        if v is not None:
            v = v.strip().upper()
            if len(v) < 3:
                raise ValueError('SKU must be at least 3 characters long')
            if not re.match(r'^[A-Z0-9\-]+$', v):
                raise ValueError('SKU can only contain letters, numbers, and hyphens')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "name": "Updated Laptop",
                "price": 899.99,
                "active": False
            }
        }


class ProductResponse(BaseModel):
    """Schema for product response"""
    id: int
    name: str
    description: Optional[str]
    price: float
    sku: str
    active: bool

    class Config:
        from_attributes = True
        json_schema_extra = {
            "example": {
                "id": 1,
                "name": "Laptop",
                "description": "High-performance laptop",
                "price": 999.99,
                "sku": "LAP-001",
                "active": True
            }
        }
