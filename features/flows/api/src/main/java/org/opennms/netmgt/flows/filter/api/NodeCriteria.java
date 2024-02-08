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
