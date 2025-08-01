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
package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.EventProxyAware;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextAware;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintContainer;

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

public class BlueprintIViewContribution implements IViewContribution {
    private final BlueprintContainer m_container;
    private final String m_beanId;
    private String m_title;

    public BlueprintIViewContribution(final BlueprintContainer container, final String beanId) {
        m_container = container;
        m_beanId = beanId;
    }

    @Override
    public Component getView(final VaadinApplicationContext vaadinApplicationContext, final WidgetContext widgetContext) {
        // Get the component by asking the blueprint container to instantiate a prototype bean
        final Component component = (Component)m_container.getComponentInstance(m_beanId);
        final BundleContext bundleContext = (BundleContext) m_container.getComponentInstance("blueprintBundleContext");
        final EventProxy eventProxy = vaadinApplicationContext.getEventProxy(bundleContext);
        eventProxy.addPossibleEventConsumer(component);

        injectEventProxy(component, eventProxy);
        injectVaadinApplicationContext(component, vaadinApplicationContext);

        return component;
    }

    private void injectEventProxy(final Component component, final EventProxy eventProxy) {
        if(component instanceof EventProxyAware){
            ((EventProxyAware)component).setEventProxy(eventProxy);
        }
    }

    private void injectVaadinApplicationContext(final Component component, final VaadinApplicationContext vaadinApplicationContext) {
        if (component instanceof VaadinApplicationContextAware) {
            ((VaadinApplicationContextAware)component).setVaadinApplicationContext(vaadinApplicationContext);
        }
    }

    @Override
    public Resource getIcon() {
        return null;
    }

    @Override
    public String getTitle() {
        return m_title;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    @Override
    public String toString() {
        return "BlueprintIViewContribution [container=" + m_container + ", beanId=" + m_beanId + ", title=" + m_title + "]";
    }
}
