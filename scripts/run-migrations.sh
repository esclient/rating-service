#!/bin/bash
export INFISICAL_TOKEN=${INFISICAL_SERVER_TOKEN}

DATABASE_URL=$(infisical secrets get DATABASE_URL --projectId=d1a7e949-296b-49ce-a3fe-806ae77191fa --env=dev --path=/rating-service --plain)

# Go all the way down
echo "Rolling back all migrations..."
goose -dir src/main/resources/db/migrations postgres "$DATABASE_URL" reset

# Come back up with corrected migration
echo "Running migrations fresh..."
goose -dir src/main/resources/db/migrations postgres "$DATABASE_URL" up

echo "Done!"