package org.opennms.core.soa.osgi.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OsgiServiceRegistry implements ServiceRegistry {
	
	private BundleContext m_context;

	public OsgiServiceRegistry(BundleContext context) {
		m_context = context;
	}
	
	public <T> void addListener(Class<T> service, RegistrationListener<T> listener) {
		// TODO Auto-generated method stub
		
	}

	public <T> void addListener(Class<T> service, RegistrationListener<T> listener, boolean notifyForExistingProviders) {
		// TODO Auto-generated method stub
		
	}

	public <T> T findProvider(Class<T> serviceInterface) {
		ServiceReference reference = m_context.getServiceReference(serviceInterface.getName());
		Object service = m_context.getService(reference);
		return serviceInterface.cast(service);
	}

	public <T> T findProvider(Class<T> serviceInterface, String filter) {
		try {
			
			ServiceReference[] references = m_context.getServiceReferences(serviceInterface.getName(), filter);
			
			if(references != null && references.length > 0){ 
				
				Object service = m_context.getService( references[0] );
				return serviceInterface.cast( service );
				
			}
			
			return null;
			
		} catch (InvalidSyntaxException e) {
			throw new IllegalArgumentException("invalid filter", e); 
		}
	}

	public <T> Collection<T> findProviders(Class<T> serviceInterface) {
		try {
			
			ServiceReference[] references = m_context.getServiceReferences(serviceInterface.getName(), null);
			List<T> services = new ArrayList<T>(references.length);
			
			if(references != null && references.length > 0){ 
				
				for(ServiceReference serviceReference : references){
					services.add( serviceInterface.cast( m_context.getService( serviceReference ) ) );
				}
				return services;
			}
			
			return null;
			
		} catch (InvalidSyntaxException e) {
			throw new IllegalArgumentException("invalid filter", e); 
		}		
	}

	public <T> Collection<T> findProviders(Class<T> service, String filter) {
		
		return null;
	}

	public Registration register(Object serviceProvider, Class<?>... services) {
		return null;
	}

	public Registration register(Object serviceProvider, Map<String, String> properties, Class<?>... services) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> void removeListener(Class<T> service, RegistrationListener<T> listener) {
		// TODO Auto-generated method stub
		
	}

}
