DROP TABLE IF EXISTS payment_discount;
DROP TABLE IF EXISTS payment_detail;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS discount_policy;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS member;

CREATE TABLE member (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    grade       VARCHAR(20)  NOT NULL,
    point       BIGINT       NOT NULL DEFAULT 0,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE TABLE orders (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_name    VARCHAR(200) NOT NULL,
    original_price  BIGINT       NOT NULL,
    member_id       BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL
);

CREATE TABLE discount_policy (
    id                    BIGINT         AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(100)   NOT NULL,
    target_grade          VARCHAR(20),
    target_payment_method VARCHAR(20),
    discount_type         VARCHAR(20)    NOT NULL,
    discount_value        DECIMAL(15, 4) NOT NULL,
    priority              INT            NOT NULL DEFAULT 0,
    active                BOOLEAN        NOT NULL DEFAULT TRUE,
    version               BIGINT         NOT NULL DEFAULT 0,
    created_at            TIMESTAMP      NOT NULL,
    updated_at            TIMESTAMP      NOT NULL
);

CREATE TABLE payment (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    idempotency_key   VARCHAR(100) NOT NULL UNIQUE,
    order_id          BIGINT       NOT NULL,
    original_amount   BIGINT       NOT NULL,
    discount_amount   BIGINT       NOT NULL,
    final_amount      BIGINT       NOT NULL,
    paid_at           TIMESTAMP    NOT NULL
);

CREATE TABLE payment_detail (
    id              BIGINT      AUTO_INCREMENT PRIMARY KEY,
    payment_id      BIGINT      NOT NULL,
    payment_method  VARCHAR(20) NOT NULL,
    gross_amount    BIGINT      NOT NULL,
    charged_amount  BIGINT      NOT NULL
);

CREATE TABLE payment_discount (
    id                BIGINT         AUTO_INCREMENT PRIMARY KEY,
    payment_id        BIGINT         NOT NULL,
    policy_id         BIGINT,
    policy_name       VARCHAR(100)   NOT NULL,
    policy_version    BIGINT,
    discount_type     VARCHAR(20)    NOT NULL,
    discount_value    DECIMAL(15, 4) NOT NULL,
    discount_amount   BIGINT         NOT NULL,
    applied_scope     VARCHAR(20)    NOT NULL,
    applied_method    VARCHAR(20),
    applied_at        TIMESTAMP      NOT NULL
);
