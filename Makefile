# Makefile for IoT Devices Management System Development and Testing
# Provides commands for running the application, tests, and k6 performance tests

.PHONY: help clean build test start stop restart logs health

# Default target
help:
	@echo "IoT Devices Management System Development Commands"
	@echo "=================================="
	@echo ""
	@echo "Application Management:"
	@echo "  start          - Start PostgreSQL and the application"
	@echo "  start-k6       - Start application with k6 testing profile"
	@echo "  stop           - Stop the application and PostgreSQL"
	@echo "  restart        - Restart the application and PostgreSQL"
	@echo "  logs           - Show application logs"
	@echo "  health         - Check application health"
	@echo ""
	@echo "Testing:"
	@echo "  test           - Run unit and integration tests"
	@echo "  k6-test        - Run all k6 performance tests"
	@echo "  k6-test-automated - Automated k6 workflow (reset + smoke + load + cleanup)"
	@echo "  k6-test-quick  - Quick k6 test (reset + smoke + cleanup)"
	@echo "  k6-test-individual - Run each k6 test individually"
	@echo "  k6-test-individual-fresh - Individual tests with fresh database"
	@echo "  k6-smoke       - Run k6 smoke test (basic validation)"
	@echo "  k6-load        - Run k6 load test (normal load)"
	@echo "  k6-stress      - Run k6 stress test (find breaking point)"
	@echo "  k6-spike       - Run k6 spike test (sudden traffic surge)"
	@echo "  k6-concurrent  - Run k6 concurrent test (race conditions)"
	@echo "  k6-mixed       - Run k6 mixed workload test (realistic simulation)"
	@echo "  k6-duplicate   - Run k6 duplicate vote test (duplicate vote handling)"
	@echo "  k6-expiration  - Run k6 session expiration test (expired session handling)"
	@echo "  k6-resilience  - Run k6 tests with resilience-test profile (circuit breaker testing)"
	@echo ""
	@echo "Development:"
	@echo "  clean          - Clean build artifacts and containers"
	@echo "  build          - Build the application"
	@echo "  setup          - Initial setup (build + start)"

# Application Management
start:
	@echo "🚀 Starting IoT Devices Management System..."
	@echo "1. Stopping any existing containers..."
	docker-compose down -v
	@echo "2. Starting PostgreSQL..."
	docker-compose up -d postgres
	@echo "3. Waiting for PostgreSQL to be ready..."
	sleep 10
	@echo "4. Starting the application..."
	mvn spring-boot:run

start-k6:
	@echo "🚀 Starting Voting System for K6 Testing..."
	@echo "1. Stopping any existing containers..."
	docker-compose down -v
	@echo "2. Starting PostgreSQL..."
	docker-compose up -d postgres
	@echo "3. Waiting for PostgreSQL to be ready..."
	sleep 10
	@echo "4. Starting the application with K6 profile..."
	mvn spring-boot:run -Dspring-boot.run.profiles=k6

start-resilience:
	@echo "🚀 Starting Voting System for Resilience Testing..."
	@echo "1. Stopping any existing containers..."
	docker-compose down -v
	@echo "2. Starting PostgreSQL..."
	docker-compose up -d postgres
	@echo "3. Waiting for PostgreSQL to be ready..."
	sleep 10
	@echo "4. Starting the application with resilience-test profile..."
	mvn spring-boot:run -Dspring-boot.run.profiles=resilience-test

stop:
	@echo "🛑 Stopping IoT Devices Management System..."
	docker-compose down -v
	pkill -f "spring-boot:run" || true

restart: stop start

logs:
	docker-compose logs -f

health:
	@echo "🏥 Checking application health..."
	@curl -s http://localhost:8080/actuator/health | python3 -m json.tool || echo "❌ Application not responding"

# Testing
test:
	@echo "🧪 Running unit and integration tests..."
	mvn test

k6-test: k6-smoke k6-load k6-stress k6-spike k6-concurrent k6-mixed
	@echo "✅ All k6 tests completed!"

k6-smoke:
	@echo "💨 Running k6 smoke test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test validates basic functionality with minimal load (5 VUs, 1 minute)"
	@echo ""
	k6 run k6/scripts/smoke-test.js

k6-load:
	@echo "📊 Running k6 load test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test simulates normal expected load (50 VUs, 9 minutes)"
	@echo ""
	k6 run k6/scripts/load-test.js

k6-stress:
	@echo "🔥 Running k6 stress test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test finds the system's breaking point (200-300 VUs, 14 minutes)"
	@echo ""
	k6 run k6/scripts/stress-test.js

k6-spike:
	@echo "⚡ Running k6 spike test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test simulates sudden traffic surge (10→200→10 VUs, 5.5 minutes)"
	@echo ""
	k6 run k6/scripts/spike-test.js

k6-concurrent:
	@echo "🔄 Running k6 concurrent test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test validates database consistency under high concurrency (100 VUs, 2 minutes)"
	@echo ""
	k6 run k6/scripts/concurrent-voting.js

k6-mixed:
	@echo "🎯 Running k6 mixed workload test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test simulates realistic production patterns (30 VUs, 10 minutes)"
	@echo ""
	k6 run k6/scripts/mixed-workload.js

k6-duplicate:
	@echo "🔄 Running k6 duplicate vote test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test validates duplicate vote handling (3 VUs, 30 seconds)"
	@echo ""
	k6 run k6/scripts/duplicate-vote-test.js

k6-expiration:
	@echo "⏰ Running k6 session expiration test..."
	@echo "Prerequisites: Application must be running (use 'make start')"
	@echo "This test validates session expiration handling (2 VUs, 2 minutes)"
	@echo ""
	k6 run k6/scripts/session-expiration-test.js

k6-resilience:
	@echo "🛡️ Running k6 resilience test..."
	@echo "Prerequisites: Application must be running with resilience-test profile (use 'make start-resilience')"
	@echo "This test validates circuit breakers and resilience patterns with aggressive limits"
	@echo ""
	k6 run k6/scripts/load-test.js

# Development
clean:
	@echo "🧹 Cleaning build artifacts and containers..."
	mvn clean
	docker-compose down -v
	docker system prune -f

build:
	@echo "🔨 Building the application..."
	mvn clean compile

setup: clean build start
	@echo "✅ Setup complete! Application is starting..."

# Database Management
db-reset:
	@echo "🗄️ Resetting database..."
	docker-compose down -v
	docker-compose up -d postgres
	sleep 10
	@echo "✅ Database reset complete"

db-clean-quick:
	@echo "🧹 Quick database cleanup (keeping application running)..."
	@docker exec votacao-postgres psql -U votacao -d votacao -c "TRUNCATE TABLE agendas, voting_sessions, votes RESTART IDENTITY CASCADE;" || echo "⚠️ Database cleanup failed, but continuing..."
	@echo "✅ Database cleaned"

# K6 Test with Database Reset
k6-smoke-fresh: db-reset
	@echo "⏳ Waiting for application to start..."
	@sleep 5
	@make start-k6 &
	@sleep 30
	@echo "💨 Running k6 smoke test with fresh database..."
	k6 run k6/scripts/smoke-test.js
	@make stop

k6-load-fresh: db-reset
	@echo "⏳ Waiting for application to start..."
	@sleep 5
	@make start-k6 &
	@sleep 30
	@echo "📊 Running k6 load test with fresh database..."
	k6 run k6/scripts/load-test.js
	@make stop

# Automated K6 Testing Workflow
k6-test-automated:
	@echo "🤖 Starting automated k6 testing workflow..."
	@echo "1. Stopping any existing processes..."
	@make stop || true
	@echo "2. Resetting database..."
	@make db-reset
	@echo "3. Starting application with k6 profile..."
	@make start-k6 &
	@echo "4. Waiting for application to be ready..."
	@sleep 30
	@echo "5. Running smoke test..."
	@make k6-smoke
	@echo "6. Running load test..."
	@make k6-load
	@echo "7. Cleaning up..."
	@make stop
	@echo "✅ Automated k6 testing workflow completed!"

k6-test-quick:
	@echo "⚡ Quick k6 test (smoke only)..."
	@make stop || true
	@make db-reset
	@make start-k6 &
	@sleep 30
	@make k6-smoke
	@make stop
	@echo "✅ Quick k6 test completed!"

# Performance Monitoring
k6-with-report:
	@echo "📈 Running k6 test with HTML report..."
	k6 run --out json=reports/k6-results.json k6/scripts/load-test.js
	@echo "📊 Results saved to reports/k6-results.json"

# Quick Development Cycle
dev-cycle: clean build start
	@echo "🔄 Development cycle complete!"
	@echo "Application is running. Use 'make logs' to see output."
	@echo "Use 'make stop' to stop the application."

# Individual K6 Test Runner
k6-test-individual:
	@echo "🧪 Running individual k6 tests..."
	@echo "Prerequisites: Application must be running (use 'make start-k6')"
	@echo ""
	@echo "Testing smoke-test.js..."
	@make db-clean-quick
	@k6 run k6/scripts/smoke-test.js
	@echo ""
	@echo "Testing load-test.js..."
	@make db-clean-quick
	@k6 run k6/scripts/load-test.js
	@echo ""
	@echo "Testing stress-test.js..."
	@make db-clean-quick
	@k6 run k6/scripts/stress-test.js
	@echo ""
	@echo "Testing spike-test.js..."
	@make db-clean-quick
	@k6 run k6/scripts/spike-test.js
	@echo ""
	@echo "Testing concurrent-voting.js..."
	@make db-clean-quick
	@k6 run k6/scripts/concurrent-voting.js
	@echo ""
	@echo "Testing mixed-workload.js..."
	@make db-clean-quick
	@k6 run k6/scripts/mixed-workload.js
	@echo ""
	@echo "Testing duplicate-vote-test.js..."
	@make db-clean-quick
	@k6 run k6/scripts/duplicate-vote-test.js
	@echo ""
	@echo "Testing session-expiration-test.js..."
	@make db-clean-quick
	@k6 run k6/scripts/session-expiration-test.js
	@echo "✅ All individual k6 tests completed!"

# Individual K6 Test with Fresh Database
k6-test-individual-fresh: db-reset
	@echo "🧪 Running individual k6 tests with fresh database..."
	@echo "⏳ Waiting for application to start..."
	@sleep 5
	@make start-k6 &
	@sleep 30
	@make k6-test-individual
	@make stop
	@echo "✅ Individual k6 tests with fresh database completed!"


# Production-like Testing
k6-production-simulation:
	@echo "🏭 Running production-like k6 simulation..."
	@echo "This runs a sequence of tests to simulate production conditions"
	@make k6-smoke-fresh
	@make k6-load-fresh
	@echo "✅ Production simulation complete!"
