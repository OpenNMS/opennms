package org.opennms.features.topology.app.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.ops4j.pax.vaadin.ApplicationFactory;
import org.osgi.framework.ServiceReference;

import com.vaadin.Application;

public class HelloWorldFactory implements ApplicationFactory {
    
    
    
    @Override
    public Application createApplication(HttpServletRequest request) throws ServletException {
        HelloWorld app = new HelloWorld();
        
        return app;
    }

    @Override
    public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
        return HelloWorld.class;
    }
    
    public void onBind(ServiceReference reference) {
        
    }
    
    public void onUnBind(ServiceReference reference) {
        
    }

}
