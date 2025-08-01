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
