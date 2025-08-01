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
package org.opennms.netmgt.graph.api.generic;

/**
 * These properties are generally supported and may be used to persist as values when building a {@link GenericElement}.
 *
 * @author mvrueden
 */
public interface GenericProperties {
    /** The id of the element */
    String ID = "id";

    /** The namespace of the element. */
    String NAMESPACE = "namespace";

    /** The description of the element */
    String DESCRIPTION = "description";

    /** The label of the element */
    String LABEL = "label";

    /** Reference to a node, either the id, or a <foreignSource>:<foreignId> statement */
    String NODE_CRITERIA = "nodeCriteria";

    String NODE_INFO = "nodeInfo";

    String NODE_ID = "nodeID";
    String FOREIGN_SOURCE = "foreignSource";
    String FOREIGN_ID = "foreignID";

    interface Enrichment {
        /** Determines if vertices containing a node ref should be enriched with the node information. */
        String RESOLVE_NODES = "enrichment.resolveNodes";

        /**
         * Determines if vertices containing a node ref should be enriched with
         * status information (based on their associated alarms)
         */
        String DEFAULT_STATUS = "enrichment.defaultStatus";
    }
}
