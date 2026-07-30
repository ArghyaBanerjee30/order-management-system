"""Product service for business logic"""
from typing import List
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError

from inventory_service.repository.product_repository import ProductRepository
from inventory_service.schemas.product_schema import ProductCreate, ProductUpdate, ProductResponse
from inventory_service.exceptions import ProductNotFoundException, DuplicateProductException
from inventory_service.mappers import (
    create_product_entity,
    to_product_response,
    to_product_responses,
    apply_product_update
)


class ProductService:
    def __init__(self, db: Session):
        self.repository = ProductRepository(db)

    def create_product(self, product_data: ProductCreate) -> ProductResponse:
        if self.repository.get_by_sku(product_data.sku):
            raise DuplicateProductException(product_data.sku)

        try:
            product = create_product_entity(product_data)
            created = self.repository.create(product)
            return to_product_response(created)
        except IntegrityError:
            raise DuplicateProductException(product_data.sku)

    def get_product(self, product_id: int) -> ProductResponse:
        product = self.repository.get_by_id(product_id)
        if not product:
            raise ProductNotFoundException(product_id)
        return to_product_response(product)

    def get_all_products(self, skip: int = 0, limit: int = 100, active_only: bool = True) -> List[ProductResponse]:
        products = self.repository.get_all(skip=skip, limit=limit, active_only=active_only)
        return to_product_responses(products)

    def update_product(self, product_id: int, product_data: ProductUpdate) -> ProductResponse:
        product = self.repository.get_by_id(product_id)
        if not product:
            raise ProductNotFoundException(product_id)

        if product_data.sku and product_data.sku != product.sku:
            if self.repository.get_by_sku(product_data.sku):
                raise DuplicateProductException(product_data.sku)

        try:
            updated = apply_product_update(product, product_data)
            saved = self.repository.update(updated)
            return to_product_response(saved)
        except IntegrityError:
            raise DuplicateProductException(product_data.sku if product_data.sku else product.sku)

    def delete_product(self, product_id: int) -> bool:
        success = self.repository.soft_delete(product_id)
        if not success:
            raise ProductNotFoundException(product_id)
        return success
