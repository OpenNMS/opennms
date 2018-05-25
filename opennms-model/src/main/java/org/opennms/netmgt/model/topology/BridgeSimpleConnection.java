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

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class BridgeSimpleConnection implements Topology {

    public static Set<String> getMacs(BridgeForwardingTable xBridge,
            BridgeForwardingTable yBridge, BridgeSimpleConnection simple)
            throws BridgeTopologyException {

        if ( simple.getFirstPort() == null) {
            throw new BridgeTopologyException("getMacs: not found simpleconnection ["
                    + xBridge.getNodeId() + "]", simple);
        }

        if ( simple.getSecondPort() == null) {
            throw new BridgeTopologyException("getMacs: not found simpleconnection ["
                    + yBridge.getNodeId() + "]", simple);
        }

        if (xBridge.getNodeId() != simple.getFirstPort().getNodeId()) {
            throw new BridgeTopologyException("getMacs: node mismatch ["
                    + xBridge.getNodeId() + "]", simple);
        }

        if (yBridge.getNodeId() != simple.getSecondPort().getNodeId()) {
            throw new BridgeTopologyException("getMacs: node mismatch ["
                    + yBridge.getNodeId() + "]", simple);
        }

        Set<String> macsOnSegment = xBridge.getBridgePortWithMacs(simple.getFirstPort()).getMacs();
        macsOnSegment.retainAll(yBridge.getBridgePortWithMacs(simple.getSecondPort()).getMacs());

        return macsOnSegment;
    }

    static final Logger LOG = LoggerFactory.getLogger(BridgeSimpleConnection.class);

    private final BridgeForwardingTable m_xBridge;
    private final BridgeForwardingTable m_yBridge;
    private BridgePort m_xyPort;
    private BridgePort m_yxPort;

    public BridgePort getFirstPort() {
        return m_xyPort;
    }
    
    public BridgePort getSecondPort() {
        return m_yxPort;
    }

    public Integer getFirstBridgePort() {
        return m_xyPort.getBridgePort();
    }
    
    public Integer getSecondBridgePort() {
        return m_yxPort.getBridgePort();
    }

    public BridgeSimpleConnection(BridgeForwardingTable xBridge, 
            BridgeForwardingTable yBridge) {
        super();
        m_xBridge = xBridge;
        m_yBridge = yBridge;
    }
    
    public boolean doit() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("doit: ->\n first bridge -> \n{}\n second bridge -> \n{}",
                      m_xBridge.printTopology(),
                      m_yBridge.printTopology());
        }
        
        // there is a mac of Y found on X BFT
        
        m_xyPort = BridgeSimpleConnection.condition1(m_yBridge.getIdentifiers(), m_xBridge.getMactoport());

        // there is a mac of X found on Y BFT
        m_yxPort = BridgeSimpleConnection.condition1(m_xBridge.getIdentifiers(), m_yBridge.getMactoport());
        	            
        if (m_xyPort == null || m_yxPort == null) {
            Set<String> commonlearnedmacs = new HashSet<String>(m_xBridge.getMactoport().keySet()); 
            commonlearnedmacs.retainAll(new HashSet<String>(m_yBridge.getMactoport().keySet()));
            if (m_yxPort != null && m_xyPort == null) { 
                m_xyPort = BridgeSimpleConnection.condition2(commonlearnedmacs,
                                                             m_yxPort,
                                             m_yBridge.getMactoport(),
                                             m_xBridge.getMactoport());
            } else if (m_yxPort == null && m_xyPort != null) {
                m_yxPort = BridgeSimpleConnection.condition2(commonlearnedmacs,
                                                             m_xyPort,
                                             m_xBridge.getMactoport(),
                                             m_yBridge.getMactoport());
            } else {
                List<BridgePort> ports = BridgeSimpleConnection.condition3(commonlearnedmacs,
                                           m_xBridge.getMactoport(),
                                           m_yBridge.getMactoport());
                if (ports.size() == 2) {
                    m_xyPort = ports.get(0);
                    m_yxPort= ports.get(1);
                }
            }
        }    
        if (
            m_xyPort == null || 
            m_yxPort == null || 
            m_xyPort.getBridgePort() == null ||
            m_yxPort.getBridgePort() == null
            ) {
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("condition3: common (learned mac): -> {}",
                  commonlearnedmacs);
        }
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
                    LOG.debug("condition3: mac:[{}] {} - {} ", mac1,
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
                    LOG.debug("condition3: mac:[{}] {} - {} ", mac2,
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
                LOG.debug("condition3: mac:[{}] {} - {} ", mac,
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
    private static BridgePort condition2(Set<String> commonlearnedmacs, BridgePort yx, Map<String,BridgeForwardingTableEntry> ybft, Map<String,BridgeForwardingTableEntry> xbft) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("condition2: {}",yx.printTopology());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("condition2: common (learned mac): -> {}",
                  commonlearnedmacs);
        }

        Set<BridgePort> ports =  new HashSet<BridgePort>();
        for (String mac: commonlearnedmacs) {
            BridgePort y1 = BridgePort.getFromBridgeForwardingTableEntry(ybft.get(mac));
            BridgePort x1= BridgePort.getFromBridgeForwardingTableEntry(xbft.get(mac));
            ports.add(x1);
            if (LOG.isDebugEnabled()) {
                LOG.debug("condition2: mac:[{}] {} - {}", mac, y1.printTopology(), x1.printTopology());
            }
            if (y1.getBridgePort().intValue() != yx.getBridgePort().intValue()) {
                return x1;
            }
        }
        if (ports.size() == 1) {
            return ports.iterator().next();
        }
        return null;
    }
    
    private static BridgePort condition1(Set<String> bridgemacaddressess, Map<String,BridgeForwardingTableEntry> otherbridgebft) {
        for (String mac: bridgemacaddressess) {
            if (otherbridgebft.containsKey(mac)) {
                BridgePort bp = BridgePort.getFromBridgeForwardingTableEntry(otherbridgebft.get(mac));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("condition1: matched {}, bridge mac identifier:[{}]",bp.printTopology(), mac);
                }
                return bp;
            }
        }
        return null;
    }
                
    public String printTopology() {
        StringBuffer strbfr = new StringBuffer();
        strbfr.append("simple connection: [");
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