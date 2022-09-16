/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface InterfaceToNodeCache {
    void dataSourceSync();

    boolean setNodeId(String location, InetAddress ipAddr, int nodeId);

    boolean removeNodeId(String location, InetAddress ipAddr, int nodeId);

    /**
     * Should only be used for testing.
     */
    void clear();

    void removeInterfacesForNode(int nodeId);

    int size();

    Optional<Entry> getFirst(String location, InetAddress ipAddr);

    default Optional<Integer> getFirstNodeId(String location, InetAddress ipAddr) {
        return this.getFirst(location, ipAddr).map(e -> e.nodeId);
    }

    void setUpdateCallback(InterfaceToNodeCacheUpdateCallback callback);

    class Entry {
        public int nodeId;
        public int interfaceId;
        public InetAddress ipAddr;
        public String location;
        public String foreignId;
        public String foreignSource;
        public List<String> categories;

        // For twin jackson use
        public Entry() {
        }

        public Entry(final int nodeId, final int interfaceId, InetAddress ipAddr, String location) {
            this.nodeId = nodeId;
            this.interfaceId = interfaceId;
            this.ipAddr = ipAddr;
            this.location = location;
//            this.foreignId = foreignId;
//            this.foreignSource = foreignSource;
//            this.categories = categories;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Entry)) {
                return false;

            }
            final Entry that = (Entry) o;
            return Objects.equals(this.nodeId, that.nodeId)
                    && Objects.equals(this.interfaceId, that.interfaceId)
                    && Objects.equals(this.ipAddr, that.ipAddr)
                    && Objects.equals(this.location, that.location)
                    && Objects.equals(this.foreignId, that.foreignId)
                    && Objects.equals(this.foreignSource, that.foreignSource)
                    && Objects.equals(this.categories, that.categories);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.nodeId,
                    this.interfaceId, this.ipAddr, this.location, this.foreignId, this.foreignSource, this.categories);
        }
    }
}
