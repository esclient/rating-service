#!/bin/bash

# Set Infisical configuration from environment or use defaults
export INFISICAL_TOKEN=${INFISICAL_SERVER_TOKEN}

# Fetch DATABASE_URL from Infisical
echo "Fetching DATABASE_URL from Infisical..."
DATABASE_URL=$(infisical secrets get DATABASE_URL --projectId=d1a7e949-296b-49ce-a3fe-806ae77191fa --env=dev --path=/rating-service --plain)

if [ -z "$DATABASE_URL" ]; then
    echo "ERROR: Failed to retrieve DATABASE_URL from Infisical"
    exit 1
fi

echo "Running migrations..."
goose -dir src/main/resources/db/migrations postgres "$DATABASE_URL" up
echo "Migrations complete!"