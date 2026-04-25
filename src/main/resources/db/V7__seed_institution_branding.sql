WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

DECLARE
    v_active_count NUMBER := 0;
BEGIN
    SELECT COUNT(*)
      INTO v_active_count
      FROM SEC_INSTITUTION_BRANDING
     WHERE STATUS = 'ACTIVE';

    IF v_active_count = 0 THEN
        INSERT INTO SEC_INSTITUTION_BRANDING (
            ID_BRANDING,
            INSTITUTION_NAME,
            MAIN_LOGO,
            MAIN_LOGO_MIME_TYPE,
            HEADER_LOGO,
            HEADER_LOGO_MIME_TYPE,
            STATUS,
            CREATED_AT,
            UPDATED_AT
        )
        VALUES (
            SQ_SEC_INSTITUTION_BRANDING.NEXTVAL,
            'Nombre institucional por configurar',
            NULL,
            NULL,
            NULL,
            NULL,
            'ACTIVE',
            SYSTIMESTAMP,
            SYSTIMESTAMP
        );
    END IF;
END;
/
