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
package org.opennms.features.vaadin.dashboard.config;

import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is responsible for holding the {@link DashletFactory} instances of the OSGI container.
 *
 * @author Christian Pape
 */
public class DashletSelector implements BundleActivator {
    /**
     * A {@link List} holding the {@link ServiceListChangedListener} references
     */
    List<ServiceListChangedListener> m_serviceListChangedListeners = new ArrayList<>();
    /**
     * A {@link Map} holding the {@link DashletFactory} instances
     */
    Map<String, DashletFactory> m_serviceInterfaceMap = new TreeMap<String, DashletFactory>();

    /**
     * Definition of the {@link ServiceListChangedListener} interface.
     */
    public interface ServiceListChangedListener {
        public void serviceListChanged(List<DashletFactory> factoryList);
    }

    /**
     * Start method of this registry.
     *
     * @param context the {@link BundleContext}
     * @throws Exception
     */
    public void start(final BundleContext context) throws Exception {
        // System.out.println("start() called");
    }

    /**
     * Stop method of this registry.
     *
     * @param context the {@link BundleContext}
     * @throws Exception
     */
    public void stop(final BundleContext context) throws Exception {
        // System.out.println("stop() called");
    }

    /**
     * This method adds a {@link DashletFactory} to the list of available dashlet factories.
     *
     * @param dashletFactory the new {@link DashletFactory}
     */
    public void bind(DashletFactory dashletFactory) {
        if (dashletFactory != null) {
            LoggerFactory.getLogger(DashletSelector.class).info("bind service " + dashletFactory.getClass().getName());

            m_serviceInterfaceMap.put(dashletFactory.getName(), dashletFactory);
            fireServiceListChangedListeners();
        } else {
            LoggerFactory.getLogger(DashletSelector.class).warn("service is null");
        }
    }

    /**
     * This method removes a {@link DashletFactory} from the list of available dashlet factories.
     *
     * @param dashletFactory the {@link DashletFactory} to be removed
     */
    public void unbind(DashletFactory dashletFactory) {
        if (dashletFactory != null) {
            LoggerFactory.getLogger(DashletSelector.class).info("unbind service " + dashletFactory.getClass().getName());

            m_serviceInterfaceMap.remove(dashletFactory.getName());
            fireServiceListChangedListeners();
        } else {
            LoggerFactory.getLogger(DashletSelector.class).warn("service is null");
        }
    }

    /**
     * Method for adding a {@link ServiceListChangedListener}.
     *
     * @param serviceListChangedListener the {@link ServiceListChangedListener} to add
     */
    public void addServiceListChangedListener(ServiceListChangedListener serviceListChangedListener) {
        m_serviceListChangedListeners.add(serviceListChangedListener);
    }

    /**
     * Method for removing a {@link ServiceListChangedListener}.
     *
     * @param serviceListChangedListener the {@link ServiceListChangedListener} to be removed
     */
    public void removeServiceListChangedListener(ServiceListChangedListener serviceListChangedListener) {
        m_serviceListChangedListeners.remove(serviceListChangedListener);
    }

    /**
     * This method will fire execute the {@link ServiceListChangedListener} listeners.
     */
    private void fireServiceListChangedListeners() {
        List<DashletFactory> factoryList = new ArrayList<>();
        factoryList.addAll(m_serviceInterfaceMap.values());

        for (ServiceListChangedListener serviceListChangedListener : m_serviceListChangedListeners) {
            serviceListChangedListener.serviceListChanged(factoryList);
        }
    }

    /**
     * Returns the list of {@link DashletFactory} instances known by this class.
     *
     * @return a {@link List} of {@link DashletFactory} instances
     */
    public List<DashletFactory> getDashletFactoryList() {
        List<DashletFactory> factoryList = new ArrayList<>();
        factoryList.addAll(m_serviceInterfaceMap.values());
        return factoryList;
    }

    /**
     * Returns the {@link DashletFactory} instance for a given nane.
     *
     * @param name the naem fo the {@link DashletFactory}
     * @return the {@link DashletFactory}
     */
    public DashletFactory getDashletFactoryForName(String name) {
        if (m_serviceInterfaceMap.containsKey(name)) {
            return m_serviceInterfaceMap.get(name);
        } else {
            return m_serviceInterfaceMap.get("Undefined");
        }
    }

}
