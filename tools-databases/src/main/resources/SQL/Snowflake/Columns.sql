SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "ORDINAL_POSITION"
    , "COLUMN_NAME"
    , "DATA_TYPE"
    , CASE
        WHEN "DATA_TYPE" = 'TEXT'
            AND ("CHARACTER_MAXIMUM_LENGTH" >= 16777215)    THEN
            "DATA_TYPE"
        WHEN "DATA_TYPE" = 'TEXT'                           THEN
            'VARCHAR('
                || TO_CHAR("CHARACTER_MAXIMUM_LENGTH")
                || ')'
        WHEN "DATA_TYPE" = 'FLOAT'                          THEN
            "DATA_TYPE"
        WHEN "DATA_TYPE" = 'NUMBER'                         THEN
            'NUMBER('
                || TO_CHAR("NUMERIC_PRECISION")
                || ','
                || TO_CHAR("NUMERIC_SCALE")
                || ')'
        WHEN "DATA_TYPE" IN ('ARRAY'
            , 'BINARY'
            , 'BOOLEAN'
            , 'DATE'
            , 'OBJECT'
            , 'TIME'
            , 'VARIANT')                                    THEN
            "DATA_TYPE"
        WHEN "DATA_TYPE" IN ('TIMESTAMP'
            , 'TIMESTAMP_LTZ'
            , 'TIMESTAMP_NTZ'
            , 'TIMESTAMP_TZ')                               THEN
            "DATA_TYPE"
                || '('
                ||  "DATETIME_PRECISION"
                || ')'
        ELSE
            '???'
        END                 AS "Data_Type_Full"
    , "IS_NULLABLE"
    , "COLUMN_DEFAULT"
    , "COMMENT"
    , CURRENT_ACCOUNT()     AS "SNOWFLAKE_INSTANCE"
    , SYSDATE()             AS "EXTRACTION_TIMESTAMP_UTC"
FROM
    "INFORMATION_SCHEMA"."COLUMNS";
