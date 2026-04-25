WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO DOC_ESTADO_FLUJO_DOCUMENTAL target
USING (
    SELECT 'PLANTILLA_DISPONIBLE' AS CODIGO_ESTADO_FLUJO,
           'Plantilla disponible' AS NOMBRE_ESTADO_FLUJO,
           'Estado base para la plantilla institucional disponible.' AS DESCRIPCION,
           10 AS ORDEN_FLUJO
    FROM DUAL
    UNION ALL
    SELECT 'LISTO_PARA_FIRMA',
           'Listo para firma',
           'Documento preparado por el sistema y disponible para el inicio de firma.',
           20
    FROM DUAL
    UNION ALL
    SELECT 'FIRMA_SOLICITADA',
           'Firma solicitada',
           'Solicitud de firma creada en el flujo documental del lote actual.',
           30
    FROM DUAL
    UNION ALL
    SELECT 'EN_FIRMA',
           'En firma',
           'Flujo en curso con Firma Peru como proveedor principal.',
           40
    FROM DUAL
    UNION ALL
    SELECT 'FIRMADO',
           'Firmado',
           'Retorno satisfactorio de firma recibido para el documento preparado.',
           50
    FROM DUAL
    UNION ALL
    SELECT 'REGISTRADO',
           'Registrado',
           'Documento firmado persistido en el almacenamiento desacoplado.',
           60
    FROM DUAL
    UNION ALL
    SELECT 'VIGENTE',
           'Vigente',
           'Documento firmado activo y disponible para consulta posterior.',
           70
    FROM DUAL
    UNION ALL
    SELECT 'ERROR_FIRMA',
           'Error de firma',
           'La solicitud de firma retorno con error controlado.',
           80
    FROM DUAL
    UNION ALL
    SELECT 'FIRMA_CANCELADA',
           'Firma cancelada',
           'La solicitud de firma fue cancelada antes de registrar el documento firmado.',
           90
    FROM DUAL
) source
ON (target.CODIGO_ESTADO_FLUJO = source.CODIGO_ESTADO_FLUJO)
WHEN MATCHED THEN
    UPDATE SET
        target.NOMBRE_ESTADO_FLUJO = source.NOMBRE_ESTADO_FLUJO,
        target.DESCRIPCION = source.DESCRIPCION,
        target.ORDEN_FLUJO = source.ORDEN_FLUJO,
        target.ESTADO_REGISTRO = 'ACTIVO',
        target.FECHA_ACTUALIZACION = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_ESTADO_FLUJO_DOCUMENTAL,
        CODIGO_ESTADO_FLUJO,
        NOMBRE_ESTADO_FLUJO,
        DESCRIPCION,
        ORDEN_FLUJO,
        ESTADO_REGISTRO,
        FECHA_CREACION,
        FECHA_ACTUALIZACION
    )
    VALUES (
        SQ_DOC_ESTADO_FLUJO_DOCUMENTAL.NEXTVAL,
        source.CODIGO_ESTADO_FLUJO,
        source.NOMBRE_ESTADO_FLUJO,
        source.DESCRIPCION,
        source.ORDEN_FLUJO,
        'ACTIVO',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
