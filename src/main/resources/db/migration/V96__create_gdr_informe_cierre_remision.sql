WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

-- =============================================================================
-- V96: Registro de Remisión a SERVIR — POSIBLE_CAMBIO_RRHH_GDR_008
-- Registra la evidencia de remisión del Informe de Cierre GDR a SERVIR.
--
-- P0 (dev): remisión manual controlada. El sistema genera el informe y
--   permite registrar la evidencia de envío a SERVIR (fecha, canal, N° trámite,
--   nombre del documento de cargo/constancia).
--
-- P1 (comentado): flujo interno de aprobación previo a remisión.
--   Requiere roles adicionales (jefe ORH, jefatura de planificación, etc.).
--
-- P2 (comentado): integración automática si SERVIR publica API/canal de recepción.
--   Activar solo cuando SERVIR formalice el mecanismo de envío electrónico.
--
-- Normativa: RPE 068-2020-SERVIR-PE Art. 55.
-- =============================================================================

CREATE SEQUENCE SQ_GDR_INFORME_REMISION
  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE GDR_INFORME_CIERRE_REMISION (
  ID_REMISION          NUMBER(19)    NOT NULL,
  ID_INFORME_CIERRE    NUMBER(19)    NOT NULL,
  FECHA_REMISION       DATE          NOT NULL,
  CANAL_REMISION       VARCHAR2(80)  NOT NULL,
  NUMERO_TRAMITE       VARCHAR2(80),
  OBSERVACIONES        VARCHAR2(2000),
  NOMBRE_DOC_EVIDENCIA VARCHAR2(300),
  TIPO_DOC_EVIDENCIA   VARCHAR2(80),
  ESTADO_REMISION      VARCHAR2(30)  DEFAULT 'REGISTRADO' NOT NULL,
  REGISTRADO_POR       VARCHAR2(120) NOT NULL,
  CREATED_AT           TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL,
  -- P1: ID_APROBADOR VARCHAR2(120) — usuario que aprueba el envío
  -- P1: FECHA_APROBACION DATE
  -- P1: ESTADO_APROBACION VARCHAR2(30)
  -- P2: ID_RESPUESTA_SERVIR VARCHAR2(120) — token/confirmación de SERVIR API
  -- P2: FECHA_CONFIRMACION_SERVIR DATE
  CONSTRAINT PK_GDR_INFORME_REMISION  PRIMARY KEY (ID_REMISION),
  CONSTRAINT FK_GDR_INFREM_INFORME    FOREIGN KEY (ID_INFORME_CIERRE) REFERENCES GDR_INFORME_CIERRE(ID_INFORME_CIERRE),
  CONSTRAINT CK_GDR_INFREM_CANAL      CHECK (CANAL_REMISION IN ('MESA_PARTES','CORREO_ELECTRONICO','FISICO','PLATAFORMA_SERVIR','OTRO')),
  CONSTRAINT CK_GDR_INFREM_TIPO_DOC   CHECK (TIPO_DOC_EVIDENCIA IN ('CARGO_RECEPCION','CONSTANCIA_ENVIO','CORREO_RESPUESTA','OTRO') OR TIPO_DOC_EVIDENCIA IS NULL),
  CONSTRAINT CK_GDR_INFREM_EST        CHECK (ESTADO_REMISION IN ('REGISTRADO','CONFIRMADO','OBSERVADO'))
) TABLESPACE TBS_GDR_DATA;

CREATE INDEX IX_GDR_INFREM_INFORME ON GDR_INFORME_CIERRE_REMISION(ID_INFORME_CIERRE) TABLESPACE TBS_GDR_DATA;

COMMENT ON TABLE  GDR_INFORME_CIERRE_REMISION               IS 'Evidencia de remisión del informe de cierre a SERVIR. P0=manual; P1=aprobación interna; P2=API SERVIR. POSIBLE_CAMBIO_RRHH_GDR_008.';
COMMENT ON COLUMN GDR_INFORME_CIERRE_REMISION.CANAL_REMISION IS 'Canal de envío: MESA_PARTES, CORREO_ELECTRONICO, FISICO, PLATAFORMA_SERVIR, OTRO.';
COMMENT ON COLUMN GDR_INFORME_CIERRE_REMISION.NUMERO_TRAMITE IS 'Número de trámite, expediente o referencia del envío.';
COMMENT ON COLUMN GDR_INFORME_CIERRE_REMISION.NOMBRE_DOC_EVIDENCIA IS 'Nombre del archivo adjunto que sirve como evidencia del envío.';
COMMENT ON COLUMN GDR_INFORME_CIERRE_REMISION.TIPO_DOC_EVIDENCIA IS 'Tipo de documento de evidencia: cargo, constancia, correo de respuesta.';
