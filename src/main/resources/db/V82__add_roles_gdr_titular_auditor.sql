-- V82: Agrega roles ROLE_GDR_TITULAR y ROLE_GDR_AUDITOR
-- Normativa: RPE 068-2020-SERVIR-PE / buenas prácticas institucionales

-- Titular / Alta Dirección: solo consulta institucional y aprobación de cronograma
-- según flujo interno que defina la entidad (POSIBLE_CAMBIO_RRHH_GDR_005).
INSERT INTO SEC_ROLE (CODE, NAME, DESCRIPTION, STATUS)
VALUES (
    'GDR_TITULAR',
    'ROLE_GDR_TITULAR',
    'Titular / Alta Dirección. Consulta institucional del ciclo GDR, avances y resultados. No opera casos diarios.',
    'ACTIVE'
);

-- Auditor / OCI: solo lectura de trazabilidad, documentos y reportes
INSERT INTO SEC_ROLE (CODE, NAME, DESCRIPTION, STATUS)
VALUES (
    'GDR_AUDITOR',
    'ROLE_GDR_AUDITOR',
    'Auditor / OCI / Consulta especializada. Solo lectura: trazabilidad, documentos y reportes. Sin modificar el proceso.',
    'ACTIVE'
);
