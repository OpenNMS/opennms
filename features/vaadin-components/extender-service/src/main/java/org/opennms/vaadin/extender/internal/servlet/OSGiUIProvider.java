package org.opennms.vaadin.extender.internal.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.vaadin.extender.ApplicationFactory;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.UIProviderEvent;
import com.vaadin.ui.UI;

//TODO MVR
// responsible to create the UI.
// Original this was for each servlet, but now is for all servlets
public class OSGiUIProvider extends UIProvider {

    // cannonical class name -> application factory
    private Map<String, ApplicationFactory> factories = new HashMap<>();

    public OSGiUIProvider() {

    }

    public OSGiUIProvider(ApplicationFactory applicationFactory) {
        addApplicationFactory(applicationFactory);
    }

    @Override
    public synchronized Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        final String uiClassName = extractUIClassName(event);
        if (uiClassName != null) {
            final ApplicationFactory applicationFactory = factories.get(uiClassName);
            if (applicationFactory != null) {
                return applicationFactory.getUIClass();
            }
        }
        return null;
    }
    
    @Override
    public synchronized UI createInstance(final UICreateEvent event) {
        final String uiClassName = extractUIClassName(event);
        if (uiClassName != null) {
            return factories.get(uiClassName).createUI();
        }
        return null;
    }

    public synchronized void addApplicationFactory(ApplicationFactory applicationFactory) {
        Objects.requireNonNull(applicationFactory);
        Objects.requireNonNull(applicationFactory.getUIClass());
        final Class<? extends UI> uiClass = applicationFactory.getUIClass();
        factories.put(uiClass.getCanonicalName(), applicationFactory);
    }

    public synchronized void removeApplicationFactory(ApplicationFactory applicationFactory) {
        Objects.requireNonNull(applicationFactory);
        Objects.requireNonNull(applicationFactory.getUIClass());
        factories.remove(applicationFactory.getUIClass().getCanonicalName());
    }

    private String extractUIClassName(UIProviderEvent event) {
        return event.getService().getDeploymentConfiguration().getInitParameters().getProperty("ui.class");
    }
}
