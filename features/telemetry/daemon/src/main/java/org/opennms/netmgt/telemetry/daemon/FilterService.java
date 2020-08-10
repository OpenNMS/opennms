/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.daemon;

import java.net.InetAddress;

import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.filter.api.FilterDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FIXME: Move this elsewhere.
 */
public class FilterService {

    public static final String MATCH_ALL_FILTER_RULE = "IPADDR != '0.0.0.0'";

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private SessionUtils sessionUtils;


    public static class Session implements AutoCloseable {
        @Override
        public void close() throws Exception {

        }
    }

    public interface NodeInterfaceUpdateListener {
        /**
         * Called when an interface matches the filter.
         *
         * @param iff interface
         */
        void onInterfaceMatchedFilter(NodeInterface iff);

        /**
         * Called when an interface that was previously passed to a {@link #onInterfaceMatchedFilter} call,
         * no longer matches the filter.
         *
         * @param iff interface
         */
        void onInterfaceStoppedMatchingFilter(NodeInterface iff);
    }

    public Session watchFilter(String filterRule, NodeInterfaceUpdateListener listener) {
        return new Session();
    }

    public static class NodeInterface {
        private final int nodeId;
        private final InetAddress interfaceAddress;

        public NodeInterface(int nodeId, InetAddress interfaceAddress) {
            this.nodeId = nodeId;
            this.interfaceAddress = interfaceAddress;
        }

        public int getNodeId() {
            return nodeId;
        }

        public InetAddress getInterfaceAddress() {
            return interfaceAddress;
        }
    }
}
