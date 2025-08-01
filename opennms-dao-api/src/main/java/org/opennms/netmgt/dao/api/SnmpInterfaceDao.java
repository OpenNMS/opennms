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

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.OnmsSnmpInterface;


/**
 * <p>SnmpInterfaceDao interface.</p>
 *
 * @author Ted Kazmark
 * @author David Hustace
 */
public interface SnmpInterfaceDao extends LegacyOnmsDao<OnmsSnmpInterface, Integer> {

    /**
     * <p>findByNodeIdAndIfIndex</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    OnmsSnmpInterface findByNodeIdAndIfIndex(Integer nodeId, Integer ifIndex);

    List<OnmsSnmpInterface> findByNodeId(Integer nodeId);

    List<OnmsSnmpInterface> findByMacLinksOfNode(Integer nodeId);

    /**
     * Returns all OnmsSnmpInterfaces that have a physAddr that matches an isisISAdjNeighSNPAAddress of an IsIsLink related to the given
     * node. Used to retrieve all OnmsSnmpInterfaces that need to be accessed when finding IsIs links of a node.
     */
    List<OnmsSnmpInterface> findBySnpaAddressOfRelatedIsIsLink(int nodeId);

    /**
     * <p>findByForeignKeyAndIfIndex</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    OnmsSnmpInterface findByForeignKeyAndIfIndex(String foreignSource, String foreignId, Integer ifIndex);

    OnmsSnmpInterface findByNodeIdAndDescription(Integer nodeId, String description);

    void markHavingIngressFlows(final Integer nodeId, final Collection<Integer> ingressSnmpIfIndexes);
    void markHavingEgressFlows(final Integer nodeId, final Collection<Integer> egressSnmpIfIndexes);

    List<OnmsSnmpInterface> findAllHavingFlows(final Integer nodeId);
    List<OnmsSnmpInterface> findAllHavingIngressFlows(final Integer nodeId);
    List<OnmsSnmpInterface> findAllHavingEgressFlows(final Integer nodeId);

    /**
     * Returns the number of interfaces that have been marked as having flows irrespective of the
     * MAX_FLOW_AGE and INGRESS_AND_EGRESS_REQUIRED properties settings.
     */
    long getNumInterfacesWithFlows();
}
