from setuptools import setup, find_packages

setup(
    name="inventory-service",
    version="1.0.0",
    description="Inventory Management Service",
    packages=find_packages(),
    python_requires=">=3.10",
    install_requires=[
        "fastapi==0.109.0",
        "uvicorn[standard]==0.27.0",
        "sqlalchemy==2.0.25",
        "alembic==1.13.1",
        "pydantic==2.5.3",
        "pydantic-settings==2.1.0",
        "python-multipart==0.0.6",
        "aiosqlite==0.19.0",
    ],
)
