/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.LoggerFactory;

/**
 * This class listens for {@link IViewContribution} service registrations.
 */
public class WidgetManager implements WidgetUpdateListener {
    
    private Map<String, Widget> m_widgets = new HashMap<String, Widget>();
    private Map<String, List<WidgetUpdateListener>> m_listeners = new HashMap<String, List<WidgetUpdateListener>>();
    
    
    public void addUpdateListener(String widgetId, WidgetUpdateListener listener) {
        LoggerFactory.getLogger(this.getClass()).info("Adding WidgetUpdateListener {} to WidgetManager {} for Widget {}", listener, this, widgetId);
        synchronized (m_listeners) {
            if (m_listeners.get(widgetId) == null) m_listeners.put(widgetId, new CopyOnWriteArrayList<WidgetUpdateListener>());
            m_listeners.get(widgetId).add(listener);
        }
    }

    public void removeUpdateListener(String widgetId, WidgetUpdateListener listener) {
        LoggerFactory.getLogger(this.getClass()).info("Removing WidgetUpdateListener {} from WidgetManager {} for Widget {}", listener, this, widgetId);
        synchronized (m_listeners) {
            m_listeners.get(widgetId).remove(listener);
            m_listeners.remove(listener);
        }
    }
    
//
//    public int widgetCount() {
//        return m_viewContributors.size();
//    }
//    
//    /**
//     * Gets the list of Widgets as IViewContributions
//     * 
//     * @return List<IViewContribution>
//     */
//    public List<IViewContribution> getWidgets(){
//        List<IViewContribution> widgets = new ArrayList<IViewContribution>();
//        widgets.addAll(m_viewContributors);
//        // Sort the widgets by their title
//        Collections.sort(widgets, new Comparator<IViewContribution>() {
//            @Override
//            public int compare(IViewContribution o1, IViewContribution o2) {
//                return o1.getTitle().compareTo(o2.getTitle());
//            }
//        });
//        return Collections.unmodifiableList(widgets);
//    }
//    
    public synchronized void onBind(Widget widget) {
        if (widget == null) return;
        LoggerFactory.getLogger(this.getClass()).info("Binding Widget {} to WidgetManager {}", widget, this);
        synchronized (m_widgets) {
            try {
                m_widgets.put(widget.getId(), widget);
                widget.addUpdateListener(this);
            } catch (Throwable e) {
                LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
            }
        }
    }
    
    public synchronized void onUnbind(Widget widget) {
        if (widget == null) return;
        LoggerFactory.getLogger(this.getClass()).info("Unbinding Widget {} to WidgetManager {}", widget, this);
        synchronized (m_widgets) {
            try {
                m_widgets.remove(widget.getId());
                widget.removeUpdateListener(this);
            } catch (Throwable e) {
                LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
            }
        }
    }

    private void updateWidgetListeners(final WidgetUpdateEvent updateEvent) {
        if (m_listeners.get(updateEvent.getSource().getId()) == null) return;
        for(WidgetUpdateListener listener : m_listeners.get(updateEvent.getSource().getId())) {
            listener.widgetContentUpdated(updateEvent);
        }
    }
    @Override
    public void widgetContentUpdated(WidgetUpdateEvent e) {
        updateWidgetListeners(e);
    }
}
