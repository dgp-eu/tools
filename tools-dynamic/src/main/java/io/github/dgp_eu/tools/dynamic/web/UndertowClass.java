/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.dynamic.web;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

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
        SessionClass.initializeSession(inExchange);
        SessionClass.handleTimeZoneSession();
        ParametersClass.redirectPageIfNeeded(inExchange);
        final Object tzAttribute = SessionClass.getSession().getAttribute("TZ");
        final String timeZone = tzAttribute != null ? tzAttribute.toString() : "UTC";
        HtmlClass.TableSubClass.setOutTimeZone(timeZone);
    }

    /**
     * Query Parameters and Page logic
     * @param inExchange input Exchange
     */
    public static void handleQueryParametersAndPage(final HttpServerExchange inExchange) {
        ParametersClass.setQueryParameters(inExchange);
        ParametersClass.setPageParameter();
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
            final HttpHandler sessionHandler = SessionClass.getSessionHandler(routesHandler);
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

    private UndertowClass() {
        // intentionally blank
    }

}
