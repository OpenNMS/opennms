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

package org.opennms.core.rpc.utils;

import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.Function;

/**
 * Utility class used to compute the target (location, system-id) for some
 * service oriented (i.e. monitor, collector) request.
 */
public class RpcTargetHelper {

    protected static final String LOCATION_KEY = "location";
    protected static final String SYSTEM_ID_KEY = "system-id";
    protected static final String USE_FOREIGN_ID_AS_SYSTEM_ID_KEY = "use-foreign-id-as-system-id";

    @Autowired(required=false)
    private NodeDao nodeDao;

    public static class RpcTargetBuilder {
        private String location;
        private String systemId;
        private Integer nodeId;
        private NodeDao nodeDao;
        private Map<String, Object> serviceAttributes;
        private Function<String, String> locationOverride;

        public RpcTargetBuilder withNodeId(Integer nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        protected RpcTargetBuilder withNodeDao(NodeDao nodeDao) {
            this.nodeDao = nodeDao;
            return this;
        }

        public RpcTargetBuilder withLocation(String location) {
            this.location = location;
            return this;
        }

        public RpcTargetBuilder withSystemId(String systemId) {
            this.systemId = systemId;
            return this;
        }

        public RpcTargetBuilder withServiceAttributes(Map<String, Object> attributes) {
            this.serviceAttributes = attributes;
            return this;
        }

        public RpcTargetBuilder withLocationOverride(Function<String, String> locationOverride) {
            this.locationOverride = locationOverride;
            return this;
        }

        public RpcTarget build() {
            // Start with the provided location
            String targetLocation = location;

            // Use the location set in the attributes, if any
            final String locationFromAttributes = getStringAttribute(LOCATION_KEY);
            if (locationFromAttributes != null) {
                targetLocation = locationFromAttributes;
            }

            // And finally, apply the override
            if (locationOverride != null) {
                final String locationFromOverride = locationOverride.apply(targetLocation);
                if (locationFromOverride != null) {
                    targetLocation = locationFromOverride;
                }
            }

            // Now start with the provided system id
            String targetSystemId = systemId;

            // Use the system-id set in the attributes, if any
            final String systemIdFromAttributes = getStringAttribute(SYSTEM_ID_KEY);
            if (systemIdFromAttributes != null) {
                targetSystemId = systemIdFromAttributes;
            }

            // Override using the foreign-id, if the flag is set, and we have
            // all of the necessary bits
            if (Boolean.TRUE.toString().equalsIgnoreCase(getStringAttribute(USE_FOREIGN_ID_AS_SYSTEM_ID_KEY))
                    && nodeId != null && nodeDao != null) {
                final OnmsNode node = nodeDao.get(nodeId);
                if (node != null && node.getForeignId() != null) {
                    targetSystemId = node.getForeignId();
                }
            }

            return new RpcTarget(targetLocation, targetSystemId);
        }

        private String getStringAttribute(String key) {
            if (serviceAttributes == null) {
                return null;
            }
            final Object value = serviceAttributes.get(key);
            if (value != null && value instanceof String) {
                return (String)value;
            }
            return null;
        }
    }

    public RpcTargetBuilder target() {
        return new RpcTargetBuilder().withNodeDao(nodeDao);
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

}
