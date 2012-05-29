package org.opennms.features.topology.plugins.menu.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.ops4j.pax.vaadin.ApplicationFactory;
import org.osgi.framework.ServiceReference;

import com.vaadin.Application;

public class DefaultMenuFactory implements ApplicationFactory {

	@Override
	public Application createApplication(HttpServletRequest request) throws ServletException {
		DefaultMenu menu = new DefaultMenu();
		return menu;
	}

	@Override
	public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		return DefaultMenu.class;
	}
	
    public void onBind(ServiceReference reference) {
        
    }
    
    public void onUnBind(ServiceReference reference) {
        
    }
}
