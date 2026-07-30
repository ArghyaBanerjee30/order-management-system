"""Main application entry point for Inventory Service"""
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from inventory_service.database.config import init_db
from inventory_service.exceptions import (
    ProductNotFoundException,
    ProductNotFoundBySKUException,
    DuplicateProductException,
    InsufficientStockException,
    InventoryNotFoundException,
    InvalidOperationException
)
from inventory_service.logging_config import setup_logging, get_logger
import time
from datetime import datetime

# Configure logging
setup_logging(log_level="INFO")
logger = get_logger(__name__)

# Create FastAPI application
app = FastAPI(
    title="Inventory Service API",
    description="""
    # Inventory Management Service

    REST API for managing product inventory and stock operations in the Order Management System.

    ## Features

    * **Product Management**: Create, read, update, and delete products
    * **Inventory Tracking**: Track available and reserved quantities
    * **Stock Operations**: Reserve, release, add, and remove stock
    * **Inventory History**: Maintain audit trail of all inventory operations

    ## API Endpoints

    * `/products` - Product CRUD operations
    * `/inventory` - Inventory management and stock operations

    ## Response Codes

    * `200` - Successful operation
    * `201` - Resource created successfully
    * `400` - Invalid request or insufficient stock
    * `404` - Resource not found
    * `409` - Duplicate resource (e.g., SKU already exists)
    * `500` - Internal server error
    """,
    version="1.0.0",
    contact={
        "name": "Order Management System Team",
        "email": "support@ordermanagement.com"
    },
    license_info={
        "name": "MIT License",
        "url": "https://opensource.org/licenses/MIT"
    },
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/api/openapi.json"
)

# Request/Response Logging Middleware
@app.middleware("http")
async def log_requests(request: Request, call_next):
    """Log all HTTP requests and responses"""
    # Generate request ID
    request_id = f"{int(time.time() * 1000)}"

    # Log request
    logger.info(
        f"Request started: {request.method} {request.url.path} "
        f"[ID: {request_id}] [Client: {request.client.host if request.client else 'unknown'}]"
    )

    # Process request
    start_time = time.time()
    try:
        response = await call_next(request)
        process_time = time.time() - start_time

        # Log response
        logger.info(
            f"Request completed: {request.method} {request.url.path} "
            f"[ID: {request_id}] [Status: {response.status_code}] "
            f"[Duration: {process_time:.3f}s]"
        )

        # Add custom headers
        response.headers["X-Request-ID"] = request_id
        response.headers["X-Process-Time"] = str(process_time)

        return response
    except Exception as e:
        process_time = time.time() - start_time
        logger.error(
            f"Request failed: {request.method} {request.url.path} "
            f"[ID: {request_id}] [Error: {str(e)}] "
            f"[Duration: {process_time:.3f}s]",
            exc_info=True
        )
        raise

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify actual origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup_event():
    """Initialize database on application startup"""
    logger.info("Starting Inventory Service...")
    init_db()
    logger.info("Database initialized successfully")
    logger.info("Inventory Service started and ready to accept requests")


@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on application shutdown"""
    logger.info("Shutting down Inventory Service...")
    logger.info("Application shutdown complete")


@app.get("/")
async def root():
    """Root endpoint - health check"""
    return {
        "service": "Inventory Service",
        "version": "1.0.0",
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """Basic health check endpoint for monitoring"""
    return {
        "status": "healthy",
        "service": "inventory-service"
    }


@app.get("/health/ready")
async def readiness_check():
    """
    Readiness check endpoint - indicates if service is ready to accept traffic.
    Checks database connectivity.
    """
    from inventory_service.database.config import engine

    try:
        # Test database connection
        with engine.connect() as connection:
            connection.execute("SELECT 1")

        return {
            "ready": True,
            "service": "inventory-service",
            "database": "UP"
        }
    except Exception as e:
        logger.error(f"Readiness check failed: {str(e)}")
        from fastapi import status
        from fastapi.responses import JSONResponse
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content={
                "ready": False,
                "service": "inventory-service",
                "database": "DOWN",
                "error": str(e)
            }
        )


@app.get("/health/live")
async def liveness_check():
    """
    Liveness check endpoint - indicates if service is alive.
    Simple check without external dependencies.
    """
    return {
        "alive": True,
        "service": "inventory-service",
        "status": "UP"
    }


# Exception handlers

@app.exception_handler(ProductNotFoundException)
async def product_not_found_handler(request: Request, exc: ProductNotFoundException):
    """Handle ProductNotFoundException (404)"""
    logger.error(f"Product not found: {exc.message}")
    return JSONResponse(
        status_code=404,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 404,
            "message": exc.message,
            "path": str(request.url.path)
        }
    )


@app.exception_handler(ProductNotFoundBySKUException)
async def product_not_found_by_sku_handler(request: Request, exc: ProductNotFoundBySKUException):
    """Handle ProductNotFoundBySKUException (404)"""
    logger.error(f"Product not found by SKU: {exc.message}")
    return JSONResponse(
        status_code=404,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 404,
            "message": exc.message,
            "path": str(request.url.path)
        }
    )


@app.exception_handler(DuplicateProductException)
async def duplicate_product_handler(request: Request, exc: DuplicateProductException):
    """Handle DuplicateProductException (409)"""
    logger.error(f"Duplicate product: {exc.message}")
    return JSONResponse(
        status_code=409,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 409,
            "message": exc.message,
            "path": str(request.url.path)
        }
    )


@app.exception_handler(InsufficientStockException)
async def insufficient_stock_handler(request: Request, exc: InsufficientStockException):
    """Handle InsufficientStockException (400)"""
    logger.error(f"Insufficient stock: {exc.message}")
    return JSONResponse(
        status_code=400,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 400,
            "message": exc.message,
            "path": str(request.url.path),
            "details": {
                "product_id": exc.product_id,
                "available": exc.available,
                "requested": exc.requested
            }
        }
    )


@app.exception_handler(InventoryNotFoundException)
async def inventory_not_found_handler(request: Request, exc: InventoryNotFoundException):
    """Handle InventoryNotFoundException (404)"""
    logger.error(f"Inventory not found: {exc.message}")
    return JSONResponse(
        status_code=404,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 404,
            "message": exc.message,
            "path": str(request.url.path)
        }
    )


@app.exception_handler(InvalidOperationException)
async def invalid_operation_handler(request: Request, exc: InvalidOperationException):
    """Handle InvalidOperationException (400)"""
    logger.error(f"Invalid operation: {exc.message}")
    return JSONResponse(
        status_code=400,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 400,
            "message": exc.message,
            "path": str(request.url.path)
        }
    )


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """Handle validation errors (422)"""
    logger.error(f"Validation error: {exc.errors()}")

    errors = []
    for error in exc.errors():
        field = ".".join(str(loc) for loc in error["loc"] if loc != "body")
        errors.append({
            "field": field,
            "message": error["msg"]
        })

    return JSONResponse(
        status_code=400,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 400,
            "message": "Validation failed",
            "path": str(request.url.path),
            "errors": errors
        }
    )


@app.exception_handler(Exception)
async def generic_exception_handler(request: Request, exc: Exception):
    """Handle generic exceptions (500)"""
    logger.error(f"Unexpected error: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={
            "timestamp": datetime.now().isoformat(),
            "status": 500,
            "message": f"An unexpected error occurred: {str(exc)}",
            "path": str(request.url.path)
        }
    )


# Router registration
from inventory_service.routers import product, inventory

app.include_router(product.router, prefix="/products", tags=["Products"])
app.include_router(inventory.router, prefix="/inventory", tags=["Inventory"])
