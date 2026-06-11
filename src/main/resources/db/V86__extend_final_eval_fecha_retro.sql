-- ============================================================================
-- V86 - P3: Retroalimentacion final formal (RPE 068-2020 Art. 33-42)
-- Agrega a GDR_EVALUACION_FINAL:
--   FECHA_REUNION_RETRO_FINAL    : fecha de la reunion de retroalimentacion
--                                  final con el evaluado. Activa el plazo de
--                                  solicitud de confirmacion (Art. 41).
--   PLAZO_SOLICITUD_CONFIRMACION : fecha limite calculada (+5 dias habiles,
--                                  excluye sabados, domingos y feriados de
--                                  GDR_PUBLIC_HOLIDAY).
-- Nota: columnas nuevas sobre tabla existente; no requieren TABLESPACE.
-- ============================================================================

ALTER TABLE GDR_EVALUACION_FINAL ADD (
  FECHA_REUNION_RETRO_FINAL    DATE,
  PLAZO_SOLICITUD_CONFIRMACION DATE
);

COMMENT ON COLUMN GDR_EVALUACION_FINAL.FECHA_REUNION_RETRO_FINAL IS
  'Fecha de reunion de retroalimentacion final con el evaluado (RPE 068-2020 Art. 33-39).';

COMMENT ON COLUMN GDR_EVALUACION_FINAL.PLAZO_SOLICITUD_CONFIRMACION IS
  'Fecha limite para que el evaluado solicite confirmacion de calificacion: +5 dias habiles desde la reunion (RPE 068-2020 Art. 41).';
