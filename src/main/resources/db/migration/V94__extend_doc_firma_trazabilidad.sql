WHENEVER SQLERROR EXIT FAILURE ROLLBACK;

-- =============================================================================
-- V94: Trazabilidad de Firma Digital — POSIBLE_CAMBIO_RRHH_GDR_003
-- Extiende DOC_DOCUMENTO_FIRMADO con campos de trazabilidad para firma.
--
-- P0 (MVP/dev): firma interna electrónica — stub con trazabilidad completa.
--   El sistema registra usuario, fecha, IP, sesión y hash del documento.
--   El documento se muestra como "Conformidad interna registrada /
--   Pendiente de firma digital real".
--
-- P1 (producción — comentado hasta decisión RRHH):
--   Integración con firma digital certificada (FirmaPeru / PKI / SUNAT).
--   Requiere: constancia de firma, DNI firmante, certificado, validador externo.
--
-- P2: flujo completo evaluador + evaluado ambos firman digitalmente.
-- =============================================================================

ALTER TABLE DOC_DOCUMENTO_FIRMADO ADD (
  TIPO_FIRMA      VARCHAR2(30) DEFAULT 'PENDIENTE' NOT NULL,
  ESTADO_FIRMA    VARCHAR2(30) DEFAULT 'PENDIENTE' NOT NULL,
  HASH_DOCUMENTO  VARCHAR2(64),
  IP_FIRMANTE     VARCHAR2(45),
  ID_SESION_FIRMA VARCHAR2(128)
  -- P1: CONSTANCIA_FIRMA_DIGITAL VARCHAR2(100)  -- número de constancia de PKI/FirmaPeru
  -- P1: DNI_FIRMANTE              VARCHAR2(8)    -- DNI del titular del certificado digital
  -- P1: ENTIDAD_CERTIFICADORA     VARCHAR2(100)  -- nombre de la CA (ej: SUNAT, DigiSign)
  -- P2: HASH_FIRMA_DIGITAL        VARCHAR2(512)  -- hash de la firma incrustada en el PDF
);

ALTER TABLE DOC_DOCUMENTO_FIRMADO ADD (
  CONSTRAINT CK_GDR_DOCFIRM_TIPO CHECK (
    TIPO_FIRMA IN ('PENDIENTE','INTERNA_ELECTRONICA','DIGITAL_CERTIFICADA')
  ),
  CONSTRAINT CK_GDR_DOCFIRM_EST CHECK (
    ESTADO_FIRMA IN ('PENDIENTE','FIRMADO_INTERNAMENTE','FIRMADO_DIGITALMENTE','RECHAZADO')
  )
);

COMMENT ON COLUMN DOC_DOCUMENTO_FIRMADO.TIPO_FIRMA      IS 'PENDIENTE=sin firma; INTERNA_ELECTRONICA=stub dev P0; DIGITAL_CERTIFICADA=P1 integración real.';
COMMENT ON COLUMN DOC_DOCUMENTO_FIRMADO.ESTADO_FIRMA    IS 'Estado actual de la firma del documento.';
COMMENT ON COLUMN DOC_DOCUMENTO_FIRMADO.HASH_DOCUMENTO  IS 'SHA-256 del contenido del archivo en el instante de firma. Trazabilidad de integridad.';
COMMENT ON COLUMN DOC_DOCUMENTO_FIRMADO.IP_FIRMANTE     IS 'IP (IPv4/IPv6) del firmante al momento del registro de conformidad.';
COMMENT ON COLUMN DOC_DOCUMENTO_FIRMADO.ID_SESION_FIRMA IS 'Identificador de sesión HTTP al momento de firmar. Trazabilidad de acceso.';
