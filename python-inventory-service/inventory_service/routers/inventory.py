"""Inventory router for API endpoints"""
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from inventory_service.database.config import get_db
from inventory_service.schemas.inventory_schema import (
    InventoryResponse,
    ReserveStockRequest,
    ReleaseStockRequest,
    AddStockRequest,
    RemoveStockRequest,
    InventoryOperationResponse
)
from inventory_service.services.inventory_service import InventoryService

router = APIRouter(
    responses={
        404: {"description": "Inventory not found for product"},
        400: {"description": "Insufficient stock or invalid request"}
    }
)


@router.get(
    "/{product_id}",
    response_model=InventoryResponse,
    summary="Get inventory by product ID",
    description="Retrieves current inventory levels for a specific product"
)
def get_inventory(
    product_id: int,
    db: Session = Depends(get_db)
):
    """Get inventory for a product"""
    service = InventoryService(db)
    return service.get_inventory(product_id)


@router.post(
    "/reserve",
    response_model=InventoryOperationResponse,
    status_code=status.HTTP_200_OK,
    summary="Reserve stock",
    description="Reserves stock for an order (decreases available, increases reserved). Used when processing orders.",
    responses={
        200: {
            "description": "Stock reserved successfully",
            "content": {
                "application/json": {
                    "example": {
                        "success": True,
                        "message": "Successfully reserved 5 units of product 1",
                        "inventory": {
                            "product_id": 1,
                            "available_quantity": 95,
                            "reserved_quantity": 5,
                            "last_updated": "2026-07-21T10:30:00"
                        }
                    }
                }
            }
        },
        400: {"description": "Insufficient stock available"}
    }
)
def reserve_stock(
    request: ReserveStockRequest,
    db: Session = Depends(get_db)
):
    """Reserve stock for an order"""
    service = InventoryService(db)
    return service.reserve_stock(request)


@router.post(
    "/release",
    response_model=InventoryOperationResponse,
    status_code=status.HTTP_200_OK,
    summary="Release reserved stock",
    description="Releases reserved stock (increases available, decreases reserved)"
)
def release_stock(
    request: ReleaseStockRequest,
    db: Session = Depends(get_db)
):
    """Release reserved stock"""
    service = InventoryService(db)
    return service.release_stock(request)


@router.post(
    "/add",
    response_model=InventoryOperationResponse,
    status_code=status.HTTP_200_OK,
    summary="Add stock to inventory",
    description="Adds stock to the available quantity for a product"
)
def add_stock(
    request: AddStockRequest,
    db: Session = Depends(get_db)
):
    """Add stock to inventory"""
    service = InventoryService(db)
    return service.add_stock(request)


@router.post(
    "/remove",
    response_model=InventoryOperationResponse,
    status_code=status.HTTP_200_OK,
    summary="Remove stock from inventory",
    description="Removes stock from the available quantity for a product"
)
def remove_stock(
    request: RemoveStockRequest,
    db: Session = Depends(get_db)
):
    """Remove stock from inventory"""
    service = InventoryService(db)
    return service.remove_stock(request)
