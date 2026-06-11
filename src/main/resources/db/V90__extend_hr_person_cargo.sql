-- ============================================================================
-- V90 - HR_PERSON cargo y nivel remunerativo (P6 Formato GDR 2025)
-- Normativa: RPE N. 000041-2025/PE — campos PUESTO y NIVEL REMUNERATIVO
-- Tablespace: TBS_GDR_DATA
-- ============================================================================
WHENEVER SQLERROR EXIT FAILURE ROLLBACK;
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

ALTER TABLE HR_PERSON ADD (
    CARGO               VARCHAR2(200 CHAR),
    NIVEL_REMUNERATIVO  VARCHAR2(80 CHAR)
);

COMMENT ON COLUMN HR_PERSON.CARGO IS
    'Denominacion del puesto del servidor para Formato GDR 2025 (RPE 000041-2025/PE).';
COMMENT ON COLUMN HR_PERSON.NIVEL_REMUNERATIVO IS
    'Nivel remunerativo AIRHSP/MEF para Formato GDR 2025.';

COMMIT;
