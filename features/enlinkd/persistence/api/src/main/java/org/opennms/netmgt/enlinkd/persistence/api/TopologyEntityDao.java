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

import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;

/**
 * Retrieves TopologyEntities from the database. TopologyEntities are views on OnmsEntities (such as OnmsNode, CdpLink, etc.)
 * which are reduced to the needs of displaying a toplogy:
 * - they are derived from one OnmsEntity / a database table
 * - they contain only the relevant subset of attributes of their OnmsEntity
 * - relations are just referenced by a primitive key such as a String or Integer instead of (lazy loaded) object references
 * The above features allow for fast database retrieval.
 */
public interface TopologyEntityDao {
    List<NodeTopologyEntity> getNodeTopologyEntities();
    List<CdpLinkTopologyEntity> getCdpLinkTopologyEntities();
    List<IsIsLinkTopologyEntity> getIsIsLinkTopologyEntities();
    List<LldpLinkTopologyEntity> getLldpLinkTopologyEntities();
    List<OspfLinkTopologyEntity> getOspfLinkTopologyEntities();
    List<OspfAreaTopologyEntity> getOspfAreaTopologyEntities();
    List<SnmpInterfaceTopologyEntity> getSnmpTopologyEntities();
    List<IpInterfaceTopologyEntity> getIpTopologyEntities();
    List<CdpElementTopologyEntity> getCdpElementTopologyEntities();
    List<IsIsElementTopologyEntity> getIsIsElementTopologyEntities();
    List<LldpElementTopologyEntity> getLldpElementTopologyEntities();
}
