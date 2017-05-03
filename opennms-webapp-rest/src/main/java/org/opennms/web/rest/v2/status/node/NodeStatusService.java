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

package org.opennms.web.rest.v2.status.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.support.CriteriaBuilderUtils;
import org.opennms.web.rest.support.QueryParameters;
import org.opennms.web.rest.v2.status.AbstractStatusService;
import org.opennms.web.rest.v2.status.StatusSummary;
import org.opennms.web.rest.v2.status.node.strategy.AlarmStatusCalculator;
import org.opennms.web.rest.v2.status.node.strategy.OutageStatusCalculator;
import org.opennms.web.rest.v2.status.node.strategy.Status;
import org.opennms.web.rest.v2.status.node.strategy.StatusCalculationStrategy;
import org.opennms.web.rest.v2.status.node.strategy.StatusCalculator;
import org.opennms.web.rest.v2.status.node.strategy.StatusQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeStatusService extends AbstractStatusService<NodeDTO, NodeQuery> {

    @Autowired
    private AlarmStatusCalculator alarmStatusCalculator;

    @Autowired
    private OutageStatusCalculator outageStatusCalculator;

    @Autowired
    private NodeDao nodeDao;

    @Override
    protected int countMatching(Criteria criteria) {
        return nodeDao.countMatching(criteria);
    }

    public StatusSummary getSummary(StatusCalculationStrategy strategy) {
        final List<OnmsNode> nodes = nodeDao.findAll();
        final Map<Integer, OnmsNode> nodeMap = nodes.stream().collect(Collectors.toMap(node -> node.getId(), node -> node));

        // calculate status
        final Status status = calculateStatus(nodeMap.keySet(), strategy);
        final long totalCount = nodeDao.countAll();
        final List<OnmsSeverity> severityList = nodeMap.keySet()
                .stream()
                .map(nodeId -> status.getSeverity(nodeId))
                .filter(s -> s != null).collect(Collectors.toList());

        return new StatusSummary(severityList, totalCount);
    }

    @Override
    protected List<NodeDTO> findMatching(NodeQuery query, CriteriaBuilder criteriaBuilder) {
        final List<OnmsNode> nodes = nodeDao.findMatching(criteriaBuilder.toCriteria());
        final Map<Integer, OnmsNode> nodeMap = nodes.stream().collect(Collectors.toMap(node -> node.getId(), node -> node));

        // calculate status
        Status status = calculateStatus(nodeMap.keySet(), query.getStatusCalculationStrategy());

        // convert to DTO
        return nodeMap.values().stream().map(node -> {
            OnmsSeverity nodeStatus = status.getSeverity(node.getId());
            if (nodeStatus == null) {
                nodeStatus = OnmsSeverity.NORMAL;
            }
            NodeDTO nodeDTO = new NodeDTO();
            nodeDTO.setSeverity(nodeStatus);
            nodeDTO.setId(node.getId());
            nodeDTO.setName(node.getLabel());
            return nodeDTO;
        }).collect(Collectors.toList());
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(QueryParameters queryParameters) {
        return CriteriaBuilderUtils.buildFrom(OnmsNode.class, queryParameters);
    }

    private Status calculateStatus(Collection<Integer> nodeIds, StatusCalculationStrategy strategy) {
        final StatusQuery statusQuery = new StatusQuery();
        statusQuery.setIncludeAcknowledgedAlarms(false);
        statusQuery.setNodeIds(nodeIds);
        statusQuery.setSeverity(OnmsSeverity.NORMAL);
        Status status = getStatusCalculator(strategy).calculateStatus(statusQuery);
        return status;
    }

    private StatusCalculator getStatusCalculator(StatusCalculationStrategy strategy) {
        switch (strategy) {
            case Alarms:
                return alarmStatusCalculator;
            case Outages:
                return outageStatusCalculator;
            default:
            case None:
                return (query) -> new Status();
        }
    }
}
