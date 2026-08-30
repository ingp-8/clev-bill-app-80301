-- Refresh tokens are stored hashed (SHA-256), never in plaintext — mirrors
-- the password-hash convention. The raw token is only ever seen by the
-- client it was issued to.
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
