"""Product router for API endpoints"""
from typing import List
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from inventory_service.database.config import get_db
from inventory_service.schemas.product_schema import ProductCreate, ProductUpdate, ProductResponse
from inventory_service.services.product_service import ProductService

router = APIRouter(
    responses={
        404: {"description": "Product not found"},
        409: {"description": "Product with this SKU already exists"}
    }
)


@router.post(
    "/",
    response_model=ProductResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create a new product",
    description="Creates a new product with the provided information. SKU must be unique.",
    responses={
        201: {
            "description": "Product created successfully",
            "content": {
                "application/json": {
                    "example": {
                        "id": 1,
                        "name": "Laptop",
                        "description": "High-performance laptop",
                        "price": 999.99,
                        "sku": "LAP-001",
                        "active": True
                    }
                }
            }
        },
        409: {"description": "Product with this SKU already exists"}
    }
)
def create_product(
    product: ProductCreate,
    db: Session = Depends(get_db)
):
    """
    Create a new product with the following information:

    - **name**: Product name (2-100 characters)
    - **description**: Product description (optional, max 500 characters)
    - **price**: Product price (must be > 0)
    - **sku**: Stock Keeping Unit (unique, 3-50 characters, alphanumeric and hyphens)
    - **active**: Whether the product is active (default: True)
    """
    service = ProductService(db)
    return service.create_product(product)


@router.get(
    "/",
    response_model=List[ProductResponse],
    summary="Get all products",
    description="Retrieves a list of all products with optional pagination"
)
def get_all_products(
    skip: int = Query(0, ge=0, description="Number of records to skip"),
    limit: int = Query(100, ge=1, le=500, description="Maximum number of records to return"),
    active_only: bool = Query(True, description="Return only active products"),
    db: Session = Depends(get_db)
):
    """Get all products with pagination"""
    service = ProductService(db)
    return service.get_all_products(skip=skip, limit=limit, active_only=active_only)


@router.get(
    "/{product_id}",
    response_model=ProductResponse,
    summary="Get product by ID",
    description="Retrieves a specific product by its ID"
)
def get_product(
    product_id: int,
    db: Session = Depends(get_db)
):
    """Get product by ID"""
    service = ProductService(db)
    return service.get_product(product_id)


@router.put(
    "/{product_id}",
    response_model=ProductResponse,
    summary="Update product",
    description="Updates an existing product's information"
)
def update_product(
    product_id: int,
    product: ProductUpdate,
    db: Session = Depends(get_db)
):
    """Update an existing product"""
    service = ProductService(db)
    return service.update_product(product_id, product)


@router.delete(
    "/{product_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete product",
    description="Soft deletes a product by setting active=False"
)
def delete_product(
    product_id: int,
    db: Session = Depends(get_db)
):
    """Delete (soft delete) a product"""
    service = ProductService(db)
    service.delete_product(product_id)
    return None
