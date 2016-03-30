package org.opennms.vaadin.extender;


import com.vaadin.server.*;
import com.vaadin.server.ServiceException;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionListenerRepository implements SessionInitListener, SessionDestroyListener {

    private final static Logger LOG = LoggerFactory.getLogger(SessionListenerRepository.class);

    private final BundleContext context;
    private Map<VaadinSession, String> vaadinSessionIdMap = new HashMap<VaadinSession, String>();

    public static SessionListenerRepository getRepository(BundleContext context) {
        ServiceReference reference = context.getServiceReference(SessionListenerRepository.class.getName());
        return (SessionListenerRepository)context.getService(reference);
    }

    public SessionListenerRepository(BundleContext context) {
        this.context = context;
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        final String sessionId = vaadinSessionIdMap.get(event.getSession());
        if (sessionId == null) throw new IllegalArgumentException("Unknown session : " + event.getSession());
        for (SessionListener eachListener : getSessionListeners()) {
            eachListener.sessionDestroyed(sessionId);
        }
        vaadinSessionIdMap.remove(event.getSession());
    }

    @Override
    public void sessionInit(SessionInitEvent event) throws ServiceException {
        vaadinSessionIdMap.put(event.getSession(), event.getSession().getSession().getId());
        for (SessionListener eachListener : getSessionListeners()) {
            eachListener.sessionInitialized(vaadinSessionIdMap.get(event.getSession()));
        }
    }

    private List<SessionListener> getSessionListeners() {
        List<SessionListener> sessionListeners = new ArrayList<SessionListener>();
        ServiceReference[] references = new ServiceReference[0];
        try {
            references = context.getAllServiceReferences(SessionListener.class.getName(), null);
            if (references != null) {
                for (ServiceReference eachReference : references) {
                    Object service = context.getService(eachReference);
                    if (service == null) continue;
                    sessionListeners.add((SessionListener)service);
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Error retrieving SessionListeners", e);
        }
        return sessionListeners;
    }
}
