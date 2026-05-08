-- Add created_by and last_modified_by to core tables for auditing
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);

ALTER TABLE customers ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);

ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);

ALTER TABLE loans ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE loans ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);

ALTER TABLE account_limits ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE account_limits ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);

ALTER TABLE revoked_access_tokens ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE revoked_access_tokens ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(120);
