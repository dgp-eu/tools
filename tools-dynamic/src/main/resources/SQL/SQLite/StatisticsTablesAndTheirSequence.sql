SELECT
      "m"."name"                        AS "Table"
    , IFNULL("q"."seq", 0)              AS "Sequence"
FROM
    "sqlite_master"                     AS "m"
    LEFT JOIN "sqlite_sequence"         AS "q"  ON
        "m"."name" = "q"."name"
WHERE
        "m"."type" = 'table'
    AND "m"."name" NOT LIKE 'sqlite_%';
