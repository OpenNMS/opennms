package org.opennms.vaadin.extender;

public interface SessionListener {
    void sessionDestroyed(String sessionId);
    void sessionInitialized(String sessionId);
}
