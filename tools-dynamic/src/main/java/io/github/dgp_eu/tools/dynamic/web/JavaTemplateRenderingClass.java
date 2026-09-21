package io.github.dgp_eu.tools.dynamic.web;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.github.dgp_eu.tools.core.time.TimingClass;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

/**
 * Template management
 */
public final class JavaTemplateRenderingClass {
    /** Content Disposition for HTML content */
    private static final String DFLT_CTNT_DISP = "inline";
    /** Content Type for HTML content */
    private static final String DFLT_CTNT_TYP = "text/html";
    /** Content Disposition value variable */
    /* default */ private static String contentDispositn = DFLT_CTNT_DISP;
    /** Content Type value variable */
    /* default */ private static String contentTypeValue = DFLT_CTNT_TYP;
    /** server exchange variable */
    private static HttpServerExchange exchange;
    /** output handler variable */
    private static Utf8ByteOutput output;
    /** page parameters variable */
    private static final Map<String, Object> TEMPLATE_PARAMS = new ConcurrentHashMap<>();

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
    public static void handleResponseHeader(final HeaderMap header) {
        final long contentLength = output.getContentLength();
        header.put(Headers.CONTENT_TYPE, contentTypeValue);
        header.put(Headers.CONTENT_LENGTH, String.valueOf(contentLength));
        header.put(Headers.CONTENT_DISPOSITION, contentDispositn);
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
        final String sessionTimeZone = UndertowSessionClass.getSession().getAttribute("TZ").toString();
        final gg.jte.Content selectTimeZones = output -> output.writeContent(HtmlClass.buildTimeZoneSelect(sessionTimeZone));
        packParameter("timeZoneSelect", selectTimeZones);
        packParameter("currentPageQuery", getCurrentPageQuery());
        packParameter("geoCoordinates", HtmlClass.buildGeographicalCoordinatesFromTimeZone(sessionTimeZone));
        final gg.jte.Content appDetail = output -> output.writeContent(HtmlClass.buildApplicationDetail());
        packParameter("appDetails", appDetail);
        final gg.jte.Content appCopyright = output -> output.writeContent(HtmlClass.buildApplicationCopyright());
        packParameter("appCopyright", appCopyright);
        final String dtNow = TimingClass.getCurrentDateTimeLocal(sessionTimeZone);
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
     * Setter for contentDispositn
     * @param inContentDisp input Content Disposition
     */
    public static void setContentDisposition(final String inContentDisp) {
       contentDispositn = inContentDisp;
    }

    /**
     * Setter for contentDispositn and contentTypeValue
     */
    public static void setContentDispositionAndTypeValuesForHtmlContent() {
       contentDispositn = DFLT_CTNT_DISP;
       contentTypeValue = DFLT_CTNT_TYP;
    }

    /**
     * Setter for contentTypeValue
     * @param inContentTypeVal input Content Type
     */
    public static void setContentTypeValue(final String inContentTypeVal) {
       contentTypeValue = inContentTypeVal;
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
    private JavaTemplateRenderingClass() {
        // intentional empty
    }

}