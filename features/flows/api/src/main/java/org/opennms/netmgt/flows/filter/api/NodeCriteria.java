/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.filter.api;

import java.util.Objects;

public class NodeCriteria {
    private final String foreignSource;
    private final String foreignId;
    private final Integer nodeId;

    public NodeCriteria(String criteria) {
        final String[] tokens = criteria.split(":");
        if (tokens.length == 1) {
            this.nodeId = Integer.parseInt(tokens[0]);
            this.foreignSource = null;
            this.foreignId = null;
        } else if (tokens.length == 2) {
            this.foreignSource = tokens[0];
            this.foreignId = tokens[1];
            this.nodeId = null;
        } else {
            throw new IllegalArgumentException("Invalid node criteria " + criteria);
        }
    }

    public NodeCriteria(String foreignSource, String foreignId) {
        this.foreignSource = Objects.requireNonNull(foreignSource);
        this.foreignId = Objects.requireNonNull(foreignId);
        nodeId = null;
    }

    public NodeCriteria(Integer nodeId) {
        this.nodeId = Objects.requireNonNull(nodeId);
        foreignSource = null;
        foreignId = null;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public String getForeignId() {
        return foreignId;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getCriteria() {
        return toCriteria(nodeId, foreignId, foreignSource);
    }

    public static String toCriteria(int id, String foreignId, String foreignSource) {
        if (foreignId != null && foreignSource != null) {
            return String.format("%s:%s", foreignSource, foreignId);
        } else {
            return Integer.toString(id);
        }
    }

    @Override
    public String toString() {
        return String.format("NodeCriteria[%s]", getCriteria());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeCriteria that = (NodeCriteria) o;
        return Objects.equals(foreignSource, that.foreignSource) &&
                Objects.equals(foreignId, that.foreignId) &&
                Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreignSource, foreignId, nodeId);
    }

}
