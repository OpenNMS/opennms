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

package org.opennms.features.status.api.node.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.QueryParameters;

import com.google.common.collect.Lists;

public class NodeStatusCalculatorConfig {
    private boolean includeAcknowledgedAlarms;
    private String location;
    private Collection<Integer> nodeIds = new ArrayList<>();
    private NodeStatusCalculationStrategy calculationStrategy;
    private List<OnmsSeverity> severities = Lists.newArrayList(
            OnmsSeverity.NORMAL, OnmsSeverity.WARNING, OnmsSeverity.MINOR, OnmsSeverity.MAJOR, OnmsSeverity.CRITICAL);
    private Integer limit;
    private QueryParameters.Order order;
    private Integer offset;

    public boolean isIncludeAcknowledgedAlarms() {
        return includeAcknowledgedAlarms;
    }

    public void setIncludeAcknowledgedAlarms(boolean includeAcknowledgedAlarms) {
        this.includeAcknowledgedAlarms = includeAcknowledgedAlarms;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNodeIds(Collection<Integer> nodeIds) {
        this.nodeIds = nodeIds;
    }

    public Collection<Integer> getNodeIds() {
        return nodeIds;
    }

    public void setCalculationStrategy(NodeStatusCalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
    }

    public NodeStatusCalculationStrategy getCalculationStrategy() {
        return calculationStrategy;
    }

    public void setSeverities(List<OnmsSeverity> severities) {
        this.severities = severities;
    }

    public List<OnmsSeverity> getSeverities() {
        return severities;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setOrder(QueryParameters.Order order) {
        this.order = order;
    }

    public QueryParameters.Order getOrder() {
        return order;
    }

    public Integer getOffset() {
        return offset;
    }

    public void prepareForCounting() {
        this.limit = null;
        this.order = null;
        this.offset = null;
    }

    public void setSeverity(OnmsSeverity severity) {
        if (severity == null) {
            severities.clear();
        } else {
            setSeverities(Lists.newArrayList(severity));
        }
    }
}
