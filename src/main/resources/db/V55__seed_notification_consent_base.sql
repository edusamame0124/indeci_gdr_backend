WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

INSERT INTO NOTIF_CANAL (
    ID_CANAL,
    CODIGO_CANAL,
    NOMBRE_CANAL,
    DESCRIPCION,
    ESTADO_REGISTRO,
    FECHA_CREACION,
    FECHA_ACTUALIZACION
)
SELECT
    SQ_NOTIF_CANAL.NEXTVAL,
    'BANDEJA_INTERNA',
    'Bandeja interna',
    'Canal interno del sistema para mensajes operativos del ciclo GDR.',
    'ACTIVO',
    SYSTIMESTAMP,
    SYSTIMESTAMP
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM NOTIF_CANAL WHERE CODIGO_CANAL = 'BANDEJA_INTERNA'
);

INSERT INTO NOTIF_PLANTILLA (
    ID_PLANTILLA_NOTIFICACION,
    CODIGO_PLANTILLA,
    NOMBRE_PLANTILLA,
    ASUNTO,
    CUERPO_MENSAJE,
    ID_CANAL,
    VERSION_PLANTILLA,
    ESTADO_REGISTRO,
    FECHA_CREACION,
    FECHA_ACTUALIZACION
)
SELECT
    SQ_NOTIF_PLANTILLA.NEXTVAL,
    plantilla.CODIGO_PLANTILLA,
    plantilla.NOMBRE_PLANTILLA,
    plantilla.ASUNTO,
    plantilla.CUERPO_MENSAJE,
    canal.ID_CANAL,
    1,
    'ACTIVO',
    SYSTIMESTAMP,
    SYSTIMESTAMP
FROM (
    SELECT 'DOCUMENTO_FIRMADO_REGISTRADO' CODIGO_PLANTILLA,
           'Documento firmado registrado' NOMBRE_PLANTILLA,
           'Documento firmado registrado' ASUNTO,
           'Se registro correctamente un documento firmado dentro del flujo GDR. Referencia: {referencia}.' CUERPO_MENSAJE
    FROM DUAL
    UNION ALL
    SELECT 'OPORTUNIDAD_MEJORA_REGISTRADA',
           'Oportunidad de mejora registrada',
           'Oportunidad de mejora registrada',
           'Se registro una oportunidad de mejora vinculada al ciclo GDR. Referencia: {referencia}.'
    FROM DUAL
    UNION ALL
    SELECT 'SEGUIMIENTO_MEJORA_REGISTRADO',
           'Seguimiento de mejora registrado',
           'Seguimiento de mejora registrado',
           'Se registro un seguimiento de oportunidad de mejora. Referencia: {referencia}.'
    FROM DUAL
    UNION ALL
    SELECT 'OPORTUNIDAD_MEJORA_CERRADA',
           'Oportunidad de mejora cerrada',
           'Oportunidad de mejora cerrada',
           'Se cerro una oportunidad de mejora del ciclo GDR. Referencia: {referencia}.'
    FROM DUAL
) plantilla
CROSS JOIN (
    SELECT ID_CANAL FROM NOTIF_CANAL WHERE CODIGO_CANAL = 'BANDEJA_INTERNA'
) canal
WHERE NOT EXISTS (
    SELECT 1
    FROM NOTIF_PLANTILLA existente
    WHERE existente.CODIGO_PLANTILLA = plantilla.CODIGO_PLANTILLA
      AND existente.VERSION_PLANTILLA = 1
);

INSERT INTO CONSENT_TIPO (
    ID_TIPO_CONSENTIMIENTO,
    CODIGO_CONSENTIMIENTO,
    NOMBRE_CONSENTIMIENTO,
    TEXTO_CONSENTIMIENTO,
    VERSION_CONSENTIMIENTO,
    REQUERIDO,
    ESTADO_REGISTRO,
    FECHA_CREACION,
    FECHA_ACTUALIZACION
)
SELECT
    SQ_CONSENT_TIPO.NEXTVAL,
    'USO_INFORMACION_GDR',
    'Uso informativo de datos GDR',
    'Declaro haber leido el texto informativo del sistema GDR Access y acepto el registro de la informacion del ciclo para seguimiento, evaluacion, mejora continua y trazabilidad operativa interna.',
    1,
    'N',
    'ACTIVO',
    SYSTIMESTAMP,
    SYSTIMESTAMP
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM CONSENT_TIPO
    WHERE CODIGO_CONSENTIMIENTO = 'USO_INFORMACION_GDR'
      AND VERSION_CONSENTIMIENTO = 1
);
