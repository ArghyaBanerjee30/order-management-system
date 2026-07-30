"""
Custom exception classes for the inventory service.
"""


class ProductNotFoundException(Exception):
    """Exception raised when a product is not found."""

    def __init__(self, product_id: int):
        self.product_id = product_id
        self.message = f"Product with id {product_id} not found"
        super().__init__(self.message)


class ProductNotFoundBySKUException(Exception):
    """Exception raised when a product is not found by SKU."""

    def __init__(self, sku: str):
        self.sku = sku
        self.message = f"Product with SKU '{sku}' not found"
        super().__init__(self.message)


class DuplicateProductException(Exception):
    """Exception raised when attempting to create a product with duplicate SKU."""

    def __init__(self, sku: str):
        self.sku = sku
        self.message = f"Product with SKU '{sku}' already exists"
        super().__init__(self.message)


class InsufficientStockException(Exception):
    """Exception raised when there is insufficient stock for an operation."""

    def __init__(self, product_id: int, available: int, requested: int):
        self.product_id = product_id
        self.available = available
        self.requested = requested
        self.message = f"Insufficient stock for product {product_id}. Available: {available}, Requested: {requested}"
        super().__init__(self.message)


class InventoryNotFoundException(Exception):
    """Exception raised when inventory record is not found."""

    def __init__(self, product_id: int):
        self.product_id = product_id
        self.message = f"Inventory record for product {product_id} not found"
        super().__init__(self.message)


class InvalidOperationException(Exception):
    """Exception raised when an invalid operation is attempted."""

    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)
