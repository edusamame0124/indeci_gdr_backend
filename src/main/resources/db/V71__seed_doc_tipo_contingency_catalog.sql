WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO DOC_TIPO_DOCUMENTO t
USING (
    SELECT 'FORMATO_PLANIFICACION' AS CODIGO,
           'Formato de Planificación' AS NOMBRE,
           'Tipo documental para contingencia / catalogo.' AS DESCR
    FROM DUAL
    UNION ALL
    SELECT 'ACTA_REUNION',
           'Acta de Reunión',
           'Tipo documental para contingencia / catalogo.'
    FROM DUAL
    UNION ALL
    SELECT 'COMPROMISO_FIRMADO',
           'Compromiso Firmado',
           'Tipo documental para contingencia / catalogo.'
    FROM DUAL
    UNION ALL
    SELECT 'FORMATO_MEJORA',
           'Formato de Mejora',
           'Tipo documental para contingencia / catalogo.'
    FROM DUAL
    UNION ALL
    SELECT 'OTROS',
           'Otros',
           'Tipo documental para contingencia / catalogo.'
    FROM DUAL
) s
ON (t.CODIGO_TIPO_DOCUMENTO = s.CODIGO)
WHEN MATCHED THEN
    UPDATE SET
        t.NOMBRE_TIPO_DOCUMENTO = s.NOMBRE,
        t.DESCRIPCION = s.DESCR,
        t.ESTADO = 'ACTIVO',
        t.FECHA_ACTUALIZACION = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_TIPO_DOCUMENTO,
        CODIGO_TIPO_DOCUMENTO,
        NOMBRE_TIPO_DOCUMENTO,
        DESCRIPCION,
        ESTADO,
        FECHA_CREACION,
        FECHA_ACTUALIZACION
    )
    VALUES (
        SQ_DOC_TIPO_DOCUMENTO.NEXTVAL,
        s.CODIGO,
        s.NOMBRE,
        s.DESCR,
        'ACTIVO',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
