// Copyright 2026 Daniel-Gheorghe Popiniuc
package io.github.dgp_eu.tools.undertow;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.ResourceCodeResolver;
import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ZoneDataServiceClass;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Sessions;
import io.undertow.util.StatusCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Undertow common class
 */
public final class UndertowClass {
    /**
     * Root handle variable
     */
    private static HttpHandler rootHandler;
    /**
     * Web IP variable
     */
    private static String webIp;
    /**
     * Web port variable
     */
    private static String webPort;
    /**
     * Web protocol variable
     */
    private static String webProtocol;

    /**
     * Initiating Template Engine
     * @return TemplateEngine
     */
    public static TemplateEngine createTemplateEngine() {
        final ResourceCodeResolver resolver = new ResourceCodeResolver("web/templates");
        final TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Html);
        templateEngine.setBinaryStaticContent(true);
        return templateEngine;
    }


    /**
     * Common web page handle logic
     * @param inExchange input Exchange
     */
    public static void handleCommonThings(final HttpServerExchange inExchange) {
        ParametersSubClass.setQueryParameters(inExchange);
        ParametersSubClass.setPageParameter();
        SessionSubClass.initializeSession(inExchange);
        SessionSubClass.handleTimeZoneSession();
        ParametersSubClass.redirectPageIfNeeded(inExchange);
        final Object tzAttribute = UndertowClass.SessionSubClass.getSession().getAttribute("TZ");
        final String timeZone = tzAttribute != null ? tzAttribute.toString() : "UTC";
        HtmlClass.TableSubClass.setTimeZone(timeZone);
    }

    /**
     * Reading Project Properties
     */
    private static void readWebConfigurationFromProjectProperties() {
        final String[] varsToPick = {"webIp", "webProtocol"};
        final Properties webProperties = BasicStructuresClass.PropertiesReaderSubClass.getVariableFromProjectProperties("/undertow.properties", varsToPick);
        webIp = webProperties.get("webIp").toString();
        webProtocol = webProperties.get("webProtocol").toString();
    }

    /**
     * Web server logic
     */
    public static void runWebServer() {
        readWebConfigurationFromProjectProperties();
        final String pathStatic = "web/static";
        try (ClassPathResourceManager resourceManager = new ClassPathResourceManager(
                Thread.currentThread().getContextClassLoader(),
                pathStatic)) {
            // handle static content
            final ResourceHandler staticHandler = new ResourceHandler(resourceManager)
                    .setDirectoryListingEnabled(false);
            // handle static + dynamic content
            final PathHandler routesHandler = Handlers.path()
                    .addPrefixPath("/" + pathStatic, staticHandler)
                    .addPrefixPath("/", rootHandler);
            // finally package everything to consider Session handler
            final HttpHandler sessionHandler = SessionSubClass.getSessionHandler(routesHandler);
            // determine the relevant port
            final int relevantWebPort = BasicStructuresClass.convertStringIntoInteger(webPort);
            // start Web Server
            final Undertow.Builder builder = Undertow.builder()
                    .addHttpListener(relevantWebPort, webIp)
                    .setHandler(sessionHandler);
            final Undertow server = builder.build();
            final String strFeedback = String.format("Server running at %s://%s:%s", webProtocol, webIp, webPort);
            LogExposureClass.LOGGER.info(strFeedback);
            server.start();
        } catch (IOException ex) {
            final String strFeedbackErr = String.format("Error on getting static resources... %s", Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
    }

    /**
     * setter for Root Handler
     * @param inRootHandler map with root handler
     */
    public static void setRootHandler(final HttpHandler inRootHandler) {
        rootHandler = inRootHandler;
    }

    /**
     * setter for webPort
     * @param inWebPort web port to use
     */
    public static void setWebPort(final String inWebPort) {
        webPort = inWebPort;
    }

    /**
     * Template management
     */
    public static final class ParametersSubClass {
        /**
         * Page variable
         */
        private static String parameterPage;
        /**
         * page parameter variables
         */
        private static Map<String, Deque<String>> queryParams;

        /**
         * Getter for parameterPage
         * @return String
         */
        public static String getPageParameter() {
            return parameterPage;
        }

        /**
         * Getter for queryParams
         * @return Map
         */
        public static Map<String, Deque<String>> getQueryParameters() {
            return queryParams;
        }

        /**
         * Getting Response Header
         * @param exchange HttpServerExchange
         * @return HeaderMap
         */
        private static HeaderMap getResponseHeader(final HttpServerExchange exchange) {
            return exchange.getResponseHeaders();
        }

        /**
         * Redirecting page
         * @param exchange HttpServerExchange used to set redirect status,
         *  location header and end the exchange
         */
        public static void redirectPageIfNeeded(final HttpServerExchange exchange) {
            if (queryParams.get("redirectAction") != null) {
                exchange.setStatusCode(StatusCodes.SEE_OTHER); // 303 Redirect
                final HeaderMap responseHeader = getResponseHeader(exchange);
                responseHeader.put(Headers.LOCATION, "/?" + queryParams.get("redirectAction").getFirst());
                exchange.endExchange();
            }
        }

        /**
         * Page parameter isolation
         */
        public static void setPageParameter() {
            final Deque<String> pageParams = queryParams.get("page");
            parameterPage = (pageParams == null) ? "home" : pageParams.getFirst();
        }

        /**
         * get query parameters into local variable
         * @param inExchange HttpServerExchange
         */
        public static void setQueryParameters(final HttpServerExchange inExchange) {
            // Get the 'page' query parameter (Deques are used for multi-value parameters)
            queryParams = inExchange.getQueryParameters(); 
        }

        // Private constructor to prevent instantiation
        private ParametersSubClass() {
            // intentionally blank
        }

    }

    /**
     * Template management
     */
    public static final class SessionSubClass {
        /**
         * Session Manager handle
         */
        private static final InMemorySessionManager SESSION_MANAGER = new InMemorySessionManager("SESSION_MANAGER");
        /**
         * Session Config handle
         */
        private static final SessionCookieConfig SESSION_CONFIG = new SessionCookieConfig();
        /**
         * Session variable
         */
        private static Session session;

        /**
         * Getter for session
         * @return Session
         */
        public static Session getSession() {
            return session;
        }

        /**
         * Getter Session Handler
         * @param routesHandler routes
         * @return HttpHandler
         */
        public static HttpHandler getSessionHandler(final PathHandler routesHandler) {
            return new SessionAttachmentHandler(routesHandler, SESSION_MANAGER, SESSION_CONFIG);
        }

        /**
         * Time Zone set logic
         */
        public static void handleTimeZoneSession() {
            final Map<String, Deque<String>> queryParams = ParametersSubClass.getQueryParameters();
            if (queryParams.get("TZ") != null) {
                session.setAttribute("TZ", queryParams.get("TZ").getFirst());
            }
            if (session.getAttribute("TZ") == null) {
                final SequencedMap<String, String> sortedTimeZones = ZoneDataServiceClass.loadSupportedTimeZones();
                final String crtUserTimeZone = System.getProperty("user.timezone");
                if (crtUserTimeZone != null
                        && !sortedTimeZones.getOrDefault(crtUserTimeZone, "").isEmpty()) {
                    session.setAttribute("TZ", crtUserTimeZone);
                } else {
                    session.setAttribute("TZ", "Asia/Kolkata");
                }
            }
        }

        /**
         * Session initializing
         * @param inExchange input Exchange
         */
        public static void initializeSession(final HttpServerExchange inExchange) {
            session = Sessions.getOrCreateSession(inExchange);
        }

        // Private constructor to prevent instantiation
        private SessionSubClass() {
            // intentionally blank
        }

    }

    /**
     * Template management
     */
    public static final class TemplateRenderingSubClass {
        /**
         * server exchange
         */
        private static HttpServerExchange exchange;
        /**
         * page parameters
         */
        private static final Map<String, Object> TEMPLATE_PARAMS = new ConcurrentHashMap<>();
        /**
         * output handler
         */
        private static Utf8ByteOutput output;

        /**
         * Getter for parameterPage
         * @return String
         */
        private static String getCurrentPageQuery() {
            return exchange.getQueryString().replaceAll("([|])", "");
        }

        /**
         * handle Response Header
         * @param header map
         */
        private static void handleResponseHeader(final HeaderMap header) {
            final long contentLength = output.getContentLength();
            header.put(Headers.CONTENT_TYPE, "text/html");
            header.put(Headers.CONTENT_LENGTH, String.valueOf(contentLength));
        }

        /**
         * handle Response Sender
         * @param response received response 
         */
        private static void handleResponseSender(final Sender response) {
            response.send(ByteBuffer.wrap(output.toByteArray()));
        }

        /**
         * Common parameters packing
         */
        public static void packCommonParameters() {
            final String sessionTimeZone = UndertowClass.SessionSubClass.getSession().getAttribute("TZ").toString();
            final gg.jte.Content selectTimeZones = output -> output.writeContent(HtmlClass.buildTimeZoneSelect(sessionTimeZone));
            packParameter("timeZoneSelect", selectTimeZones);
            packParameter("currentPageQuery", getCurrentPageQuery());
            packParameter("geoCoordinates", HtmlClass.buildGeographicalCoordinatesFromTimeZone(sessionTimeZone));
            final gg.jte.Content myApp = output -> output.writeContent(HtmlClass.buildApplicationDetail());
            packParameter("appDetails", myApp);
            final String dtNow = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss", Locale.US)
                    .format(ZonedDateTime.now(ZoneId.of(sessionTimeZone)));
            packParameter("timeNow", dtNow);
        }

        /**
         * pack Parameter
         * @param name for parameter
         * @param value for parameter
         */
        public static void packParameter(final String name, final Object value) {
            TEMPLATE_PARAMS.put(name, value);
        }

        /**
         * Helper method with explicit typing to handle rendering
         * @param engine template pointer
         * @param fileName name of file for template
         */
        public static void renderTemplate(final TemplateEngine engine, final String fileName) {
            engine.render(fileName, TEMPLATE_PARAMS, output);
            final HeaderMap header = exchange.getResponseHeaders();
            handleResponseHeader(header);
            final Sender response = exchange.getResponseSender();
            handleResponseSender(response);
        }

        /**
         * Setter for Server Exchange
         * @param inExchange input Exchange
         */
        public static void setServerExchange(final HttpServerExchange inExchange) {
           exchange = inExchange;
        }
        /**
         * Setter for output
         * @param inOutput Output
         */
        public static void setOutput(final Utf8ByteOutput inOutput) {
           output = inOutput;
        }

        // Private constructor to prevent instantiation
        private TemplateRenderingSubClass() {
            // intentional empty
        }

    }

    private UndertowClass() {
        // intentionally blank
    }

}
