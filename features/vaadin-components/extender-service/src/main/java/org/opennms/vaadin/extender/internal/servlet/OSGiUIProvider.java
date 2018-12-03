package org.opennms.vaadin.extender.internal.servlet;

import org.opennms.vaadin.extender.ApplicationFactory;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class OSGiUIProvider extends UIProvider {
    
    private ApplicationFactory m_uiFactory;
    
    public OSGiUIProvider(ApplicationFactory appFactory) {
        m_uiFactory = appFactory;
    }
    
    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
       return m_uiFactory.getUIClass();
    }
    
    @Override
    public UI createInstance(final UICreateEvent e) {
        return m_uiFactory.createUI();
    }
}
