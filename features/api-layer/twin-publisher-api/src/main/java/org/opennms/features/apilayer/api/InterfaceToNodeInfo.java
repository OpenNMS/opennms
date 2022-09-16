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


package org.opennms.features.apilayer.api;

import org.opennms.core.mate.api.ContextKey;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache.Entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InterfaceToNodeInfo {
    public static final String FULL_KEY = "org.opennms.interfaceToNodePublisher.full";
    public static final String NODE_METADATA_CACHE = "flows.node.metadata";

    private Map<Integer, Entry> entryMap;
    private Map<NodeMetadataKey, Integer> nodeMetaToEntry;
    private Map<NodeMetadataKey, Integer> networkMetaToEntry;

    public Map<Integer, Entry> getEntryMap() {
        return entryMap;
    }

    public void setEntryMap(Map<Integer, Entry> entryMap) {
        this.entryMap = entryMap;
    }

    public Map<NodeMetadataKey, Integer> getNodeMetaToEntry() {
        return nodeMetaToEntry;
    }

    public void setNodeMetaToEntry(Map<NodeMetadataKey, Integer> nodeMetaToEntry) {
        this.nodeMetaToEntry = nodeMetaToEntry;
    }

    public Map<NodeMetadataKey, Integer> getNetworkMetaToEntry() {
        return networkMetaToEntry;
    }

    public void setNetworkMetaToEntry(Map<NodeMetadataKey, Integer> networkMetaToEntry) {
        this.networkMetaToEntry = networkMetaToEntry;
    }

    // Key class, which is used to cache NodeInfo for a given node metadata.
    public static class NodeMetadataKey {

        public final ContextKey contextKey;

        public final String value;

        public NodeMetadataKey(final ContextKey contextKey, final String value) {
            this.contextKey = contextKey;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NodeMetadataKey that = (NodeMetadataKey) o;
            return Objects.equals(contextKey, that.contextKey) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contextKey, value);
        }
    }

//
//    public static class CacheKey {
//        private String[] keys;
//
//        public CacheKey(String... keys) {
//            this.keys = keys;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (!(o instanceof CacheKey)) {
//                return false;
//            }
//            final CacheKey that = (CacheKey) o;
//            return Arrays.equals(this.keys, that.keys);
//        }
//
//        @Override
//        public int hashCode() {
//            return Arrays.hashCode(keys);
//        }
//    }

}
