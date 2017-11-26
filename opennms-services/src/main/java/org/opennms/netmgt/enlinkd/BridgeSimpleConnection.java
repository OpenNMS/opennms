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

    final BridgeForwardingTable m_xBridge;
    final BridgeForwardingTable m_yBridge;
    Integer m_xy;
    Integer m_yx;
    Map<BridgePort, Set<String>> m_throughSetX= new HashMap<BridgePort, Set<String>>();
    Map<BridgePort, Set<String>> m_throughSetY= new HashMap<BridgePort, Set<String>>();
    Set<BridgeForwardingTableEntry> m_forwardersX = new HashSet<BridgeForwardingTableEntry>();
    Set<BridgeForwardingTableEntry> m_forwardersY = new HashSet<BridgeForwardingTableEntry>();
    Set<String> m_macsOnSegment=new HashSet<String>();
    BridgePort m_xyPort;
    BridgePort m_yxPort;

    public BridgeSimpleConnection(BridgeForwardingTable xBridge, 
            BridgeForwardingTable yBridge) {
        super();
        m_xBridge = xBridge;
        m_yBridge = yBridge;
    }
    
    public boolean findSimpleConnection() {
        Map<String,BridgeForwardingTableEntry> xmactoport = new HashMap<String, BridgeForwardingTableEntry>();
        Map<String,BridgeForwardingTableEntry> ymactoport = new HashMap<String, BridgeForwardingTableEntry>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("searching connection between:\n xbft -> \n{}\n ybft -> \n{}",
                      m_xBridge.printTopology(),
                      m_yBridge.printTopology());
        }
        for (BridgeForwardingTableEntry xlink: m_xBridge.getBFTEntries()) {
            if (m_xBridge.getNodeId().intValue() == xlink.getNodeId().intValue() 
                    && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                xmactoport.put(xlink.getMacAddress(), xlink);
            }
            if (m_xBridge.getNodeId().intValue() == xlink.getNodeId().intValue() 
                    && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                m_xBridge.getIdentifiers().add(xlink.getMacAddress());
        }
        for (BridgeForwardingTableEntry ylink: m_yBridge.getBFTEntries()) {
            if (m_yBridge.getNodeId().intValue() == ylink.getNodeId().intValue() && 
                    ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                ymactoport.put(ylink.getMacAddress(), ylink);
            }
            if (m_yBridge.getNodeId().intValue() == ylink.getNodeId().intValue() 
                    && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                m_yBridge.getIdentifiers().add(ylink.getMacAddress());
        }

        // there is a mac of Y found on X BFT
        Integer xy = condition1(m_yBridge.getIdentifiers(), xmactoport);
        if (xy != null) {
        	m_xy=xy;
            LOG.debug("[{} port: {} --> {}].",
                      m_xBridge.getNodeId(),
                      m_xy,
                      m_yBridge.getNodeId());
        }

        // there is a mac of X found on Y BFT
        Integer yx = condition1(m_xBridge.getIdentifiers(), ymactoport);
        if (yx != null) {
        	m_yx=yx;
            LOG.debug("simple connection: [{} <-- {} port: {}].",
                      m_xBridge.getNodeId(),
                      m_yBridge.getNodeId(),
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
            LOG.debug("[{}, port {} <--> {}, port: {}] common (learned mac): {}",
                      m_xBridge.getNodeId(),
                      m_xy,
                      m_yBridge.getNodeId(),
                      m_yx,
                      commonlearnedmacs);

        }    
        if (m_xy == null || m_xy == null) {
            LOG.warn("[{}, port {}] <--> [{}, port {}]. Not found. exiting", 
                      m_xBridge.getNodeId(), 
                      m_xy,
                      m_yBridge.getNodeId()
                      , m_yx);
            return false;
        }
        
        BridgeForwardingTableEntry xylink = null;
        BridgeForwardingTableEntry yxlink = null;
        for (BridgeForwardingTableEntry xlink: m_xBridge.getBFTEntries()) {
            if (xlink.getBridgePort() == m_xy 
                    && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                if (ymactoport.get(xlink.getMacAddress()) != null 
                		&& m_yx == ymactoport.get(xlink.getMacAddress()).getBridgePort()) {
                	m_macsOnSegment.add(xlink.getMacAddress());
                    LOG.debug("[{}, port {}] <--> [{}, port {}], forward set: [bridge:[{}],port:{},mac:{}].", 
                              m_xBridge.getNodeId(), 
                              m_xy,
                              m_yBridge.getNodeId()
                              , m_yx,
                              xlink.getNodeId(),
                		xlink.getBridgePort(),
                		xlink.getMacAddress());
                } else if (ymactoport.get(xlink.getMacAddress()) == null){
                    m_forwardersX.add(xlink);
                    LOG.debug("[{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                              m_xBridge.getNodeId(), 
                              m_xy,
                              m_yBridge.getNodeId()
                              , m_yx,
                              xlink.getNodeId(),
                            xlink.getBridgePort(),
                            xlink.getMacAddress());
                }
                if (xylink == null)
                    xylink = xlink;
            } else if (xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                LOG.debug("[{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                          m_xBridge.getNodeId(), 
                          m_xy,
                          m_yBridge.getNodeId()
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
        
        for (BridgeForwardingTableEntry ylink: m_yBridge.getBFTEntries()) {
            if (ylink.getBridgePort() == m_yx && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                if ( xmactoport.get(ylink.getMacAddress()) != null &&
                		m_xy == xmactoport.get(ylink.getMacAddress()).getBridgePort()) {
                	m_macsOnSegment.add(ylink.getMacAddress());
                    LOG.debug("[{}, port {}] <--> [{}, port {}], forward set: [bridge:[{}],port:{},mac:{}].", 
                              m_xBridge.getNodeId(), 
                              m_xy,
                              m_yBridge.getNodeId()
                              , m_yx,
                              ylink.getNodeId(),
                            ylink.getBridgePort(),
                            ylink.getMacAddress());
                } else if (xmactoport.get(ylink.getMacAddress()) == null){
                    m_forwardersY.add(ylink);
                    LOG.debug("[{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                              m_xBridge.getNodeId(), 
                              m_xy,
                              m_yBridge.getNodeId()
                              , m_yx,
                              ylink.getNodeId(),
                            ylink.getBridgePort(),
                            ylink.getMacAddress());
                }
                if (yxlink == null)
                    yxlink = ylink;
            } else if (ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                LOG.debug("[{}, port {}] <--> [{}, port {}], through set: [bridge:[{}],port:{},mac:{}].", 
                          m_xBridge.getNodeId(), 
                          m_xy,
                          m_yBridge.getNodeId()
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
                  m_xBridge.getNodeId(), 
                  m_xy,
                  m_yBridge.getNodeId()
                  , m_yx,
        		m_macsOnSegment);

        if (yxlink != null ) {
            m_yxPort = BridgePort.getFromBridgeForwardingTableEntry(yxlink);
        }
        
        if (xylink != null) {
            m_xyPort = BridgePort.getFromBridgeForwardingTableEntry(xylink);
        }
        return true;
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
    
    public String printTopology() {
        StringBuffer strbfr = new StringBuffer();
        strbfr.append("simple connection: port[");
        if (m_xyPort != null) {
            strbfr.append(m_xyPort.printTopology());
        } else {
            strbfr.append("null");
        }
        strbfr.append("], port [");
        if (m_yxPort != null) {
            strbfr.append(m_yxPort.printTopology());
        } else {
            strbfr.append("null");
        }
       strbfr.append("].");
       return strbfr.toString();
    }

}