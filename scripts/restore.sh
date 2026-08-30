#!/usr/bin/env bash
#
# Restores a backup produced by backup.sh into a target database. Per
# docs/ARCHITECTURE.md section 8, this procedure must be tested at least
# once before go-live — don't assume backup.sh working means restore does.
#
# Usage: DB_PASSWORD=... ./restore.sh <dump-file> [target-db-name]
#
# The target database is created if it doesn't exist. Restoring into the
# live clevbill_db while the app is running will corrupt state — stop the
# backend first, or restore into a differently-named database to verify
# the dump without touching production.

set -euo pipefail

DUMP_FILE="${1:?Usage: DB_PASSWORD=... ./restore.sh <dump-file> [target-db-name]}"
TARGET_DB="${2:-clevbill_db}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-clevbill}"

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "ERROR: DB_PASSWORD is not set" >&2
  exit 1
fi

if [ ! -f "$DUMP_FILE" ]; then
  echo "ERROR: dump file not found: $DUMP_FILE" >&2
  exit 1
fi

EXISTS="$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc \
  "SELECT 1 FROM pg_database WHERE datname = '$TARGET_DB'")"

if [ "$EXISTS" != "1" ]; then
  echo "Creating database $TARGET_DB"
  PGPASSWORD="$DB_PASSWORD" createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$TARGET_DB"
fi

echo "Restoring $DUMP_FILE into $TARGET_DB"
PGPASSWORD="$DB_PASSWORD" pg_restore \
  --host="$DB_HOST" --port="$DB_PORT" --username="$DB_USER" \
  --dbname="$TARGET_DB" --clean --if-exists --no-owner "$DUMP_FILE"

echo "Restore complete."
