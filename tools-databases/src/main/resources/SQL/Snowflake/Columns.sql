WITH
    "CTE__System_And_Timing"                                                    AS (
        SELECT
              CURRENT_ACCOUNT()                                                 AS "SNOWFLAKE_INSTANCE"
            , SYSDATE()                                                         AS "EXTRACTION_TIMESTAMP_UTC"
    )
SELECT
      "metadata"."TABLE_CATALOG"                                                AS "TABLE_CATALOG"
    , "metadata"."TABLE_SCHEMA"                                                 AS "TABLE_SCHEMA"
    , "metadata"."TABLE_NAME"                                                   AS "TABLE_NAME"
    , "metadata"."ORDINAL_POSITION"                                             AS "ORDINAL_POSITION"
    , "metadata"."COLUMN_NAME"                                                  AS "COLUMN_NAME"
    , "metadata"."DATA_TYPE"                                                    AS "DATA_TYPE"
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
        END                                                                     AS "Data_Type_Full"
    , "metadata"."IS_NULLABLE"                                                  AS "IS_NULLABLE"
    , "metadata"."COLUMN_DEFAULT"                                               AS "COLUMN_DEFAULT"
    , "metadata"."COMMENT"                                                      AS "COMMENT"
    , "csat".*
FROM
    "INFORMATION_SCHEMA"."COLUMNS"
    INNER JOIN "CTE__System_And_Timing"                                         AS "csat"   ON
        1 = 1;
