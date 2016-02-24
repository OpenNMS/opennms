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

package org.opennms.features.topology.app.internal.operations;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

public class TopologySelector {

	private BundleContext m_bundleContext;
	private final Map<GraphProvider, TopologySelectorOperation> m_operations = new HashMap<>();
	private final Map<GraphProvider, ServiceRegistration<CheckedOperation>> m_registrations = new HashMap<>();

	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}
	
	public synchronized void addGraphProvider(GraphProvider topologyProvider, Map<?,?> metaData) {
		try {
        	LoggerFactory.getLogger(getClass()).debug("Adding graph provider: " + topologyProvider);
        	
            TopologySelectorOperation operation = new TopologySelectorOperation(m_bundleContext, topologyProvider, metaData);
        	
        	m_operations.put(topologyProvider, operation);
        	
        	Dictionary<String,String> properties = new Hashtable<String,String>();
            properties.put("operation.menuLocation", "View");
            properties.put("operation.label", operation.getLabel()+"?group=topology");
        	
        	ServiceRegistration<CheckedOperation> reg = m_bundleContext.registerService(CheckedOperation.class, operation, properties);
        	
        	m_registrations.put(topologyProvider, reg);
		} catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during addGraphProvider()", e);
		}
    }
    
	public synchronized void removeGraphProvider(GraphProvider topologyProvider, Map<?,?> metaData) {
    	try {
        	LoggerFactory.getLogger(getClass()).debug("Removing graph provider: {}", topologyProvider);
        	
        	m_operations.remove(topologyProvider);
        	ServiceRegistration<CheckedOperation> reg = m_registrations.remove(topologyProvider);
        	if (reg != null) {
        		reg.unregister();
        	}
		} catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during removeGraphProvider()", e);
		}
    }
}
