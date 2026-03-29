CREATE TABLE app.user (
    id                       BIGSERIAL       PRIMARY KEY,
    name                     VARCHAR(100)    NOT NULL,
    email                    VARCHAR(255)    NOT NULL,
    password_hash            VARCHAR(255)    NOT NULL,
    multiple_session_allowed BOOLEAN         NOT NULL DEFAULT false,
    add_date                 TIMESTAMP       NOT NULL DEFAULT now(),
    delete_info              JSONB           NULL
);

CREATE UNIQUE INDEX idx_user_email ON app.user(email);
