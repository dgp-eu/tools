WITH
    "CTE__Raw"                                                                  AS (
        %s
    )
SELECT
      ROW_NUMBER () OVER (ORDER BY "Table")                                     AS "#"
    , "Table"                                                                   AS "Table"
    , "Records"                                                                 AS "Records"
    , "Sequence"                                                                AS "Sequence"
    , 'color:'
        || CASE
            WHEN    "Records" = 0
                AND "Sequence" = 0      THEN
                'gray;font-size:0.85rem;font-style:italic'
            WHEN "Sequence" = "Records" THEN
                'green'
            WHEN "Sequence" = 0         THEN
                'blue'
            ELSE
                'red'
            END || ';'                                                          AS "RowStyle"
    , "Sequence" - "Records"                                                    AS "Gap"
FROM
    "CTE__Raw";
