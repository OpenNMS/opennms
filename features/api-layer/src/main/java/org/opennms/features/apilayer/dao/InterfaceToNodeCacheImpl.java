/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.dao;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.opennms.integration.api.v1.dao.InterfaceToNodeCache;

public class InterfaceToNodeCacheImpl implements InterfaceToNodeCache {

    private final org.opennms.netmgt.dao.api.InterfaceToNodeCache cache;

    public InterfaceToNodeCacheImpl(org.opennms.netmgt.dao.api.InterfaceToNodeCache cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public Stream<Integer> getNodeIds(String location, InetAddress ipAddr) {
        return StreamSupport.stream(cache.getNodeId(location, ipAddr).spliterator(), false);
    }

    @Override
    public Optional<Integer> getFirstNodeId(String location, InetAddress ipAddr) {
        return getNodeIds(location, ipAddr).findFirst();
    }

    @Override
    public void refresh() {
        cache.dataSourceSync();
    }
}
