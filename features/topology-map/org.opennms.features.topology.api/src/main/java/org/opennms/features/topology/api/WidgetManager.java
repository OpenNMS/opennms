/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens for {@link IViewContribution} service registrations.
 */
public class WidgetManager {
    private static final Logger LOG = LoggerFactory.getLogger(WidgetManager.class);

    private List<IViewContribution> m_viewContributors = new CopyOnWriteArrayList<>();
    private List<WidgetUpdateListener> m_listeners = new CopyOnWriteArrayList<>();
    
    private Comparator<IViewContribution> TITLE_COMPARATOR = new Comparator<IViewContribution>() {
        @Override
        public int compare(final IViewContribution o1, final IViewContribution o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };

    public WidgetManager() {}
    
    public void addUpdateListener(final WidgetUpdateListener listener) {
        LOG.debug("Adding WidgetUpdateListener {} to WidgetManager {}", listener, this);
        synchronized (m_listeners) {
            m_listeners.add(listener);
        }
    }

    public void removeUpdateListener(final WidgetUpdateListener listener) {
        LOG.debug("Removing WidgetUpdateListener {} from WidgetManager {}", listener, this);
        synchronized (m_listeners) {
            m_listeners.remove(listener);
        }
    }

    public int widgetCount() {
        return m_viewContributors.size();
    }
    
    /**
     * Gets the list of Widgets as IViewContributions
     * 
     * @return List<IViewContribution>
     */
    public List<IViewContribution> getWidgets(){
        final List<IViewContribution> widgets = new ArrayList<>();
        widgets.addAll(m_viewContributors);
        // Sort the widgets by their title
        Collections.sort(widgets, TITLE_COMPARATOR);
        return Collections.unmodifiableList(widgets);
    }
    
    public synchronized void onBind(IViewContribution viewContribution) {
        LOG.info("Binding IViewContribution {} to WidgetManager {}", viewContribution, this);
        synchronized (m_viewContributors) {
            try {
                m_viewContributors.add(viewContribution);
                updateWidgetListeners();
            } catch (final Throwable e) {
                LOG.warn("Exception during onBind()", e);
            }
        }
    }

    private void updateWidgetListeners() {
        for(final WidgetUpdateListener listener : m_listeners) {
            listener.widgetListUpdated(this);
        }
    }
    
    public synchronized void onUnbind(final IViewContribution viewContribution) {
        LOG.info("Unbinding IViewContribution {} from WidgetManager {}", viewContribution, this);
        synchronized (m_viewContributors) {
            try {
                m_viewContributors.remove(viewContribution);
                updateWidgetListeners();
            } catch (final Throwable e) {
                LOG.warn("Exception during onUnbind()", e);
            }
        }
    }
}
