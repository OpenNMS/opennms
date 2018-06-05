/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.status.api.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.status.api.StatusEntity;
import org.opennms.features.status.api.StatusEntityWrapper;
import org.opennms.features.status.api.StatusSummary;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class NodeStatusService {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private NodeStatusCalculator statusCalculator;

    public StatusSummary getSummary(NodeStatusCalculationStrategy strategy) {
        final NodeStatusCalculatorConfig config = new NodeStatusCalculatorConfig();
        config.setSeverities(Lists.newArrayList(
                OnmsSeverity.NORMAL,
                OnmsSeverity.WARNING,
                OnmsSeverity.MINOR,
                OnmsSeverity.MAJOR,
                OnmsSeverity.CRITICAL));
        config.setCalculationStrategy(strategy);

        final Map<OnmsSeverity, Long> statusOverviewMap = statusCalculator.calculateStatusOverview(config);
        final long totalCount = nodeDao.countAll();
        return new StatusSummary(statusOverviewMap, totalCount);
    }

    public int count(NodeQuery query) {
        NodeStatusCalculatorConfig config = buildFrom(query);
        config.prepareForCounting();
        return statusCalculator.countStatus(config);
    }

    public List<StatusEntity<OnmsNode>> getStatus(NodeQuery query) {
        // Build query
        final NodeStatusCalculatorConfig config = buildFrom(query);

        // Calculate Status
        final Status status = statusCalculator.calculateStatus(config);

        // Find nodes for node id
        final List<OnmsNode> nodes = getNodes(status.getIds());
        final Map<Integer, OnmsNode> nodeIdMap = nodes.stream().collect(Collectors.toMap(n -> n.getId(), n -> n));

        // convert to wrapper
        return status.getIds().stream().map(nodeId -> {
            OnmsSeverity nodeStatus = status.getSeverity(nodeId);
            OnmsNode node = nodeIdMap.get(nodeId);
            if (nodeStatus == null) {
                throw new IllegalStateException("nodeStatus should not be null");
            }
            if (node == null) {
                throw new IllegalStateException("node should not be null");
            }
            return new StatusEntityWrapper<>(node, nodeStatus);
        }).collect(Collectors.toList());
    }

    private List<OnmsNode> getNodes(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        final List<OnmsNode> nodes = nodeDao.findMatching(new CriteriaBuilder(OnmsNode.class).in("id", ids).toCriteria());
        return nodes;
    }

    private NodeStatusCalculatorConfig buildFrom(NodeQuery query) {
        final NodeStatusCalculatorConfig config = new NodeStatusCalculatorConfig();
        config.setCalculationStrategy(query.getStatusCalculationStrategy());

        if (query.getSeverityFilter() != null && query.getSeverityFilter().getSeverities() != null) {
            config.setSeverities(query.getSeverityFilter().getSeverities());
        }
        if (query.getParameters().getOffset() != null) {
            config.setOffset(query.getParameters().getOffset());
        }
        if (query.getParameters().getLimit() != null) {
            config.setLimit(query.getParameters().getLimit());
        }
        if (query.getParameters().getOrder() != null) {
            config.setOrder(query.getParameters().getOrder());
        }
        return config;
    }
}
