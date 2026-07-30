"""Inventory service for business logic"""
from sqlalchemy.orm import Session
from fastapi import HTTPException, status

from inventory_service.repository.inventory_repository import InventoryRepository
from inventory_service.exceptions import InsufficientStockException
from inventory_service.constants import ErrorMessages, SuccessMessages
from inventory_service.schemas.inventory_schema import (
    InventoryResponse,
    ReserveStockRequest,
    ReleaseStockRequest,
    AddStockRequest,
    RemoveStockRequest,
    InventoryOperationResponse
)
from inventory_service.mappers import create_inventory_operation_response


class InventoryService:
    def __init__(self, db: Session):
        self.repository = InventoryRepository(db)

    def get_inventory(self, product_id: int) -> InventoryResponse:
        inventory = self.repository.get_by_product_id(product_id)
        if not inventory:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ErrorMessages.INVENTORY_NOT_FOUND_FOR_PRODUCT.format(product_id)
            )
        return InventoryResponse.model_validate(inventory)

    def reserve_stock(self, request: ReserveStockRequest) -> InventoryOperationResponse:
        try:
            self.repository.reserve_stock(request.product_id, request.quantity)
            inventory = self.repository.get_by_product_id(request.product_id)
            return create_inventory_operation_response(
                True,
                SuccessMessages.STOCK_RESERVED,
                request.quantity,
                request.product_id,
                inventory
            )
        except InsufficientStockException as e:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

    def release_stock(self, request: ReleaseStockRequest) -> InventoryOperationResponse:
        success = self.repository.release_stock(request.product_id, request.quantity)
        if not success:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ErrorMessages.INVENTORY_NOT_FOUND_FOR_PRODUCT.format(request.product_id)
            )

        inventory = self.repository.get_by_product_id(request.product_id)
        return create_inventory_operation_response(
            success,
            SuccessMessages.STOCK_RELEASED,
            request.quantity,
            request.product_id,
            inventory
        )

    def add_stock(self, request: AddStockRequest) -> InventoryOperationResponse:
        self.repository.add_stock(request.product_id, request.quantity)
        inventory = self.repository.get_by_product_id(request.product_id)
        return create_inventory_operation_response(
            True,
            SuccessMessages.STOCK_ADDED,
            request.quantity,
            request.product_id,
            inventory
        )

    def remove_stock(self, request: RemoveStockRequest) -> InventoryOperationResponse:
        try:
            self.repository.remove_stock(request.product_id, request.quantity)
            inventory = self.repository.get_by_product_id(request.product_id)
            return create_inventory_operation_response(
                True,
                SuccessMessages.STOCK_REMOVED,
                request.quantity,
                request.product_id,
                inventory
            )
        except InsufficientStockException as e:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
