DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS member;

CREATE TABLE member (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    grade      VARCHAR(20)  NOT NULL,
    point      BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

CREATE TABLE orders (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_name   VARCHAR(200) NOT NULL,
    original_price BIGINT       NOT NULL,
    member_id      BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL
);

CREATE TABLE payment (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT      NOT NULL,
    final_amount   BIGINT      NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    paid_at        TIMESTAMP   NOT NULL
);
