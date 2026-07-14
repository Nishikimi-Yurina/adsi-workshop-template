CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO employees (employee_code, name, email, password, role)
VALUES
    ('admin', '管理者', 'admin@example.com', '$2a$10$1dbBHa4DYwMT740yupBhZ.cn0rf7xA6jfNCr6VJDrfs6SLPzIQB9a', 'ADMIN'),
    ('user01', '山田太郎', 'yamada@example.com', '$2a$10$1dbBHa4DYwMT740yupBhZ.cn0rf7xA6jfNCr6VJDrfs6SLPzIQB9a', 'EMPLOYEE');
