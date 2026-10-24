CREATE TABLE IF NOT EXISTS invoices (
    id             BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(20)   NOT NULL UNIQUE,
    customer_name  VARCHAR(100)  NOT NULL,
    amount         NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    currency       VARCHAR(3)    NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices (status);
