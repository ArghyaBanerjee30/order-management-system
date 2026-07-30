"""Constants for the inventory service."""


class ErrorMessages:
    """Error message constants."""

    PRODUCT_NOT_FOUND = "Product with id {} not found"
    PRODUCT_NOT_FOUND_BY_SKU = "Product with SKU '{}' not found"
    PRODUCT_DUPLICATE_SKU = "Product with SKU '{}' already exists"
    INSUFFICIENT_STOCK = "Insufficient stock for product {}. Available: {}, Requested: {}"
    INVENTORY_NOT_FOUND = "Inventory record for product {} not found"
    INVENTORY_NOT_FOUND_FOR_PRODUCT = "Inventory not found for product {}"


class SuccessMessages:
    """Success message constants."""

    STOCK_RESERVED = "Successfully reserved {} units of product {}"
    STOCK_RELEASED = "Successfully released {} units of product {}"
    STOCK_ADDED = "Successfully added {} units to product {}"
    STOCK_REMOVED = "Successfully removed {} units from product {}"
