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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaTopologySelectorOperation extends AbstractCheckedOperation {

	private static final Logger LOG = LoggerFactory.getLogger(MetaTopologySelectorOperation.class);

	private final MetaTopologyProvider m_metaTopologyProvider;
	private final String m_label;

    public MetaTopologySelectorOperation(MetaTopologyProvider metaTopologyProvider, Map<?,?> metadata) {
		this(metaTopologyProvider,
			 metadata.get("label") == null ? "No Label for Meta Topology Provider" : (String) metadata.get("label"));
    }

	private MetaTopologySelectorOperation(MetaTopologyProvider metaTopologyProvider, String label) {
	    m_metaTopologyProvider = metaTopologyProvider;
		m_label = label;
	}

    public String getLabel() {
        return m_label;
    }

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        execute(operationContext.getGraphContainer());
    }

    public void execute(GraphContainer container) {
		execute(container, true);
	}

	/**
	 * Changes the base topology to {@link #m_metaTopologyProvider} and optionally resets all criteria and sets the szl to 1.
	 * @param container The GraphContainer.
	 * @param resetCriteriaAndSzl Defines if the criteria and szl is reset.
     */
	private void execute(GraphContainer container, boolean resetCriteriaAndSzl) {
	    LOG.debug("Active provider is: {}", m_metaTopologyProvider);

        // only change if provider changed
	    final MetaTopologyProvider currentMetaTopologyProvider = container.getMetaTopologyProvider();
        if(currentMetaTopologyProvider == null || !currentMetaTopologyProvider.equals(m_metaTopologyProvider)) {
            container.setMetaTopologyProvider(m_metaTopologyProvider);
			container.selectTopologyProvider(m_metaTopologyProvider.getDefaultGraphProvider(), resetCriteriaAndSzl);
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
        final MetaTopologyProvider activeMetaTopologyProvider = container.getMetaTopologyProvider();
        return m_metaTopologyProvider.equals(activeMetaTopologyProvider);
    }

    @Override
    public Map<String, String> createHistory(GraphContainer container) {
        return Collections.singletonMap(this.getClass().getName() + "." + getLabel(), Boolean.toString(isChecked(container)));
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        // If the class name and label tuple is set to true, then set the base topology provider
        if ("true".equals(settings.get(this.getClass().getName() + "." + getLabel()))) {
            execute(container, false);
        }
    }

	public static MetaTopologySelectorOperation createOperationForDefaultGraphProvider(BundleContext bundleContext, String filterCriteria) {
		try {
			Collection<ServiceReference<MetaTopologyProvider>> serviceReferences = bundleContext.getServiceReferences(MetaTopologyProvider.class, filterCriteria);
			if (!serviceReferences.isEmpty()) {
				ServiceReference<?> reference = serviceReferences.iterator().next();
				return new MetaTopologySelectorOperation(
						(MetaTopologyProvider) bundleContext.getService(reference),
						(String) reference.getProperty("label"));
			}
		} catch (InvalidSyntaxException e) {
			LOG.error("Could not query BundleContext for services", e);
		}
		return null;
	}
}
