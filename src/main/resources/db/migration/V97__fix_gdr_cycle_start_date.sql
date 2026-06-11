WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

-- Backfill START_DATE para ciclos creados sin esa columna (seed V11).
-- Usa la fecha inicio de la etapa PLANIFICACION del cronograma.
-- Si no hay etapa PLANIFICACION, usa la fecha inicio mínima de cualquier etapa.
UPDATE GDR_CYCLE c
   SET c.START_DATE = (
           SELECT NVL(
               MIN(CASE WHEN cr.ETAPA = 'PLANIFICACION' THEN cr.FECHA_INICIO END),
               MIN(cr.FECHA_INICIO)
           )
             FROM GDR_CRONOGRAMA cr
            WHERE cr.ID_CICLO = c.ID_CYCLE
       ),
       c.UPDATED_AT = SYSTIMESTAMP
 WHERE c.START_DATE IS NULL;

COMMIT;
