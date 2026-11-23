CREATE TABLE IF NOT EXISTS project (
    id               BIGINT PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    total_amount     DECIMAL(12,2) NOT NULL,
    available_amount DECIMAL(12,2) NOT NULL,
    frozen_amount    DECIMAL(12,2) NOT NULL,
    used_amount      DECIMAL(12,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    project_id BIGINT       NOT NULL
);

CREATE TABLE IF NOT EXISTS application (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        NOT NULL,
    applicant_id    BIGINT,
    purpose         VARCHAR(200)  NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    approved_amount DECIMAL(12,2),
    actual_amount   DECIMAL(12,2),
    status          VARCHAR(20)   NOT NULL,
    reason          VARCHAR(200),
    CONSTRAINT fk_application_project FOREIGN KEY (project_id) REFERENCES project(id)
);
