WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

-- =============================================================================
-- V95: Snapshot de Cargo/Puesto por Asignación — POSIBLE_CAMBIO_RRHH_GDR_004
-- Captura el cargo, unidad orgánica y datos laborales del evaluado en el
-- momento de la asignación al ciclo GDR. Permite trazabilidad histórica del
-- puesto al inicio del período de evaluación.
--
-- Fuente oficial: SISRH / legajo / puesto laboral / estructura orgánica.
-- Para dev: se toma de HR_PERSON.CARGO + HR_PERSON.NIVEL_REMUNERATIVO.
--
-- El ajuste manual (DATO_AJUSTADO_MANUALMENTE = 'S') solo se permite como
-- excepción auditada: debe registrar motivo, valor anterior y responsable.
--
-- P1 (comentado — integración SISRH):
--   Consulta en tiempo real al legajo del empleado en SISRH para obtener
--   puesto laboral, unidad orgánica y régimen laboral vigentes.
--   FUENTE_DATO_SNAP = 'SISRH_API' cuando se integre.
-- =============================================================================

ALTER TABLE GDR_EVALUATION_ASSIGNMENT ADD (
  CARGO_PUESTO_SNAP         VARCHAR2(200),
  NIVEL_REMUNERATIVO_SNAP   VARCHAR2(80),
  UNIDAD_ORGANICA_SNAP      VARCHAR2(200),
  EVALUADOR_SNAP            VARCHAR2(150),
  FECHA_CORTE_SNAP          DATE,
  FUENTE_DATO_SNAP          VARCHAR2(50) DEFAULT 'GDR_HRPERSON',
  DATO_AJUSTADO_MANUALMENTE CHAR(1)      DEFAULT 'N' NOT NULL,
  MOTIVO_AJUSTE_MANUAL      VARCHAR2(500),
  CARGO_PUESTO_ANTERIOR     VARCHAR2(200),
  AJUSTADO_POR              VARCHAR2(120)
);

ALTER TABLE GDR_EVALUATION_ASSIGNMENT ADD (
  CONSTRAINT CK_GDR_ASSIGN_AJUSTADO CHECK (DATO_AJUSTADO_MANUALMENTE IN ('S','N')),
  CONSTRAINT CK_GDR_ASSIGN_FUENTE   CHECK (
    FUENTE_DATO_SNAP IN ('GDR_HRPERSON','AJUSTE_MANUAL','SISRH_API')
    -- P1: SISRH_API se activa cuando se integre con el servicio de legajos
  )
);

COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.CARGO_PUESTO_SNAP         IS 'Denominación del puesto del evaluado al inicio del ciclo (desde HR_PERSON.CARGO).';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.NIVEL_REMUNERATIVO_SNAP   IS 'Nivel remunerativo del evaluado al inicio del ciclo.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.UNIDAD_ORGANICA_SNAP      IS 'Unidad orgánica del evaluado al inicio del ciclo.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.EVALUADOR_SNAP            IS 'Nombre del evaluador asignado al inicio del ciclo.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.FECHA_CORTE_SNAP          IS 'Fecha en que se tomó el snapshot (tipicamente fecha de creación de la asignación).';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.FUENTE_DATO_SNAP          IS 'GDR_HRPERSON=desde HR_PERSON; AJUSTE_MANUAL=excepción auditada; SISRH_API=P1 integración.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.DATO_AJUSTADO_MANUALMENTE IS 'S=cargo ajustado manualmente (excepción auditada). Requiere MOTIVO_AJUSTE_MANUAL.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.MOTIVO_AJUSTE_MANUAL      IS 'Motivo del ajuste manual. Obligatorio cuando DATO_AJUSTADO_MANUALMENTE=S.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.CARGO_PUESTO_ANTERIOR     IS 'Valor previo al ajuste manual. Trazabilidad de cambio.';
COMMENT ON COLUMN GDR_EVALUATION_ASSIGNMENT.AJUSTADO_POR              IS 'Usuario que realizó el ajuste manual.';
