/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
