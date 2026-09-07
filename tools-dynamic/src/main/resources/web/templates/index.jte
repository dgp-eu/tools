@import gg.jte.Content
@param Content appCopyright
@param Content appDetails
@param Content infoContext
@param Content menu
@param Content mainContent
@param Content timeZoneSelect
@param String currentPageQuery
@param String geoCoordinates
@param String page
@param String timeNow
@param String title
<!DOCTYPE html>
<html lang="en-US">
<head>
    <title>${title}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="Author" content="Daniel Popiniuc">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/web/static/styles.css">
    <!-- Fontawesome components -->
    <link rel="stylesheet" href="/web/static/FontAwesome/7.3.1/fontawesome.min.css">
    <script src="/web/static/FontAwesome/7.3.1/brands.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.3.1/fontawesome.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.3.1/regular.min.js" defer></script>
    <script src="/web/static/FontAwesome/7.3.1/solid.min.js" defer></script>
    <!-- Tabber components -->
    <link rel="stylesheet" href="/web/static/Tabber/1.0.0/tabber.css">
    <script src="/web/static/Tabber/1.0.0/tabber.min.js" defer></script>
</head>
<body>
    <style type="text/css">.tabber{display:none;}</style>
    <header id="TitleContainer">
        <h1 id="PageTitle">${title}</h1>
        <span id="now" class="nowClass">${timeNow}</span>
    </header>
    <nav>
        <ul>${menu}</ul>
        <div class="infoContextClass blueShaddowBorderLeft" id="infoContextId">${infoContext}</div>
    </nav>
    <main>${mainContent}</main>
    <footer>
        <form action="/" method="get" id="TZform" class="timeZoneFormClass">
            <a href="https://maps.google.com/maps?q=${geoCoordinates}" target="_blank" style="margin-right:5px;">
                <i class="fa-solid fa-map-location"></i>
            </a>
            ${timeZoneSelect}
            <input type="hidden" name="page" value="Localization">
            <input type="hidden" name="redirectAction" value="${currentPageQuery}">
        </form>
        <span id="app" class="appDetails">${appDetails}</span>
        <span id="app" class="appCopyright">${appCopyright}</span>
    </footer>
</body>
</html>