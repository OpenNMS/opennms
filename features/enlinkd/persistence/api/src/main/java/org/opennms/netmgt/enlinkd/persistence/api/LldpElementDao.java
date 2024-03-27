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
package org.opennms.netmgt.enlinkd.persistence.api;

import java.util.List;

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.model.LldpElement;

/**
 * <p>LldpElementDao interface.</p>
 */
public interface LldpElementDao extends ElementDao<LldpElement, Integer> {

    List<LldpElement> findByChassisId(String chassisId, LldpChassisIdSubType type);

    /**
     * Returns all LldpElements that have a chassisId/chassisIdSubType that match the corresponding fields of a
     * LldpElement that is related to the given node. Used to retrieve all LldpElements that need to be accessed when
     * finding lldp links of a node.
     */
    List<LldpElement> findByChassisOfLldpLinksOfNode(int nodeId);

    LldpElement findBySysname(String sysname);

}
