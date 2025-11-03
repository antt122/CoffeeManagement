#!/bin/bash

set -e
set -u

function create_user_and_database() {
    local database=$1

    echo "  ▶ Checking database '$database'..."
    DB_EXIST=$(psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "SELECT 1 FROM pg_database WHERE datname='${database}'")

    if [ "$DB_EXIST" = "1" ]; then
        echo "  ⚙️  Database '$database' already exists, skipping creation."
    else
        echo "  🟢 Creating user and database '$database'"
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
            CREATE DATABASE $database;
            GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
    fi
}

# --- 1️⃣ Tạo các DB trong POSTGRES_MULTIPLE_DATABASES nếu có ---
if [ -n "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_user_and_database "$db"
    done
    echo "✅ Multiple databases created successfully."
else
    echo "⚠ No multiple databases defined. Skipping creation list."
fi

# --- 2️⃣ Đảm bảo coffee_hr_db luôn tồn tại ---
echo "Ensuring 'coffee_hr_db' database exists..."
create_user_and_database "coffee_hr_db"

echo "🎉 Database initialization completed."
