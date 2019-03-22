/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.topology;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.dao.EdgeDao;
import org.opennms.integration.api.v1.model.TopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;

/**
 * This class acts as a simple intermediary layer on top of {@link OnmsTopologyDao} to satisfy the contract of
 * {@link EdgeDao the integration API} by exposing the functionality only via OSGI.
 */
public class EdgeDaoImpl implements EdgeDao {
    private final OnmsTopologyDao onmsTopologyDao;

    public EdgeDaoImpl(OnmsTopologyDao onmsTopologyDao) {
        this.onmsTopologyDao = Objects.requireNonNull(onmsTopologyDao);
    }

    private static boolean includeAll(OnmsTopologyProtocol protocol) {
        return true;
    }

    private long getEdgeCount(Predicate<OnmsTopologyProtocol> filter) {
        return onmsTopologyDao.getTopologies()
                .entrySet()
                .stream()
                .filter(entry -> filter.test(entry.getKey()))
                .mapToLong(entry -> entry.getValue().getEdges().size())
                .sum();
    }

    private Collection<TopologyEdge> getEdges(Predicate<OnmsTopologyProtocol> filter) {
        Set<TopologyEdge> currentEdges = new HashSet<>();
        onmsTopologyDao.getTopologies()
                .entrySet()
                .stream()
                .filter(entry -> filter.test(entry.getKey()))
                .forEach(entry -> entry.getValue().getEdges()
                        .stream()
                        .map(edge -> ModelMappers.toEdge(entry.getKey().getId(), edge))
                        .forEach(currentEdges::add));
        return currentEdges;
    }

    @Override
    public long getEdgeCount() {
        return getEdgeCount(EdgeDaoImpl::includeAll);
    }

    @Override
    public long getEdgeCount(String protocol) {
        return getEdgeCount(p -> p.getId().equals(protocol));
    }

    @Override
    public Collection<TopologyEdge> getEdges() {
        return getEdges(EdgeDaoImpl::includeAll);
    }

    @Override
    public Collection<TopologyEdge> getEdges(String protocol) {
        return getEdges(p -> p.getId().equals(protocol));
    }

    @Override
    public Set<String> getProtocols() {
        return onmsTopologyDao.getSupportedProtocols();
    }
}
