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

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

public class TopologySelector {

	private BundleContext m_bundleContext;
	private final Map<GraphProvider, TopologySelectorOperation> m_operations = new HashMap<GraphProvider, TopologySelector.TopologySelectorOperation>();
	private final Map<GraphProvider, ServiceRegistration<CheckedOperation>> m_registrations = new HashMap<GraphProvider, ServiceRegistration<CheckedOperation>>();
	
    
    private class TopologySelectorOperation extends AbstractCheckedOperation {
    	
    	private GraphProvider m_topologyProvider;
    	private Map<?,?> m_metaData;

    	public TopologySelectorOperation(GraphProvider topologyProvider, Map<?,?> metaData) {
    		m_topologyProvider = topologyProvider;
    		m_metaData = metaData;
		}
    	
    	public String getLabel() {
    		return m_metaData.get("label") == null ? "No Label for Topology Provider" : (String)m_metaData.get("label");
    	}
    	

    	@Override
    	public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
    		execute(operationContext.getGraphContainer());
    		return null;
    	}
    	
    	private void execute(GraphContainer container) {
    		LoggerFactory.getLogger(getClass()).debug("Active provider is: {}", m_topologyProvider);
    		boolean redoLayout = true;
    		if(container.getBaseTopology() == m_topologyProvider) {
    		    redoLayout = false;
    		}
    		container.setBaseTopology(m_topologyProvider);
    		
    		if(redoLayout) { container.redoLayout(); }
    	}

    	@Override
    	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
    		return true;
    	}

    	@Override
    	public String getId() {
    		return getLabel();
    	}

        @Override
        protected boolean isChecked(GraphContainer container) {
			GraphProvider activeGraphProvider = container.getBaseTopology();
			return m_topologyProvider.equals(activeGraphProvider);
		}

        @Override
        public Map<String, String> createHistory(GraphContainer container) {
        	return Collections.singletonMap(this.getClass().getName() + "." + getLabel(), Boolean.toString(isChecked(container)));
        }

        @Override
        public void applyHistory(GraphContainer container, Map<String, String> settings) {
        	// If the class name and label tuple is set to true, then set the base topology provider
        	if ("true".equals(settings.get(this.getClass().getName() + "." + getLabel()))) {
        		execute(container);
        	}
        }
    }


	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}
	
	public synchronized void addGraphProvider(GraphProvider topologyProvider, Map<?,?> metaData) {
		try {
        	LoggerFactory.getLogger(getClass()).debug("Adding graph provider: " + topologyProvider);
        	
        	TopologySelectorOperation operation = new TopologySelectorOperation(topologyProvider, metaData);
        	
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
