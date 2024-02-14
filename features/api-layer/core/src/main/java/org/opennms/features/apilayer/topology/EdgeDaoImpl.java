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
package org.opennms.features.apilayer.topology;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.EdgeMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.dao.EdgeDao;
import org.opennms.integration.api.v1.model.TopologyEdge;
import org.opennms.integration.api.v1.model.TopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;

/**
 * This class acts as a simple intermediary layer on top of {@link OnmsTopologyDao} to satisfy the contract of
 * {@link EdgeDao the integration API} by exposing the functionality only via OSGI.
 */
public class EdgeDaoImpl implements EdgeDao {
    private final OnmsTopologyDao onmsTopologyDao;
    private final EdgeMapper edgeMapper;

    public EdgeDaoImpl(OnmsTopologyDao onmsTopologyDao, EdgeMapper edgeMapper) {
        this.onmsTopologyDao = Objects.requireNonNull(onmsTopologyDao);
        this.edgeMapper = Objects.requireNonNull(edgeMapper);
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

    private Set<TopologyEdge> getEdges(Predicate<OnmsTopologyProtocol> filter) {
        Set<TopologyEdge> currentEdges = new HashSet<>();
        onmsTopologyDao.getTopologies()
                .entrySet()
                .stream()
                .filter(entry -> filter.test(entry.getKey()))
                .forEach(entry -> entry.getValue().getEdges()
                        .stream()
                        .map(edge -> edgeMapper.toEdge(entry.getKey(), edge))
                        .forEach(currentEdges::add));
        return currentEdges;
    }

    @Override
    public long getEdgeCount() {
        return getEdgeCount(EdgeDaoImpl::includeAll);
    }

    @Override
    public long getEdgeCount(TopologyProtocol protocol) {
        if(protocol == TopologyProtocol.ALL) {
            return getEdgeCount();
        }
        return getEdgeCount(p -> p.equals(ModelMappers.toOnmsTopologyProtocol(protocol)));
    }

    @Override
    public Set<TopologyEdge> getEdges() {
        return getEdges(EdgeDaoImpl::includeAll);
    }

    @Override
    public Set<TopologyEdge> getEdges(TopologyProtocol protocol) {
        if(protocol == TopologyProtocol.ALL) {
            return getEdges();
        }
        return getEdges(p -> p.equals(ModelMappers.toOnmsTopologyProtocol(protocol)));
    }

    @Override
    public Set<TopologyProtocol> getProtocols() {
        return onmsTopologyDao.getSupportedProtocols()
                .stream()
                .map(ModelMappers::toTopologyProtocol)
                .collect(Collectors.toSet());
    }
}
