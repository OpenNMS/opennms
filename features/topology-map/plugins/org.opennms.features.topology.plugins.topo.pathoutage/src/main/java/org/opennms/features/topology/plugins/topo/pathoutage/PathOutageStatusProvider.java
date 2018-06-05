/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
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
	public Map<VertexRef, Status> getStatusForVertices(VertexProvider vertexProvider, Collection<VertexRef> vertices, Criteria[] criteria) {
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