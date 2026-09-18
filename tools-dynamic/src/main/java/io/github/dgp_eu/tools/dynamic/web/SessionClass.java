package io.github.dgp_eu.tools.dynamic.web;

import java.util.Deque;
import java.util.Map;
import java.util.SequencedMap;

import io.github.dgp_eu.tools.core.time.ZoneDataServiceClass;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.Sessions;

/**
 * Template management
 */
public final class SessionClass {
    /** Session Manager handle */
    private static final InMemorySessionManager SESSION_MANAGER = new InMemorySessionManager("SESSION_MANAGER");
    /** Session Config handle */
    private static final SessionCookieConfig SESSION_CONFIG = new SessionCookieConfig();
    /** Session variable */
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
        final Map<String, Deque<String>> queryParams = ParametersClass.getQueryParameters();
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
    private SessionClass() {
        // intentionally blank
    }

}