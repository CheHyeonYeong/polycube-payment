INSERT INTO member (name, grade, point, version, created_at, updated_at) VALUES
    ('일반회원', 'NORMAL', 0,      0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VIP회원',  'VIP',    50000,  0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VVIP회원', 'VVIP',   200000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO discount_policy
    (name, target_grade, target_payment_method, discount_type, discount_value, priority, active, version, created_at, updated_at)
VALUES
    ('VIP 1,000원 정액 할인',  'VIP',  NULL,    'FIXED', 1000.0000, 10, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VVIP 10% 정률 할인',     'VVIP', NULL,    'RATE',  0.1000,    10, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('포인트 결제 5% 중복 할인', NULL,  'POINT', 'RATE',  0.0500,    20, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
