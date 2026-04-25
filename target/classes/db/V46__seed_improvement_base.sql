WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO GDR_ESTADO_OPORTUNIDAD_MEJORA target
USING (
    SELECT 'OPEN' AS CODIGO_ESTADO,
           'Abierta' AS NOMBRE_ESTADO,
           'Oportunidad de mejora abierta y en seguimiento.' AS DESCRIPCION
    FROM DUAL
    UNION ALL
    SELECT 'CLOSED',
           'Cerrada',
           'Oportunidad de mejora cerrada en el lote actual.'
    FROM DUAL
) source
ON (target.CODIGO_ESTADO = source.CODIGO_ESTADO)
WHEN MATCHED THEN
    UPDATE SET
        target.NOMBRE_ESTADO = source.NOMBRE_ESTADO,
        target.DESCRIPCION = source.DESCRIPCION,
        target.ESTADO_REGISTRO = 'ACTIVO',
        target.FECHA_ACTUALIZACION = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_ESTADO_OPORTUNIDAD_MEJORA,
        CODIGO_ESTADO,
        NOMBRE_ESTADO,
        DESCRIPCION,
        ESTADO_REGISTRO,
        FECHA_CREACION,
        FECHA_ACTUALIZACION
    )
    VALUES (
        SQ_GDR_ESTADO_OPORTUNIDAD_MEJORA.NEXTVAL,
        source.CODIGO_ESTADO,
        source.NOMBRE_ESTADO,
        source.DESCRIPCION,
        'ACTIVO',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
