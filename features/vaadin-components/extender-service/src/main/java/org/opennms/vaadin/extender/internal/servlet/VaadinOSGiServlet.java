package org.opennms.vaadin.extender.internal.servlet;

import java.util.Objects;

import org.opennms.vaadin.extender.SessionListenerRepository;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;

public class VaadinOSGiServlet extends VaadinServlet {
    private final UIProvider m_provider;
    private final org.slf4j.Logger LOG = LoggerFactory.getLogger(getClass());
    private final BundleContext m_context;

    public VaadinOSGiServlet(final UIProvider uiProvider, BundleContext bundleContext) {
        m_provider = Objects.requireNonNull(uiProvider);
        m_context = bundleContext;
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
        
        final VaadinServletService service = super.createServletService(deploymentConfiguration);
        service.addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException {
                final VaadinSession session = event.getSession();
                if(session.getUIProviders().isEmpty() || !session.getUIProviders().contains(m_provider)) {
                    session.addUIProvider(m_provider);
                }
            }
        });

        service.addSessionDestroyListener(new SessionDestroyListener() {
            @Override
            public void sessionDestroy(SessionDestroyEvent event) {
                final VaadinSession session = event.getSession();
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

        // The Default location of the favicon is "theme://favicon.ico".
        // However the theme may not provide a favicon. To avoid each theme to host the favicon.ico file, we just use
        // the one defined in /opennms
        service.addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException {
                event.getSession().addBootstrapListener(new BootstrapListener() {
                    @Override
                    public void modifyBootstrapPage(BootstrapPageResponse response) {
                        response.getDocument().head()
                                .getElementsByAttributeValue("rel", "shortcut icon")
                                    .attr("href", "/opennms/favicon.ico");
                        response.getDocument().head()
                                .getElementsByAttributeValue("rel", "icon")
                                    .attr("href", "/opennms/favicon.ico");
                    }

                    @Override
                    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {

                    }
                });
            }
        });
        return service;
    }
}
