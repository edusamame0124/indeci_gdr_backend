WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO HR_ORG_UNIT target
USING (
    SELECT 'OTI' AS UNIT_CODE, 'Oficina de Tecnologias de la Informacion' AS UNIT_NAME FROM DUAL
) source
ON (target.UNIT_CODE = source.UNIT_CODE)
WHEN MATCHED THEN
    UPDATE SET
        target.UNIT_NAME = source.UNIT_NAME,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_ORG_UNIT,
        UNIT_CODE,
        UNIT_NAME,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_HR_ORG_UNIT.NEXTVAL,
        source.UNIT_CODE,
        source.UNIT_NAME,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

MERGE INTO HR_PERSON target
USING (
    SELECT
        '70000001' AS DOCUMENT_NUMBER,
        'evaluador.demo@gdr.gob.pe' AS EMAIL,
        'Evaluador Demo' AS DISPLAY_NAME,
        org.ID_ORG_UNIT AS ID_ORG_UNIT
    FROM HR_ORG_UNIT org
    WHERE org.UNIT_CODE = 'OTI'
) source
ON (target.DOCUMENT_NUMBER = source.DOCUMENT_NUMBER)
WHEN MATCHED THEN
    UPDATE SET
        target.EMAIL = source.EMAIL,
        target.DISPLAY_NAME = source.DISPLAY_NAME,
        target.ID_ORG_UNIT = source.ID_ORG_UNIT,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_PERSON,
        DOCUMENT_NUMBER,
        EMAIL,
        DISPLAY_NAME,
        ID_ORG_UNIT,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_HR_PERSON.NEXTVAL,
        source.DOCUMENT_NUMBER,
        source.EMAIL,
        source.DISPLAY_NAME,
        source.ID_ORG_UNIT,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

MERGE INTO HR_PERSON target
USING (
    SELECT
        '70000002' AS DOCUMENT_NUMBER,
        'servidor.demo@gdr.gob.pe' AS EMAIL,
        'Servidor Demo' AS DISPLAY_NAME,
        org.ID_ORG_UNIT AS ID_ORG_UNIT
    FROM HR_ORG_UNIT org
    WHERE org.UNIT_CODE = 'OTI'
) source
ON (target.DOCUMENT_NUMBER = source.DOCUMENT_NUMBER)
WHEN MATCHED THEN
    UPDATE SET
        target.EMAIL = source.EMAIL,
        target.DISPLAY_NAME = source.DISPLAY_NAME,
        target.ID_ORG_UNIT = source.ID_ORG_UNIT,
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_PERSON,
        DOCUMENT_NUMBER,
        EMAIL,
        DISPLAY_NAME,
        ID_ORG_UNIT,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_HR_PERSON.NEXTVAL,
        source.DOCUMENT_NUMBER,
        source.EMAIL,
        source.DISPLAY_NAME,
        source.ID_ORG_UNIT,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

MERGE INTO GDR_EVALUATION_ASSIGNMENT target
USING (
    SELECT
        cycle.ID_CYCLE AS ID_CYCLE,
        evaluator.ID_PERSON AS ID_EVALUATOR_PERSON,
        evaluated.ID_PERSON AS ID_EVALUATED_PERSON
    FROM GDR_CYCLE cycle
    JOIN HR_PERSON evaluator ON evaluator.DOCUMENT_NUMBER = '70000001'
    JOIN HR_PERSON evaluated ON evaluated.DOCUMENT_NUMBER = '70000002'
    WHERE cycle.STATUS = 'ACTIVE'
) source
ON (
    target.ID_CYCLE = source.ID_CYCLE
    AND target.ID_EVALUATOR_PERSON = source.ID_EVALUATOR_PERSON
    AND target.ID_EVALUATED_PERSON = source.ID_EVALUATED_PERSON
)
WHEN MATCHED THEN
    UPDATE SET
        target.STATUS = 'ACTIVE',
        target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (
        ID_ASSIGNMENT,
        ID_CYCLE,
        ID_EVALUATOR_PERSON,
        ID_EVALUATED_PERSON,
        STATUS,
        CREATED_AT,
        UPDATED_AT
    )
    VALUES (
        SQ_GDR_EVALUATION_ASSIGNMENT.NEXTVAL,
        source.ID_CYCLE,
        source.ID_EVALUATOR_PERSON,
        source.ID_EVALUATED_PERSON,
        'ACTIVE',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
