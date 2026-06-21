WITH
    "CTE__INITIAL"                                                              AS (
        SELECT DISTINCT
              "o"."OrganizationName"                                            AS "OrganizationName"
            , "p"."ProductName"                                                 AS "ProductName"
            , "p"."ProfileId"                                                   AS "ProfileId"
            , "pb"."BranchName"                                                 AS "BranchName"
            , "o"."OrganizationId"                                              AS "OrganizationId"
            , "pb"."ProductId"                                                  AS "ProductId"
            , "pb"."BranchId"                                                   AS "BranchId"
            , JSON_EXTRACT("pb"."BranchURLs", '$.Releases')                     AS "Releases"
            , MAX("bv"."VersionId") OVER
                (PARTITION BY
                      "o"."OrganizationName"
                    , "p"."ProductName"
                    , "pb"."BranchName"
                ORDER BY
                      "bv"."ReleaseDate" DESC
                    , "bv"."VersionId" DESC)                                    AS "Latest VersionId"
        FROM
            "organization"                                                      AS "o"
            LEFT JOIN "organization_product"                                    AS "op"   ON
                "o"."OrganizationId"  = "op"."OrganizationId"
            LEFT JOIN "product"                                                 AS "p"    ON
                "op"."ProductId"      = "p"."ProductId"
            LEFT JOIN "product_branch"                                          AS "pb"   ON
                "p"."ProductId"       = "pb"."ProductId"
            LEFT JOIN "branch_versions"                                         AS "bv"   ON
                "pb"."BranchId"       = "bv"."BranchId"
        WHERE
            "pb"."BranchStatus" IN ('Active', 'Innovation', 'LTS')
    ),
    "CTE__FINAL"                                                                AS (
        SELECT
              "ci"."OrganizationName"                                           AS "OrganizationName"
            , "ci"."ProductName"                                                AS "ProductName"
            , "ci"."BranchName"                                                 AS "BranchName"
            , "ci"."OrganizationId"                                             AS "OrganizationId"
            , "ci"."ProductId"                                                  AS "ProductId"
            , "ci"."BranchId"                                                   AS "BranchId"
            , "ci"."Releases"                                                   AS "Releases"
            , "bv"."VersionCode"                                                AS "Latest release version"
            , "bv"."ReleaseDate"                                                AS "Latest release date"
            , SUBSTR(TIMEDIFF('now', "bv"."ReleaseDate"), 0, 12)                AS "Latest release aging full"
            , (JulianDay(date()) - JulianDay("bv"."ReleaseDate"))               AS "Latest release aging days"
            , "bv"."VersionId"                                                  AS "VersionId"
            , IFNULL("pl"."ProfileName", '#')                                   AS "Profile Name"
            , IFNULL("pl"."ProfileId", 999)                                     AS "ProfileId"
        FROM
            "CTE__INITIAL"                                                      AS "ci"
            INNER JOIN "branch_versions"                                        AS "bv"   ON
                    "ci"."BranchId"              = "bv"."BranchId"
                AND "ci"."Latest VersionId"      = "bv"."VersionId"
            LEFT JOIN "profile_list"                                            AS "pl"   ON
                "ci"."ProfileId"                 = "pl"."ProfileId"
    ),
    "CTE__Constants"                                                            AS (
        SELECT
              'InstalledIdentifier'                                             AS "Installed_Id"
            , 'Kit'                                                             AS "Kit"
    ),
    "CTE__Files"                                                                AS (
        SELECT
              "fv"."VersionId"                                                  AS "VersionId"
            , TRIM(
                GROUP_CONCAT(
                    IIF("fv"."FileType" = "cc"."Installed_Id"
                        , "fl"."FileName", '')), ',')                           AS "File Installed Name"
            , TRIM(
                GROUP_CONCAT(
                    IIF("fv"."FileType" = "cc"."Kit"
                        , "fl"."FileName", '')), ',')                           AS "File Kit Name"
            , TRIM(
                GROUP_CONCAT(
                    IIF("fv"."FileType" = "cc"."Installed_Id"
                        , "fl"."FileId", '')), ',')                             AS "File Installed Id"
            , TRIM(
                GROUP_CONCAT(
                    IIF("fv"."FileType" = "cc"."Kit"
                        , "fl"."FileId", '')), ',')                             AS "File Kit Id"
        FROM
            "file_version"                                                      AS "fv"
            INNER JOIN "file_list"                                              AS "fl"    ON
                "fv"."FileId" = "fl"."FileId"
            INNER JOIN "CTE__Constants"                                         AS "cc"    ON
                1 = 1
        WHERE
            "fv"."FileType"  IN ("cc"."Installed_Id", "cc"."Kit")
        GROUP BY
            "fv"."VersionId"
    )
SELECT
      "cf"."OrganizationName"
    , "cf"."ProductName"
    , "cf"."BranchName"
    , "cf"."OrganizationId"
    , "cf"."ProductId"
    , "cf"."BranchId"
    , "cf"."Releases"
    , "cf"."Latest release version"
    , "cf"."Latest release date"
    , "cf"."Latest release aging days"
    , "cf"."Latest release aging full"
    , "cf"."ProfileId"
    , "cf"."Profile Name"
    , "cf"."VersionId"
    , "ci"."File Installed Name"
    , "ci"."File Installed Id"
    , "ci"."File Kit Name"
    , "ci"."File Kit Id"
FROM
    "CTE__FINAL"                                                                AS "cf"
    INNER JOIN "CTE__Files"                                                     AS "ci"     ON
        "cf"."VersionId"  = "ci"."VersionId"
ORDER BY
      "cf"."Profile Name"
    , "cf"."OrganizationName"
    , "cf"."ProductName"
    , "cf"."BranchName";
