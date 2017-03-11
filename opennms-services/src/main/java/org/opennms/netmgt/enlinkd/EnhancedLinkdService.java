/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero
 * General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/ For more information contact: OpenNMS(R)
 * Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;

/**
 * <p>
 * QueryManager interface.
 * </p>
 *
 * @author antonio
 * @version $Id: $
 */
public interface EnhancedLinkdService {

    /**
     * <p>
     * getSnmpNodeList
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    List<Node> getSnmpNodeList();

    /**
     * <p>
     * getSnmpNode
     * </p>
     *
     * @param nodeid
     *            a int.
     * @return a {@link org.opennms.netmgt.enlinkd.LinkableNode} object.
     */
    Node getSnmpNode(int nodeid);

    void loadBridgeTopology();

    /**
     * <p>
     * delete
     * </p>
     *
     * @param nodeid
     *            a int.
     *            <p>
     *            Remove any reference in topology for nodeid
     *            </p>
     */
    void delete(int nodeid);

    void reconcileLldp(int nodeId, Date now);

    void reconcileCdp(int nodeId, Date now);

    void reconcileOspf(int nodeId, Date now);

    void reconcileIsis(int nodeId, Date now);

    void reconcileIpNetToMedia(int nodeId, Date now);

    void reconcileBridge(int nodeId, Date now);
    
    void store(int nodeId, LldpLink link);

    void store(int nodeId, LldpElement element);

    void store(int nodeId, OspfLink link);

    void store(int nodeId, OspfElement element);

    void store(int nodeId, IsIsLink link);

    void store(int nodeId, IsIsElement element);

    void store(int nodeId, CdpElement cdp);

    void store(int nodeId, CdpLink link);

    void store(int nodeId, IpNetToMedia link);

    void store(int nodeId, BridgeElement bridge);

    void store(int nodeId, BridgeStpLink link);

    void store(int nodeId, List<BridgeMacLink> link);
    
    void store(BroadcastDomain domain, Date now);
    
    void save(BroadcastDomain domain);

    void cleanBroadcastDomains();

    Set<BroadcastDomain> getAllBroadcastDomains();
    
    Map<Integer, List<BridgeMacLink>> getUpdateBftMap();
    
    BroadcastDomain getBroadcastDomain(int nodeId);

    List<BridgeMacLink> useBridgeTopologyUpdateBFT(int nodeid);

    List<BridgeMacLink> getBridgeTopologyUpdateBFT(int nodeid);

    boolean hasUpdatedBft(int nodeid);
        
    List<BridgeElement> getBridgeElements(Set<Integer> nodeids);

}
