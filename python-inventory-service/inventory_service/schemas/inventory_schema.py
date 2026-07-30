"""Pydantic schemas for Inventory"""
from typing import Optional
from datetime import datetime
from pydantic import BaseModel, Field, field_validator


class InventoryResponse(BaseModel):
    """Schema for inventory response"""
    product_id: int
    available_quantity: int
    reserved_quantity: int
    last_updated: Optional[datetime]

    class Config:
        from_attributes = True
        json_schema_extra = {
            "example": {
                "product_id": 1,
                "available_quantity": 100,
                "reserved_quantity": 10,
                "last_updated": "2026-07-21T09:00:00Z"
            }
        }


class ReserveStockRequest(BaseModel):
    """Schema for reserving stock"""
    product_id: int = Field(..., gt=0, description="Product ID")
    quantity: int = Field(..., gt=0, le=10000, description="Quantity to reserve (must be greater than 0)")

    @field_validator('product_id')
    @classmethod
    def validate_product_id(cls, v):
        """Ensure product_id is positive"""
        if v <= 0:
            raise ValueError('Product ID must be positive')
        return v

    @field_validator('quantity')
    @classmethod
    def validate_quantity(cls, v):
        """Ensure quantity is positive and reasonable"""
        if v <= 0:
            raise ValueError('Quantity must be greater than 0')
        if v > 10000:
            raise ValueError('Quantity cannot exceed 10,000 per operation')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "product_id": 1,
                "quantity": 5
            }
        }


class ReleaseStockRequest(BaseModel):
    """Schema for releasing reserved stock"""
    product_id: int = Field(..., gt=0, description="Product ID")
    quantity: int = Field(..., gt=0, le=10000, description="Quantity to release (must be greater than 0)")

    @field_validator('product_id')
    @classmethod
    def validate_product_id(cls, v):
        """Ensure product_id is positive"""
        if v <= 0:
            raise ValueError('Product ID must be positive')
        return v

    @field_validator('quantity')
    @classmethod
    def validate_quantity(cls, v):
        """Ensure quantity is positive and reasonable"""
        if v <= 0:
            raise ValueError('Quantity must be greater than 0')
        if v > 10000:
            raise ValueError('Quantity cannot exceed 10,000 per operation')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "product_id": 1,
                "quantity": 5
            }
        }


class AddStockRequest(BaseModel):
    """Schema for adding stock"""
    product_id: int = Field(..., gt=0, description="Product ID")
    quantity: int = Field(..., gt=0, le=100000, description="Quantity to add (must be greater than 0)")

    @field_validator('product_id')
    @classmethod
    def validate_product_id(cls, v):
        """Ensure product_id is positive"""
        if v <= 0:
            raise ValueError('Product ID must be positive')
        return v

    @field_validator('quantity')
    @classmethod
    def validate_quantity(cls, v):
        """Ensure quantity is positive and reasonable"""
        if v <= 0:
            raise ValueError('Quantity must be greater than 0')
        if v > 100000:
            raise ValueError('Quantity cannot exceed 100,000 per operation')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "product_id": 1,
                "quantity": 50
            }
        }


class RemoveStockRequest(BaseModel):
    """Schema for removing stock"""
    product_id: int = Field(..., gt=0, description="Product ID")
    quantity: int = Field(..., gt=0, le=100000, description="Quantity to remove (must be greater than 0)")

    @field_validator('product_id')
    @classmethod
    def validate_product_id(cls, v):
        """Ensure product_id is positive"""
        if v <= 0:
            raise ValueError('Product ID must be positive')
        return v

    @field_validator('quantity')
    @classmethod
    def validate_quantity(cls, v):
        """Ensure quantity is positive and reasonable"""
        if v <= 0:
            raise ValueError('Quantity must be greater than 0')
        if v > 100000:
            raise ValueError('Quantity cannot exceed 100,000 per operation')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "product_id": 1,
                "quantity": 10
            }
        }


class InventoryOperationResponse(BaseModel):
    """Schema for inventory operation response"""
    success: bool
    message: str
    inventory: Optional[InventoryResponse] = None

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "message": "Stock reserved successfully",
                "inventory": {
                    "product_id": 1,
                    "available_quantity": 95,
                    "reserved_quantity": 15,
                    "last_updated": "2026-07-21T09:00:00Z"
                }
            }
        }
