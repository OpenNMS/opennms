/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.topology.BridgeForwardingTable;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopology;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// least condition theorem for simple connections
// X and Y are bridges
// m_1 m_2 m_3 are mac addresses
// m_x is a mac address of bridge X
// piX, pjX are port on bridgeX
// FDB(pi,X) is the Bridge Forwarding Set for port pi on bridge X
// TS(pi,X) is the Through Set for port pi on bridge X
// minimun requiremnt:
// X and Y are simple connected by xy on X and yx on Y
// condition 1
// if exists m_x and m_y :     m_x belongs FDB(yx,Y) 
//                             m_y belongs FDB(xy,X)
//
// condition 2X
// if exists m_x, m_1 and m_2, p1 and p2 on Y : m_x belongs to FDB(yx,Y) 
//                                              m_1 belongs to FDB(p1,Y) FDB(xy,X)
//                                              m_2 belongs to FDB(p2,Y) FDB(xy,X)
// condition 2Y
// if exists m_y, m_1 and m_2, p1 and p2 on X : m_y belongs to FDB(xy,X) 
//                                              m_1 belongs to FDB(p1,X) FDB(yx,Y)
//                                              m_2 belongs to FDB(p2,X) FDB(yx,Y)
//
//
// condition 3X
// if exist m_1,m_2,m_3 and p1,p2 on Y and p3 on X: m_1 belongs to FDB(p1,Y) FDB(xy,X) 
//                                                  m_2 belongs to FDB(p2,Y) FDB(xy,X) 
//                                                  m_3 belongs to FDB(yx,Y) FDB(p3,X)      
//
// condition 3Y
// if exist m_1,m_2,m_3 and p1,p2 on Y and p3 on X: m_1 belongs to FDB(p1,X) FDB(yx,Y) 
//                                                  m_2 belongs to FDB(p2,X) FDB(yx,Y) 
//                                                  m_3 belongs to FDB(xy,X) FDB(p3,Y)
//
// condition 3XY
// if exist m_1,m_k,m_3 and p1 on Y and p3 on X: m_1 belongs to FDB(p1,Y) FDB(xy,X)
//                                               m_k belongs to FDB(yx,Y) FDB(xy,X) 
//                                               m_3 belongs to FDB(yx,Y) FDB(p3,X)
//
// condition 4
// intersection is made only by macs living on xy of X and yx of Y
// only one common port on X and Y
// these is no other common forwarding port
// first step is to find the common macs.
// then we work on this set (if the size is only 2......no way)
// get m_1 m_2 m_3 and check the ports on the two bridges...to match rules
// 
public class BridgeSimpleConnection implements BridgeTopology {
    
    private static final Logger LOG = LoggerFactory.getLogger(BridgeSimpleConnection.class);

    private final BridgeForwardingTable m_xBridge;
    private final BridgeForwardingTable m_yBridge;
    private BridgePort m_xyPort;
    private BridgePort m_yxPort;
    private Map<String,BridgeForwardingTableEntry> m_xmactoport = new HashMap<String, BridgeForwardingTableEntry>();
    private Map<String,BridgeForwardingTableEntry> m_ymactoport = new HashMap<String, BridgeForwardingTableEntry>();

    public BridgeForwardingTable getFirst() {
        return m_xBridge;
    }

    public BridgeForwardingTable getSecond() {
        return m_yBridge;
    }


    public BridgePort getFirstBridgePort() {
        return m_xyPort;
    }
    
    public BridgePort getSecondBridgePort() {
        return m_yxPort;
    }


    public BridgeSimpleConnection(BridgeForwardingTable xBridge, 
            BridgeForwardingTable yBridge) {
        super();
        m_xBridge = xBridge;
        m_yBridge = yBridge;
        for (BridgeForwardingTableEntry xlink: m_xBridge.getBFTEntries()) {
            if (m_xBridge.getNodeId().intValue() == xlink.getNodeId().intValue() 
                    && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                m_xmactoport.put(xlink.getMacAddress(), xlink);
            }
            if (m_xBridge.getNodeId().intValue() == xlink.getNodeId().intValue() 
                    && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                m_xBridge.getIdentifiers().add(xlink.getMacAddress());
        }
        for (BridgeForwardingTableEntry ylink: m_yBridge.getBFTEntries()) {
            if (m_yBridge.getNodeId().intValue() == ylink.getNodeId().intValue() && 
                    ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                m_ymactoport.put(ylink.getMacAddress(), ylink);
            }
            if (m_yBridge.getNodeId().intValue() == ylink.getNodeId().intValue() 
                    && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                m_yBridge.getIdentifiers().add(ylink.getMacAddress());
        }
    }
    
    //FIXME parsed only three macs...but what happens if there is a wrong
    //      because obsolete entry? For example you can have 
    //      a new bft with changed a mac position check.....
    //      and remove
    public boolean findSimpleConnection() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("findSimpleConnection: ->\n first bridge -> \n{}\n second bridge -> \n{}",
                      m_xBridge.printTopology(),
                      m_yBridge.printTopology());
        }

        // there is a mac of Y found on X BFT
        m_xyPort = BridgeSimpleConnection.condition1(m_yBridge.getIdentifiers(), m_xmactoport);
        if (m_xyPort != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnection: {} --> nodeid:[{}]",
                      m_xyPort.printTopology(),
                      m_yBridge.getNodeId());
            }
        }

        // there is a mac of X found on Y BFT
        m_yxPort = BridgeSimpleConnection.condition1(m_xBridge.getIdentifiers(), m_ymactoport);
        if (m_yxPort != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("simple connection: nodeid:[{}] <-- {}",
                      m_xBridge.getNodeId(),
                      m_yxPort.printTopology());
            }
        }
        	            
        if (m_xyPort == null || m_yxPort == null) {
            Set<String> commonlearnedmacs = new HashSet<String>(m_xmactoport.keySet()); 
            commonlearnedmacs.retainAll(new HashSet<String>(m_ymactoport.keySet()));
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnection: nodeid:[{}] <--> nodeid:[{}] common (learned mac): -> \n{}",
                      m_xBridge.getNodeId(),
                      m_yBridge.getNodeId(),
                      commonlearnedmacs);
            }
            if (m_yxPort != null && m_xyPort == null) { 
                m_xyPort = BridgeSimpleConnection.condition2(commonlearnedmacs,m_yxPort,m_ymactoport,m_xmactoport);
            } else if (m_yxPort == null && m_xyPort != null) {
                m_yxPort = BridgeSimpleConnection.condition2(commonlearnedmacs,m_xyPort,m_xmactoport,m_ymactoport);
            } else {
                List<BridgePort> ports = BridgeSimpleConnection.condition3(commonlearnedmacs,m_xmactoport,m_ymactoport);
                if (ports.size() == 2) {
                    m_xyPort = ports.get(0);
                    m_yxPort= ports.get(1);
                }
            }
        }    
        if (m_xyPort == null || m_xyPort == null) {
            return false;
        }
        return true;
    }

    private static List<BridgePort> condition3(Set<String> commonlearnedmacs,Map<String,BridgeForwardingTableEntry> xbft,Map<String,BridgeForwardingTableEntry> ybft) {
    
    //
    // condition 3XY
    // if exist m_1,m_k,m_3 and p1 on Y and p3 on X: m_1 belongs to FDB(p1,Y) FDB(xy,X)
    //                                               m_k belongs to FDB(yx,Y) FDB(xy,X) 
    //                                               m_3 belongs to FDB(yx,Y) FDB(p3,X)
    // condition 3Y
    // if exist m_1,m_2,m_3 and p1,p2 on Y and p3 on X: m_1 belongs to FDB(p1,X) FDB(yx,Y) 
    //                                                  m_2 belongs to FDB(p2,X) FDB(yx,Y) 
    //                                                  m_3 belongs to FDB(xy,X) FDB(p3,Y)
    //
        String mac1=null;
        String mac2=null;
        BridgePort yp1=null;
        BridgePort yp2=null;
        BridgePort xp1=null;
        BridgePort xp2=null;
        List<BridgePort> bbports = new ArrayList<BridgePort>(2);
        for (String mac: commonlearnedmacs) {
            if (mac1 == null) {
                mac1=mac;
                yp1=BridgePort.getFromBridgeForwardingTableEntry(ybft.get(mac));
                xp1=BridgePort.getFromBridgeForwardingTableEntry(xbft.get(mac));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("condition3: mac1: {} -> \nxp1: {} \nyp1: {} ", mac1,
                              xp1.printTopology(),
                              yp1.printTopology());
                }
                continue;
            }
            if (ybft.get(mac).getBridgePort() == yp1.getBridgePort()
                    && xbft.get(mac).getBridgePort() == xp1.getBridgePort()) {
                continue;
            }
            if (mac2 == null) {
                mac2=mac;
                yp2=BridgePort.getFromBridgeForwardingTableEntry(ybft.get(mac));
                xp2=BridgePort.getFromBridgeForwardingTableEntry(xbft.get(mac));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("condition3: mac2: {} -> \nxp2: {} \nyp2: {} ", mac2,
                          xp2.printTopology(),
                          yp2.printTopology());
                }
                continue;
            }
            if (ybft.get(mac).getBridgePort() == yp2.getBridgePort() 
                    && xbft.get(mac).getBridgePort() == xp2.getBridgePort()) {
                continue;
            }
            BridgePort yp3 = BridgePort.getFromBridgeForwardingTableEntry(ybft.get(mac));
            BridgePort xp3 = BridgePort.getFromBridgeForwardingTableEntry(xbft.get(mac));
            if (LOG.isDebugEnabled()) {
                LOG.debug("condition3: mac3: {} -> \nx3: {} \nyp3: {} ", mac,
                          xp3.printTopology(),
                          yp3.printTopology());
            }
            //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
            //m_2 belongs to FDB(p2,Y) FDB(xy,X) 
            //m_3 belongs to FDB(yx,Y) FDB(p3,X)
            //
            //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
            //m_2 belongs to FDB(yx,Y) FDB(xy,X) 
            //m_3 belongs to FDB(yx,Y) FDB(p3,X)
            if (xp1.getBridgePort() == xp2.getBridgePort() 
                    && xp1.getBridgePort() != xp3.getBridgePort() 
                    && (yp1.getBridgePort() != yp3.getBridgePort() 
                    || yp2.getBridgePort() != yp3.getBridgePort()) ) {
            	bbports.add(0, xp1);
            	bbports.add(1, yp3);
                return bbports;
            }
            // exchange x y
            if (yp1.getBridgePort() == yp2.getBridgePort() 
                    && yp1.getBridgePort() != yp3.getBridgePort() 
                    && (xp1.getBridgePort() != xp3.getBridgePort() 
                    || xp2.getBridgePort() != xp3.getBridgePort()) ) {
            	bbports.add(0, xp3);
            	bbports.add(1, yp1);                    
                return bbports;
            }
            // exchange 3 with 2
            //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
            //m_2 belongs to FDB(yx,Y) FDB(p3,X)
            //m_3 belongs to FDB(p2,Y) FDB(xy,X) 
            //
            //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
            //m_2 belongs to FDB(yx,Y) FDB(p3,X)
            //m_3 belongs to FDB(yx,Y) FDB(xy,X) 
            if (xp1.getBridgePort() == xp3.getBridgePort() 
                    && xp1.getBridgePort() != xp2.getBridgePort() 
                    && (yp1.getBridgePort() != yp2.getBridgePort() 
                    || yp2.getBridgePort() != yp3.getBridgePort()) ) {
            	bbports.add(0, xp1);
            	bbports.add(1, yp2);
                return bbports;
            }
            // revert x y
            if (yp1.getBridgePort() == yp3.getBridgePort() 
                    && yp1.getBridgePort() != yp2.getBridgePort() 
                    && (xp1.getBridgePort() != xp2.getBridgePort() 
                    || xp2.getBridgePort() != xp3.getBridgePort()) ) {
            	bbports.add(0, xp2);
            	bbports.add(1, yp1);
                return bbports;
            }
            // exchange 3 with 1
            //m_1 belongs to FDB(yx,Y) FDB(p3,X)
            //m_2 belongs to FDB(p2,Y) FDB(xy,X) 
            //m_3 belongs to FDB(p1,Y) FDB(xy,X) 
            //
            //m_1 belongs to FDB(yx,Y) FDB(p3,X)
            //m_2 belongs to FDB(yx,Y) FDB(xy,X) 
            //m_3 belongs to FDB(p1,Y) FDB(xy,X) 
            if (xp3.getBridgePort() == xp2.getBridgePort() 
                    && xp1.getBridgePort() != xp3.getBridgePort() 
                    && (yp1.getBridgePort() != yp3.getBridgePort() 
                    || yp2.getBridgePort() != yp1.getBridgePort()) ) {
            	bbports.add(0, xp2);
            	bbports.add(1, yp1);
                return bbports;
            }
            if (yp3.getBridgePort() == yp2.getBridgePort()
                    && yp1.getBridgePort() != yp3.getBridgePort() 
                    && (xp1.getBridgePort() != xp3.getBridgePort() 
                    || xp2.getBridgePort() != xp1.getBridgePort()) ) {
            	bbports.add(0, xp1);
            	bbports.add(1, yp2);
                return bbports;
            }

        }
        // all macs on the same port
        if (mac2 == null) {
        	bbports.add(0, xp1);
        	bbports.add(1, yp1);
        	return bbports;
        }
        return bbports;
    }
    
    // condition 2 yx found                         m_x belongs to FDB(yx,Y)
    // if exists m_1 and m_2, p1 and p2 on Y :      m_1 belongs to FDB(p1,Y) FDB(xy,X)
    //                                              m_2 belongs to FDB(p2,Y) FDB(xy,X)
    //FIXME
    private static BridgePort condition2(Set<String> commonlearnedmacs, BridgePort yx, Map<String,BridgeForwardingTableEntry> ybft, Map<String,BridgeForwardingTableEntry> xbft) {
        String mac1=null;
        BridgePort p1=null;
        BridgePort xy1=null;
        BridgePort p2=null;
        BridgePort xy2=null;
        for (String mac: commonlearnedmacs) {
            if (mac1 == null) {
                mac1=mac;
                p1 = BridgePort.getFromBridgeForwardingTableEntry(ybft.get(mac));
                xy1= BridgePort.getFromBridgeForwardingTableEntry(xbft.get(mac));
                if (p1.getBridgePort().intValue() != yx.getBridgePort().intValue()) {
                    return xy1;
                }
                continue;
            }
            if (ybft.get(mac).getBridgePort().intValue() == p1.getBridgePort().intValue()) {
                continue;
            }
            p2 = BridgePort.getFromBridgeForwardingTableEntry(ybft.get(mac));
            xy2= BridgePort.getFromBridgeForwardingTableEntry(xbft.get(mac));
            // p1 and p2 are both different then yx
            if (xy2.getBridgePort().intValue() == xy1.getBridgePort().intValue()) {
                LOG.debug("condition2: p1 and p2 are both different then yx: xy bridge port {}",xy1.printTopology());
                return xy1;
            }
            // p1 is yx means the p2 is on the other side of the switch and xy2 is the port
            if (p1.getBridgePort().intValue() == yx.getBridgePort().intValue()) {
                LOG.debug("condition2: p1 is yx: p2 is on the other side of the switch: xy bridge port {}",xy2.printTopology());
                return xy2;
            }
            // p2 is yx means the p1 is on the other side of the switch and xy1 is the port
            if (p2.getBridgePort().intValue() == yx.getBridgePort().intValue()) {
                LOG.debug("condition2: p2 is yx: p1 is on the other side of the switch: xy bridge port {}",xy1.printTopology());
                return xy1;
            }
        }
        return null;
    }
    
    private static BridgePort condition1(Set<String> bridgemacaddressess, Map<String,BridgeForwardingTableEntry> otherbridgebft) {
        for (String mac: bridgemacaddressess) {
            if (otherbridgebft.containsKey(mac)) {
                LOG.debug("condition1: base address {} --> port: {} ",
                		mac,otherbridgebft.get(mac).getBridgePort());

                return BridgePort.getFromBridgeForwardingTableEntry(otherbridgebft.get(mac));
            }
        }
        LOG.debug("condition1: base address: {}. Not found.",
        		bridgemacaddressess);
        return null;
    }
            
    public Set<String> getSimpleConnectionMacs() {
        Set<String> macsOnSegment=new HashSet<String>();
        for (BridgeForwardingTableEntry xlink: m_xBridge.getBFTEntries()) {
            if (xlink.getBridgePort() == m_xyPort.getBridgePort() 
                    && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                if (m_ymactoport.get(xlink.getMacAddress()) != null 
                                && m_yxPort.getBridgePort() == m_ymactoport.get(xlink.getMacAddress()).getBridgePort()) {
                    macsOnSegment.add(xlink.getMacAddress());
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSimpleConnectionMacs: inter set ->\n{}.", 
                  macsOnSegment);
        }
        return macsOnSegment;
    }

    public Set<BridgePort> getSimpleConnectionPorts() {
        Set<BridgePort> ports = new HashSet<BridgePort>();
        ports.add(m_xyPort);
        ports.add(m_yxPort);
        return ports;
    }

    public Map<BridgePort,Set<String>> getTroughSet() {
        Set<BridgePort> excluded = new HashSet<BridgePort>();
        excluded.add(m_yxPort);
    	return BridgeForwardingTableEntry.getThroughSet(m_yBridge.getBFTEntries(),excluded);
    }

    public Set<BridgeForwardingTableEntry> getForwarders() {
        Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
        for (BridgeForwardingTableEntry link: m_xBridge.getBFTEntries()) {
            if (link.getBridgePort() == m_xyPort.getBridgePort() 
                && link.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED
                && m_ymactoport.get(link.getMacAddress()) == null 
            ) {
                forwarders.add(link);
            }
        }
        for (BridgeForwardingTableEntry link: m_yBridge.getBFTEntries()) {
            if (link.getBridgePort() == m_yxPort.getBridgePort() 
                && link.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED
                && m_xmactoport.get(link.getMacAddress()) == null 
            ) {
                forwarders.add(link);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getForwarders: ->\n-{}", 
              BridgeForwardingTableEntry.printTopology(forwarders));
        }
        return forwarders;
    }
    
    public String printTopology() {
        StringBuffer strbfr = new StringBuffer();
        strbfr.append("[");
        if (m_xyPort != null) {
            strbfr.append(m_xyPort.printTopology());
        } else {
            strbfr.append("null");
        }
        strbfr.append("], <--> [");
        if (m_yxPort != null) {
            strbfr.append(m_yxPort.printTopology());
        } else {
            strbfr.append("null");
        }
       strbfr.append("]");
       return strbfr.toString();
    }

}