"""Custom exception classes for the inventory service."""
from inventory_service.constants import ErrorMessages


class ProductNotFoundException(Exception):
    def __init__(self, product_id: int):
        self.product_id = product_id
        self.message = ErrorMessages.PRODUCT_NOT_FOUND.format(product_id)
        super().__init__(self.message)


class ProductNotFoundBySKUException(Exception):
    def __init__(self, sku: str):
        self.sku = sku
        self.message = ErrorMessages.PRODUCT_NOT_FOUND_BY_SKU.format(sku)
        super().__init__(self.message)


class DuplicateProductException(Exception):
    def __init__(self, sku: str):
        self.sku = sku
        self.message = ErrorMessages.PRODUCT_DUPLICATE_SKU.format(sku)
        super().__init__(self.message)


class InsufficientStockException(Exception):
    def __init__(self, product_id: int, available: int, requested: int):
        self.product_id = product_id
        self.available = available
        self.requested = requested
        self.message = ErrorMessages.INSUFFICIENT_STOCK.format(product_id, available, requested)
        super().__init__(self.message)


class InventoryNotFoundException(Exception):
    def __init__(self, product_id: int):
        self.product_id = product_id
        self.message = ErrorMessages.INVENTORY_NOT_FOUND.format(product_id)
        super().__init__(self.message)


class InvalidOperationException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)
