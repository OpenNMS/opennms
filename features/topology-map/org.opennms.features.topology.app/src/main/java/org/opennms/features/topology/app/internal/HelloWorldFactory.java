package org.opennms.features.topology.app.internal;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.api.IViewContribution;
import org.ops4j.pax.vaadin.ApplicationFactory;

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
    
    public void onBind(IViewContribution button, Map<String, Object> properties) {
        
    }
    
    public void onUnBind(IViewContribution button, Map<String, Object> properties) {
        
    }

}
