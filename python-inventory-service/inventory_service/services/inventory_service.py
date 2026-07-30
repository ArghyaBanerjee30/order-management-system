"""Inventory service for business logic"""
from typing import Optional
from sqlalchemy.orm import Session
from fastapi import HTTPException, status

from inventory_service.models.Inventory import Inventory
from inventory_service.repository.inventory_repository import InventoryRepository
from inventory_service.exceptions import InsufficientStockException
from inventory_service.schemas.inventory_schema import (
    InventoryResponse,
    ReserveStockRequest,
    ReleaseStockRequest,
    AddStockRequest,
    RemoveStockRequest,
    InventoryOperationResponse
)
from inventory_service.logging_config import get_logger

logger = get_logger(__name__)


class InventoryService:
    """Service class for Inventory business logic"""

    def __init__(self, db: Session):
        self.repository = InventoryRepository(db)

    def get_inventory(self, product_id: int) -> InventoryResponse:
        """
        Get inventory by product ID.

        Args:
            product_id: Product ID

        Returns:
            Inventory response

        Raises:
            HTTPException: If inventory not found
        """
        logger.debug(f"Fetching inventory for product ID: {product_id}")
        inventory = self.repository.get_by_product_id(product_id)
        if not inventory:
            logger.error(f"Inventory not found for product {product_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Inventory not found for product {product_id}"
            )
        logger.debug(f"Inventory found: available={inventory.available_quantity}, reserved={inventory.reserved_quantity}")
        return InventoryResponse.model_validate(inventory)

    def reserve_stock(self, request: ReserveStockRequest) -> InventoryOperationResponse:
        """
        Reserve stock for a product.

        Args:
            request: Reserve stock request

        Returns:
            Inventory operation response

        Raises:
            HTTPException: If insufficient stock
        """
        logger.info(f"Reserving {request.quantity} units of product {request.product_id}")
        try:
            success = self.repository.reserve_stock(request.product_id, request.quantity)
            inventory = self.repository.get_by_product_id(request.product_id)

            logger.info(f"Stock reserved successfully for product {request.product_id}")
            return InventoryOperationResponse(
                success=success,
                message=f"Successfully reserved {request.quantity} units of product {request.product_id}",
                inventory=InventoryResponse.model_validate(inventory) if inventory else None
            )
        except InsufficientStockException as e:
            logger.error(f"Insufficient stock for product {request.product_id}: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )

    def release_stock(self, request: ReleaseStockRequest) -> InventoryOperationResponse:
        """
        Release reserved stock for a product.

        Args:
            request: Release stock request

        Returns:
            Inventory operation response

        Raises:
            HTTPException: If inventory not found
        """
        logger.info(f"Releasing {request.quantity} units of product {request.product_id}")
        success = self.repository.release_stock(request.product_id, request.quantity)
        if not success:
            logger.error(f"Inventory not found for product {request.product_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Inventory not found for product {request.product_id}"
            )

        inventory = self.repository.get_by_product_id(request.product_id)
        logger.info(f"Stock released successfully for product {request.product_id}")
        return InventoryOperationResponse(
            success=success,
            message=f"Successfully released {request.quantity} units of product {request.product_id}",
            inventory=InventoryResponse.model_validate(inventory) if inventory else None
        )

    def add_stock(self, request: AddStockRequest) -> InventoryOperationResponse:
        """
        Add stock to available quantity.

        Args:
            request: Add stock request

        Returns:
            Inventory operation response
        """
        logger.info(f"Adding {request.quantity} units to product {request.product_id}")
        success = self.repository.add_stock(request.product_id, request.quantity)
        inventory = self.repository.get_by_product_id(request.product_id)

        logger.info(f"Stock added successfully to product {request.product_id}")
        return InventoryOperationResponse(
            success=success,
            message=f"Successfully added {request.quantity} units to product {request.product_id}",
            inventory=InventoryResponse.model_validate(inventory) if inventory else None
        )

    def remove_stock(self, request: RemoveStockRequest) -> InventoryOperationResponse:
        """
        Remove stock from available quantity.

        Args:
            request: Remove stock request

        Returns:
            Inventory operation response

        Raises:
            HTTPException: If insufficient stock
        """
        logger.info(f"Removing {request.quantity} units from product {request.product_id}")
        try:
            success = self.repository.remove_stock(request.product_id, request.quantity)
            inventory = self.repository.get_by_product_id(request.product_id)

            logger.info(f"Stock removed successfully from product {request.product_id}")
            return InventoryOperationResponse(
                success=success,
                message=f"Successfully removed {request.quantity} units from product {request.product_id}",
                inventory=InventoryResponse.model_validate(inventory) if inventory else None
            )
        except InsufficientStockException as e:
            logger.error(f"Insufficient stock for product {request.product_id}: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )
