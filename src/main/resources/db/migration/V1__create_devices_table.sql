-- Create devices table
CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on brand for faster queries
CREATE INDEX IF NOT EXISTS idx_devices_brand ON devices(brand);

-- Create index on state for faster queries
CREATE INDEX IF NOT EXISTS idx_devices_state ON devices(state);

