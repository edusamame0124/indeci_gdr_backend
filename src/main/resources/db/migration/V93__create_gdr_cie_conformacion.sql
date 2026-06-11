WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

-- =============================================================================
-- V93: CIE Configurable — POSIBLE_CAMBIO_RRHH_GDR_001
-- Permite registrar la conformación del Comité Institucional de Evaluación (CIE)
-- de forma configurable: por ciclo o institucional, con múltiples conformaciones.
-- Normativa: RPE 068-2020-SERVIR-PE Art. 42-48.
-- =============================================================================

CREATE SEQUENCE SQ_GDR_CIE_CONFORMACION
  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE SEQUENCE SQ_GDR_CIE_INTEGRANTE
  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- -----------------------------------------------------------------------------
-- GDR_CIE_CONFORMACION: conformación vigente del CIE
-- ID_CICLO nullable: una conformación puede ser institucional (multi-ciclo)
--   o específica de un ciclo.
-- -----------------------------------------------------------------------------
CREATE TABLE GDR_CIE_CONFORMACION (
  ID_CONFORMACION    NUMBER(19)    NOT NULL,
  ID_CICLO           NUMBER(19),
  RESOLUCION_NUMERO  VARCHAR2(100),
  RESOLUCION_FECHA   DATE,
  VIGENCIA_INICIO    DATE          NOT NULL,
  VIGENCIA_FIN       DATE,
  OBSERVACIONES      VARCHAR2(2000),
  ESTADO             VARCHAR2(30)  DEFAULT 'VIGENTE' NOT NULL,
  REGISTRADO_POR     VARCHAR2(120) NOT NULL,
  CREATED_AT         TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL,
  UPDATED_AT         TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT PK_GDR_CIE_CONFORMACION PRIMARY KEY (ID_CONFORMACION),
  CONSTRAINT FK_GDR_CIECONF_CICLO    FOREIGN KEY (ID_CICLO) REFERENCES GDR_CYCLE(ID_CYCLE),
  CONSTRAINT CK_GDR_CIECONF_EST      CHECK (ESTADO IN ('VIGENTE','VENCIDO','ANULADO'))
) TABLESPACE TBS_GDR_DATA;

-- -----------------------------------------------------------------------------
-- GDR_CIE_INTEGRANTE: miembros del CIE por conformación
-- Roles:
--   TITULAR_ORH      : Jefe de ORH o su representante (primer integrante)
--   REP_EVALUADOS_SEG: Representante de evaluados por segmento (segundo integrante)
--   DESIGNADO_CASO   : Designado según tipo de caso CIE (tercer integrante)
--   ACCESITARIO      : Integrante alterno/suplente
-- ID_PERSONA nullable: permite registrar integrantes externos al sistema.
-- -----------------------------------------------------------------------------
CREATE TABLE GDR_CIE_INTEGRANTE (
  ID_INTEGRANTE      NUMBER(19)    NOT NULL,
  ID_CONFORMACION    NUMBER(19)    NOT NULL,
  ROL_CIE            VARCHAR2(50)  NOT NULL,
  SEGMENTO           VARCHAR2(100),
  ID_PERSONA         NUMBER(19),
  NOMBRE_EXTERNO     VARCHAR2(150),
  CARGO_DESCRIPCION  VARCHAR2(200),
  FECHA_INICIO       DATE          NOT NULL,
  FECHA_FIN          DATE,
  ESTADO             VARCHAR2(30)  DEFAULT 'ACTIVO' NOT NULL,
  CREATED_AT         TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT PK_GDR_CIE_INTEGRANTE  PRIMARY KEY (ID_INTEGRANTE),
  CONSTRAINT FK_GDR_CIEINT_CONFORM  FOREIGN KEY (ID_CONFORMACION) REFERENCES GDR_CIE_CONFORMACION(ID_CONFORMACION),
  CONSTRAINT FK_GDR_CIEINT_PERSONA  FOREIGN KEY (ID_PERSONA)      REFERENCES HR_PERSON(ID_PERSON),
  CONSTRAINT CK_GDR_CIEINT_ROL      CHECK (ROL_CIE IN ('TITULAR_ORH','REP_EVALUADOS_SEG','DESIGNADO_CASO','ACCESITARIO')),
  CONSTRAINT CK_GDR_CIEINT_EST      CHECK (ESTADO IN ('ACTIVO','INACTIVO'))
) TABLESPACE TBS_GDR_DATA;

CREATE INDEX IX_GDR_CIEINT_CONFORM ON GDR_CIE_INTEGRANTE(ID_CONFORMACION) TABLESPACE TBS_GDR_DATA;
CREATE INDEX IX_GDR_CIEINT_PERSONA ON GDR_CIE_INTEGRANTE(ID_PERSONA)      TABLESPACE TBS_GDR_DATA;

COMMENT ON TABLE  GDR_CIE_CONFORMACION IS 'Conformación configurable del CIE. ID_CICLO null=institucional. POSIBLE_CAMBIO_RRHH_GDR_001.';
COMMENT ON TABLE  GDR_CIE_INTEGRANTE   IS 'Integrantes del CIE: TITULAR_ORH, REP_EVALUADOS_SEG, DESIGNADO_CASO, ACCESITARIO.';
COMMENT ON COLUMN GDR_CIE_INTEGRANTE.ID_PERSONA     IS 'Nulo si el integrante es externo al sistema (usar NOMBRE_EXTERNO).';
COMMENT ON COLUMN GDR_CIE_INTEGRANTE.NOMBRE_EXTERNO IS 'Nombre del integrante externo cuando no tiene cuenta en el sistema.';
COMMENT ON COLUMN GDR_CIE_INTEGRANTE.SEGMENTO       IS 'Segmento representado (solo para ROL REP_EVALUADOS_SEG).';
