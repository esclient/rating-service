-- Test database schema for H2
CREATE TABLE IF NOT EXISTS rates (
    id BIGSERIAL PRIMARY KEY,
    mod_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    rate INTEGER NOT NULL CHECK (rate >= 1 AND rate <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(mod_id, author_id)
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_ratings_mod_id ON rates(mod_id);
CREATE INDEX IF NOT EXISTS idx_ratings_rate ON rates(rate);