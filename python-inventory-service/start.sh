#!/bin/bash
# Startup script for Python Inventory Service

set -e

echo "🐍 Starting Python Inventory Service..."

# Check if venv exists
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
    echo "✅ Virtual environment created"
fi

# Activate virtual environment and install dependencies
echo "📚 Installing dependencies..."
./venv/bin/pip install -q --upgrade pip setuptools wheel
./venv/bin/pip install -q -r requirements.txt
echo "✅ Dependencies installed"

# Run migrations
echo "🗄️  Running database migrations..."
./venv/bin/alembic upgrade head
echo "✅ Database ready"

# Start service
echo "🚀 Starting service on http://localhost:8000"
echo "📖 API Documentation: http://localhost:8000/docs"
echo ""
./venv/bin/uvicorn inventory_service.main:app --reload --port 8000
