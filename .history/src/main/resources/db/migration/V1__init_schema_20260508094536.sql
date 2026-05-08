CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users(id),
    customer_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    kyc_status VARCHAR(30) NOT NULL,
    bvn VARCHAR(30),
    nin VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(30) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    balance NUMERIC(19,4) NOT NULL,
    available_balance NUMERIC(19,4) NOT NULL,
    status VARCHAR(30) NOT NULL,
    daily_limit NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(80) NOT NULL UNIQUE,
    source_account_id BIGINT REFERENCES bank_accounts(id),
    destination_account_id BIGINT REFERENCES bank_accounts(id),
    amount NUMERIC(19,4) NOT NULL,
    fee_amount NUMERIC(19,4) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(120) NOT NULL,
    action VARCHAR(120) NOT NULL,
    resource VARCHAR(120) NOT NULL,
    resource_id VARCHAR(120),
    details TEXT,
    ip_address VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    line1 VARCHAR(255) NOT NULL,
    line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL
);

CREATE TABLE kyc_documents (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    document_type VARCHAR(100) NOT NULL,
    document_url VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE risk_profiles (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id),
    risk_level VARCHAR(30) NOT NULL,
    rationale VARCHAR(255) NOT NULL
);

CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    principal_amount NUMERIC(19,4) NOT NULL,
    interest_rate NUMERIC(8,4) NOT NULL,
    tenor_months INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE loan_repayments (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id),
    amount NUMERIC(19,4) NOT NULL,
    repayment_date DATE NOT NULL
);

CREATE TABLE loan_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id),
    due_date DATE NOT NULL,
    due_amount NUMERIC(19,4) NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES bank_accounts(id),
    pan_masked VARCHAR(30) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    frozen BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE card_transactions (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT NOT NULL REFERENCES cards(id),
    amount NUMERIC(19,4) NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(150) NOT NULL,
    channel VARCHAR(40) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    template_key VARCHAR(120) NOT NULL UNIQUE,
    subject VARCHAR(200) NOT NULL,
    body_template TEXT NOT NULL
);

CREATE TABLE login_history (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    success BOOLEAN NOT NULL,
    ip_address VARCHAR(64),
    login_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE compliance_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_accounts_customer_id ON bank_accounts(customer_id);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_destination_account ON transactions(destination_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
-- Composite index to speed up daily debit aggregation for an account
CREATE INDEX idx_transactions_source_created_at ON transactions(source_account_id, created_at);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor);

-- Table to store per-account configured limits (redundant with bank_accounts.daily_limit but allows history/overrides)
CREATE TABLE account_limits (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES bank_accounts(id),
    daily_limit NUMERIC(19,4) NOT NULL,
    monthly_limit NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_account_limits_account_id ON account_limits(account_id);

-- Revoked access tokens table for access-token blacklisting
CREATE TABLE revoked_access_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(1024) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_revoked_access_tokens_token ON revoked_access_tokens(token);

-- Ensure efficient lookup of idempotency key (unique constraint already creates an index, but explicit index helps some DB engines)
CREATE INDEX IF NOT EXISTS idx_transactions_idempotency_key ON transactions(idempotency_key);
