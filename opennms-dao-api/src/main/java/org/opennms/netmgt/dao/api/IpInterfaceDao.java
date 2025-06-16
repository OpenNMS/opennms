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
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;


/**
 * <p>IpInterfaceDao interface.</p>
 *
 * @author Ted Kazmark
 * @author David Hustace
 * @author Matt Brozowski
 */
public interface IpInterfaceDao extends LegacyOnmsDao<OnmsIpInterface, Integer> {

    /**
     * <p>get</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    OnmsIpInterface get(OnmsNode node, String ipAddress);
    
    /**
     * <p>findByNodeIdAndIpAddress</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress);

    /**
     * <p>findByForeignKeyAndIpAddress</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    OnmsIpInterface findByForeignKeyAndIpAddress(String foreignSource, String foreignId, String ipAddress);

    /**
     * <p>findByIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findByIpAddress(String ipAddress);
    
    /**
     * <p>findByNodeId</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findByNodeId(Integer nodeId);

    /**
     * Finds all {@link OnmsIpInterface} instances that have an {@code ipAddress} that is related to a physical
     * address that is equal to the mac address of a {@code BridgeMacLink} of the addressed {@code node}.
     * @param nodeId
     * @return
     */
    List<OnmsIpInterface> findByMacLinksOfNode(Integer nodeId);

    /**
     * <p>findByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findByServiceType(String svcName);

    /**
     * <p>findHierarchyByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsIpInterface> findHierarchyByServiceType(String svcName);

    /**
     * Returns a map of all IP to node ID mappings in the database.
     *
     * @return a {@link java.util.Map} object.
     */
    Map<InetAddress, Integer> getInterfacesForNodes();

	OnmsIpInterface findPrimaryInterfaceByNodeId(Integer nodeId);

	List<OnmsIpInterface> findInterfacesWithMetadata(final String context, final String key, final String value, final boolean matchEnumeration);

    default List<OnmsIpInterface> findInterfacesWithMetadata(final String context, final String key, final String value) {
        return findInterfacesWithMetadata(context, key, value, false);
    }

    List<OnmsIpInterface> findByIpAddressAndLocation(String address, String location);

}
