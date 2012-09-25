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

package org.opennms.features.topology.app.internal.operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class TopologySelector  {

	private TopologyProvider m_activeTopologyProvider;
	private BundleContext m_bundleContext;
	private Map<TopologyProvider, TopologySelectorOperation> m_operations = new HashMap<TopologyProvider, TopologySelector.TopologySelectorOperation>();
	private Map<TopologyProvider, ServiceRegistration> m_registrations = new HashMap<TopologyProvider, ServiceRegistration>();
	
    
    private class TopologySelectorOperation implements CheckedOperation {
    	
    	private TopologyProvider m_topologyProvider;
    	private Map m_metaData;

    	public TopologySelectorOperation(TopologyProvider topologyProvider, Map metaData) {
    		m_topologyProvider = topologyProvider;
    		m_metaData = metaData;
		}
    	
    	public String getLabel() {
    		return m_metaData.get("label") == null ? "No Label for Topology Provider" : (String)m_metaData.get("label");
    	}
    	

    	@Override
    	public Undoer execute(List<Object> targets, OperationContext operationContext) {
    		operationContext.getGraphContainer().setDataSource(m_topologyProvider);
    		return null;
    	}

    	@Override
    	public boolean display(List<Object> targets, OperationContext operationContext) {
    		return true;
    	}

    	@Override
    	public boolean enabled(List<Object> targets, OperationContext operationContext) {
    		return true;
    	}

    	@Override
    	public String getId() {
    		return getLabel();
    	}

		@Override
		public boolean isChecked(List<Object> targets,	OperationContext operationContext) {
			TopologyProvider activeTopologyProvider = operationContext.getGraphContainer().getDataSource();
			System.err.println("Active Provider is " + activeTopologyProvider + ": Expected " + m_topologyProvider);
			return m_topologyProvider.equals(activeTopologyProvider);
		}
    }
    
    
	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}
	
    public void addTopologyProvider(TopologyProvider topologyProvider, Map metaData) {
    	
    	System.err.println("Adding Topology Provider " + topologyProvider);
    	
    	TopologySelectorOperation operation = new TopologySelectorOperation(topologyProvider, metaData);
    	
    	m_operations.put(topologyProvider, operation);
    	
    	Properties properties = new Properties();
        properties.put("operation.menuLocation", "View|Topology");
        properties.put("operation.label", operation.getLabel());
    	
    	ServiceRegistration reg = m_bundleContext.registerService(CheckedOperation.class.getName(), operation, properties);
    	
    	m_registrations.put(topologyProvider, reg);
    }
    
    public void  removeTopologyProvider(TopologyProvider topologyProvider, Map metaData) {
    	
    	System.err.println("Removing Topology Provider" + topologyProvider);

    	m_operations.remove(topologyProvider);
    	ServiceRegistration reg = m_registrations.remove(topologyProvider);
    	if (reg != null) {
    		reg.unregister();
    	}
    	
    }
}
