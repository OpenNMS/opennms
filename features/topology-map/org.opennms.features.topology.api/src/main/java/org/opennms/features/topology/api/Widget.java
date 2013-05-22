/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc. OpenNMS(R) is a
 * registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version. OpenNMS(R) is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/ For more information
 * contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.topology.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.WidgetUpdateListener.WidgetEventType;
import org.opennms.features.topology.api.WidgetUpdateListener.WidgetUpdateEvent;
import org.opennms.features.topology.api.support.BlueprintIViewContribution;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;

public abstract class Widget extends BlueprintIViewContribution {

    public static class ViewData {
        private final String title;
        private final Resource icon;
        
        protected ViewData(IViewContribution viewContribution) {
            this.title = viewContribution.getTitle();
            this.icon = viewContribution.getIcon();
        }

        public Resource getIcon() {
           return icon;
        }
       
        public String getTitle() {
           return title;
        }
    }
    
    private Map<String, IViewContribution> m_viewContributors = new HashMap<String, IViewContribution>();
    private Map<String, Component> m_components = new HashMap<String, Component>();
    private WidgetContext m_widgetContext;
    
    public Widget(BlueprintContainer container, String beanName) {
        super(container, beanName);
    }
    
    public void setWidgetContext(WidgetContext widgetContext) {
        m_widgetContext = widgetContext;
    }
    
    protected WidgetContext getWidgetContext() {
        return m_widgetContext;
    }

    private List<WidgetUpdateListener> m_listeners = new ArrayList<WidgetUpdateListener>();

    public synchronized void onBind(IViewContribution viewContribution) {
        if (viewContribution == null) return;
        LoggerFactory.getLogger(this.getClass()).info("Binding IViewContribution {} to Widget {}", viewContribution, this);
        final String id = viewContribution.getId();
        final Component component = viewContribution.getView(m_widgetContext);
        synchronized (m_viewContributors) {
            m_viewContributors.put(id, viewContribution);
        }
        synchronized(m_components) {
            m_components.put(id, component);
        }
        
        try {
            final WidgetUpdateEvent event = new WidgetUpdateEvent(this, WidgetEventType.BIND, component, new ViewData(viewContribution));
            updateWidget(event);
            notifyWidgetListeners(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
        }
    }

    public synchronized void onUnbind(IViewContribution viewContribution) {
        if (viewContribution == null) return;
        LoggerFactory.getLogger(this.getClass()).info("Unbinding IViewContribution {} from Widget {}", viewContribution, this);
        final String id = viewContribution.getId();
        final Component component = m_components.get(id);
        synchronized (m_viewContributors) {
            m_viewContributors.remove(id);
        }
        synchronized (m_components) {
            m_components.remove(id);
        }
        
        try {
            final WidgetUpdateEvent event = new WidgetUpdateEvent(this, WidgetEventType.UNBIND, component, new ViewData(viewContribution));
            updateWidget(event);
            notifyWidgetListeners(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
        }
    }
    
    private void notifyWidgetListeners(WidgetUpdateEvent event) {
        for (WidgetUpdateListener listener : m_listeners)
            listener.widgetContentUpdated(event);
    }

    public boolean isEmpty() {
        return m_viewContributors.isEmpty();
    }
    
    @Override
    public String toString() {
        return getId();
    }

    public void addUpdateListener(WidgetUpdateListener widgetListener) {
        m_listeners.add(widgetListener);
    }
    
    public void removeUpdateListener(WidgetUpdateListener widgetListener) {
        m_listeners.remove(widgetListener);
    }

    protected abstract void updateWidget(final WidgetUpdateEvent updateEvent);
    
}
