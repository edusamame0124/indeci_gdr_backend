WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO DOC_TIPO_DOCUMENTO target
USING (
    SELECT 'FORMATO_RESULTADO_FIRMADO' AS CODIGO_TIPO_DOCUMENTO,
           'Formato de resultado firmado' AS NOMBRE_TIPO_DOCUMENTO,
           'Documento firmado vinculado al resultado consolidado.' AS DESCRIPCION
    FROM DUAL
    UNION ALL
    SELECT 'ACTA_RETROALIMENTACION',
           'Acta de retroalimentacion',
           'Acta minima de retroalimentacion vinculada al resultado consolidado.'
    FROM DUAL
) source
ON (target.CODIGO_TIPO_DOCUMENTO = source.CODIGO_TIPO_DOCUMENTO)
WHEN MATCHED THEN
    UPDATE SET
        target.NOMBRE_TIPO_DOCUMENTO = source.NOMBRE_TIPO_DOCUMENTO,
        target.DESCRIPCION = source.DESCRIPCION,
        target.ESTADO = 'ACTIVO',
        target.FECHA_ACTUALIZACION = SYSTIMESTAMP
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
        source.CODIGO_TIPO_DOCUMENTO,
        source.NOMBRE_TIPO_DOCUMENTO,
        source.DESCRIPCION,
        'ACTIVO',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

MERGE INTO DOC_PLANTILLA target
USING (
    SELECT
        tipo.ID_TIPO_DOCUMENTO AS ID_TIPO_DOCUMENTO,
        'Plantilla de resultado firmado' AS NOMBRE_PLANTILLA,
        'Plantilla base para el documento firmado del resultado.' AS DESCRIPCION,
        'plantillas/formato_resultado_firmado.pdf' AS CLAVE_ARCHIVO,
        'formato_resultado_firmado.pdf' AS NOMBRE_ORIGINAL,
        'application/pdf' AS MIME_TYPE,
        0 AS TAMANIO_BYTES
    FROM DOC_TIPO_DOCUMENTO tipo
    WHERE tipo.CODIGO_TIPO_DOCUMENTO = 'FORMATO_RESULTADO_FIRMADO'
    UNION ALL
    SELECT
        tipo.ID_TIPO_DOCUMENTO,
        'Plantilla de acta de retroalimentacion',
        'Plantilla base para el acta minima de retroalimentacion.',
        'plantillas/acta_retroalimentacion.pdf',
        'acta_retroalimentacion.pdf',
        'application/pdf',
        0
    FROM DOC_TIPO_DOCUMENTO tipo
    WHERE tipo.CODIGO_TIPO_DOCUMENTO = 'ACTA_RETROALIMENTACION'
) source
ON (target.CLAVE_ARCHIVO = source.CLAVE_ARCHIVO)
WHEN MATCHED THEN
    UPDATE SET
        target.ID_TIPO_DOCUMENTO = source.ID_TIPO_DOCUMENTO,
        target.NOMBRE_PLANTILLA = source.NOMBRE_PLANTILLA,
        target.DESCRIPCION = source.DESCRIPCION,
        target.NOMBRE_ORIGINAL = source.NOMBRE_ORIGINAL,
        target.MIME_TYPE = source.MIME_TYPE,
        target.TAMANIO_BYTES = source.TAMANIO_BYTES,
        target.ESTADO = 'ACTIVO',
        target.FECHA_ACTUALIZACION = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_PLANTILLA,
        ID_TIPO_DOCUMENTO,
        NOMBRE_PLANTILLA,
        DESCRIPCION,
        CLAVE_ARCHIVO,
        NOMBRE_ORIGINAL,
        MIME_TYPE,
        TAMANIO_BYTES,
        ESTADO,
        FECHA_CREACION,
        FECHA_ACTUALIZACION
    )
    VALUES (
        SQ_DOC_PLANTILLA.NEXTVAL,
        source.ID_TIPO_DOCUMENTO,
        source.NOMBRE_PLANTILLA,
        source.DESCRIPCION,
        source.CLAVE_ARCHIVO,
        source.NOMBRE_ORIGINAL,
        source.MIME_TYPE,
        source.TAMANIO_BYTES,
        'ACTIVO',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
