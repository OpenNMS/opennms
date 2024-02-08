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
package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Iterables;

public interface InterfaceToNodeCache {

	void dataSourceSync();

	boolean setNodeId(String location, InetAddress ipAddr, int nodeId);

	boolean removeNodeId(String location, InetAddress ipAddr, int nodeId);

	int size();

	/**
	 * Should only be used for testing.
	 */
	void clear();

	Optional<Entry> getFirst(String location, InetAddress ipAddr);

	default Optional<Integer> getFirstNodeId(String location, InetAddress ipAddr) {
		return this.getFirst(location, ipAddr).map(e -> e.nodeId);
	}

	void removeInterfacesForNode(int nodeId);

	class Entry {
		public final int nodeId;
		public final int interfaceId;

		public Entry(final int nodeId, final int interfaceId) {
			this.nodeId = nodeId;
			this.interfaceId = interfaceId;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}

			if (!(o instanceof Entry)) {
				return false;

			}
			final Entry entry = (Entry) o;
			return this.nodeId == entry.nodeId &&
				   this.interfaceId == entry.interfaceId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.nodeId,
			                    this.interfaceId);
		}
	}
}
