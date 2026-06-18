WITH
    "CTE__COMMON"                                                               AS (
        SELECT
              CURRENT_ACCOUNT()                                                 AS "SNOWFLAKE_INSTANCE"
            , SYSDATE()                                                         AS "EXTRACTION_TIMESTAMP_UTC"
    )
SELECT
      "cols"."TABLE_CATALOG"                                                    AS "Database"
    , "cols"."TABLE_SCHEMA"                                                     AS "Schema"
    , "cols"."TABLE_NAME"                                                       AS "Object"
    , "cols"."ORDINAL_POSITION"                                                 AS "Position"
    , "cols"."COLUMN_NAME"                                                      AS "Column"
    , "cols"."DATA_TYPE"                                                        AS "Data Type"
    , CASE
        WHEN "cols"."DATA_TYPE" = 'TEXT'
            AND ("cols"."CHARACTER_MAXIMUM_LENGTH" >= 16777215)    THEN
            "cols"."DATA_TYPE"
        WHEN "cols"."DATA_TYPE" = 'TEXT'                           THEN
            'VARCHAR('
                || TO_CHAR("cols"."CHARACTER_MAXIMUM_LENGTH")
                || ')'
        WHEN "cols"."DATA_TYPE" = 'FLOAT'                          THEN
            "cols"."DATA_TYPE"
        WHEN "cols"."DATA_TYPE" = 'NUMBER'                         THEN
            'NUMBER('
                || TO_CHAR("cols"."NUMERIC_PRECISION")
                || ','
                || TO_CHAR("cols"."NUMERIC_SCALE")
                || ')'
        WHEN "cols"."DATA_TYPE" IN ('ARRAY'
            , 'BINARY'
            , 'BOOLEAN'
            , 'DATE'
            , 'OBJECT'
            , 'TIME'
            , 'VARIANT')                                    THEN
            "cols"."DATA_TYPE"
        WHEN "cols"."DATA_TYPE" IN ('TIMESTAMP'
            , 'TIMESTAMP_LTZ'
            , 'TIMESTAMP_NTZ'
            , 'TIMESTAMP_TZ')                               THEN
            "cols"."DATA_TYPE"
                || '('
                ||  "cols"."DATETIME_PRECISION"
                || ')'
        ELSE
            '???'
        END                                                                     AS "Data Type Full"
    , "cols"."IS_NULLABLE"                                                      AS "NULLable"
    , "cols"."COLUMN_DEFAULT"                                                   AS "Default"
    , "cols"."COMMENT"                                                          AS "Comment"
    , "common"."SNOWFLAKE_INSTANCE"                                             AS "SNOWFLAKE_INSTANCE"
    , "common"."EXTRACTION_TIMESTAMP_UTC"                                       AS "EXTRACTION_TIMESTAMP_UTC"
FROM
    "%s"."INFORMATION_SCHEMA"."COLUMNS"                                        AS "cols"
    INNER JOIN "CTE__COMMON"                                                    AS "common"     ON  1 = 1
WHERE
        "cols"."TABLE_SCHEMA"   = '%s'
    AND "cols"."TABLE_NAME"     IN('%s');
