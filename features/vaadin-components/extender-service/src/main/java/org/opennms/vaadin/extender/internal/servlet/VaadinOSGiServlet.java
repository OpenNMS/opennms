package org.opennms.vaadin.extender.internal.servlet;

import com.vaadin.server.*;

import org.opennms.vaadin.extender.ApplicationFactory;
import org.opennms.vaadin.extender.SessionListenerRepository;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VaadinOSGiServlet extends VaadinServlet {
    private final OSGiUIProvider m_provider;
    private final Set<VaadinSession> m_sessions = Collections.synchronizedSet(new HashSet<VaadinSession>());
    private final org.slf4j.Logger LOG = LoggerFactory.getLogger(getClass());
    private final BundleContext m_context;

    public VaadinOSGiServlet(final ApplicationFactory factory, BundleContext bundleContext) {
        m_provider = new OSGiUIProvider(factory);
        m_context = bundleContext;
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
        
        final VaadinServletService service = super.createServletService(deploymentConfiguration);
        service.addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException {
                final VaadinSession session = event.getSession();
                m_sessions.add(session);
                if(session.getUIProviders().isEmpty() || !session.getUIProviders().contains(m_provider)) {
                    session.addUIProvider(m_provider);
                }
            }
        });

        service.addSessionDestroyListener(new SessionDestroyListener() {
            @Override
            public void sessionDestroy(SessionDestroyEvent event) {
                final VaadinSession session = event.getSession();
                m_sessions.remove(session);
                if (session.getUIProviders().contains(m_provider)) {
                    session.removeUIProvider(m_provider);
                }
            }
        });

        // Additional listeners
        SessionListenerRepository sessionListenerRepository = SessionListenerRepository.getRepository(m_context);
        if (sessionListenerRepository != null) {
            service.addSessionInitListener(sessionListenerRepository);
            service.addSessionDestroyListener(sessionListenerRepository);
        }
        return service;
    }

    @Override
    protected void servletInitialized() throws ServletException {
        LOG.info("Servlet Initialized");
    }
    
    @Override
    public void destroy() {
        for (final VaadinSession vaadinSession : m_sessions) {
            vaadinSession.removeFromSession(vaadinSession.getService());
        }
        super.destroy();
    }

}
