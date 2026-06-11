-- ============================================================================
-- V91 - DOC_VERSION_DOCUMENTO referencia normativa (P6-03)
-- Tablespace: TBS_GDR_DATA (indice si aplica)
-- ============================================================================
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

ALTER TABLE DOC_VERSION_DOCUMENTO ADD (
    REF_NORMATIVA VARCHAR2(120 CHAR)
);

COMMENT ON COLUMN DOC_VERSION_DOCUMENTO.REF_NORMATIVA IS
    'Referencia normativa del formato o acta al momento del registro (ej. RPE 000041-2025/PE).';

COMMIT;
