WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO GDR_ESTADO_EVIDENCIA target
USING (
    SELECT 'REGISTERED' AS STATUS_CODE, 'Registrada' AS STATUS_NAME, 'Evidencia registrada' AS DESCRIPTION FROM DUAL
    UNION ALL
    SELECT 'OBSERVED', 'Observada', 'Evidencia observada con accion correctiva' FROM DUAL
    UNION ALL
    SELECT 'SUBSANATED', 'Subsanada', 'Evidencia actualizada tras observacion' FROM DUAL
    UNION ALL
    SELECT 'APPROVED', 'Aprobada', 'Evidencia revisada como conforme' FROM DUAL
) source
ON (target.STATUS_CODE = source.STATUS_CODE)
WHEN MATCHED THEN
    UPDATE SET
        target.STATUS_NAME = source.STATUS_NAME,
        target.DESCRIPTION = source.DESCRIPTION,
        target.STATUS = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        ID_EVIDENCE_STATUS,
        STATUS_CODE,
        STATUS_NAME,
        DESCRIPTION,
        STATUS,
        CREATED_AT
    )
    VALUES (
        SQ_GDR_ESTADO_EVIDENCIA.NEXTVAL,
        source.STATUS_CODE,
        source.STATUS_NAME,
        source.DESCRIPTION,
        'ACTIVE',
        SYSTIMESTAMP
    );

MERGE INTO GDR_EVIDENCIA target
USING (
    SELECT
        goal.ID_GOAL AS ID_GOAL,
        status.ID_EVIDENCE_STATUS AS ID_EVIDENCE_STATUS,
        'Evidencia funcional inicial' AS EVIDENCE_TITLE,
        'Evidencia base creada para validar el flujo funcional del lote 3.' AS EVIDENCE_DETAIL
    FROM GDR_GOAL goal
    JOIN GDR_ESTADO_EVIDENCIA status ON status.STATUS_CODE = 'REGISTERED'
    WHERE goal.GOAL_TITLE = 'Meta institucional inicial'
) source
ON (target.ID_GOAL = source.ID_GOAL AND target.EVIDENCE_TITLE = source.EVIDENCE_TITLE)
WHEN MATCHED THEN
    UPDATE SET
        target.ID_EVIDENCE_STATUS = source.ID_EVIDENCE_STATUS,
        target.EVIDENCE_DETAIL = source.EVIDENCE_DETAIL,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_EVIDENCE,
        ID_GOAL,
        ID_EVIDENCE_STATUS,
        EVIDENCE_TITLE,
        EVIDENCE_DETAIL,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_GDR_EVIDENCIA.NEXTVAL,
        source.ID_GOAL,
        source.ID_EVIDENCE_STATUS,
        source.EVIDENCE_TITLE,
        source.EVIDENCE_DETAIL,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
