package org.opennms.vaadin.extender.internal.extender;

import static org.opennms.vaadin.extender.internal.extender.PaxVaadinBundleTracker.findWidgetset;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.vaadin.extender.ApplicationFactory;
import org.opennms.vaadin.extender.Constants;
import org.opennms.vaadin.extender.internal.servlet.OSGiUIProvider;
import org.opennms.vaadin.extender.internal.servlet.VaadinOSGiServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class ApplicationFactoryServiceTracker extends ServiceTracker<ApplicationFactory, ApplicationFactory> {
    
    private Map<ApplicationFactory, ServiceRegistration> m_serviceRegistration = new HashMap<ApplicationFactory, ServiceRegistration>();
    private final Logger logger = LoggerFactory.getLogger(ApplicationFactoryServiceTracker.class.getName());
    private final OSGiUIProvider uiProvider = new OSGiUIProvider();
    
    public ApplicationFactoryServiceTracker(BundleContext context) {
        super(context, ApplicationFactory.class.getName(), null);
    }
    
    @SuppressWarnings({"unchecked"})
    @Override
    public ApplicationFactory addingService(ServiceReference reference) {
        final ApplicationFactory factory = super.addingService(reference);
        if (factory == null) return null;
        final FactoryServlet servlet = new FactoryServlet(uiProvider, factory, reference.getBundle().getBundleContext());
        final Dictionary props = new Properties();
        
        for(String key : reference.getPropertyKeys()) {
            props.put(key, reference.getProperty(key));
        }

        // Alias is no longer supported, use new pattern key
        if (props.get(Constants.ALIAS) != null && props.get(Constants.OSGI_HTTP_WHITEBOARD_SERVLET_PATTERN) == null) {
            logger.warn("{} is deprecated. Please use {} instead. For now I am going to do that for you", Constants.ALIAS, Constants.OSGI_HTTP_WHITEBOARD_SERVLET_PATTERN);
            props.put(Constants.OSGI_HTTP_WHITEBOARD_SERVLET_PATTERN, props.get(Constants.ALIAS));
        }

        // Ensure we have the servlet.pattern defined
        if(props.get(Constants.OSGI_HTTP_WHITEBOARD_SERVLET_PATTERN) == null) {
            logger.warn("You have not set the {} property for ApplicationFactory: {}", Constants.OSGI_HTTP_WHITEBOARD_SERVLET_PATTERN, factory);
        }

        // Support the old way of defining the widgetset as well
        if (props.get("init.widgetset") != null) {
            logger.warn("Property {} is deprecated. Please use {} instead. For now I am going to do that for you", "init.widgetset", "servlet.init.widgetset");
            props.put("servlet.init.widgetset", props.get("init.widgetset"));
        }

        // Auto-detect widgetset if not set manually
        if (props.get("servlet.init.widgetset") != null) {
            logger.debug("Widgetset configured to be used: {}", props.get("servlet.init.widgetset"));
        } else {
            // No widget set defined, try to auto-detect it
            final String widgetset = findWidgetset(reference.getBundle());
            if (widgetset != null) {
                logger.debug("Widgetset found: {}", widgetset);
                props.put("servlet.init.widgetset", widgetset);
            }
        }

        // Ensure uiClass is set
        final Class<? extends UI> uiClass = factory.getUIClass();
        if (uiClass == null) {
            throw new IllegalStateException("Cannot register ApplicationFactory as getUIClass() returned null");
        }

        // Set uiClass value for Vaadin Deployment Configuration to later on match the ui providers to the application class properly
        props.put("servlet.init.ui.class", uiClass.getCanonicalName());

        // Register ApplicationFactory
        logger.debug("Found factory for ui class {}, with the following headers {} and service properties {}.", uiClass, factory.getAdditionalHeaders(), props);
        m_serviceRegistration.put(factory, context.registerService(Servlet.class.getName(), servlet, props));

        // Add application factory so it can be used for vaadin ui creation
        uiProvider.addApplicationFactory(factory);
        return factory;
    }

    @Override
    public void modifiedService(ServiceReference reference, ApplicationFactory service) {
        //TODO: When does this get called
        super.modifiedService(reference, service);
    }

    @Override
    public void removedService(ServiceReference reference, ApplicationFactory service) {
        final ApplicationFactory factory = (ApplicationFactory) context.getService(reference);
        final ServiceRegistration servletRegistration = m_serviceRegistration.remove(factory);
        if (servletRegistration != null) {
            servletRegistration.unregister();
        }
        uiProvider.removeApplicationFactory(factory);
        super.removedService(reference, service);
    }
    
    private class FactoryServlet extends VaadinOSGiServlet {
        private static final long serialVersionUID = 7458986273769030388L;

        private ApplicationFactory m_factory;

        public FactoryServlet(UIProvider uiProvider, ApplicationFactory factory, BundleContext context) {
            super(uiProvider, context);
            m_factory = factory;
        }

        @Override
        protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            final Map<String,String> headers = m_factory.getAdditionalHeaders();
            if (headers.size() > 0) {
                for (final Map.Entry<String,String> entry : headers.entrySet()) {
                    response.addHeader(entry.getKey(), entry.getValue());
                }
            }
            super.service(request, response);
        }
    }

}
