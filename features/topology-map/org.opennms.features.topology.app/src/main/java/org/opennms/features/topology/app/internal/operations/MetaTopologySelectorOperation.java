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
package org.opennms.features.topology.app.internal.operations;

import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.Callbacks;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

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
		execute(container, Callbacks.applyDefaults());
	}

	/**
	 * Changes the base topology to {@link #m_metaTopologyProvider} and optionally executes callbacks (e.g. to reset criteria, set default SZL, etc.)
	 * @param container The GraphContainer.
	 * @param callbacks Define callbacks to execute AFTER the topology provider has changed.
     */
	private void execute(GraphContainer container, GraphContainer.Callback... callbacks) {
	    LOG.debug("Active provider is: {}", m_metaTopologyProvider);

        // only change if provider changed
	    final String currentMetaTopologyId = container.getMetaTopologyId();
        if(currentMetaTopologyId == null || !currentMetaTopologyId.equals(m_metaTopologyProvider.getId())) {
            container.setMetaTopologyId(m_metaTopologyProvider.getId());
			container.selectTopologyProvider(m_metaTopologyProvider.getDefaultGraphProvider(), callbacks);
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
        final String activeMetaTopologyProviderId = container.getMetaTopologyId();
        return m_metaTopologyProvider.getId().equals(activeMetaTopologyProviderId);
    }

    @Override
    public Map<String, String> createHistory(GraphContainer container) {
        return ImmutableMap
                .<String, String>builder()
                .put(getClass().getName() + "." + getLabel(), Boolean.toString(isChecked(container)))
                .put(getClass().getName() + ".selectedLayer", container.getTopologyServiceClient().getNamespace())
                .build();
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        // If the class name and label tuple is set to true, then set the base topology provider
        if ("true".equals(settings.get(getClass().getName() + "." + getLabel()))) {
            execute(container);
        }

        // Select the according layer
        final String selectedLayer = settings.get(getClass().getName() + ".selectedLayer");
        if (container.getMetaTopologyId() != null
                && !Strings.isNullOrEmpty(selectedLayer)
                && !selectedLayer.equals(container.getTopologyServiceClient().getNamespace())) {
            // Find provider for selected layer and select
            container.getTopologyServiceClient().getGraphProviders().stream()
                    .filter(p -> p.getNamespace().equals(selectedLayer))
                    .findFirst()
                    .ifPresent(container::selectTopologyProvider);
        }
    }
}
