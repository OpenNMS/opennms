package org.opennms.features.topology.app.internal;

import java.util.Dictionary;
import java.util.Properties;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;




/**
 * Extension of the default OSGi bundle activator
 */
public final class Activator
    implements BundleActivator
{
	
	private Bundle m_vaadin = null;
	private ServiceRegistration m_resourceRegistration;
	
    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc )
        throws Exception
    {
        System.out.println( "STARTING org.opennms.features.topology.app" );
        
        
        bc.addBundleListener(new BundleListener() {
			
			@Override
			public void bundleChanged(BundleEvent bundleEvent) {
				setupVaadinBundle(bundleEvent.getBundle(), bundleEvent.getType());
			}
		});
        
        for(Bundle bundle : bc.getBundles()) {
        	if (bundle.getState() == Bundle.ACTIVE) {
        		setupVaadinBundle(bundle, BundleEvent.STARTED);
        	}
        }


        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("alias", "/hello");
        // add specific service properties here...

        System.out.println( "REGISTER VaadinServlet" );

        // Register our example service implementation in the OSGi service registry
        bc.registerService( Servlet.class.getName(), new VaadinServlet(), props );
        
        
    }

	protected void setupVaadinBundle(Bundle bundle, int eventType) {
		if (m_vaadin == null && eventType == BundleEvent.STARTED) {
			if ("com.vaadin".equals(bundle.getSymbolicName())) {
				m_vaadin = bundle;
				System.out.println( "REGISTER /VAADIN resources" );
				DefaultResourceMapping vaadinResources = new DefaultResourceMapping();
				vaadinResources.setAlias("/VAADIN");
				vaadinResources.setPath("/VAADIN");

				System.err.println("vaadin bundlecontext" + m_vaadin.getBundleContext());

				m_resourceRegistration = m_vaadin.getBundleContext().registerService(ResourceMapping.class.getName(), vaadinResources, null );
			}
		}
		
		else if (m_vaadin != null && eventType == BundleEvent.STOPPED) {
			m_resourceRegistration.unregister();
			m_resourceRegistration = null;
			m_vaadin = null;
		}
		

	}

	/**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc )
        throws Exception
    {
        System.out.println( "STOPPING org.opennms.features.topology.app" );

        // no need to unregister our service - the OSGi framework handles it for us
    }
}

