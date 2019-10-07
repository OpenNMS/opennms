/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;

/**
 * Caches TopologyEntities. This is a cache wrapper around @{@link TopologyEntityDao}. See there for an explanation of
 * TopologyEntities.
 * We use the cache to improve the displaying speed of topologies.
 */
public interface TopologyEntityCache {
    List<NodeTopologyEntity> getNodeTopologyEntities();
    List<CdpLinkTopologyEntity> getCdpLinkTopologyEntities();
    List<OspfLinkTopologyEntity> getOspfLinkTopologyEntities();
    List<IsIsLinkTopologyEntity> getIsIsLinkTopologyEntities();
    List<LldpLinkTopologyEntity> getLldpLinkTopologyEntities();
    List<CdpElementTopologyEntity> getCdpElementTopologyEntities();
    List<IsIsElementTopologyEntity> getIsIsElementTopologyEntities();
    List<LldpElementTopologyEntity> getLldpElementTopologyEntities();
    List<SnmpInterfaceTopologyEntity> getSnmpInterfaceTopologyEntities();
    List<IpInterfaceTopologyEntity> getIpInterfaceTopologyEntities();
    void refresh();

}
