package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.OpenNMSEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.OpenNMSEvent.Type;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.OpenNMSEventHandler;

public class OpenNMSEventManager {
    private static final Logger LOG = Logger.getLogger(OpenNMSEventManager.class.getName());
    private Map<OpenNMSEvent.Type<? extends OpenNMSEventHandler>, Set<? extends OpenNMSEventHandler>> m_handlers = new HashMap<OpenNMSEvent.Type<? extends OpenNMSEventHandler>, Set<? extends OpenNMSEventHandler>>();

    public OpenNMSEventManager() {
    }

    @SuppressWarnings("unchecked")
    protected <T extends OpenNMSEventHandler> Set<T> getHandlersForEvent(final OpenNMSEvent<T> event) {
        return (Set<T>) m_handlers.get(event.getAssociatedType());
    }

    public <T extends OpenNMSEventHandler> void fireEvent(final OpenNMSEvent<T> event) {
        LOG.info("OpenNMSEventManager.fireEvent(" + event.toDebugString() + ")");
        final Set<T> handlers = getHandlersForEvent(event);
        if (handlers != null) {
            for (final T handler : handlers) {
                event.dispatch(handler);
            }
        }
    }

    public <H extends OpenNMSEventHandler> void addHandler(final OpenNMSEvent.Type<H> type, final H handler) {
        @SuppressWarnings("unchecked")
        Set<H> handlers = (Set<H>) m_handlers.get(type);
        if (handlers == null) {
            handlers = new HashSet<H>();
            m_handlers.put(type, handlers);
        }
        handlers.add(handler);
    }

    @SuppressWarnings("unchecked")
    public <H extends OpenNMSEventHandler> void removeHandler(final Type<H> type, final H handler) {
        final Set<H> handlers = (Set<H>) m_handlers.get(type);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
}
