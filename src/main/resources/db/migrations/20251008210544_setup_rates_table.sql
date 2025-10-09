-- +goose Up
CREATE TABLE IF NOT EXISTS rates (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    mod_id BIGINT NOT NULL,
    rate BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_rates_author_id ON rates(author_id);
CREATE INDEX IF NOT EXISTS idx_rates_mod_id ON rates(mod_id);
CREATE INDEX IF NOT EXISTS idx_rates_mod_author ON rates(mod_id, author_id);

-- +goose Down
DROP INDEX IF EXISTS idx_rates_mod_author;
DROP INDEX IF EXISTS idx_rates_mod_id;
DROP INDEX IF EXISTS idx_rates_author_id;
DROP TABLE IF EXISTS rates;
