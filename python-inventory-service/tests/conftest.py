"""
Pytest configuration and fixtures for inventory service tests.
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from database.config import Base, get_db
from main import app
from models.inventory import Inventory
from models.inventory_history import InventoryHistory, InventoryAction
from models.product import Product


# Create in-memory SQLite database for testing
SQLALCHEMY_TEST_DATABASE_URL = "sqlite:///:memory:"

engine = create_engine(
    SQLALCHEMY_TEST_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture(scope="function")
def test_db():
    """
    Create a fresh database for each test.
    """
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture(scope="function")
def db_session(test_db):
    """
    Provide a database session for testing.
    """
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()


def override_get_db():
    """
    Override the get_db dependency for testing.
    """
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()


@pytest.fixture(scope="function")
def client(test_db):
    """
    Provide a test client with overridden database dependency.
    """
    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


# Test Data Factories

@pytest.fixture
def sample_product_data():
    """
    Sample product data for testing.
    """
    return {
        "name": "Test Product",
        "description": "This is a test product",
        "price": 99.99,
        "sku": "TEST-SKU-001",
        "active": True
    }


@pytest.fixture
def sample_product(db_session, sample_product_data):
    """
    Create a sample product in the database.
    """
    product = Product(**sample_product_data)
    db_session.add(product)
    db_session.commit()
    db_session.refresh(product)
    return product


@pytest.fixture
def sample_inventory(db_session, sample_product):
    """
    Create sample inventory for a product.
    """
    inventory = Inventory(
        product_id=sample_product.id,
        available_quantity=100,
        reserved_quantity=0
    )
    db_session.add(inventory)
    db_session.commit()
    db_session.refresh(inventory)
    return inventory


@pytest.fixture
def multiple_products(db_session):
    """
    Create multiple products for testing list operations.
    """
    products = [
        Product(name="Product 1", description="First", price=10.00, sku="SKU-001", active=True),
        Product(name="Product 2", description="Second", price=20.00, sku="SKU-002", active=True),
        Product(name="Product 3", description="Third", price=30.00, sku="SKU-003", active=False),
    ]
    for product in products:
        db_session.add(product)
    db_session.commit()
    for product in products:
        db_session.refresh(product)
    return products


@pytest.fixture
def reserve_stock_request():
    """
    Sample reserve stock request data.
    """
    return {
        "product_id": 1,
        "quantity": 10
    }


@pytest.fixture
def release_stock_request():
    """
    Sample release stock request data.
    """
    return {
        "product_id": 1,
        "quantity": 10
    }


@pytest.fixture
def add_stock_request():
    """
    Sample add stock request data.
    """
    return {
        "product_id": 1,
        "quantity": 50
    }


@pytest.fixture
def remove_stock_request():
    """
    Sample remove stock request data.
    """
    return {
        "product_id": 1,
        "quantity": 20
    }
