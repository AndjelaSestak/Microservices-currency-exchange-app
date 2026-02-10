-- BTC ↔ EUR / USD
INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (3, 'BTC', 'EUR', 100.0);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (4, 'EUR', 'BTC', 0.5);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (5, 'BTC', 'USD', 27000.0);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (6, 'USD', 'BTC', 0.000037);

-- ETH ↔ EUR / USD
INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (7, 'ETH', 'USD', 1800.0);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (8, 'USD', 'ETH', 0.00056);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (9, 'ETH', 'EUR', 1650.0);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (10, 'EUR', 'ETH', 0.00061);

-- LTC ↔ EUR / USD
INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (11, 'LTC', 'EUR', 100.0);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (12, 'EUR', 'LTC', 0.01);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (13, 'LTC', 'USD', 110.0);

INSERT INTO trade_service (id, currency_from, currency_to, conversion) 
VALUES (14, 'USD', 'LTC', 0.0091);