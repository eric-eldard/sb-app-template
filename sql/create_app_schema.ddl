-- TODO - add details for the database user that will run the app
DROP USER IF EXISTS 'your_app_db_user'@'%';
DROP DATABASE IF EXISTS your_db_name;

CREATE DATABASE your_db_name;

USE your_db_name;

CREATE USER IF NOT EXISTS 'your_app_db_user'@'%' IDENTIFIED BY 'your_db_app_users_password';

GRANT INSERT, UPDATE, DELETE, SELECT ON your_db_name.* TO 'your_app_db_user'@'%';

CREATE TABLE app_user(
    id                        BIGINT            NOT NULL  PRIMARY KEY  AUTO_INCREMENT,
    username                  VARCHAR(50)       NOT NULL,
    password                  VARCHAR(72)       NOT NULL,
    failed_password_attempts  TINYINT UNSIGNED  NOT NULL  DEFAULT 0,
    locked_on                 DATETIME,
    authorized_until          DATE,
    enabled                   BOOLEAN           NOT NULL  DEFAULT true,
    admin                     BOOLEAN           NOT NULL,
    UNIQUE (username)
);

CREATE TABLE login_attempt(
    id              BIGINT                      NOT NULL  PRIMARY KEY  AUTO_INCREMENT,
    user_id         BIGINT                      NOT NULL,
    outcome         ENUM('SUCCESS', 'FAILURE')  NOT NULL,
    failure_reason  ENUM('ACCOUNT_DISABLED', 'ACCOUNT_EXPIRED', 'ACCOUNT_LOCKED', 'BAD_CREDENTIALS'),
    timeStamp       DATETIME                    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- TODO - set your AppAuthority values as ENUM values
CREATE TABLE app_authority(
    user_id    BIGINT                NOT NULL,
    authority  ENUM('AN_AUTHORITY')  NOT NULL,
    PRIMARY KEY (user_id, authority),
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);
