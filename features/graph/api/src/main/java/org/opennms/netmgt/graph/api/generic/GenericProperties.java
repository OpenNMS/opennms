/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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
