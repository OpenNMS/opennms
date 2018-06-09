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

        if (xBridge.getNodeId().intValue() != simple.getFirstPort().getNodeId().intValue()) {
            throw new BridgeTopologyException("getMacs: node mismatch ["
                    + xBridge.getNodeId() + "] found " , simple.getFirstPort());
        }

        if (yBridge.getNodeId().intValue() != simple.getSecondPort().getNodeId().intValue()) {
            throw new BridgeTopologyException("getMacs: node mismatch ["
                    + yBridge.getNodeId() + "]", simple.getSecondPort());
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

    private BridgeSimpleConnection(BridgeForwardingTable xBridge, 
            BridgeForwardingTable yBridge) {
        super();
        m_xBridge = xBridge;
        m_yBridge = yBridge;
    }
    
    private void findSimpleConnection() throws BridgeTopologyException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("\n first bridge -> \n{}\n second bridge -> \n{}",
                      m_xBridge.printTopology(),
                      m_yBridge.printTopology());
        }

        if (m_xBridge.getPorttomac().size() == 1) {
            m_xyPort=m_xBridge.getPorttomac().iterator().next().getPort();
            LOG.debug("only one port found: bridge:[{}] <- {} ", 
                      m_yBridge.getNodeId(),
                      m_xyPort.printTopology());
        }

        if (m_yBridge.getPorttomac().size() == 1) {
            m_yxPort=m_yBridge.getPorttomac().iterator().next().getPort();
            LOG.debug("only one port found: bridge:[{}] <- {} ", 
                      m_xBridge.getNodeId(),
                      m_yxPort.printTopology());
        }

        // there is a mac of Y found on X BFT
        if (m_xyPort == null) {
             try {
                m_xyPort = condition1(m_yBridge, m_xBridge);
            } catch (BridgeTopologyException e) {
                LOG.debug("bridge: [{}] -> [{}], {}",
                      m_xBridge.getNodeId(),
                      m_yBridge.getNodeId(),
                      e.getMessage());
            }
        }        

        // there is a mac of X found on Y BFT
        if (m_yxPort == null) {
            try {
               m_yxPort = condition1(m_xBridge, m_yBridge);
            } catch (BridgeTopologyException e) {
                LOG.debug("bridge: [{}] -> [{}], {}",
                      m_yBridge.getNodeId(),
                      m_xBridge.getNodeId(),
                      e.getMessage());
            }
        }        
        
        if (m_xyPort != null && m_yxPort != null) {
            return;
        }
        
        
        Set<String> commonlearnedmacs = new HashSet<String>(m_xBridge.getMactoport().keySet()); 
        commonlearnedmacs.retainAll(new HashSet<String>(m_yBridge.getMactoport().keySet()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("bridge: [{}] <-> [{}] common (learned mac): {}",
                  m_yBridge.getNodeId(),
                  m_xBridge.getNodeId(),
                  commonlearnedmacs);
        }

        if (m_yxPort != null && m_xyPort == null) { 
            try {
                m_xyPort = condition2(commonlearnedmacs,
                                             m_yxPort,
                                             m_yBridge,
                                             m_xBridge);
            } catch (BridgeTopologyException e) {
                LOG.warn("bridge: [{}] -> [{}], {}",
                          m_xBridge.getNodeId(),
                          m_yBridge.getNodeId(),
                          e.getMessage());
                m_xyPort = conditionB(commonlearnedmacs, m_xBridge);
            }
            return;
        } 
        
        if (m_yxPort == null && m_xyPort != null) {
            try {
                m_yxPort = BridgeSimpleConnection.condition2(commonlearnedmacs,
                                             m_xyPort,
                                             m_xBridge,
                                             m_yBridge);
            } catch (BridgeTopologyException e) {
                LOG.warn("bridge: [{}] -> [{}], {}",
                          m_yBridge.getNodeId(),
                          m_xBridge.getNodeId(),
                          e.getMessage());
                m_yxPort = conditionB(commonlearnedmacs, m_yBridge);
            }
            return;
        } 
        
        List<BridgePort> ports = BridgeSimpleConnection.condition3(commonlearnedmacs,
               m_xBridge,
               m_yBridge);
        m_xyPort = ports.get(0);
        m_yxPort= ports.get(1);
    }

    private static List<BridgePort> condition3(Set<String> commonlearnedmacs,
                                               BridgeForwardingTable bridgexFt,
                                               BridgeForwardingTable bridgeyFt
                                               ) throws BridgeTopologyException {
    
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

        Map<String,BridgePort> xbft = bridgexFt.getMactoport();
        Map<String,BridgePort> ybft = bridgeyFt.getMactoport();

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
                yp1=ybft.get(mac);
                xp1=xbft.get(mac);
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
                yp2=ybft.get(mac);
                xp2=xbft.get(mac);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("condition3: mac:[{}], {} - {} ", mac2,
                          xp2.printTopology(),
                          yp2.printTopology());
                }
                continue;
            }
            if (ybft.get(mac).getBridgePort() == yp2.getBridgePort() 
                    && xbft.get(mac).getBridgePort() == xp2.getBridgePort()) {
                continue;
            }
            BridgePort yp3 = ybft.get(mac);
            BridgePort xp3 = xbft.get(mac);
            if (LOG.isDebugEnabled()) {
                LOG.debug("condition3: mac:[{}], {} - {} ", mac,
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
        throw new BridgeTopologyException("condition3: bridge: no simple connection", 
                                          bridgeyFt);
   }
    
    // condition 2 yx found                         m_x belongs to FDB(yx,Y)
    // if exists m_1 and m_2, p1 and p2 on Y :      m_1 belongs to FDB(p1,Y) FDB(xy,X)
    //                                              m_2 belongs to FDB(p2,Y) FDB(xy,X)
    private static BridgePort condition2(Set<String> commonlearnedmacs, BridgePort bridge1port, 
            BridgeForwardingTable bridge1Ft, 
            BridgeForwardingTable bridge2Ft) throws BridgeTopologyException {
        
        for (String mac: commonlearnedmacs) {
            BridgePort bridge1port1 = bridge1Ft.getMactoport().get(mac);
            BridgePort bridge2port1 = bridge2Ft.getMactoport().get(mac);
            if (bridge1port.getBridgePort().intValue() != bridge1port1.getBridgePort().intValue()) {
                LOG.debug("condition2: bridge:[{}] <- {}", 
                          bridge1Ft.getNodeId(),
                          bridge2port1.printTopology());
                return bridge2port1;
            }
        }

        throw new BridgeTopologyException("condition2: bridge: no simple connection", 
                                          bridge2Ft.getBridge());
    }
    
    
    // This is a particolar condition in which I get the port with max bft entries
    public static BridgePort conditionA( 
        BridgeForwardingTable bridge2Ft) throws BridgeTopologyException {
    
        BridgePort port = null;
        int size = 0;
        for (BridgePortWithMacs bft: bridge2Ft.getPorttomac()) {
            if (size <= bft.getMacs().size()) {
                port = bft.getPort();
                size = bft.getMacs().size();
            }
        }
        if (size == 0) {
            throw new BridgeTopologyException("conditionA: no elements in bft",bridge2Ft);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("conditionA: root bridge! selected port: bridge:[] <- {}", 
                      port.printTopology());
        }
        return port;
    }
    
    // This is a particolar condition 
    // in which I get the port without intersection only if I have two forwarding port
    public static BridgePort conditionB(Set<String> commonlearnedmacs, 
            BridgeForwardingTable bridge2Ft) throws BridgeTopologyException {
                
        if (bridge2Ft.getPorttomac().size() != 2) {
            throw new BridgeTopologyException("conditionB: bft has more then 2 forwarding ports", bridge2Ft);
        }

        Set<BridgePort> ports =  new HashSet<BridgePort>();
        for (String mac: commonlearnedmacs) {
            ports.add(bridge2Ft.getMactoport().get(mac));
        }
        
        if (ports.size() != 1) {
            throw new BridgeTopologyException("conditionB: common macs have more then 1 forwarding port", bridge2Ft);
        }
        for (BridgePortWithMacs bft: bridge2Ft.getPorttomac()) {
            for (BridgePort parsed: ports) {
                if (bft.getPort().equals(parsed)) {
                    continue;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("conditionB: new bridge and common mac on only one port: bridge:[] <- {}", 
                      bft.getPort().printTopology());
                }
                return bft.getPort();
            }
        }
        throw new BridgeTopologyException("conditionB: error forwarding ports", bridge2Ft);
    }
 
    private static BridgePort condition1(BridgeForwardingTable bridge1Ft, BridgeForwardingTable bridge2Ft) throws BridgeTopologyException {
        for (String mac: bridge1Ft.getIdentifiers()) {
            if (bridge2Ft.getMactoport().containsKey(mac)) {
                BridgePort bp = bridge2Ft.getMactoport().get(mac);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("condition1: {} -> bridge:[{}] identifier:[{}]",bp.printTopology(), bridge1Ft.getNodeId(), mac);
                }
                return bp;
            }
        }
        throw new BridgeTopologyException("condition1: no bridge identifier found on fpt");
    }
 
    public static BridgeSimpleConnection createAndRun(BridgeForwardingTable xBridge, 
    BridgeForwardingTable yBridge) throws BridgeTopologyException {
        BridgeSimpleConnection sp = new BridgeSimpleConnection(xBridge, yBridge);
        sp.findSimpleConnection();
        return sp;
        
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