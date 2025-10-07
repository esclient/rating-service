-- +goose Up
-- +goose StatementBegin
CREATE TABLE IF NOT EXISTS public.rates (
    id BIGINT NOT NULL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    mod_id BIGINT NOT NULL,
    rate BIGINT NOT NULL
);
-- +goose StatementEnd

-- +goose StatementBegin
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'rates' 
        AND indexname = 'idx_rates_author_id'
    ) THEN
        CREATE INDEX idx_rates_author_id ON public.rates(author_id);
    END IF;
END $$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'rates' 
        AND indexname = 'idx_rates_mod_id'
    ) THEN
        CREATE INDEX idx_rates_mod_id ON public.rates(mod_id);
    END IF;
END $$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'rates' 
        AND indexname = 'idx_rates_mod_author'
    ) THEN
        CREATE INDEX idx_rates_mod_author ON public.rates(mod_id, author_id);
    END IF;
END $$;
-- +goose StatementEnd

-- +goose Down
DROP INDEX IF EXISTS public.idx_rates_mod_author;
DROP INDEX IF EXISTS public.idx_rates_mod_id;
DROP INDEX IF EXISTS public.idx_rates_author_id;