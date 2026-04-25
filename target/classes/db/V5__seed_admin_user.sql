WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

-- Password inicial de ejemplo: password
-- Cambiar inmediatamente en ambientes reales.
MERGE INTO SEC_USER target
USING (
    SELECT
        'admin' AS USERNAME,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS PASSWORD_HASH,
        'admin@gdr.local' AS EMAIL,
        'Administrador del Sistema' AS DISPLAY_NAME
    FROM DUAL
) source
ON (target.USERNAME = source.USERNAME)
WHEN MATCHED THEN
    UPDATE SET
        target.PASSWORD_HASH = source.PASSWORD_HASH,
        target.EMAIL = source.EMAIL,
        target.DISPLAY_NAME = source.DISPLAY_NAME,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_USER,
        USERNAME,
        PASSWORD_HASH,
        EMAIL,
        DISPLAY_NAME,
        STATUS,
        FAILED_ATTEMPTS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_SEC_USER.NEXTVAL,
        source.USERNAME,
        source.PASSWORD_HASH,
        source.EMAIL,
        source.DISPLAY_NAME,
        'ACTIVE',
        0,
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

MERGE INTO SEC_USER_ROLE target
USING (
    SELECT
        u.ID_USER AS ID_USER,
        r.ID_ROLE AS ID_ROLE
    FROM SEC_USER u
    JOIN SEC_ROLE r ON r.ROLE_CODE = 'ADMIN'
    WHERE u.USERNAME = 'admin'
) source
ON (target.ID_USER = source.ID_USER AND target.ID_ROLE = source.ID_ROLE)
WHEN MATCHED THEN
    UPDATE SET
        target.STATUS = 'ACTIVE',
        target.ASSIGNED_AT = COALESCE(target.ASSIGNED_AT, SYSTIMESTAMP)
WHEN NOT MATCHED THEN
    INSERT (ID_USER_ROLE, ID_USER, ID_ROLE, STATUS, ASSIGNED_AT)
    VALUES (SQ_SEC_USER_ROLE.NEXTVAL, source.ID_USER, source.ID_ROLE, 'ACTIVE', SYSTIMESTAMP);
