package io.github.dgp_eu.tools.dynamic.web;

import java.util.Deque;
import java.util.Map;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * Template management
 */
public final class UndertowParametersClass {
    /** Page variable */
    private static String parameterPage;
    /** page parameter variables */
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
    private UndertowParametersClass() {
        // intentionally blank
    }

}