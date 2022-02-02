/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.dao.api.AbstractInterfaceToNodeCache;

import com.google.common.collect.Maps;

public class MockInterfaceToNodeCache extends AbstractInterfaceToNodeCache {

    private Map<Key, Entry> keyToEntry = Maps.newHashMap();

    @Override
    public boolean setNodeId(String location, InetAddress ipAddr, int nodeId) {
        return keyToEntry.put(new Key(location, ipAddr), new Entry(nodeId, 0)) != null;
    }

    @Override
    public boolean removeNodeId(String location, InetAddress ipAddr, int nodeId) {
        return keyToEntry.remove(new Key(location, ipAddr)) != null;
    }

    @Override
    public Optional<Entry> getFirst(String location, InetAddress ipAddr) {
        return Optional.ofNullable(keyToEntry.get(new Key(location, ipAddr)));
    }

    @Override
    public void dataSourceSync() {}

    @Override
    public int size() { return keyToEntry.size(); }

    @Override
    public void clear() {
        keyToEntry.clear();
    }

    @Override
    public void removeInterfacesForNode(int nodeId) {
    }

    private static class Key {
        private String location;
        private InetAddress ipAddr;

        public Key(String location, InetAddress ipAddr) {
            this.location = location;
            this.ipAddr = ipAddr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(location, key.location) &&
                    Objects.equals(ipAddr, key.ipAddr);
        }

        @Override
        public int hashCode() {

            return Objects.hash(location, ipAddr);
        }
    }
}
