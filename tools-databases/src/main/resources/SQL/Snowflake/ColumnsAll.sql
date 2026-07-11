WITH
    "CTE__System_And_Timing"                                                    AS (
        SELECT
              CURRENT_ACCOUNT()                                                 AS "SNOWFLAKE_INSTANCE"
            , SYSDATE()                                                         AS "EXTRACTION_TIMESTAMP_UTC"
    )
SELECT
      "metadata"."TABLE_CATALOG"                                                AS "Database"
    , "metadata"."TABLE_SCHEMA"                                                 AS "Schema"
    , "metadata"."TABLE_NAME"                                                   AS "Object"
    , "metadata"."ORDINAL_POSITION"                                             AS "Position"
    , "metadata"."COLUMN_NAME"                                                  AS "Column"
    , "metadata"."DATA_TYPE"                                                    AS "Data Type"
    , CASE
        WHEN    "metadata"."DATA_TYPE"                  = 'TEXT'
            AND "metadata"."CHARACTER_MAXIMUM_LENGTH"   >= 16777215         THEN
            "metadata"."DATA_TYPE"
        WHEN "metadata"."DATA_TYPE"                     = 'TEXT'            THEN
            'VARCHAR('
                || TO_CHAR("metadata"."CHARACTER_MAXIMUM_LENGTH") || ')'
        WHEN "metadata"."DATA_TYPE"                     IN (  'FLOAT'
                                                            , 'DECFLOAT')   THEN
            "metadata"."DATA_TYPE"
        WHEN "metadata"."DATA_TYPE"                     = 'NUMBER'          THEN
            'NUMBER('
                || TO_CHAR("metadata"."NUMERIC_PRECISION")
                || ',' || TO_CHAR("metadata"."NUMERIC_SCALE") || ')'
        WHEN "metadata"."DATA_TYPE"                     IN (  'ARRAY'
                                                            , 'BINARY'
                                                            , 'BOOLEAN'
                                                            , 'DATE'
                                                            , 'MAP'
                                                            , 'OBJECT'
                                                            , 'TIME'
                                                            , 'VARIANT')    THEN
            "metadata"."DATA_TYPE"
        WHEN "metadata"."DATA_TYPE"                     LIKE 'TIMESTAMP%'   THEN
            "metadata"."DATA_TYPE"
                || '(' ||  "metadata"."DATETIME_PRECISION" || ')'
        ELSE
            '???'
        END                                                                     AS "Data Type Full"
    , "metadata"."IS_NULLABLE"                                                  AS "NULLable"
    , "metadata"."COLUMN_DEFAULT"                                               AS "Default"
    , "metadata"."COMMENT"                                                      AS "Comment"
    , "csat".*
FROM
    "%s"."INFORMATION_SCHEMA"."COLUMNS"                                         AS "metadata"
    INNER JOIN "CTE__System_And_Timing"                                         AS "csat"   ON
        1 = 1
WHERE
        "metadata"."TABLE_SCHEMA"   = '%s'
    AND "metadata"."TABLE_NAME"     IN('%s');
