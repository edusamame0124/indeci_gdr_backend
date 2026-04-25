WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO GDR_INDICATOR target
USING (
    SELECT
        'IND-001' AS INDICATOR_CODE,
        'Cumplimiento de objetivos base' AS INDICATOR_NAME,
        'Indicador base del lote 2' AS DESCRIPTION,
        valueType.ID_VALUE_TYPE AS ID_VALUE_TYPE,
        formula.ID_FORMULA AS ID_FORMULA,
        segment.ID_SEGMENT AS ID_SEGMENT
    FROM GDR_VALUE_TYPE valueType
    JOIN GDR_FORMULA formula ON formula.FORMULA_CODE = 'DIRECT'
    JOIN GDR_SEGMENT segment ON segment.SEGMENT_CODE = 'GENERAL'
    WHERE valueType.VALUE_TYPE_CODE = 'NUMERIC'
) source
ON (target.INDICATOR_CODE = source.INDICATOR_CODE)
WHEN MATCHED THEN
    UPDATE SET
        target.INDICATOR_NAME = source.INDICATOR_NAME,
        target.DESCRIPTION = source.DESCRIPTION,
        target.ID_VALUE_TYPE = source.ID_VALUE_TYPE,
        target.ID_FORMULA = source.ID_FORMULA,
        target.ID_SEGMENT = source.ID_SEGMENT,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_INDICATOR,
        INDICATOR_CODE,
        INDICATOR_NAME,
        DESCRIPTION,
        ID_VALUE_TYPE,
        ID_FORMULA,
        ID_SEGMENT,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_GDR_INDICATOR.NEXTVAL,
        source.INDICATOR_CODE,
        source.INDICATOR_NAME,
        source.DESCRIPTION,
        source.ID_VALUE_TYPE,
        source.ID_FORMULA,
        source.ID_SEGMENT,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

MERGE INTO GDR_GOAL target
USING (
    SELECT
        assignment.ID_ASSIGNMENT AS ID_ASSIGNMENT,
        indicator.ID_INDICATOR AS ID_INDICATOR,
        'Meta institucional inicial' AS GOAL_TITLE,
        'Meta base creada para validar el flujo del lote 2' AS DESCRIPTION,
        100 AS EXPECTED_VALUE,
        50 AS WEIGHT
    FROM GDR_EVALUATION_ASSIGNMENT assignment
    JOIN GDR_CYCLE cycle ON cycle.ID_CYCLE = assignment.ID_CYCLE AND cycle.STATUS = 'ACTIVE'
    JOIN HR_PERSON evaluator ON evaluator.ID_PERSON = assignment.ID_EVALUATOR_PERSON AND evaluator.DOCUMENT_NUMBER = '70000001'
    JOIN HR_PERSON evaluated ON evaluated.ID_PERSON = assignment.ID_EVALUATED_PERSON AND evaluated.DOCUMENT_NUMBER = '70000002'
    JOIN GDR_INDICATOR indicator ON indicator.INDICATOR_CODE = 'IND-001'
) source
ON (target.ID_ASSIGNMENT = source.ID_ASSIGNMENT AND target.GOAL_TITLE = source.GOAL_TITLE)
WHEN MATCHED THEN
    UPDATE SET
        target.ID_INDICATOR = source.ID_INDICATOR,
        target.DESCRIPTION = source.DESCRIPTION,
        target.EXPECTED_VALUE = source.EXPECTED_VALUE,
        target.WEIGHT = source.WEIGHT,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_GOAL,
        ID_ASSIGNMENT,
        ID_INDICATOR,
        GOAL_TITLE,
        DESCRIPTION,
        EXPECTED_VALUE,
        WEIGHT,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_GDR_GOAL.NEXTVAL,
        source.ID_ASSIGNMENT,
        source.ID_INDICATOR,
        source.GOAL_TITLE,
        source.DESCRIPTION,
        source.EXPECTED_VALUE,
        source.WEIGHT,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
