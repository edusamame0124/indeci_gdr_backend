-- V81: Agrega rol ROLE_GDR_CIE — Comité Institucional de Evaluación
-- Normativa: RPE 068-2020-SERVIR-PE Art. 43-49
-- El CIE solo interviene cuando el evaluado solicita confirmación de calificación.
-- No puede editar metas ni evaluar ordinariamente.

INSERT INTO SEC_ROLE (CODE, NAME, DESCRIPTION, STATUS)
VALUES (
    'GDR_CIE',
    'ROLE_GDR_CIE',
    'Comité Institucional de Evaluación. Solo interviene en solicitudes de confirmación de calificación (RPE 068-2020 Art. 43-49).',
    'ACTIVE'
);
