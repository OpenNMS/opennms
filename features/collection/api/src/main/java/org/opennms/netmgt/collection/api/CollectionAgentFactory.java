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
package org.opennms.netmgt.collection.api;

import java.net.InetAddress;

import org.opennms.netmgt.model.OnmsIpInterface;

/**
 * Used to create {@link CollectionAgent}s for a given IP interface.
 *
 * @author jwhite
 */
public interface CollectionAgentFactory {

    CollectionAgent createCollectionAgent(OnmsIpInterface ipIf);

    CollectionAgent createCollectionAgent(String nodeCriteria, InetAddress ipAddr);

    /**
     * Create a collection agent for the given IP interface, and
     * optionally override the node's location.
     *
     * Overriding the node's location is strictly used for testing
     * (i.e. via the opennms:collect) command in the Karaf console
     * and is not used in normal operations.
     *
     * @param nodeCriteria node id or fs:fid
     * @param ipAddr ip address associated with the node
     * @param location <b>null</b> if the nodes existing location should be used
     * @return the {@link CollectionAgent}
     */
    CollectionAgent createCollectionAgentAndOverrideLocation(String nodeCriteria, InetAddress ipAddr, String location);

}
