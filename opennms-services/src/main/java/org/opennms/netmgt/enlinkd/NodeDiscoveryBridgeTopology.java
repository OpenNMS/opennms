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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryBridgeTopology extends NodeDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridgeTopology.class);


    private static final int DOMAIN_MATCH_MIN_SIZE = 5;
    private static final float DOMAIN_MATCH_MIN_RATIO = 0.1f;

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
    public class BridgeTopologyHelper {
        
        Integer m_xy;
        Integer m_yx;
        Map<BridgePort, Set<String>> m_throughSetX= new HashMap<BridgePort, Set<String>>();
        Map<BridgePort, Set<String>> m_throughSetY= new HashMap<BridgePort, Set<String>>();
        Set<BridgeForwardingTableEntry> m_forwardersX = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> m_forwardersY = new HashSet<BridgeForwardingTableEntry>();
        Set<String> m_macsOnSegment=new HashSet<String>();
        BridgePort m_xyPort;
        BridgePort m_yxPort;

        public BridgeTopologyHelper(Bridge xBridge, Set<BridgeForwardingTableEntry> xBFT,Bridge yBridge, Set<BridgeForwardingTableEntry> yBFT) {
            super();
            Map<String,BridgeForwardingTableEntry> xmactoport = new HashMap<String, BridgeForwardingTableEntry>();
            Map<String,BridgeForwardingTableEntry> ymactoport = new HashMap<String, BridgeForwardingTableEntry>();
            if (LOG.isDebugEnabled()) {
                LOG.debug("simple connection [{} <--> {}]: searching.\n xbft -> \n{}\n ybft -> \n{}",
                    xBridge.getNodeId(),
                    yBridge.getNodeId(),
                    BridgeForwardingTableEntry.printTopology(xBFT),
                    BridgeForwardingTableEntry.printTopology(yBFT));
            }
            for (BridgeForwardingTableEntry xlink: xBFT) {
                if (xBridge.getNodeId().intValue() == xlink.getNodeId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    xmactoport.put(xlink.getMacAddress(), xlink);
                }
                if (xBridge.getNodeId().intValue() == xlink.getNodeId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    xBridge.getIdentifiers().add(xlink.getMacAddress());
            }
            for (BridgeForwardingTableEntry ylink: yBFT) {
                if (yBridge.getNodeId().intValue() == ylink.getNodeId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    ymactoport.put(ylink.getMacAddress(), ylink);
                }
                if (yBridge.getNodeId().intValue() == ylink.getNodeId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    yBridge.getIdentifiers().add(ylink.getMacAddress());
            }

            // there is a mac of Y found on X BFT
            Integer xy = condition1(yBridge.getIdentifiers(), xmactoport);
            if (xy != null) {
            	m_xy=xy;
                LOG.debug("simple connection: [{} port: {} --> {}].",
                          xBridge.getNodeId(),
                          m_xy,
                          yBridge.getNodeId());
            }

            // there is a mac of X found on Y BFT
            Integer yx = condition1(xBridge.getIdentifiers(), ymactoport);
            if (yx != null) {
            	m_yx=yx;
                LOG.debug("simple connection: [{} <-- {} port: {}].",
                          xBridge.getNodeId(),
                          yBridge.getNodeId(),
                          m_yx);
            }
            	            
            if (m_xy == null || m_yx == null) {
                Set<String> commonlearnedmacs = new HashSet<String>(xmactoport.keySet()); 
                commonlearnedmacs.retainAll(new HashSet<String>(ymactoport.keySet()));
                if (m_yx != null && m_xy == null) { 
                    m_xy = condition2(commonlearnedmacs,m_yx,ymactoport,xmactoport);
                } else if (m_yx == null && m_xy != null) {
                    m_yx = condition2(commonlearnedmacs,m_xy,xmactoport,ymactoport);
                } else {
                    List<Integer> ports = condition3(commonlearnedmacs,xmactoport,ymactoport);
                    if (ports.size() == 2) {
                        m_xy = ports.get(0);
                        m_yx= ports.get(1);
                    }
                }
                LOG.debug("simple connection: [{}, port {} <--> {}, port: {}] common (learned mac): {}",
                          xBridge.getNodeId(),
                          m_xy,
                          yBridge.getNodeId(),
                          m_yx,
                          commonlearnedmacs);

            }    
            //FIXME What do do if I do not find the connection?
            if (m_xy == null || m_xy == null) {
                LOG.warn("simple connection: [{}, port {}] <--> [{}, port {}]. Not found. exiting", 
                          xBridge.getNodeId(), 
                          m_xy,
                          yBridge.getNodeId()
                          , m_yx);
                return;
            }
            
            BridgeForwardingTableEntry xylink = null;
            BridgeForwardingTableEntry yxlink = null;
            for (BridgeForwardingTableEntry xlink: xBFT) {
                if (xlink.getBridgePort() == m_xy && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    if (ymactoport.get(xlink.getMacAddress()) != null 
                    		&& m_yx == ymactoport.get(xlink.getMacAddress()).getBridgePort()) {
                    	m_macsOnSegment.add(xlink.getMacAddress());
                        LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], forward set: [bridge:[{}],port:{},mac:{}].", 
                                  xBridge.getNodeId(), 
                                  m_xy,
                                  yBridge.getNodeId()
                                  , m_yx,
                                  xlink.getNodeId(),
                    		xlink.getBridgePort(),
                    		xlink.getMacAddress());
                    } else if (ymactoport.get(xlink.getMacAddress()) == null){
                        m_forwardersX.add(xlink);
                        LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                                  xBridge.getNodeId(), 
                                  m_xy,
                                  yBridge.getNodeId()
                                  , m_yx,
                                  xlink.getNodeId(),
                                xlink.getBridgePort(),
                                xlink.getMacAddress());
                    }
                    if (xylink == null)
                        xylink = xlink;
                } else if (xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                              xBridge.getNodeId(), 
                              m_xy,
                              yBridge.getNodeId()
                              , m_yx,
                              xlink.getNodeId(),
                            xlink.getBridgePort(),
                            xlink.getMacAddress());
                    if (!m_throughSetX.containsKey(BridgePort.getFromBridgeForwardingTableEntry(xlink))) {
                    	m_throughSetX.put(BridgePort.getFromBridgeForwardingTableEntry(xlink), new HashSet<String>());
                    }
                    m_throughSetX.get(BridgePort.getFromBridgeForwardingTableEntry(xlink)).add(xlink.getMacAddress());
                }
            }
            
            for (BridgeForwardingTableEntry ylink: yBFT) {
                if (ylink.getBridgePort() == m_yx && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    if ( xmactoport.get(ylink.getMacAddress()) != null &&
                    		m_xy == xmactoport.get(ylink.getMacAddress()).getBridgePort()) {
                    	m_macsOnSegment.add(ylink.getMacAddress());
                        LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], forward set: [bridge:[{}],port:{},mac:{}].", 
                                  xBridge.getNodeId(), 
                                  m_xy,
                                  yBridge.getNodeId()
                                  , m_yx,
                                  ylink.getNodeId(),
                                ylink.getBridgePort(),
                                ylink.getMacAddress());
                    } else if (xmactoport.get(ylink.getMacAddress()) == null){
                        m_forwardersY.add(ylink);
                        LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                                  xBridge.getNodeId(), 
                                  m_xy,
                                  yBridge.getNodeId()
                                  , m_yx,
                                  ylink.getNodeId(),
                                ylink.getBridgePort(),
                                ylink.getMacAddress());
                    }
                    if (yxlink == null)
                        yxlink = ylink;
                } else if (ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                              xBridge.getNodeId(), 
                              m_xy,
                              yBridge.getNodeId()
                              , m_yx,
                              ylink.getNodeId(),
                            ylink.getBridgePort(),
                            ylink.getMacAddress());
                    if (!m_throughSetY.containsKey(BridgePort.getFromBridgeForwardingTableEntry(ylink))) {
                    	m_throughSetY.put(BridgePort.getFromBridgeForwardingTableEntry(ylink), new HashSet<String>());
                    }
                    m_throughSetY.get(BridgePort.getFromBridgeForwardingTableEntry(ylink)).add(ylink.getMacAddress());
                }
            }
            LOG.debug("simple connection: [{}, port {}] <--> [{}, port {}], interse set:{}.",
                      xBridge.getNodeId(), 
                      m_xy,
                      yBridge.getNodeId()
                      , m_yx,
            		m_macsOnSegment);
    
            if (yxlink != null ) {
                m_yxPort = BridgePort.getFromBridgeForwardingTableEntry(yxlink);
            }
            
            if (xylink != null) {
                m_xyPort = BridgePort.getFromBridgeForwardingTableEntry(xylink);
            }
        }

        private List<Integer> condition3(Set<String> commonlearnedmacs,Map<String,BridgeForwardingTableEntry> xbft,Map<String,BridgeForwardingTableEntry> ybft) {
        
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
            Integer yp1=null;
            Integer yp2=null;
            Integer xp1=null;
            Integer xp2=null;
            List<Integer> bbports = new ArrayList<Integer>(2);
            for (String mac: commonlearnedmacs) {
                LOG.debug("condition3: parsing common BFT mac: {}",mac);
                if (mac1 == null) {
                    mac1=mac;
                    yp1=ybft.get(mac).getBridgePort();
                    xp1=xbft.get(mac).getBridgePort();
                    LOG.debug("condition3: mac1: {} xp1: {} yp1: {} ", mac1,xp1,yp1);
                    continue;
                }
                if (ybft.get(mac).getBridgePort() == yp1 && xbft.get(mac).getBridgePort() == xp1)
                    continue;
                if (mac2 == null) {
                    mac2=mac;
                    yp2=ybft.get(mac).getBridgePort();
                    xp2=xbft.get(mac).getBridgePort();
                    LOG.debug("condition3: mac2: {} xp2: {} yp2: {} ", mac2,xp2,yp2);
                    continue;
                }
                if (ybft.get(mac).getBridgePort() == yp2 && xbft.get(mac).getBridgePort() == xp2)
                    continue;
                Integer yp3 = ybft.get(mac).getBridgePort();
                Integer xp3 = xbft.get(mac).getBridgePort();
                LOG.debug("condition3: mac3: {} x3: {} yp3: {} ", mac,xp3,yp3);

                //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
                //m_2 belongs to FDB(p2,Y) FDB(xy,X) 
                //m_3 belongs to FDB(yx,Y) FDB(p3,X)
                //
                //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
                //m_2 belongs to FDB(yx,Y) FDB(xy,X) 
                //m_3 belongs to FDB(yx,Y) FDB(p3,X)
                if (xp1 == xp2 && xp1 != xp3 && (yp1 != yp3 || yp2 != yp3) ) {
                	bbports.add(0, xp1);
                	bbports.add(1, yp3);
                    return bbports;
                }
                // exchange x y
                if (yp1 == yp2 && yp1 != yp3 && (xp1 != xp3 || xp2 != xp3) ) {
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
                if (xp1 == xp3 && xp1 != xp2 && (yp1 != yp2 || yp2 != yp3) ) {
                	bbports.add(0, xp1);
                	bbports.add(1, yp2);
                    return bbports;
                }
                // revert x y
                if (yp1 == yp3 && yp1 != yp2 && (xp1 != xp2 || xp2 != xp3) ) {
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
                if (xp3 == xp2 && xp1 != xp3 && (yp1 != yp3 || yp2 != yp1) ) {
                	bbports.add(0, xp2);
                	bbports.add(1, yp1);
                    return bbports;
                }
                if (yp3 == yp2 && yp1 != yp3 && (xp1 != xp3 || xp2 != xp1) ) {
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
        private Integer condition2(Set<String> commonlearnedmacs, Integer yx, Map<String,BridgeForwardingTableEntry> ybft, Map<String,BridgeForwardingTableEntry> xbft) {
            String mac1=null;
            String mac2=null;
            Integer p1=null;
            Integer xy1=null;
            Integer p2=null;
            Integer xy2=null;
            for (String mac: commonlearnedmacs) {
                if (ybft.get(mac) == null || ybft.get(mac).getBridgePort() == null 
                        ||xbft.get(mac) == null || xbft.get(mac).getBridgePort() == null )
                    continue;
                if (mac1 == null) {
                    mac1 = mac;
                    p1 = ybft.get(mac).getBridgePort();
                    xy1= xbft.get(mac).getBridgePort();
                    LOG.debug("condition2: mac1: {} xy1: {} p1: {} ", mac1,xy1,p1);
                    if (p1.intValue() != yx.intValue()) {
                        LOG.debug("condition2: p1 is not yx: so is on the other side. xy bridge port {}",xy1);
                        return xy1;
                    }
                    continue;
                }
                if (ybft.get(mac).getBridgePort().intValue() == p1.intValue())
                    continue;
                mac2 = mac;
                p2 = ybft.get(mac).getBridgePort();
                xy2= xbft.get(mac).getBridgePort();
                LOG.debug("condition2: mac2: {} xy2: {} p2: {} ", mac2,xy2,p2);
                // p1 and p2 are both different then yx
                if (xy2.intValue() == xy1.intValue()) {
                    LOG.debug("condition2: p1 and p2 are both different then yx: xy bridge port {}",xy1);
                    return xy1;
                }
                // p1 is yx means the p2 is on the other side of the switch and xy2 is the port
                if (p1.intValue() == yx) {
                    LOG.debug("condition2: p1 is yx: p2 is on the other side of the switch: xy bridge port {}",xy2);
                    return xy2;
                }
                // p2 is yx means the p1 is on the other side of the switch and xy1 is the port
                if (p2.intValue() == yx) {
                    LOG.debug("condition2: p2 is yx: p1 is on the other side of the switch: xy bridge port {}",xy1);
                    return xy1;
                }
            }
            return null;
        }
        
        private Integer condition1(Set<String> bridgemacaddressess, Map<String,BridgeForwardingTableEntry> otherbridgebft) {
            for (String mac: bridgemacaddressess) {
                if (otherbridgebft.containsKey(mac)) {
                    LOG.debug("condition1: base address {} --> port: {} ",
                    		mac,otherbridgebft.get(mac).getBridgePort());

                    return otherbridgebft.get(mac).getBridgePort();
                }
            }
            LOG.debug("condition1: base address: {}. Not found.",
            		bridgemacaddressess);
            return null;
        }
        
        public Integer getFirstBridgeConnectionPort() {
            return m_xy;
        }
        
        public Integer getSecondBridgeConnectionPort() {
            return m_yx;
        }
        
        public BridgePort getFirstBridgePort() {
            return m_xyPort;
        }
        
        public BridgePort getSecondBridgePort() {
            return m_yxPort;
        }
        
        public Set<String> getSimpleConnectionMacs() {
            return m_macsOnSegment; 
        }


        public Map<BridgePort,Set<String>> getFirstBridgeTroughSetBft() {
            return m_throughSetX;
        }

        public Map<BridgePort,Set<String>> getSecondBridgeTroughSetBft() {
        	return m_throughSetY;
        }

        public Set<BridgeForwardingTableEntry> getFirstBridgeForwarders() {
            return m_forwardersX;
        }

        public Set<BridgeForwardingTableEntry> getSecondBridgeForwarders() {
            return m_forwardersY;
        }

        public Set<BridgePort> getSimpleConnection() {
            Set<BridgePort> ports = new HashSet<BridgePort>();
            ports.add(m_xyPort);
            ports.add(m_yxPort);
            return ports;
        }

    }

    Map<Bridge,Set<BridgeForwardingTableEntry>> m_notYetParsedBFTMap;
    BroadcastDomain m_domain;
    //List<BridgeStpLink> m_STPLinks = new ArrayList<BridgeStpLink>();

    public BroadcastDomain getDomain() {
        return m_domain;
    }

    public void setDomain(BroadcastDomain domain) {
        m_domain = domain;
    }

    public Map<Bridge, Set<BridgeForwardingTableEntry>> getNotYetParsedBFTMap() {
        return m_notYetParsedBFTMap;
    }

    public void addUpdatedBFT(Bridge bridge, Set<BridgeForwardingTableEntry> notYetParsedBFT) {
        if (m_notYetParsedBFTMap==null)
            m_notYetParsedBFTMap = new HashMap<Bridge, Set<BridgeForwardingTableEntry>>();
        m_notYetParsedBFTMap.put(bridge, notYetParsedBFT);
    }

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
    }

    private Set<Integer> getAllNodesWithUpdatedBFTOnDomain(Set<String>incomingSet, Map<Integer,Set<BridgeForwardingTableEntry>> nodeBftMap) {
        Set<Integer> nodeswithupdatedbftonbroadcastdomain= new HashSet<Integer>();
        nodeswithupdatedbftonbroadcastdomain.add(getNodeId());

        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. Start", getNodeId());
        synchronized (nodeBftMap) {
            for (Integer curNodeId: nodeBftMap.keySet()) {
                if (curNodeId.intValue() == getNodeId())
                    continue;
                Set<String>retainedSet = new HashSet<String>();
                for (BridgeForwardingTableEntry link: nodeBftMap.get(curNodeId)) {
                    retainedSet.add(link.getMacAddress());
                }
                LOG.debug("run: node: [{}], parsing updated bft node: [{}], macs {}", getNodeId(), curNodeId,retainedSet);
                retainedSet.retainAll(incomingSet);
                LOG.debug("run: node: [{}], node: [{}] - common mac address set: {}", getNodeId(), curNodeId, retainedSet);
                if (retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
                        || retainedSet.size() >= incomingSet.size() * DOMAIN_MATCH_MIN_RATIO) {
                    nodeswithupdatedbftonbroadcastdomain.add(curNodeId);
                    LOG.debug("run: node: [{}], node: [{}] - put on same broadcast domain, common macs: {} ", getNodeId(), 
                             curNodeId,
                             retainedSet);
                }
            }            
        }
        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. End", getNodeId());
        return nodeswithupdatedbftonbroadcastdomain;
    }
    
    public void updateBridgeOnDomain() {
        if (m_domain == null) {
            return;
        }

        for (Bridge bridge: m_domain.getBridges()) {
            bridge.clear();
            for (BridgeElement element: m_linkd.getQueryManager().getBridgeElements(bridge.getNodeId())) {
                if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress())) {
                    bridge.getIdentifiers().add(element.getBaseBridgeAddress());
                }
                if (InetAddressUtils.
                        isValidStpBridgeId(element.getStpDesignatedRoot()) 
                        && !element.getBaseBridgeAddress().
                        equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
                    bridge.setDesignated(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()));
                }
            }
        }
    }


    @Override
    public void run() {

        if (!m_linkd.getQueryManager().hasUpdatedBft(getNodeId())) {
            LOG.info("run: node: [{}], no bft.Exiting Bridge Topology Discovery", getNodeId());
            return;
        }

    	Set<BridgeForwardingTableEntry> links =  m_linkd.
        		getQueryManager().
        		getBridgeTopologyUpdateBFT(
        				getNodeId());
    	if (links == null || links.size() == 0) {
            LOG.info("run: node: [{}]. no updates macs found.", getNodeId());
            return;
    	}
    	Date now = new Date();
                
        Set<String> incomingSet = new HashSet<String>();
        synchronized (links) {
            for (BridgeForwardingTableEntry link : links) {
                incomingSet.add(link.getMacAddress());
            }            
        }
        LOG.info("run: node: [{}]. macs found: {}", getNodeId(), incomingSet);

        LOG.info("run: node: [{}], getting broadcast domain. Start", getNodeId());
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("run: node: [{}], parsing domain: {}", 
                      getNodeId(),
                      domain.printTopology());
            }
            Set<String>retainedSet = new HashSet<String>(
                                                          domain.getMacsOnDomain());
            retainedSet.retainAll(incomingSet);
            LOG.info("run: node: [{}], retained: {}", getNodeId(), retainedSet);
            // should contain at list 5 or 10% of the all size
            if (retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
                    || retainedSet.size() >= incomingSet.size() * DOMAIN_MATCH_MIN_RATIO) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("run: node: [{}], domain {} found!",getNodeId(), 
                          domain.printTopology());
                }
                m_domain = domain;
                // TODO: We should find the *best* domain, instead of using the last match
            }
        }
        if (m_domain == null) {
            LOG.info("run: node: [{}] Creating a new Domain", getNodeId());
            m_domain = new BroadcastDomain();
            m_linkd.getQueryManager().save(m_domain);
        } else if (m_domain.getRootBridge() == null) {
            //FIXME????
            LOG.warn("run: node: [{}] Domain without root, clearing topology", getNodeId());
            m_domain.clearTopology();
        }
        LOG.info("run: node: [{}], getting broadcast domain. End", getNodeId());
                
        Map<Integer,Set<BridgeForwardingTableEntry>> nodeBftMap = m_linkd.getQueryManager().getUpdateBftMap();
        Set<Integer> nodeswithupdatedbftonbroadcastdomain = getAllNodesWithUpdatedBFTOnDomain(incomingSet,nodeBftMap);            

        LOG.info("run: node: [{}], clean broadcast domains. Start", getNodeId());
        boolean clean = false;
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
        	if (m_domain == domain)
        		continue;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("run: node [{}]: cleaning broadcast domain {}.",
            		getNodeId(),
                      domain.getBridgeNodesOnDomain());
                }
            for (Integer curNodeId: nodeswithupdatedbftonbroadcastdomain) {
                Bridge bridge = domain.getBridge(curNodeId);
                if (bridge != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("run: node [{}]: node [{}]: removing from broadcast domain {}!",
                    		getNodeId(),
                    		  curNodeId,
                              domain.getBridgeNodesOnDomain());
                    }
                    synchronized (domain) {
                        try {
                            domain.clearTopologyForBridge(bridge);
                        } catch (BridgeTopologyException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        domain.removeBridge(curNodeId);
                        m_linkd.getQueryManager().store(domain,now);
                    }
                    clean=true;
                }
            }
        }
        if (clean) {
            m_linkd.getQueryManager().cleanBroadcastDomains();
        }
        LOG.info("run: node: [{}], clean broadcast domains. End", getNodeId());

        synchronized (m_domain) {
            m_notYetParsedBFTMap = new HashMap<Bridge, Set<BridgeForwardingTableEntry>>();
            for (Integer nodeid : nodeswithupdatedbftonbroadcastdomain) {
                sendStartEvent(nodeid);
                //FIXME use Bridge class for moving from one domain to another
                if (m_domain.addBridge(new Bridge(nodeid))) {
                    LOG.info("run: node: [{}], added bridge  node [{}] on domain", getNodeId(), nodeid);
                } else {
                    
                }
                LOG.debug("run: node: [{}], getting update bft for node [{}] on domain", getNodeId(), nodeid);
                Set<BridgeForwardingTableEntry> bft = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(nodeid);
                if (bft == null || bft.isEmpty()) {
                    LOG.debug("run: node: [{}], no update bft for node [{}] on domain", getNodeId(), nodeid);
                    continue;
                }
                m_notYetParsedBFTMap.put(m_domain.getBridge(nodeid), bft);
            }

            Set<Integer> nodetoberemovedondomain = new HashSet<Integer>();
            synchronized (nodeBftMap) {
                for (Integer nodeid : nodeBftMap.keySet()) {
                    if (nodeswithupdatedbftonbroadcastdomain.contains(nodeid))
                        continue;
                    LOG.info("run: node [{}]: bridge [{}] with updated bft. Not even more on broadcast domain {}: clear topology.",
                            getNodeId(),
                            nodeid,
                            m_domain.getBridgeNodesOnDomain());
                    nodetoberemovedondomain.add(nodeid);
                }
            }
            for (Integer nodeid : nodetoberemovedondomain) {
                try {
                    Bridge bridge = m_domain.getBridge(nodeid);
                    m_domain.clearTopologyForBridge(bridge);
                } catch (BridgeTopologyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                m_domain.removeBridge(nodeid);
            }
            m_linkd.getQueryManager().cleanBroadcastDomains();

            //FIXME check everything is right
            updateBridgeOnDomain();

            if (m_notYetParsedBFTMap.isEmpty()) {
                LOG.info("run: node: [{}], broadcast domain has no topology updates. No more action is needed.", getNodeId());
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("calculate: node: [{}], start: broadcast domain {} topology calculation.", 
                         getNodeId(),
                         m_domain.printTopology());
                }
                calculate();
                if (LOG.isInfoEnabled()) {
                    LOG.info("calculate: node: [{}], end: broadcast domain {} topology calculation.", 
                         getNodeId(),
                         m_domain.printTopology());
                }
            }

            LOG.info("run: node: [{}], saving Topology.", getNodeId());
            m_linkd.getQueryManager().store(m_domain, now);
            LOG.info("run: node: [{}], saved Topology.", getNodeId());

            for (Integer curNode : nodeswithupdatedbftonbroadcastdomain) {
                sendCompletedEvent(curNode);
            }
        }
    }
            
    @Override
    protected void runCollection() {
    }

    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

    private Bridge getElectedRootBridge() throws BridgeTopologyException {
        
        Bridge electedRoot = m_domain.electRootBridge();
        
        Bridge rootBridge = m_domain.getRootBridge();
        if (electedRoot == null && rootBridge != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], electRootBridge: mantaining old root bridge: {}", 
                    getNodeId(),
                    rootBridge.printTopology());
            }
            electedRoot = m_domain.getRootBridge();        	
        } else if (electedRoot == null) {
	        // no spanning tree root?
	        // why I'm here?
	        // not root bridge defined (this mean no calculation yet done...
	        // so checking the best into not parsed
	        int size=0;
	        
	        for (Bridge bridge:  m_notYetParsedBFTMap.keySet()) {
	            LOG.debug("calculate: node: [{}], bridge [{}]: max bft size \"{}\" in topology",
	                    getNodeId(),
	                    bridge.getNodeId(), 
	                    m_notYetParsedBFTMap.get(bridge).size());
	            if (size < m_notYetParsedBFTMap.get(bridge).size()) {
	                rootBridge = bridge;
	                size = m_notYetParsedBFTMap.get(bridge).size();
	            }
	        }
	        if (rootBridge != null ) {
	            if (LOG.isDebugEnabled()) {
	                LOG.debug("calculate: node: [{}], bridge [{}]: elected root with max bft size \"{}\" in topology",
	                    getNodeId(),
	                    rootBridge.getNodeId(), 
	                    size);
	            }
	            electedRoot = rootBridge;
	        }
        } 
        
        if (electedRoot == null ) {
            electedRoot = m_domain.getBridges().iterator().next();
            if (LOG.isDebugEnabled()) {
        	LOG.debug("calculate: node: [{}], electRootBridge: first root bridge: {}", 
                       getNodeId(),
                       electedRoot.printTopology());
            }
        }

        if (electedRoot.getNodeId() == null) {
            LOG.error("calculate: node: [{}], electedRootBridge must have an id!",
                    getNodeId()
            		);
            throw new BridgeTopologyException("electedRoot bridge id cannot be null", electedRoot);
        }
        return electedRoot;
        
    }

    private Set<BridgeForwardingTableEntry> getRootBridgeForwardingTable(Bridge electedRoot) throws BridgeTopologyException {

        Bridge rootBridge = m_domain.getRootBridge();
        Set<BridgeForwardingTableEntry> rootBft = 
                m_notYetParsedBFTMap.remove(electedRoot);
        
        if (rootBridge != null && rootBridge.getNodeId() == electedRoot.getNodeId() && rootBft == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], no updated bft, elected is equal to old root bridge: {}",
                    getNodeId(), 
            		electedRoot.printTopology());
            }
            rootBft = m_domain.calculateRootBFT();
        } else if ( rootBft != null ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], elected root bridge: [{}], has updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
            }
            m_domain.clearTopologyForBridge(electedRoot);
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], root bridge: [{}], cleared topology:\n{}",
                      getNodeId(), 
                      electedRoot.printTopology(),
                       m_domain.printTopology());
            }
            if (m_domain.getSharedSegments().isEmpty()) {
               LOG.debug("calculate: node: [{}], new elected root bridge: [{}], is the first bridge in topology. Adding root shared segments",
                       getNodeId(), 
                        electedRoot.getNodeId());
                loadFirstLevelSharedSegment(rootBft);
                electedRoot.setRootBridge();
           } else {
                calculate(m_domain.getRootBridge(), m_domain.calculateRootBFT(),
                                electedRoot, rootBft);
                addForwarding(m_domain, rootBft);
                m_domain.hierarchySetUp(electedRoot);
           }
        } else {
           LOG.debug("calculate: node: [{}], elected root bridge: [{}], is new, without updated bft",
                    getNodeId(), 
                     electedRoot.getNodeId());
           m_domain.hierarchySetUp(electedRoot);
           rootBft = m_domain.calculateRootBFT();
        }
        return rootBft;
    }

    protected  void calculate() {
        Bridge electedRoot =null;
        try {
            electedRoot = getElectedRootBridge();
        } catch (BridgeTopologyException e) {
            LOG.error("calculate: node: [{}], no bridge to be elected.Clearing topology:\n{}",
                      getNodeId(), 
                      m_domain.printTopology());
            m_domain.clearTopology();
            m_domain.getBridgeNodesOnDomain().clear();
        }
        Set<BridgeForwardingTableEntry> rootBft = null;
        try {
            rootBft = getRootBridgeForwardingTable(electedRoot);
        } catch (BridgeTopologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (!m_notYetParsedBFTMap.isEmpty()) {
            for (Bridge xBridge: m_notYetParsedBFTMap.keySet()) {
                try {
                    m_domain.clearTopologyForBridge(xBridge);
                } catch (BridgeTopologyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}], Removed bridge: [{}] form domain: {}", 
                		getNodeId(),
                		xBridge.getNodeId(),
                		m_domain.printTopology());
                }
            }
        }

        Set<Bridge> nodetobeparsed = new HashSet<Bridge>(m_notYetParsedBFTMap.keySet());
        for (Bridge xBridge: nodetobeparsed) {
            Set<BridgeForwardingTableEntry> xBft = new HashSet<BridgeForwardingTableEntry>(m_notYetParsedBFTMap.remove(xBridge));
            try {
                calculate(electedRoot, rootBft, xBridge, xBft);
            } catch (BridgeTopologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
        m_domain.cleanForwarders();
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}], Print Topology {}",
                    getNodeId(),
                    m_domain.printTopology());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("calculate: node: [{}], stop: broadcast domain {} topology calculated.",
                    getNodeId(),
                    m_domain.getBridgeNodesOnDomain());
        }
    }
     
    private void addForwarding(BroadcastDomain domain, Set<BridgeForwardingTableEntry> bft) {
        for (BridgeForwardingTableEntry maclink: bft) {
            if (domain.getMacsOnDomain().contains(maclink.getMacAddress())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}]. Skipping forwarding: {}",
                          getNodeId(), 
                          maclink.printTopology());
                }
                continue;                    
            }
            domain.addForwarding(maclink);
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}]. Adding forwarding: {}",
                      getNodeId(), 
                      maclink.printTopology());
            }
        }
    }

    private void loadFirstLevelSharedSegment(Set<BridgeForwardingTableEntry> electedRootBFT) throws BridgeTopologyException {
        Map<BridgePort, Set<String>> rootleafs = new HashMap<BridgePort, Set<String>>();
        
        for (BridgeForwardingTableEntry link : electedRootBFT) {
            if (link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                continue;
            }
            if (!rootleafs.containsKey(BridgePort.getFromBridgeForwardingTableEntry(link))) {
                rootleafs.put(BridgePort.getFromBridgeForwardingTableEntry(link),
                              new HashSet<String>());
            }
            rootleafs.get(BridgePort.getFromBridgeForwardingTableEntry(link)).add(link.getMacAddress());
        }
        
        for (BridgePort port : rootleafs.keySet()) {
            SharedSegment segment = new SharedSegment(m_domain,port,rootleafs.get(port));
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}], add shared segment[designated bridge:[{}],"
            		+ "designated port:{}, macs: {}]",
            		 getNodeId(),
                     segment.getDesignatedBridge(),
                     segment.getDesignatedPort().getBridgePort(),
                     segment.getMacsOnSegment());
            }
        }      
   }
    
    private void calculate(Bridge root,  
            Set<BridgeForwardingTableEntry> rootbft, 
            Bridge xBridge, 
            Set<BridgeForwardingTableEntry> xbft) throws BridgeTopologyException {
        //FIXME        checkStp(root, xBridge);
        BridgeTopologyHelper rx = new BridgeTopologyHelper(root, rootbft, xBridge,xbft);
        Integer rxDesignatedPort = rx.getFirstBridgeConnectionPort();
        if (rxDesignatedPort == null) {
            LOG.warn("calculate: node: [{}], cannot found simple connection for bridges: [{},{}]", 
            		getNodeId(),
            		root.getNodeId(), 
            		xBridge.getNodeId());
            m_domain.clearTopology();
            return;
        }
        Integer xrDesignatedPort = rx.getSecondBridgeConnectionPort();
        if (xrDesignatedPort == null) {
             LOG.warn("calculate: node: [{}], cannot found simple connectionfor bridges: [{},{}]",
             		getNodeId(),
             		xBridge.getNodeId(), 
             		root.getNodeId());
             m_domain.clearTopology();
             return;
        }
        LOG.debug("calculate: node: [{}], level: 1, bridge: [{}], root port:[{}] ",
        		getNodeId(),
        		xBridge.getNodeId(),
        		xrDesignatedPort);
        xBridge.setRootPort(xrDesignatedPort);
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = m_domain.getSharedSegment(root.getNodeId(),rxDesignatedPort);
        if (topSegment == null) {
            LOG.warn("calculate: node: [{}], level: 1, nodeid: [{}], port {}. top segment not found.",
            		getNodeId(),
            		m_domain.getRootBridge().getNodeId(),
            		rxDesignatedPort);
            m_domain.clearTopology();
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}], bridge: [{}] -> root segment: \n{} ",
                        getNodeId(),
                        xBridge.getNodeId(),
                        topSegment.printTopology());
        }

        if (!findBridgesTopo(rx,topSegment, xBridge, xbft,0))
            m_domain.clearTopology();
    }

    // here we assume that rbridge exists in topology
    // while xBridge is to be added
    private boolean findBridgesTopo(BridgeTopologyHelper rx,
            SharedSegment topSegment, 
            Bridge xBridge, 
            Set<BridgeForwardingTableEntry> xBFT, 
            int level) throws BridgeTopologyException {
        if (topSegment == null) {
            LOG.warn("calculate: node: [{}]: level: {}, bridge: [{}], top segment is null exiting.....",
                     getNodeId(),
                     level,
                     xBridge.getNodeId());
         return false;
        }
        level++;
        if (level == 30) {
            LOG.warn("calculate: node: [{}]: level: {}, bridge: [{}], too many iteration on topology exiting.....",
                        getNodeId(),
                        level,
                        xBridge.getNodeId());
            return false;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}], level: {}, bridge: [{}] -> check if is child of: \n{} ",
        		getNodeId(),
        		level,
        		xBridge.getNodeId(),
        		topSegment.printTopology());
        }
        Set<Integer> portsAdded=new HashSet<Integer>();
        Set<String> macsOnSegment=rx.getSimpleConnectionMacs();
        Map<Integer,Set<BridgeForwardingTableEntry>> bftSets=new HashMap<Integer,Set<BridgeForwardingTableEntry>>();
        Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
        forwarders.addAll(rx.getFirstBridgeForwarders());
        forwarders.addAll(rx.getSecondBridgeForwarders());

        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            bftSets.put(yBridge.getNodeId(), m_domain.calculateBFT(yBridge));
        }
        
        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            Integer yBridgeId = yBridge.getNodeId();
            // X is a leaf of top segment: of course
            if (yBridgeId.intValue() == topSegment.getDesignatedBridge().intValue()) {
                continue;
            } 
            Integer yrDesignatedPort = yBridge.getRootPort();
            LOG.debug("calculate: node: [{}], level: {}, bridge: [{}], bridge: [{}, designated port: {}]",
            		getNodeId(),
                     level,
                     xBridge.getNodeId(),
                     yBridgeId,
                     yrDesignatedPort);
            BridgeTopologyHelper   yx = new BridgeTopologyHelper(yBridge,bftSets.get(yBridgeId) ,xBridge, xBFT);
            Integer  xyDesignatedPort = yx.getSecondBridgeConnectionPort();
            Integer  yxDesignatedPort = yx.getFirstBridgeConnectionPort();
            // if X is a leaf of Y then iterate
            if (xyDesignatedPort == rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.debug("calculate: node: [{}]: level: {}, bridge: [{}] is a leaf of bridge: [{}], going one level down",
                		getNodeId(),
                		level,xBridge.getNodeId(),yBridge.getNodeId());
                if (!findBridgesTopo(yx,m_domain.getSharedSegment(yBridgeId, yxDesignatedPort), xBridge, xBFT,level))
                	return false;
                return true;
            }
            // Y is a leaf of X then 
            // remove Y from topSegment
            // Create shared Segment with X designated bridge 
            // or if exists then retain all common macs on domain
            // Assign Forwarders for Y
            // Clean also topSegment macs.
            if (yxDesignatedPort == yrDesignatedPort && xyDesignatedPort != rx.getSecondBridgeConnectionPort()) {
                //create a SharedSegment with root port
                LOG.info("calculate: node: [{}], level: {}, bridge: [{},designated port [{}]]: found level.", 
                          getNodeId(), 
                          level,
                          xBridge.getNodeId(),
                          xyDesignatedPort);
                LOG.debug("calculate: node: [{}], level: {}, bridge: [{},designated port [{}]]: is 'hierarchy up' for bridge: [{}].", 
                		getNodeId(), 
                		level,
                		xBridge.getNodeId(),
                		xyDesignatedPort,
                                yBridge.getNodeId());
                SharedSegment leafSegment = m_domain.getSharedSegment(xBridge.getNodeId(), xyDesignatedPort);
                if (leafSegment == null) {
                    leafSegment = new SharedSegment(m_domain,yx.getSimpleConnection(),yx.getSimpleConnectionMacs(),xBridge.getNodeId());
                } else {
                    leafSegment.retain(yx.getSimpleConnectionMacs(),yx.getFirstBridgePort());
                }
                portsAdded.add(xyDesignatedPort);
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("calculate: node: [{}], level: {}, bridge [{}]. Remove bridge [{}] and macs {} from top segment.\n{}", 
                		getNodeId(), 
                         level,
                         xBridge.getNodeId(),
                         yBridge.getNodeId(),
                         topSegment.getMacsOnSegment(),topSegment.printTopology());
                }
                topSegment.getMacsOnSegment().clear();
                topSegment.removeBridge(yBridgeId);
            } else if (xyDesignatedPort != rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.warn("calculate: node: [{}]: level {}: bridge [{}]. Topology mismatch. Clearing...topology",
                		getNodeId(), 
                		level,
                		xBridge.getNodeId());
                return false;
            } else {
                macsOnSegment.retainAll(yx.getSimpleConnectionMacs());                
            }
            forwarders.addAll(yx.getFirstBridgeForwarders());
            forwarders.addAll(yx.getSecondBridgeForwarders());
        }
        // if we are here is because X is NOT a leaf of any bridge found
        // on topSegment so X is connected to top Segment by it's root 
        // port or rx is a direct connection
        topSegment.assign(macsOnSegment,rx.getSecondBridgePort());
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculate: node: [{}]: level: {}, bridge: [{}], port[{}], macs{} -> assigned.\n{}", 
                        getNodeId(), 
                 level,xBridge.getNodeId(),rx.getSecondBridgePort().getBridgePort(),macsOnSegment,topSegment.printTopology());
        }
        for (BridgePort xbridgePort : rx.getSecondBridgeTroughSetBft().keySet()) {
            if (portsAdded.contains(xbridgePort.getBridgePort())) {
                continue;
            }
            SharedSegment xleafSegment = new SharedSegment(m_domain, xbridgePort,
                                                           rx.getSecondBridgeTroughSetBft().get(xbridgePort));  
            if (LOG.isDebugEnabled()) {
                LOG.debug("calculate: node: [{}]: level: {}, bridge: [{}]. Add shared segment. "
            		+ "[ designated bridge:[{}], "
            		+ "port:{}, "
            		+ "mac: {}]",
            		getNodeId(), 
                     level,
                     xBridge.getNodeId(),
                     xleafSegment.getDesignatedBridge(),
                     xleafSegment.getDesignatedPort().getBridgePort(),
                     xleafSegment.getMacsOnSegment());
            }
        }
        addForwarding(m_domain, forwarders);
        return true;
    }

}

