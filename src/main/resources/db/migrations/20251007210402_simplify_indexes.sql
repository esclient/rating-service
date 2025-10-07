-- +goose Up
-- Drop the old indexes (they already exist from previous migration)
DROP INDEX IF EXISTS public.idx_rates_author_id;
DROP INDEX IF EXISTS public.idx_rates_mod_id;
DROP INDEX IF EXISTS public.idx_rates_mod_author;

-- Recreate them simply (they'll be the same, just cleaner migration history)
CREATE INDEX idx_rates_author_id ON public.rates(author_id);
CREATE INDEX idx_rates_mod_id ON public.rates(mod_id);
CREATE INDEX idx_rates_mod_author ON public.rates(mod_id, author_id);

-- +goose Down
DROP INDEX public.idx_rates_mod_author;
DROP INDEX public.idx_rates_mod_id;
DROP INDEX public.idx_rates_author_id;