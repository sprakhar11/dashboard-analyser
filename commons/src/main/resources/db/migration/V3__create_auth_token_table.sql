CREATE TABLE app.auth_token (
    id           BIGSERIAL       PRIMARY KEY,
    user_id      BIGINT          NOT NULL REFERENCES app.user(id),
    token        VARCHAR(255)    NOT NULL,
    browser_id   VARCHAR(255)    NULL,
    add_date     TIMESTAMP       NOT NULL DEFAULT now(),
    log_out_date TIMESTAMP       NULL,
    expiry_date  TIMESTAMP       NOT NULL,
    delete_info  JSONB           NULL
);

CREATE UNIQUE INDEX idx_auth_token_token ON app.auth_token(token);
CREATE INDEX idx_auth_token_user_id ON app.auth_token(user_id);
CREATE INDEX idx_auth_token_browser_id ON app.auth_token(browser_id);
CREATE INDEX idx_auth_token_user_browser ON app.auth_token(user_id, browser_id);
CREATE INDEX idx_auth_token_user_expiry_logout ON app.auth_token(user_id, expiry_date, log_out_date);
