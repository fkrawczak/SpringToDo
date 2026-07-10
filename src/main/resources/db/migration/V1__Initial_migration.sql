CREATE TABLE refresh_tokens
(
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    token_hash VARCHAR(64)                 NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

CREATE TABLE task_items
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    updated_by  UUID                        NOT NULL,
    title       TEXT                        NOT NULL,
    description TEXT,
    deadline    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status      VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_task_items PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_tokenhash UNIQUE (token_hash);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE task_items
    ADD CONSTRAINT FK_TASK_ITEMS_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE task_items
    ADD CONSTRAINT FK_TASK_ITEMS_ON_UPDATED_BY FOREIGN KEY (updated_by) REFERENCES users (id);