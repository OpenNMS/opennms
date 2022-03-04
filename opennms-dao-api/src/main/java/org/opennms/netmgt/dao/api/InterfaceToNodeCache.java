/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2014 The OpenNMS Group, Inc.
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
