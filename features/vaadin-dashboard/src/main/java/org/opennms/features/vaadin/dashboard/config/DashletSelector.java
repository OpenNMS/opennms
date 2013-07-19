/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
    List<ServiceListChangedListener> m_serviceListChangedListeners = new ArrayList<ServiceListChangedListener>();
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
            LoggerFactory.getLogger(DashletSelector.class).warn("bind service " + dashletFactory.getClass().getName());

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
            LoggerFactory.getLogger(DashletSelector.class).warn("unbind service " + dashletFactory.getClass().getName());

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
        List<DashletFactory> factoryList = new ArrayList<DashletFactory>();
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
        List<DashletFactory> factoryList = new ArrayList<DashletFactory>();
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
