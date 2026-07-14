CREATE TABLE overtime_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    date DATE NOT NULL,
    expected_hours DECIMAL(4,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approver_id BIGINT REFERENCES employees(id),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_overtime_requests_employee_status ON overtime_requests(employee_id, status);
CREATE INDEX idx_overtime_requests_approver_status ON overtime_requests(approver_id, status);
