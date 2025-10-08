-- +goose Up
-- +goose StatementBegin
CREATE TABLE IF NOT EXISTS public.rates (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    mod_id BIGINT NOT NULL,
    rate BIGINT NOT NULL
);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX IF NOT EXISTS idx_rates_author_id ON public.rates(author_id);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX IF NOT EXISTS idx_rates_mod_id ON public.rates(mod_id);
-- +goose StatementEnd

-- +goose StatementBegin
CREATE INDEX IF NOT EXISTS idx_rates_mod_author ON public.rates(mod_id, author_id);
-- +goose StatementEnd

-- +goose Down
DROP INDEX IF EXISTS public.idx_rates_mod_author;
DROP INDEX IF EXISTS public.idx_rates_mod_id;
DROP INDEX IF EXISTS public.idx_rates_author_id;
DROP TABLE IF EXISTS public.rates;