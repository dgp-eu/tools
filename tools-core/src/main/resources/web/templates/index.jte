@import gg.jte.Content
@param String page
@param String title
@param Content menu
@param Content content
@param Content timeZoneSelect
@param String currentPageQuery
@param String geoCoordinates
@param Content appDetails
@param String timeNow
<!DOCTYPE html>
<html lang="en-US">
<head>
    <title>${title}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="Author" content="Daniel Popiniuc">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/web/static/styles.css">
    <link rel="stylesheet" href="/web/static/common-sample1.css">
    <!-- Bootstrap components -->
    <link rel="stylesheet" href="/web/static/BootStrap/5.3.8/bootstrap.min.css">
    <script src="/web/static/BootStrap/5.3.8/bootstrap.bundle.min.js" defer></script>
    <!-- Fontawesome components -->
    <link rel="stylesheet" href="/web/static/FontAwesome/7.2.0/fontawesome.min.css">
    <script src="/web/static/FontAwesome/7.2.0/brands.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.2.0/fontawesome.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.2.0/regular.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.2.0/solid.min.js" defer></script>
    <!-- Tabber components -->
    <script src="/web/static/Tabber/1.0.0/tabber.min.js" defer></script>
</head>
<body>
    <style type="text/css">.tabber{display:none;}</style>
    <header id="TitleContainer">
        <h1 id="PageTitle">${title}</h1>
    </header>
    <nav>
        <ul>${menu}</ul>
        <span id="now" style="top:0rem;font-size:0.6rem;margin:0.25rem;position:fixed;">${timeNow}</span>
    </nav>
    <main>${content}</main>
    <footer>
        <form action="/cc/indexSnowflake.php " method="get" id="TZform" style="float:left;margin:0rem 0.5rem;font-size:0.8rem;">
            <a href="https://maps.google.com/maps?q=${geoCoordinates}" target="_blank" style="margin-right:5px;">
                <i class="fa-solid fa-map-location"></i>
            </a>
            ${timeZoneSelect}
            <input type="hidden" name="page" value="Localization">
            <button class="btn btn-outline-primary" onclick="javascript:document.TZform.submit();" style="padding:0 var(--bs-btn-padding-x)!important;">Set</button>
            <input type="hidden" name="redirectAction" value="${currentPageQuery}">
        </form>
        <span id="app" style="float:right;margin:0rem 0.5rem;">${appDetails}</span>
    </footer>
</body>
</html>