"""Mappers for entity-DTO conversions."""
from typing import List, Optional, Callable
from inventory_service.models.Product import Product
from inventory_service.schemas.product_schema import ProductCreate, ProductUpdate, ProductResponse
from inventory_service.schemas.inventory_schema import InventoryResponse, InventoryOperationResponse
from inventory_service.constants import SuccessMessages


def create_product_entity(product_data: ProductCreate) -> Product:
    """Convert ProductCreate DTO to Product entity."""
    return Product(
        name=product_data.name,
        description=product_data.description,
        price=product_data.price,
        sku=product_data.sku,
        active=product_data.active
    )


def to_product_response(product: Product) -> ProductResponse:
    """Convert Product entity to ProductResponse DTO."""
    return ProductResponse.model_validate(product)


def to_product_responses(products: List[Product]) -> List[ProductResponse]:
    """Convert list of Product entities to list of ProductResponse DTOs."""
    return [to_product_response(p) for p in products]


def apply_product_update(product: Product, update_data: ProductUpdate) -> Product:
    """Apply updates to a product entity functionally."""
    update_if_present(product, 'name', update_data.name)
    update_if_present(product, 'description', update_data.description)
    update_if_present(product, 'price', update_data.price)
    update_if_present(product, 'active', update_data.active)

    if update_data.sku and update_data.sku != product.sku:
        product.sku = update_data.sku

    return product


def update_if_present(obj: object, attr: str, value: Optional[any]) -> None:
    """Update object attribute if value is not None."""
    if value is not None:
        setattr(obj, attr, value)


def create_inventory_operation_response(
    success: bool,
    message_template: str,
    quantity: int,
    product_id: int,
    inventory
) -> InventoryOperationResponse:
    """Create an InventoryOperationResponse."""
    return InventoryOperationResponse(
        success=success,
        message=message_template.format(quantity, product_id),
        inventory=InventoryResponse.model_validate(inventory) if inventory else None
    )
