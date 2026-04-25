-- Script operativo/manual para carga posterior de logos BLOB.
-- Requiere SQL*Plus o SQLcl.
--
-- 1. Defina el esquema:
--    DEFINE APP_DB_SCHEMA = GDR_ACCESS
--
-- 2. Cree un DIRECTORY Oracle apuntando a la carpeta donde estarán los archivos:
--    CREATE OR REPLACE DIRECTORY GDR_BRANDING_DIR AS '/ruta/del/servidor/branding';
--    GRANT READ ON DIRECTORY GDR_BRANDING_DIR TO GDR_ACCESS;
--
-- 3. Defina los parámetros:
--    DEFINE ORACLE_DIR_NAME = GDR_BRANDING_DIR
--    DEFINE MAIN_LOGO_FILE = main_logo.png
--    DEFINE MAIN_LOGO_MIME = image/png
--    DEFINE HEADER_LOGO_FILE = header_logo.png
--    DEFINE HEADER_LOGO_MIME = image/png
--
-- 4. Ejecute este script.
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

DECLARE
    v_main_file BFILE := BFILENAME('&&ORACLE_DIR_NAME', '&&MAIN_LOGO_FILE');
    v_header_file BFILE := BFILENAME('&&ORACLE_DIR_NAME', '&&HEADER_LOGO_FILE');
    v_main_blob BLOB;
    v_header_blob BLOB;
BEGIN
    DBMS_LOB.FILEOPEN(v_main_file, DBMS_LOB.FILE_READONLY);
    DBMS_LOB.FILEOPEN(v_header_file, DBMS_LOB.FILE_READONLY);

    UPDATE SEC_INSTITUTION_BRANDING
       SET MAIN_LOGO = EMPTY_BLOB(),
           MAIN_LOGO_MIME_TYPE = '&&MAIN_LOGO_MIME',
           HEADER_LOGO = EMPTY_BLOB(),
           HEADER_LOGO_MIME_TYPE = '&&HEADER_LOGO_MIME',
           UPDATED_AT = SYSTIMESTAMP
     WHERE STATUS = 'ACTIVE'
     RETURNING MAIN_LOGO, HEADER_LOGO INTO v_main_blob, v_header_blob;

    DBMS_LOB.LOADFROMFILE(v_main_blob, v_main_file, DBMS_LOB.GETLENGTH(v_main_file));
    DBMS_LOB.LOADFROMFILE(v_header_blob, v_header_file, DBMS_LOB.GETLENGTH(v_header_file));

    DBMS_LOB.FILECLOSE(v_main_file);
    DBMS_LOB.FILECLOSE(v_header_file);

    COMMIT;
END;
/
