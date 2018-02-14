package org.opennms.vaadin.extender.internal.extender;

import org.opennms.vaadin.extender.ApplicationFactory;
import org.opennms.vaadin.extender.Constants;
import org.opennms.vaadin.extender.internal.servlet.VaadinOSGiServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ApplicationFactoryServiceTracker extends ServiceTracker {
    
    private Map<ApplicationFactory, ServiceRegistration> m_serviceRegistration = new HashMap<ApplicationFactory, ServiceRegistration>();
    private final Logger logger = LoggerFactory.getLogger(ApplicationFactoryServiceTracker.class.getName());
    
    public ApplicationFactoryServiceTracker(BundleContext context) {
        super(context, ApplicationFactory.class.getName(), null);
    }
    
    @SuppressWarnings({"unchecked"})
    @Override
    public Object addingService(ServiceReference reference) {
        ApplicationFactory factory = (ApplicationFactory) super.addingService(reference);
        if (factory == null) return null;
        FactoryServlet servlet = new FactoryServlet(factory, reference.getBundle().getBundleContext());
        Dictionary props = new Properties();
        
        for(String key : reference.getPropertyKeys()) {
            props.put(key, reference.getProperty(key));
        }
        
        if(props.get(Constants.ALIAS) == null) {
            logger.warn("You have not set the alias property for ApplicationFactory: " + factory);
        }
        m_serviceRegistration.put(factory, context.registerService(Servlet.class.getName(), servlet, props));
        
        return factory;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        //TODO: When does this get called
        super.modifiedService(reference, service);
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        
        ApplicationFactory factory = (ApplicationFactory) context.getService(reference);
        final ServiceRegistration servletRegistration = m_serviceRegistration.remove(factory);
        if (servletRegistration != null) {
            servletRegistration.unregister();
        }

        super.removedService(reference, service);
    }
    
    private class FactoryServlet extends VaadinOSGiServlet {
        private static final long serialVersionUID = 7458986273769030388L;

        private ApplicationFactory m_factory;

        public FactoryServlet(ApplicationFactory factory, BundleContext context) {
            super(factory, context);
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
