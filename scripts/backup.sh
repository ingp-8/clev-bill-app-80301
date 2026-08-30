#!/usr/bin/env bash
#
# Scheduled/manual DB backup — see docs/ARCHITECTURE.md section 8. This is
# the property's only protection against total data loss, so failures must
# be loud, not silent.
#
# Usage: DB_PASSWORD=... ./backup.sh
# Intended to run via cron on the property's server, e.g.:
#   0 2 * * * DB_PASSWORD=... /opt/clevbill/scripts/backup.sh >> /var/log/clevbill-backup.log 2>&1
#
# Config (env vars, all optional except DB_PASSWORD):
#   DB_HOST, DB_PORT, DB_NAME, DB_USER  — defaults match application.yml
#   DB_PASSWORD                         — required, no default (never commit one)
#   BACKUP_DIR                          — local staging dir before upload (default ./backups)
#   RETENTION_DAYS                      — local dumps older than this are deleted (default 7)
#   RCLONE_REMOTE                       — e.g. "clevbill-backups:clevbill-db" — an rclone remote:path
#                                          to sync dumps to cloud object storage. If unset, the dump
#                                          stays local only — set this up before relying on backups.
#   MAIL_TO                             — address for pass/fail notification via `mail`. If unset,
#                                          the script just logs to stdout/stderr (relies on cron's
#                                          own mailing or log monitoring instead).

set -euo pipefail

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-clevbill_db}"
DB_USER="${DB_USER:-clevbill}"
BACKUP_DIR="${BACKUP_DIR:-$(dirname "$0")/../backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "ERROR: DB_PASSWORD is not set" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
DUMP_FILE="$BACKUP_DIR/${DB_NAME}-${TIMESTAMP}.dump"

notify() {
  local subject="$1"
  local body="$2"
  echo "$body"
  if [ -n "${MAIL_TO:-}" ] && command -v mail >/dev/null 2>&1; then
    echo "$body" | mail -s "$subject" "$MAIL_TO"
  fi
}

if PGPASSWORD="$DB_PASSWORD" pg_dump \
  --host="$DB_HOST" --port="$DB_PORT" --username="$DB_USER" \
  --format=custom --file="$DUMP_FILE" "$DB_NAME"; then
  SIZE="$(du -h "$DUMP_FILE" | cut -f1)"
  notify "[clevbill] Backup OK" "Backup succeeded: $DUMP_FILE ($SIZE)"
else
  notify "[clevbill] Backup FAILED" "pg_dump failed for $DB_NAME at $TIMESTAMP — check the server immediately, this is the only copy of the data."
  exit 1
fi

if [ -n "${RCLONE_REMOTE:-}" ] && command -v rclone >/dev/null 2>&1; then
  if rclone copy "$DUMP_FILE" "$RCLONE_REMOTE"; then
    notify "[clevbill] Backup uploaded" "Uploaded $DUMP_FILE to $RCLONE_REMOTE"
  else
    notify "[clevbill] Backup upload FAILED" "Dump succeeded locally but upload to $RCLONE_REMOTE failed — dump is still at $DUMP_FILE, investigate the remote."
    exit 1
  fi
else
  echo "RCLONE_REMOTE not set — dump left local only at $DUMP_FILE. Configure cloud upload before relying on this backup."
fi

find "$BACKUP_DIR" -name "${DB_NAME}-*.dump" -mtime "+${RETENTION_DAYS}" -delete
