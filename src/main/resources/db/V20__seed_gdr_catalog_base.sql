WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

MERGE INTO GDR_VALUE_TYPE target
USING (
    SELECT 'NUMERIC' AS VALUE_TYPE_CODE, 'Numerico' AS VALUE_TYPE_NAME, 'Valor numerico directo' AS DESCRIPTION FROM DUAL
    UNION ALL
    SELECT 'PERCENT', 'Porcentaje', 'Valor expresado en porcentaje' FROM DUAL
) source
ON (target.VALUE_TYPE_CODE = source.VALUE_TYPE_CODE)
WHEN MATCHED THEN
    UPDATE SET
        target.VALUE_TYPE_NAME = source.VALUE_TYPE_NAME,
        target.DESCRIPTION = source.DESCRIPTION,
        target.STATUS = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        ID_VALUE_TYPE,
        VALUE_TYPE_CODE,
        VALUE_TYPE_NAME,
        DESCRIPTION,
        STATUS,
        CREATED_AT
    )
    VALUES (
        SQ_GDR_VALUE_TYPE.NEXTVAL,
        source.VALUE_TYPE_CODE,
        source.VALUE_TYPE_NAME,
        source.DESCRIPTION,
        'ACTIVE',
        SYSTIMESTAMP
    );

MERGE INTO GDR_FORMULA target
USING (
    SELECT 'DIRECT' AS FORMULA_CODE, 'Directa' AS FORMULA_NAME, 'Formula directa base' AS DESCRIPTION FROM DUAL
    UNION ALL
    SELECT 'WEIGHTED', 'Ponderada', 'Formula ponderada base' FROM DUAL
) source
ON (target.FORMULA_CODE = source.FORMULA_CODE)
WHEN MATCHED THEN
    UPDATE SET
        target.FORMULA_NAME = source.FORMULA_NAME,
        target.DESCRIPTION = source.DESCRIPTION,
        target.STATUS = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        ID_FORMULA,
        FORMULA_CODE,
        FORMULA_NAME,
        DESCRIPTION,
        STATUS,
        CREATED_AT
    )
    VALUES (
        SQ_GDR_FORMULA.NEXTVAL,
        source.FORMULA_CODE,
        source.FORMULA_NAME,
        source.DESCRIPTION,
        'ACTIVE',
        SYSTIMESTAMP
    );

MERGE INTO GDR_SEGMENT target
USING (
    SELECT 'GENERAL' AS SEGMENT_CODE, 'General' AS SEGMENT_NAME, 'Segmento general base' AS DESCRIPTION FROM DUAL
    UNION ALL
    SELECT 'DIRECTIVO', 'Directivo', 'Segmento directivo base' FROM DUAL
) source
ON (target.SEGMENT_CODE = source.SEGMENT_CODE)
WHEN MATCHED THEN
    UPDATE SET
        target.SEGMENT_NAME = source.SEGMENT_NAME,
        target.DESCRIPTION = source.DESCRIPTION,
        target.STATUS = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        ID_SEGMENT,
        SEGMENT_CODE,
        SEGMENT_NAME,
        DESCRIPTION,
        STATUS,
        CREATED_AT
    )
    VALUES (
        SQ_GDR_SEGMENT.NEXTVAL,
        source.SEGMENT_CODE,
        source.SEGMENT_NAME,
        source.DESCRIPTION,
        'ACTIVE',
        SYSTIMESTAMP
    );
