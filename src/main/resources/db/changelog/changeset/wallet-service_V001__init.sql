CREATE TABLE IF NOT EXISTS wallets (
uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
balance NUMERIC(10,2) NOT NULL DEFAULT 0.00
);

ALTER TABLE wallets
ADD CONSTRAINT chck_wallets_balance
CHECK(balance >= 0.00 AND balance <= 1000000000.00);

 INSERT INTO wallets (balance)
    VALUES
        ('99213.23'),
        ('99.32'),
        ('9999999.99'),
        ('1000000.00');