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

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.status.api.AbstractStatusService;
import org.opennms.features.status.api.StatusEntity;
import org.opennms.features.status.api.StatusEntityWrapper;
import org.opennms.features.status.api.StatusSummary;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.CriteriaBuilderUtils;
import org.opennms.web.utils.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NodeStatusService extends AbstractStatusService<OnmsNode, NodeQuery> {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private NodeStatusCalculatorManager statusCalculatorManager;

    @Override
    protected int countMatching(Criteria criteria) {
        return nodeDao.countMatching(criteria);
    }

    public StatusSummary getSummary(NodeStatusCalculationStrategy strategy) {
        final Map<OnmsSeverity, Long> statusOverviewMap = calculateStatusOverview(strategy);
        final long totalCount = nodeDao.countAll();
        return new StatusSummary(statusOverviewMap, totalCount);
    }

    @Override
    protected List<StatusEntity<OnmsNode>> findMatching(NodeQuery query, CriteriaBuilder criteriaBuilder) {
        final List<OnmsNode> nodes = nodeDao.findMatching(criteriaBuilder.toCriteria());
        final Map<Integer, OnmsNode> nodeMap = nodes.stream().collect(Collectors.toMap(node -> node.getId(), node -> node));

        // calculate status
        Status status = calculateStatus(nodeMap.keySet(), query.getStatusCalculationStrategy());

        // convert to wrapper
        return nodes.stream().map(node -> {
            OnmsSeverity nodeStatus = status.getSeverity(node.getId());
            if (nodeStatus == null) {
                nodeStatus = OnmsSeverity.NORMAL;
            }
            return new StatusEntityWrapper<>(node, nodeStatus);
        }).collect(Collectors.toList());
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(QueryParameters queryParameters) {
        return CriteriaBuilderUtils.buildFrom(OnmsNode.class, queryParameters);
    }

    private NodeStatusCalculatorConfig createConfig(Collection<Integer> nodeIds, NodeStatusCalculationStrategy strategy) {
        final NodeStatusCalculatorConfig nodeStatusCalculatorConfig = new NodeStatusCalculatorConfig();
        nodeStatusCalculatorConfig.setIncludeAcknowledgedAlarms(false);
        nodeStatusCalculatorConfig.setNodeIds(nodeIds);
        nodeStatusCalculatorConfig.setSeverity(OnmsSeverity.NORMAL);
        nodeStatusCalculatorConfig.setCalculationStrategy(strategy);
        return nodeStatusCalculatorConfig;
    }

    private Map<OnmsSeverity, Long> calculateStatusOverview(NodeStatusCalculationStrategy strategy) {
        final NodeStatusCalculatorConfig config = createConfig(new ArrayList<>(), strategy);
        final Map<OnmsSeverity, Long> statusOverview = statusCalculatorManager.calculateStatusOverview(config);
        return statusOverview;
    }

    private Status calculateStatus(Collection<Integer> nodeIds, NodeStatusCalculationStrategy strategy) {
        final NodeStatusCalculatorConfig nodeStatusCalculatorConfig = createConfig(nodeIds, strategy);
        final Status status = statusCalculatorManager.calculateStatus(nodeStatusCalculatorConfig);
        return status;
    }
}
