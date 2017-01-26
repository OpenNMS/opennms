/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TopologySelectorOperation extends AbstractCheckedOperation {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final GraphProvider m_topologyProvider;
    private final Map<?,?> m_metaData;
	private final BundleContext m_bundleContext;

    public TopologySelectorOperation(BundleContext bundleContext, GraphProvider topologyProvider, Map<?,?> metaData) {
        m_topologyProvider = topologyProvider;
        m_metaData = metaData;
		m_bundleContext = bundleContext;
    }

    public String getLabel() {
        return m_metaData.get("label") == null ? "No Label for Topology Provider" : (String)m_metaData.get("label");
    }


    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        execute(operationContext.getGraphContainer());
    }

    private void execute(GraphContainer container) {
       	LOG.debug("Active provider is: {}", m_topologyProvider);

        // only change if provider changed
        if(!container.getBaseTopology().equals(m_topologyProvider)) {
			// We automatically set status providers if there are any
			StatusProvider vertexStatusProvider = findVertexStatusProvider(m_topologyProvider);
			EdgeStatusProvider edgeStatusProvider = findEdgeStatusProvider(m_topologyProvider);
			LayoutAlgorithm layoutAlgorithm = findLayoutAlgorithm();

            // Refresh the topology provider, triggering the vertices to load  if they have not yet loaded
            m_topologyProvider.refresh();
			container.setEdgeStatusProvider(edgeStatusProvider);
			container.setVertexStatusProvider(vertexStatusProvider);
			if (layoutAlgorithm != null) {
				container.setLayoutAlgorithm(layoutAlgorithm);
			}
            container.setBaseTopology(m_topologyProvider);
            container.clearCriteria(); // remove all criteria
            container.setSemanticZoomLevel(1); // reset to 1
            container.addCriteria(container.getBaseTopology().getDefaultCriteria());
            container.redoLayout();
        }
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

	private LayoutAlgorithm findLayoutAlgorithm() {
		Object preferredLayout = m_metaData.get("preferredLayout");
		if (preferredLayout != null) {
			// LayoutOperations are exposed as CheckedOperations
			CheckedOperation operation = findSingleService(m_bundleContext, CheckedOperation.class, null, String.format("(operation.label=%s*)", preferredLayout));
			if (operation instanceof HierarchyLayoutOperation) { // Cast it to HierarchyLayout if possible
				return ((HierarchyLayoutOperation) operation).getLayoutAlgorithm();
			}
		}
		return null; // no preferredLayout defined
	}

	private StatusProvider findVertexStatusProvider(GraphProvider graphProvider) {
		StatusProvider vertexStatusProvider = findSingleService(
				m_bundleContext,
				StatusProvider.class,
				statusProvider -> statusProvider.contributesTo(graphProvider.getVertexNamespace()),
				null);
		return vertexStatusProvider;
	}

	private EdgeStatusProvider findEdgeStatusProvider(GraphProvider graphProvider) {
		EdgeStatusProvider edgeStatusProvider = findSingleService(
				m_bundleContext,
				EdgeStatusProvider.class,
				statusProvider -> statusProvider.contributesTo(graphProvider.getEdgeNamespace()),
				null);
		return edgeStatusProvider;
	}

	/**
	 * Finds a service registered with the OSGI Service Registry of type <code>clazz</code>.
	 * If a <code>bundleContextFilter</code> is provided, it is used to query for the service, e.g. "(operation.label=My Label*)".
	 * In addition each clazz of type T found in the OSGI Service Registry must afterwards pass the provided <code>postFilter</code>.
	 *
	 * If multiple services are found, only the first one is returned.
	 *
     * @return A object of type <code>clazz</code> or null.
     */
	private <T> T findSingleService(BundleContext bundleContext, Class<T> clazz, Predicate<T> postFilter, String bundleContextFilter) {
		List<T> providers = findServices(bundleContext, clazz, bundleContextFilter);
		Stream<T> stream = providers.stream();
		if (postFilter != null) { // filter may be null
			stream = stream.filter(postFilter);
		}
		providers = stream.collect(Collectors.toList());
		if (providers.size() > 1) {
			LOG.warn("Found more than one {}s. This is not supported. Using 1st one in list.", clazz.getSimpleName());
		}
		if (!providers.isEmpty()) {
			return providers.iterator().next();
		}
		return null;
	}

	/**
	 * Find services of class <code>clazz</code> registered in the OSGI Service Registry.
	 * The optional filter criteria <code>query</code> is used.
	 *
	 * @return All found services registered in the OSGI Service Registry of type <code>clazz</code>.
     */
	private <T> List<T> findServices(BundleContext bundleContext, Class<T> clazz, String query) {
		List<T> serviceList = new ArrayList<>();
		LOG.debug("Finding Service of type {} and additional filter criteria {} ...", clazz, query);
		try {
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(clazz.getName(), query);
			for (ServiceReference eachServiceReference : allServiceReferences) {
				T statusProvider = (T) bundleContext.getService(eachServiceReference);
				serviceList.add(statusProvider);
			}
		} catch (InvalidSyntaxException e) {
			LOG.error("Could not query BundleContext for services", e);
		}
		LOG.debug("Found {} services", serviceList.size());
		return serviceList;
	}
}
