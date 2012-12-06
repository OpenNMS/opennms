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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

public class TopologySelector {

	private BundleContext m_bundleContext;
	private final Map<TopologyProvider, TopologySelectorOperation> m_operations = new HashMap<TopologyProvider, TopologySelector.TopologySelectorOperation>();
	private final Map<TopologyProvider, ServiceRegistration<CheckedOperation>> m_registrations = new HashMap<TopologyProvider, ServiceRegistration<CheckedOperation>>();
	
    
    private class TopologySelectorOperation implements CheckedOperation {
    	
    	private TopologyProvider m_topologyProvider;
    	private Map<?,?> m_metaData;

    	public TopologySelectorOperation(TopologyProvider topologyProvider, Map<?,?> metaData) {
    		m_topologyProvider = topologyProvider;
    		m_metaData = metaData;
		}
    	
    	public String getLabel() {
    		return m_metaData.get("label") == null ? "No Label for Topology Provider" : (String)m_metaData.get("label");
    	}
    	

    	@Override
    	public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
    		LoggerFactory.getLogger(getClass()).debug("Active provider is: {}" + m_topologyProvider);
    		operationContext.getGraphContainer().setDataSource(m_topologyProvider);
    		operationContext.getGraphContainer().redoLayout();
    		return null;
    	}

    	@Override
    	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
    		return true;
    	}

    	@Override
    	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
    		return true;
    	}

    	@Override
    	public String getId() {
    		return getLabel();
    	}

		@Override
		public boolean isChecked(List<VertexRef> targets,	OperationContext operationContext) {
			TopologyProvider activeTopologyProvider = operationContext.getGraphContainer().getDataSource();
			return m_topologyProvider.equals(activeTopologyProvider);
		}
    }
    
    
	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}
	
	public void addTopologyProvider(TopologyProvider topologyProvider, Map<?,?> metaData) {
    	
    	LoggerFactory.getLogger(getClass()).debug("Adding topology provider: " + topologyProvider);
    	
    	TopologySelectorOperation operation = new TopologySelectorOperation(topologyProvider, metaData);
    	
    	m_operations.put(topologyProvider, operation);
    	
    	Dictionary<String,String> properties = new Hashtable<String,String>();
        properties.put("operation.menuLocation", "View");
        properties.put("operation.label", operation.getLabel()+"?group=topology");
    	
    	ServiceRegistration<CheckedOperation> reg = m_bundleContext.registerService(CheckedOperation.class, operation, properties);
    	
    	m_registrations.put(topologyProvider, reg);
    }
    
	public void removeTopologyProvider(TopologyProvider topologyProvider, Map<?,?> metaData) {
    	
    	LoggerFactory.getLogger(getClass()).debug("Removing topology provider: " + topologyProvider);
    	
    	m_operations.remove(topologyProvider);
    	ServiceRegistration<CheckedOperation> reg = m_registrations.remove(topologyProvider);
    	if (reg != null) {
    		reg.unregister();
    	}
    	
    }
}
