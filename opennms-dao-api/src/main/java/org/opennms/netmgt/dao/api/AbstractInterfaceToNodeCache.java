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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents a singular instance that is used to map trap IP
 * addresses to known nodes.
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public abstract class AbstractInterfaceToNodeCache implements InterfaceToNodeCache {

    private static final AtomicReference<InterfaceToNodeCache> s_instance = new AtomicReference<>();

    public static void setInstance(InterfaceToNodeCache cache) {
        s_instance.set(cache);
    }

    /**
     * @deprecated Inject this value instead of using singleton access.
     */
    public static InterfaceToNodeCache getInstance() {
        return s_instance.get(); 
    }

    public Optional<Integer> getFirstNodeId(String location, InetAddress ipAddr) {
		final Iterator<Integer> it = this.getNodeId(location, ipAddr).iterator();
		if (it.hasNext()) {
			return Optional.of(it.next());
		} else {
			return Optional.empty();
		}
	}
}
