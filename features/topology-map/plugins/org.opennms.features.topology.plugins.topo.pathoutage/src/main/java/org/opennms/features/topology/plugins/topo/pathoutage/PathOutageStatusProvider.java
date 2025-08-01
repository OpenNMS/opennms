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
package org.opennms.features.topology.plugins.topo.pathoutage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Lists;

/**
 * This provider works together with {@link PathOutageProvider} to provide us with status information about the visible vertices
 */
public class PathOutageStatusProvider implements StatusProvider {

	private final GenericPersistenceAccessor persistenceAccessor;

	public PathOutageStatusProvider(GenericPersistenceAccessor genericPersistenceAccessor) {
		this.persistenceAccessor = Objects.requireNonNull(genericPersistenceAccessor);
	}

	@Override
	public Map<VertexRef, Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
		final List<Integer> nodeIds = vertices.stream().filter(v -> v.getNamespace().equals(getNamespace()))
													   .map(v -> (PathOutageVertex)v)
													   .map(v -> v.getNodeID())
													   .collect(Collectors.toList());
		if (nodeIds.isEmpty()) {
			return new HashMap<>();
		}

		// Preparing database request
		final StringBuilder hql = new StringBuilder();
		hql.append("SELECT node.id, max(event.eventSeverity) ");
		hql.append("FROM OnmsOutage as outage ");
		hql.append("LEFT JOIN outage.monitoredService as ifservice ");
		hql.append("LEFT JOIN ifservice.ipInterface as ipinterface ");
		hql.append("LEFT JOIN ipinterface.node as node ");
		hql.append("LEFT JOIN outage.serviceLostEvent as event ");
		hql.append("WHERE node.id in (:nodeIds) ");
		hql.append("AND outage.perspective is null ");
		hql.append("AND outage.serviceRegainedEvent is null ");
		hql.append("GROUP BY node.id");

		final List<String> paramNames = Lists.newArrayList("nodeIds");
		final List<Object> paramValues = new ArrayList();
		paramValues.add(Lists.newArrayList(nodeIds));
		final List<Object[]> retvals = this.persistenceAccessor.findUsingNamedParameters(hql.toString(),
																						 paramNames.toArray(new String[paramNames.size()]),
																						 paramValues.toArray());
		// Generating alarms map
		final Map<Integer, OnmsSeverity> mappedAlarms = new HashedMap();
		for (int i = 0; i < retvals.size(); i++) {
			final Integer nodeId = (Integer) retvals.get(i)[0];
			final Integer severity = Optional.ofNullable((Integer) retvals.get(i)[1]).orElse(OnmsSeverity.NORMAL.ordinal());
			mappedAlarms.put(nodeId, OnmsSeverity.get(severity));
		}
		final Map<VertexRef, Status> status = vertices.stream()
				.map(v -> (PathOutageVertex)v)
				.collect(Collectors.toMap(v -> v, v -> {
					if (!mappedAlarms.containsKey(v.getNodeID())) {
						return new DefaultStatus(OnmsSeverity.NORMAL.getLabel(), 0);
					} else {
						return new DefaultStatus(mappedAlarms.get(v.getNodeID()).getLabel(), 0);
					}
				}));
		return status;
	}
	
	@Override
	public String getNamespace() {
		return PathOutageProvider.NAMESPACE;
	}

	@Override
	public boolean contributesTo(String namespace) {
		return getNamespace().equals(namespace);
	}
}