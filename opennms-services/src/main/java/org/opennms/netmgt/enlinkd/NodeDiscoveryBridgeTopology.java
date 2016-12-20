package org.opennms.netmgt.enlinkd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.opennms.netmgt.model.topology.SimpleConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryBridgeTopology extends NodeDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridgeTopology.class);

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
    private class BridgeTopologyHelper {
        
        Integer m_xy;
        Integer m_yx;
        Map<String,BridgeMacLink> xmactoport = new HashMap<String, BridgeMacLink>();
        Map<String,BridgeMacLink> ymactoport = new HashMap<String, BridgeMacLink>();
        Map<Integer, List<BridgeMacLink>> m_throughSetX = new HashMap<Integer, List<BridgeMacLink>>();
        Map<Integer, List<BridgeMacLink>> m_throughSetY = new HashMap<Integer, List<BridgeMacLink>>();
        SimpleConnection m_simpleconnection; 
        Set<String> xmacs = new HashSet<String>();
        Set<String> ymacs = new HashSet<String>();
        public BridgeTopologyHelper(Bridge xBridge, List<BridgeMacLink> xBFT, Bridge yBridge, List<BridgeMacLink> yBFT) {
            super();
            LOG.debug("BridgeTopologyHelper: searching simple connection between bridgeX: {} and bridgeY",
                      xBridge.getId(),
                      yBridge.getId()
                      );
            LOG.debug("BridgeTopologyHelper: bridgeX has bft size {}",
                      xBFT.size());
            LOG.debug("BridgeTopologyHelper: bridgeY has bft size {}",
                      yBFT.size());
            
            for (BridgeMacLink xlink: xBFT) {
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    xmactoport.put(xlink.getMacAddress(), xlink);
                    if (!m_throughSetX.containsKey(xlink.getBridgePort()))
                        m_throughSetX.put(xlink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSetX.get(xlink.getBridgePort()).add(xlink);
                }
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    xmacs.add(xlink.getMacAddress());
            }
            for (BridgeMacLink ylink: yBFT) {
                if (yBridge.getId().intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    ymactoport.put(ylink.getMacAddress(), ylink);
                    if (!m_throughSetY.containsKey(ylink.getBridgePort()))
                        m_throughSetY.put(ylink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSetY.get(ylink.getBridgePort()).add(ylink);
                }
                if (yBridge.getId().intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    ymacs.add(ylink.getMacAddress());
            }
            xmacs.addAll(m_nodeToBridgeMacIdMap.get(xBridge.getId()));
            ymacs.addAll(m_nodeToBridgeMacIdMap.get(yBridge.getId()));
            boolean cond1X = condition1BridgeX();
            boolean cond1Y = condition1BridgeY();
            
            if (!cond1X || !cond1Y) {
                Set<String> commonlearnedmacs = new HashSet<String>(xmactoport.keySet()); 
                commonlearnedmacs.retainAll(new HashSet<String>(ymactoport.keySet()));
                LOG.debug("BridgeTopologyHelper: common (learned mac) size: {} for X: {}, Y: {}",commonlearnedmacs.size(),xBridge.getId(),yBridge.getId());
                if (cond1X && !cond1Y) 
                    condition2BridgeX(commonlearnedmacs);
                if (!cond1X && cond1Y)
                    condition2BridgeY(commonlearnedmacs);
                if (!cond1X && !cond1Y)
                    condition3(commonlearnedmacs);
                if (m_xy == null || m_xy == null)
                    condition3(commonlearnedmacs);
                if (m_xy == null || m_xy == null)
                    return;
            }    
            LOG.debug("BridgeTopologyHelper: found port m_xy: {} on X: {}", m_xy, xBridge.getId());
            LOG.debug("BridgeTopologyHelper: found port m_yx: {} on Y: {}", m_yx, yBridge.getId());
            
            BridgeMacLink xylink = null;
            BridgeMacLink yxlink = null;
            List<BridgeMacLink> connectionsOnSegment=new ArrayList<BridgeMacLink>();
            for (BridgeMacLink xlink: xBFT) {
                if (xlink.getBridgePort() == m_xy && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    if (!ymactoport.containsKey(xlink.getMacAddress()) || m_yx == ymactoport.get(xlink.getMacAddress()).getBridgePort())
                        connectionsOnSegment.add(xlink);
                    if (xylink == null)
                        xylink = xlink;
                }
            }
            int xlinks = connectionsOnSegment.size();
            LOG.debug("BridgeTopologyHelper: added {}, links on simple connection for X: {}", xlinks, xBridge.getId());
            
            for (BridgeMacLink ylink: yBFT) {
                if (ylink.getBridgePort() == m_yx && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    if (!xmactoport.containsKey(ylink.getMacAddress()) || m_xy == xmactoport.get(ylink.getMacAddress()).getBridgePort())
                        connectionsOnSegment.add(ylink);
                    if (yxlink == null)
                        yxlink = ylink;
                }
            }
            int ylinks = connectionsOnSegment.size() - xlinks;
            LOG.info("BridgeTopologyHelper: added {}, links on simple connection for Y: {}", ylinks,yBridge.getId());
    
            BridgeBridgeLink blink = new BridgeBridgeLink();
            if (xylink != null && yxlink != null ) {

                blink.setNode(yxlink.getNode());
                blink.setBridgePort(yxlink.getBridgePort());
                blink.setBridgePortIfIndex(yxlink.getBridgePortIfIndex());
                blink.setBridgePortIfName(yxlink.getBridgePortIfName());
                blink.setVlan(yxlink.getVlan());
                
                blink.setDesignatedNode(xylink.getNode());
                blink.setDesignatedPort(xylink.getBridgePort());
                blink.setDesignatedPortIfIndex(xylink.getBridgePortIfIndex());
                blink.setDesignatedPortIfName(xylink.getBridgePortIfName());
                blink.setDesignatedVlan(xylink.getVlan());


            }
            m_simpleconnection = new SimpleConnection(connectionsOnSegment, blink);
            m_throughSetX.remove(m_xy);
            m_throughSetY.remove(m_yx);            
        }

        private void condition3(Set<String> commonlearnedmacs) {
        
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

            for (String mac: commonlearnedmacs) {
                LOG.debug("BridgeTopologyHelper: condition3: parsing common BFT mac: {}",mac);
                if (mac1 == null) {
                    mac1=mac;
                    yp1=ymactoport.get(mac).getBridgePort();
                    xp1=xmactoport.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition3: mac1: {} xp1: {} yp1: {} ", mac1,xp1,yp1);
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort() == yp1 && xmactoport.get(mac).getBridgePort() == xp1)
                    continue;
                if (mac2 == null) {
                    mac2=mac;
                    yp2=ymactoport.get(mac).getBridgePort();
                    xp2=xmactoport.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition3: mac2: {} xp2: {} yp2: {} ", mac2,xp2,yp2);
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort() == yp2 && xmactoport.get(mac).getBridgePort() == xp2)
                    continue;
                Integer yp3 = ymactoport.get(mac).getBridgePort();
                Integer xp3 = xmactoport.get(mac).getBridgePort();
                LOG.debug("BridgeTopologyHelper: condition3: mac3: {} x3: {} yp3: {} ", mac,xp3,yp3);

                //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
                //m_2 belongs to FDB(p2,Y) FDB(xy,X) 
                //m_3 belongs to FDB(yx,Y) FDB(p3,X)
                //
                //m_1 belongs to FDB(p1,Y) FDB(xy,X) 
                //m_2 belongs to FDB(yx,Y) FDB(xy,X) 
                //m_3 belongs to FDB(yx,Y) FDB(p3,X)
                if (xp1 == xp2 && xp1 != xp3 && (yp1 != yp3 || yp2 != yp3) ) {
                    m_xy=xp1;
                    m_yx=yp3;
                    return;
                }
                // exchange x y
                if (yp1 == yp2 && yp1 != yp3 && (xp1 != xp3 || xp2 != xp3) ) {
                    m_yx=yp1;
                    m_xy=xp3;
                    return;
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
                    m_xy=xp1;
                    m_yx=yp2;
                    return;
                }
                // revert x y
                if (yp1 == yp3 && yp1 != yp2 && (xp1 != xp2 || xp2 != xp3) ) {
                    m_yx=yp1;
                    m_xy=xp2;
                    return;
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
                    m_xy=xp2;
                    m_yx=yp1;
                    return;
                }
                if (yp3 == yp2 && yp1 != yp3 && (xp1 != xp3 || xp2 != xp1) ) {
                    m_yx=yp2;
                    m_xy=xp1;
                    return;
                }

            }
            // all macs on the same port
            if (mac2 == null) {
                m_xy=xp1;
                m_yx=yp1;
            }
        }
        
        // condition 2X
        // if exists m_x, m_1 and m_2, p1 and p2 on Y : m_x belongs to FDB(yx,Y) 
        //                                              m_1 belongs to FDB(p1,Y) FDB(xy,X)
        //                                              m_2 belongs to FDB(p2,Y) FDB(xy,X)
        private void condition2BridgeX(Set<String> commonlearnedmacs) {
            LOG.info("BridgeTopologyHelper: condition2BridgeX: found m_yx: {}, search m_xy using common macs size: {} ", m_yx, commonlearnedmacs.size() );
            String mac1=null;
            String mac2=null;
            Integer p1=null;
            Integer xy1=null;
            Integer p2=null;
            Integer xy2=null;
            for (String mac: commonlearnedmacs) {
                LOG.debug("BridgeTopologyHelper: condition2BridgeX: parsing common BFT mac: {} portX: {}, portY: {} ",
                          mac,xmactoport.get(mac).getBridgePort(),ymactoport.get(mac).getBridgePort() );
                if (mac1 == null) {
                    mac1 = mac;
                    p1 = ymactoport.get(mac).getBridgePort();
                    xy1= xmactoport.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition2BridgeX: mac1: {} xy1: {} p1: {} ", mac1,xy1,p1);
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort().intValue() == p1.intValue())
                    continue;
                if (xmactoport.get(mac).getBridgePort().intValue() == xy1.intValue()) {
                    LOG.debug("BridgeTopologyHelper: condition2BridgeX: xy1 bridge port {}",ymactoport.get(mac).getBridgePort());
                    m_xy=xy1;
                    return;
                }
                if (mac2 == null) {
                    mac2 = mac;
                    p2 = ymactoport.get(mac).getBridgePort();
                    xy2= xmactoport.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition2BridgeX: mac2: {} xy2: {} p2: {} ", mac2,xy2,p2);
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort().intValue() == p2.intValue())
                    continue;
                if (xmactoport.get(mac).getBridgePort().intValue() == xy2.intValue()) {
                    LOG.debug("BridgeTopologyHelper: condition2BridgeX: xy2 bridge port {}",ymactoport.get(mac).getBridgePort());
                    m_xy=xy2;
                    return;
                }
            }
            if (xy2 == null) 
                m_xy=xy1;
        }
        
        // condition 2Y
        // if exists m_y, m_1 and m_2, p1 and p2 on X : m_y belongs to FDB(xy,X) 
        //                                              m_1 belongs to FDB(p1,X) FDB(yx,Y)
        //                                              m_2 belongs to FDB(p2,X) FDB(yx,Y)
        private void condition2BridgeY(Set<String> commonlearnedmacs) {
            LOG.info("BridgeTopologyHelper: condition2BridgeY: found m_xy: {}, search m_yx using common macs size: {} ", m_xy, commonlearnedmacs.size() );
            String mac1=null;
            String mac2=null;
            Integer p1=null;
            Integer yx1=null;
            Integer p2=null;
            Integer yx2=null;

            for (String mac: commonlearnedmacs) {
                LOG.debug("BridgeTopologyHelper: condition2BridgeY: parsing common BFT mac: {} portX: {}, portY: {} ",
                          mac,xmactoport.get(mac).getBridgePort(),ymactoport.get(mac).getBridgePort() );
                if (mac1 == null) {
                    mac1 = mac;
                    p1 = xmactoport.get(mac).getBridgePort();
                    yx1= ymactoport.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition2BridgeY: mac1: {} yx1: {} p1: {} ", mac1,yx1,p1);
                    continue;
                }
                if (xmactoport.get(mac).getBridgePort() == p1)
                    continue;
                if (ymactoport.get(mac).getBridgePort() == yx1) {
                    LOG.debug("BridgeTopologyHelper: condition2BridgeY: yx1 bridge port {}",ymactoport.get(mac).getBridgePort());
                    m_yx=yx1;
                    return;
                }
                if (mac2 == null) {
                    mac2 = mac;
                    p2 = xmactoport.get(mac).getBridgePort();
                    yx2= ymactoport.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition2BridgeY: mac2: {} yx2: {} p2: {} ", mac2,yx2,p2);
                    continue;
                }
                if (xmactoport.get(mac).getBridgePort().intValue() == p2.intValue())
                    continue;
                if (ymactoport.get(mac).getBridgePort().intValue() == yx2.intValue()) {
                    LOG.debug("BridgeTopologyHelper: condition2BridgeY: yx2 bridge port {}",ymactoport.get(mac).getBridgePort());
                    m_yx=yx2;
                    return;
                }
            }
            if (yx2 == null) {
                m_yx=yx1;
            }
        }
        
        // there is a mac of X found on Y BFT
        private boolean condition1BridgeX() {
            LOG.info("BridgeTopologyHelper: condition1BridgeX: bridge X macs size {} ", xmacs.size() );
            for (String xmac: xmacs) {
                LOG.debug("BridgeTopologyHelper: condition1BridgeX: bridge X mac: {} ", xmac );
                if (ymactoport.containsKey(xmac)) {
                    m_yx = ymactoport.get(xmac).getBridgePort();
                    LOG.info("BridgeTopologyHelper: condition1BridgeX: found X mac: {} on Y port: {}", xmac,m_yx);
                    return true;
                }
            }
            return false;
        }
            
        // there is a mac of Y found on X BFT
        private boolean condition1BridgeY() {
            LOG.info("BridgeTopologyHelper: condition1BridgeY: bridge Y macs size {} ", ymacs.size() );
            for (String ymac: ymacs) {
                LOG.debug("BridgeTopologyHelper: condition1BridgeY: bridge Y mac: {} ", ymac );
                if (xmactoport.containsKey(ymac)) {
                    m_xy = xmactoport.get(ymac).getBridgePort();
                    LOG.info("BridgeTopologyHelper: condition1BridgeY: found Y mac: {} on X port: {}", ymac,m_xy);
                    return true;
                }
            }
            return false;
        }
        
        public Integer getFirstBridgeConnectionPort() {
            return m_xy;
        }
        
        public Integer getSecondBridgeConnectionPort() {
            return m_yx;
        }
        
        public SimpleConnection getSimpleConnection() {
            return m_simpleconnection; 
        }

        public Map<Integer,List<BridgeMacLink>> getFirstBridgeTroughSet() {
            return m_throughSetX;
        }

        public Map<Integer,List<BridgeMacLink>> getSecondBridgeTroughSet() {
            return m_throughSetY;
        }

    }

    Map<Bridge,List<BridgeMacLink>> m_notYetParsedBFTMap;
    BroadcastDomain m_domain;
    List<BridgeMacLink> m_rootBridgeBFT = new ArrayList<BridgeMacLink>();
    //List<BridgeStpLink> m_STPLinks = new ArrayList<BridgeStpLink>();
    List<BridgeElement> m_bridgeelements = new ArrayList<BridgeElement>();
    Map<Integer,Set<String>> m_nodeToBridgeMacIdMap = new HashMap<Integer, Set<String>>();
    
    public List<BridgeElement> getBridgeelements() {
        return m_bridgeelements;
    }

    public void setBridgeElements(List<BridgeElement> bridgeelements) {
        m_nodeToBridgeMacIdMap.clear();
        for (BridgeElement element: bridgeelements) {
            if (!m_nodeToBridgeMacIdMap.containsKey(element.getNode().getId()))
                m_nodeToBridgeMacIdMap.put(element.getNode().getId(), new HashSet<String>());
            if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress()))
                m_nodeToBridgeMacIdMap.get(element.getNode().getId()).add(element.getBaseBridgeAddress());
        }
        m_bridgeelements = bridgeelements;
    }

    public List<BridgeMacLink> getRootBridgeBFT() {
        return m_rootBridgeBFT;
    }

    public void setRootBridgeBFT(List<BridgeMacLink> rootBridgeBFT) {
        m_rootBridgeBFT = rootBridgeBFT;
    }

    public BroadcastDomain getDomain() {
        return m_domain;
    }

    public void setDomain(BroadcastDomain domain) {
        m_domain = domain;
    }

    public Map<Bridge, List<BridgeMacLink>> getNotYetParsedBFTMap() {
        return m_notYetParsedBFTMap;
    }

    public void addUpdatedBFT(Bridge bridge, List<BridgeMacLink> notYetParsedBFTMap) {
        if (m_notYetParsedBFTMap==null)
            m_notYetParsedBFTMap = new HashMap<Bridge, List<BridgeMacLink>>();
        m_notYetParsedBFTMap.put(bridge, notYetParsedBFTMap);
    }

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
    }

    @Override
    public void run() {        
        Date now = new Date();
                
        Set<String> incomingSet = new HashSet<String>();
        for (BridgeMacLink link : m_linkd.getQueryManager().getBridgeTopologyUpdateBFT(getNodeId())) {
            incomingSet.add(link.getMacAddress());
        }
        LOG.debug("run: node: {}. macs found: {}", getNodeId(), incomingSet);

        Set<Integer> nodeswithupdatedbftonbroadcastdomain= new HashSet<Integer>();
        nodeswithupdatedbftonbroadcastdomain.add(getNodeId());

        Map<Integer,List<BridgeMacLink>> nodeBftMap = m_linkd.getQueryManager().getUpdateBftMap();
        LOG.info("run: node: {}, getting nodes with updated bft on broadcast domain. Start", getNodeId());
        for (Integer curNodeId: nodeBftMap.keySet()) {
            if (curNodeId.intValue() == getNodeId())
                continue;
            Set<String>retainedSet = new HashSet<String>();
            for (BridgeMacLink link: nodeBftMap.get(curNodeId)) {
                retainedSet.add(link.getMacAddress());
            }
            LOG.debug("run: node: {}, parsing updated bft node: {}, macs {}", getNodeId(), curNodeId,retainedSet);
            retainedSet.retainAll(incomingSet);
            LOG.debug("run: node: {}, node: {} - common mac address set: {}", getNodeId(), curNodeId, retainedSet);
            if (retainedSet.size() > 10
                    || retainedSet.size() >= incomingSet.size() * 0.1) {
                nodeswithupdatedbftonbroadcastdomain.add(curNodeId);
                LOG.info("run: node: {}, node: {} - put on same broadcast domain, common macs: {} ", getNodeId(), 
                         curNodeId,
                         retainedSet);
            }
        }
        LOG.info("run: node: {}, getting nodes with updated bft on broadcast domain. End", getNodeId());

        LOG.info("run: node: {}, getting broadcast domain. Start", getNodeId());
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            LOG.debug("run: node: {}, parsing domain with nodes: {}, macs: {}", getNodeId(), domain.getBridgeNodesOnDomain(),domain.getMacsOnDomain());
            Set<String>retainedSet = new HashSet<String>(
                                                          domain.getMacsOnDomain());
            retainedSet.retainAll(incomingSet);
            LOG.debug("run: node: {}, retained: {}", getNodeId(), retainedSet);
            // should contain at list 10 or 10% of the all size
            if (retainedSet.size() > 10
                    || retainedSet.size() >= incomingSet.size() * 0.1) {
                m_domain = domain;
                LOG.debug("run: node: {}, domain found!",getNodeId());
                continue;
            }
            for (Integer curNodeId: nodeswithupdatedbftonbroadcastdomain) {
                if (domain.containBridgeId(curNodeId)) {
                    LOG.debug("run: remove node: {}, on not other domain {}!",curNodeId,
                              domain);
                    if (!domain.getLock(this)) {
                        LOG.info("run: broadcast domain {}: is locked for calculation cannot clear topology for node {}....scheduling with time interval {}", 
                                 domain,
                                 curNodeId, getInitialSleepTime());
                        schedule();
                        m_domain.releaseLock(this);
                        return;
                    }
                    NodeDiscoveryBridgeTopology ndbt = new NodeDiscoveryBridgeTopology(m_linkd, m_linkd.getNode(curNodeId));
                    ndbt.setDomain(domain);
                    ndbt.clearTopologyForBridge(domain.getBridge(curNodeId));
                    m_linkd.getQueryManager().store(domain,now);
                    m_linkd.getQueryManager().save(domain.getRootBridgeId(), ndbt.getRootBridgeBFT());
                    domain.removeBridge(curNodeId);
                    domain.releaseLock(this);
                }
            }
        }
        boolean cleardomain = false;        
        if (m_domain == null) {
            LOG.info("run: node: {} Creating a new Domain", getNodeId());
            m_domain = new BroadcastDomain();
            m_domain.getLock(this);
            m_linkd.getQueryManager().save(m_domain);
        } else  if (!m_domain.getLock(this)) {
            LOG.info("run: broadcast domain: is locked for calculation either on node {}....scheduling with time interval {}", 
                     getNodeId(), getInitialSleepTime());
            schedule();
            return;
        } else if (!m_domain.hasRootBridge()) {
            cleardomain = true;
            LOG.error("run: node: {}, broadcast domain has no root bridge. Clearing domain", getNodeId());
        } else if (m_linkd.getQueryManager().getBridgeTopologyRootBFT(m_domain.getRootBridgeId()) == null){
            cleardomain = true;
            LOG.warn("run: node: {}, broadcast domain has root bridge {}, with null bft. Clearing domain", getNodeId(), m_domain.getRootBridgeId());
        } else {
            m_rootBridgeBFT = m_linkd.getQueryManager().getBridgeTopologyRootBFT(m_domain.getRootBridgeId());
        }      

        
        if (cleardomain) {
            LOG.info("run: node: {} Creating a new Domain", getNodeId());
            m_domain.clearTopology();
            m_linkd.getQueryManager().store(m_domain, now);
            m_domain.clear();
            m_domain.releaseLock(this);
            m_domain = new BroadcastDomain();
            m_domain.getLock(this);
            m_linkd.getQueryManager().save(m_domain);
        } 
        LOG.info("run: node: {}, getting broadcast domain. End", getNodeId());
        
        for (Integer curNode: nodeswithupdatedbftonbroadcastdomain)
            sendStartEvent(curNode);

        for (Integer curNodeId: nodeBftMap.keySet()) {
            if (nodeswithupdatedbftonbroadcastdomain.contains(curNodeId))
                m_domain.addBridge(new Bridge(curNodeId));
            else
                m_domain.removeBridge(curNodeId);
        }
        m_linkd.getQueryManager().cleanBroadcastDomains();
        
        m_notYetParsedBFTMap = new HashMap<Bridge, List<BridgeMacLink>>();
        
        setBridgeElements(m_linkd.getQueryManager().getBridgeElements(m_domain.getBridgeNodesOnDomain()));
        
        for (Integer nodeid: nodeswithupdatedbftonbroadcastdomain) {
            LOG.debug("run: node: {}, getting update bft for node {} on domain", getNodeId(),nodeid);
            List<BridgeMacLink> bft = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(nodeid);
            if (bft == null || bft.isEmpty()) {
                LOG.warn("run: node: {}, no update bft for node {} on domain", getNodeId(),nodeid);
                continue;
            }
            m_notYetParsedBFTMap.put(m_domain.getBridge(nodeid), bft);
        }

        if (m_notYetParsedBFTMap.isEmpty()) {
            LOG.info("run: node: {}, broadcast domain has no topology updates. No more action is needed.", getNodeId());
            return;
        }
        LOG.info("run: node: {}, start: broadcast domain topology calculation.", getNodeId());
        try {
            calculate();
        } catch (Exception e) {
            LOG.error("run: node: {}, got exception",e);           
        }
        LOG.info("run: node: {}, stop: broadcast domain topology calculated.", getNodeId());

        LOG.info("run: node: {}, saving broadcast domain root bridge: {} bft.", getNodeId(),m_domain.getRootBridgeId());
        m_linkd.getQueryManager().save(m_domain.getRootBridgeId(), m_rootBridgeBFT);

        LOG.info("run: node: {}, saving broadcast domain topology.", getNodeId());
        m_linkd.getQueryManager().store(m_domain,now);
        LOG.info("run: node: {}, saved broadcast domain topology.", getNodeId());
        
        for (Integer curNode: nodeswithupdatedbftonbroadcastdomain)
            sendCompletedEvent(curNode);
        m_domain.releaseLock(this);
        LOG.info("run: node: {}, releaseLock broadcast domain for nodes: {}.", getNodeId(),
                 m_domain.getBridgeNodesOnDomain());
        m_runned = true;
        reschedule();        
    }
            
    @Override
    protected void runCollection() {
    }

    @Override
    public String getInfo() {
        return "ReadyRunnable DiscoveryBridgeTopology" + " node=" + getNodeId();
    }

    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

    protected  void calculate() {
        LOG.info("calculate: start:  calculate topology");

        for (Bridge xBridge: m_notYetParsedBFTMap.keySet()) {
            LOG.info("calculate: bridge: {} has an updated bft. Clear bridge topology", xBridge.getId());
            clearTopologyForBridge(xBridge);
        }        

        Bridge electedRoot = electRootBridge();
        if (electedRoot == null || electedRoot.getId() == null) {
            LOG.error("calculate: electedRoot should not be null");
            return;
        }
        setUpRoot(electedRoot);
        
        Set<Bridge> nodetobeparsed = new HashSet<Bridge>(m_notYetParsedBFTMap.keySet());
        for (Bridge xBridge: nodetobeparsed) {
            LOG.info("calculate: start: calculate topology for nodeid {}",xBridge.getId());
            calculate(electedRoot, m_rootBridgeBFT, xBridge, new ArrayList<BridgeMacLink>(m_notYetParsedBFTMap.remove(xBridge)));
            LOG.info("calculate: stop: calculate topology for nodeid {}",xBridge.getId());
        }
        LOG.info("calculate: stop:  calculate topology");
    }
     
    private void calculate(Bridge root,  List<BridgeMacLink> rootbft, Bridge xBridge, List<BridgeMacLink> xbft) {
        //FIXME        checkStp(root, xBridge);
        BridgeTopologyHelper rx = new BridgeTopologyHelper(root, rootbft, xBridge,xbft);
        Integer rxDesignatedPort = rx.getFirstBridgeConnectionPort();
        if (rxDesignatedPort == null) {
            LOG.info("calculate: level 0: cannot found X -> Y simple connection:  X Bridge: {}, Y Bridge: {}", root.getId(), xBridge.getId());
            m_domain.clearTopology();
            return;
        }
        Integer xrDesignatedPort = rx.getSecondBridgeConnectionPort();
        if (xrDesignatedPort == null) {
             LOG.info("calculate: level 0: cannot found Y -> X simple connection: Y Bridge: {} X Bridge: {}",xBridge.getId(), root.getId());
             m_domain.clearTopology();
             return;
        }
        LOG.info("calculate: level 0: found simple connection:  nodeid {}, port {} <--> nodeid {}, port {}",root.getId(),rxDesignatedPort,xBridge.getId(),xrDesignatedPort);
        LOG.info("calculate: level 0: set root port {} for X bridge: {}",xrDesignatedPort,xBridge.getId());
        xBridge.setRootPort(xrDesignatedPort);
        xBridge.setRootBridge(false);
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = m_domain.getSharedSegment(root.getId(),rxDesignatedPort);
        if (topSegment == null) {
            LOG.error("calculate: level 0: not found: top segment:  for nodeid {}, port {}",m_domain.getRootBridgeId(),rxDesignatedPort);
            m_domain.clearTopology();
            return;
        }
        findBridgesTopo(rx,topSegment, xBridge, xbft,0);
    }

    // here we assume that rbridge exists in topology
    // while xBridge is to be added
    private void findBridgesTopo(BridgeTopologyHelper rx,SharedSegment topSegment, Bridge xBridge, List<BridgeMacLink> xBFT, int level) {
        level++;
        if (level == 30) {
            LOG.warn("calculate: level {}: bridge: {}, too many iteration on topology exiting.....",level,xBridge.getId());
            return;
        }
        LOG.info("calculate: level {}: bridge: {}, top segment: designated port {}, designated root {}",level,xBridge.getId(),topSegment.getDesignatedPort(),
                 topSegment.getDesignatedBridge());

        Set<Integer> portsAdded=new HashSet<Integer>();
        List<Map<Integer,List<BridgeMacLink>>> throughSets=new ArrayList<Map<Integer,List<BridgeMacLink>>>();
        Map<Integer,List<BridgeMacLink>> bftSets=new HashMap<Integer,List<BridgeMacLink>>();
        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            bftSets.put(yBridge.getId(), getBFT(yBridge));
        }
        
        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            Integer yBridgeId = yBridge.getId();
            // X is a leaf of top segment: of course
            if (yBridgeId.intValue() == topSegment.getDesignatedBridge().intValue()) {
                continue;
            } 
            LOG.debug("calculate: level {}: bridge: {}, check simple connection with Bridge Y {} found on topSegment",
                      level,xBridge.getId(), yBridge.getId());
            Integer yrDesignatedPort = yBridge.getRootPort();
            LOG.info("calculate: level {}: bridge: {}, found Y designated port:  Y bridge: {}, port: {}",
                     level,xBridge.getId(),yBridgeId,yrDesignatedPort);
            BridgeTopologyHelper   yx = new BridgeTopologyHelper(yBridge,bftSets.get(yBridgeId) ,xBridge, xBFT);
            Integer  xyDesignatedPort = yx.getSecondBridgeConnectionPort();
            Integer  yxDesignatedPort = yx.getFirstBridgeConnectionPort();
            LOG.info("calculate: level {}: found simple connection:  X bridge {}, port {} <--> Y bridge {}, port {}",
                     level,xBridge.getId(),xyDesignatedPort,yBridgeId,yxDesignatedPort);
            // X is a leaf of Y then iterate
            if (xyDesignatedPort == rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.info("calculate: level {}: X Bridge: {} is a leaf of Y Bridge: {}, going one level down",
                         level,xBridge.getId(),yBridge.getId());
                findBridgesTopo(yx,m_domain.getSharedSegment(yBridgeId, yxDesignatedPort), xBridge, xBFT,level);
                return;
            }
            // Y is a leaf of X then remove Y from topSegment
            if (yxDesignatedPort == yrDesignatedPort && xyDesignatedPort != rx.getSecondBridgeConnectionPort()) {
                //create a SharedSegment with root port
                LOG.info("calculate: level {}: Y bridge: {} is a leaf of X Bridge: {}, creating shared segment for port {}", 
                         level,yBridge.getId(),xBridge.getId(),xyDesignatedPort);
                SharedSegment leafSegment = new SharedSegment(m_domain,yx.getSimpleConnection().getDlink());
                leafSegment.setBridgeMacLinks(yx.getSimpleConnection().getLinks());
                leafSegment.setDesignatedBridge(xBridge.getId());
                m_domain.add(leafSegment);
                portsAdded.add(xyDesignatedPort);
                
                LOG.info("calculate: level {}: removing Y through set {} macs from top segment", 
                         level, yx.getFirstBridgeTroughSet().size());
                topSegment.removeMacs(yx.getFirstBridgeTroughSet());
                LOG.info("calculate: level {}: removing Y Bridge {} from top segment", 
                         level,yBridge.getId());
                topSegment.removeBridge(yBridgeId);
            }            
            // this is a clear violation  of the topology tree rule
            if (xyDesignatedPort != rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.error("findBridgesTopo: level {}: topology mismatch. Clearing...topology",level);
                m_domain.clearTopology();
                return;
            }            
            throughSets.add(yx.getFirstBridgeTroughSet());
        }
        // if we are here is because X is NOT a leaf of any bridge found
        // on topSegment so X is connected to top Segment by it's root 
        // port or rx is a direct connection
        LOG.info("calculate: level {}: bridge: {}, removing X through set {} macs from top segment", 
                 level, xBridge.getId(), rx.getSecondBridgeTroughSet().size());
        topSegment.removeMacs(rx.getSecondBridgeTroughSet());
        topSegment.assign(rx.getSimpleConnection().getLinks(),rx.getSimpleConnection().getDlink());
        LOG.info("calculate: level {}: bridge: {}, assigning links to top segment: bridge {} port: {}, mac size: {}, bft size: {}",
                 level, xBridge.getId(),topSegment.getDesignatedBridge(),
                 topSegment.getDesignatedPort(), topSegment.getMacsOnSegment().size(), topSegment.getBridgeMacLinks().size());
        LOG.info("calculate: level {}: bridge: {}, removing {} Y through sets from top segment", 
                 level,xBridge.getId(),throughSets.size());
        for (Map<Integer, List<BridgeMacLink>> throughSet: throughSets)
            topSegment.removeMacs(throughSet);
        for (Integer xbridgePort : rx.getSecondBridgeTroughSet().keySet()) {
            if (portsAdded.contains(xbridgePort))
                continue;
            SharedSegment xleafSegment = new SharedSegment(m_domain);
            xleafSegment.setBridgeMacLinks(rx.getSecondBridgeTroughSet().get(xbridgePort));
            xleafSegment.setDesignatedBridge(xBridge.getId());  
            LOG.info("calculate: level {}: adding shared segment to topology: root bridge {} port: {}, mac size: {}, bft size: {}",
                     level,
                     xleafSegment.getDesignatedBridge(),
                     xleafSegment.getDesignatedPort(),
                     xleafSegment.getMacsOnSegment().size(),
                     xleafSegment.getBridgeMacLinks().size());
            m_domain.add(xleafSegment);
        }
    }
    
    private Bridge electRootBridge() {
        //if there is only one bridge....
        if (m_domain.getBridges().size() == 1) {
            LOG.debug("calculate: electRootBridge: only one bridge in topology, electing to root");
            return m_domain.getBridges().iterator().next();
        }
        //if null try set the stp roots
        Set<String> rootBridgeIds=new HashSet<String>();
        for (BridgeElement bridge: m_bridgeelements ) {
            if (InetAddressUtils.isValidStpBridgeId(bridge.getStpDesignatedRoot()) && !bridge.getBaseBridgeAddress().equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()))) {
                rootBridgeIds.add(InetAddressUtils.getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()));
                LOG.info("calculate: electRootBridge: found stp root {} on the BroadcastDomain: ", bridge.getStpDesignatedRoot());
            }
        }
        //well only one root bridge should be defined....
        //otherwise we need to skip calculation
        //so here is the place were we can
        //manage multi stp domains...
        //ignoring for the moment....
        for (String rootBridgeId: rootBridgeIds) {
            for (BridgeElement bridge: m_bridgeelements) {
                LOG.debug("calculate: electRootBridge: searching for stp root, stp root address: {}, "
                        + "against bridge: {}", rootBridgeId,bridge.getBaseBridgeAddress());
                if (bridge.getBaseBridgeAddress().equals((rootBridgeId))) {
                    LOG.debug("calculate: electRootBridge: found stp root bridge: {}", bridge.getBaseBridgeAddress());
                    return m_domain.getBridge(bridge.getNode().getId());
                }
            }
        }

        // no spanning tree root?
        // why I'm here?
        // not root bridge defined (this mean no calculation yet done...
        // so checking the best into not parsed
        int size=0;
        if (m_rootBridgeBFT != null )
            size = m_rootBridgeBFT.size();
        
        Bridge rootBridge = null;
        for (Bridge bridge:  m_notYetParsedBFTMap.keySet()) {
            if (size < m_notYetParsedBFTMap.get(bridge).size()) {
                rootBridge = bridge;
                size = m_notYetParsedBFTMap.get(bridge).size();
            }
        }
        if (rootBridge != null ) {
            LOG.debug("calculate: electRootBridge: elected bridge {} with max bft size \"{}\" in topology",rootBridge.getId(), size);
            return rootBridge;
        }

        if (m_domain.hasRootBridge()) {
            LOG.debug("calculate: electRootBridge: mantaining old root bridge: {}", m_domain.getRootBridgeId());
            return m_domain.getRootBridge();
        }

        // still not found...get the first
        return m_domain.getBridges().iterator().next();

    }

    private void loadFirstLevelSharedSegment(List<BridgeMacLink> electedRootBFT) {
         Map<Integer, SharedSegment> rootleafs = new HashMap<Integer, SharedSegment>();
         for (BridgeMacLink link : electedRootBFT) {
             if (link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED)
                 continue;
             if (rootleafs.containsKey(link.getBridgePort()))
                 rootleafs.get(link.getBridgePort()).add(link);
             else
                 rootleafs.put(link.getBridgePort(),
                               new SharedSegment(m_domain,link));
         }
         for (SharedSegment rootleaf : rootleafs.values()) {
             LOG.info("calculate: adding shared segment to topology: root bridge {} port: {}, mac size: {}, bft size: {}",
                      rootleaf.getDesignatedBridge(),
                      rootleaf.getDesignatedPort(),
                      rootleaf.getMacsOnSegment().size(),
                      rootleaf.getBridgeMacLinks().size());
             m_domain.add(rootleaf);
         }
        
    }
    private void setUpRoot(Bridge electedRoot) {
        if (electedRoot.isRootBridge() && !m_notYetParsedBFTMap.containsKey(electedRoot)) {
            LOG.info("calculate: elected root bridge: {}, is old root bridge with old bft",
                     electedRoot.getId());
        } else if (electedRoot.isRootBridge() && m_notYetParsedBFTMap.containsKey(electedRoot)) {
            LOG.info("calculate: elected root bridge: {}, is old root bridge with new bft",
                     electedRoot.getId());
            m_rootBridgeBFT=new ArrayList<BridgeMacLink>(m_notYetParsedBFTMap.remove(electedRoot));
            LOG.debug("calculate: updated new bft for root bridge {}  size:  {}",
                          electedRoot.getId(), m_rootBridgeBFT.size());
            if (m_domain.getTopology().isEmpty()) {
                LOG.info("calculate: elected root bridge: {}, clean topology found. Adding level 0 shared segments",
                         electedRoot.getId());
                loadFirstLevelSharedSegment(m_rootBridgeBFT);
            }
        } else if (!electedRoot.isRootBridge() && m_notYetParsedBFTMap.containsKey(electedRoot)) {
            LOG.info("calculate: elected root bridge: {}, is new root bridge and has new bft",
                         electedRoot.getId());
           List<BridgeMacLink>  electedRootBFT = new ArrayList<BridgeMacLink>(
                   m_notYetParsedBFTMap.remove(electedRoot));
           if (m_domain.getTopology().isEmpty()) {
               LOG.info("calculate: new elected root bridge: {}, is the first bridge in topology. Adding level 0 shared segments",
                        electedRoot.getId());
                loadFirstLevelSharedSegment(electedRootBFT);
                electedRoot.setRootBridge(true);
                electedRoot.setRootPort(null);
           } else {
                LOG.info("calculate: find topology of new elected root bridge:  {} with old root as root bridge: {}",
                         electedRoot.getId(),m_domain.getRootBridgeId());
                calculate(m_domain.getRootBridge(), m_rootBridgeBFT,
                                electedRoot, electedRootBFT);
                m_domain.hierarchySetUp(electedRoot);
           }
           m_rootBridgeBFT = electedRootBFT;
           LOG.debug("calculate: set bft for root bridge {}  size:  {}",
                          electedRoot.getId(), m_rootBridgeBFT.size());
        } else if (!electedRoot.isRootBridge() && !m_notYetParsedBFTMap.containsKey(electedRoot)){
            LOG.info("calculate: elected root bridge: {}, is new root bridge with old bft",
                     electedRoot.getId());
            m_domain.hierarchySetUp(electedRoot);
            m_rootBridgeBFT =getBFT(electedRoot);
            LOG.debug("calculate: set bft for root bridge {}  size:  {}",
                           electedRoot.getId(), m_rootBridgeBFT.size());
        }
        
    }
    
    public void clearTopologyForBridge(Bridge bridge) {
        if (bridge.isRootBridge()) {
            LOG.debug("calculate: clearTopologyForBridge: bridge {}, is root bridge. setting up a new hierarchy before clean",
                      bridge.getId());
            for (SharedSegment segment: m_domain.getSharedSegmentOnTopologyForBridge(bridge.getId())) {
                Integer newRootId = segment.getFirstNoDesignatedBridge();
                if (newRootId == null)
                    continue;
                Bridge newRootBridge=null;
                for (Bridge curBridge: m_domain.getBridges()) {
                    if (curBridge.getId().intValue() == newRootId.intValue()) {
                        newRootBridge=curBridge;
                        break;
                    }
                }
                if (newRootBridge == null)
                    continue;
                m_rootBridgeBFT = getBFT(newRootBridge);
                m_domain.hierarchySetUp(newRootBridge);
                break;
            }
        }
        LOG.debug("calculate: clearTopologyForBridge: bridge {}, bridge root port {}",
                  bridge.getId(), bridge.getRootPort());
        //all the topology will be merged with the segment for bridge designated port
        SharedSegment topsegment = m_domain.getSharedSegment(bridge.getId(), bridge.getRootPort());
        if (topsegment != null) {
            LOG.debug("calculate: clearTopologyForBridge: removing bridge {}: top segment nodes {}, macs {}, designated {}, port {}",
                      bridge.getId(),topsegment.getBridgeIdsOnSegment(),topsegment.getMacsOnSegment(),
                      topsegment.getDesignatedBridge(),topsegment.getDesignatedPort());
            topsegment.removeBridge(bridge.getId());
            LOG.debug("calculate: clearTopologyForBridge: removed bridge {}: top segment nodes {}, macs {}, designated {}, port {}",
                      bridge.getId(),topsegment.getBridgeIdsOnSegment(),topsegment.getMacsOnSegment(),
                      topsegment.getDesignatedBridge(),topsegment.getDesignatedPort());
        } else {
            LOG.debug("calculate: clearTopologyForBridge {}: no top segment found",
                      bridge.getId());
        }

        for (SharedSegment segment: m_domain.removeSharedSegmentOnTopologyForBridge(bridge.getId())) {
            LOG.debug("calculate: clearTopologyForBridge merging bridge {} on top for segment: nodes {}, macs {}, designated {}, port {}",
                      bridge.getId(),segment.getBridgeIdsOnSegment(),segment.getMacsOnSegment(),
                      segment.getDesignatedBridge(),segment.getDesignatedPort());
            if (topsegment != null)
                topsegment.mergeBridge(segment,bridge.getId());
        }        

    }

    public List<BridgeMacLink> getBFT(Bridge bridge) {
        LOG.debug("calculate: getBFT: bridge: {}, bridge root port {}",
                  bridge.getId(), bridge.getRootPort());

        Map<Integer,Set<String>> bft = new HashMap<Integer, Set<String>>();
        Integer bridgeId = bridge.getId();
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        OnmsNode node=new OnmsNode();
        node.setId(bridgeId);
        for (SharedSegment segment: m_domain.getTopology()) {
            
            Set<String> macs = segment.getMacsOnSegment();
            
            if (macs == null || macs.isEmpty())
                continue;
            Integer bridgeport = goUp(segment,bridge,0);
            LOG.info("calculate: getBFT: bridge: {}, assigning macs {} to port {}", bridgeId,macs, bridgeport );
            if (!bft.containsKey(bridgeport))
                bft.put(bridgeport, new HashSet<String>());
            bft.get(bridgeport).addAll(macs);
       }
            
        for (Integer bridgePort: bft.keySet()) {
            for (String mac: bft.get(bridgePort)) {
                BridgeMacLink link = new BridgeMacLink();
                link.setNode(node);
                link.setBridgePort(bridgePort);
                link.setMacAddress(mac);
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                links.add(link);
            }
        }
        return links;
    }

    private Integer goUp(SharedSegment down,Bridge bridge, int level) {
        if (level == 30) {
            LOG.warn("calculate: level {}: bridge: {}, too many iteration on topology exiting.....",level,bridge.getId());
            m_domain.clearTopology();
            return -1;
        }
        LOG.debug("calculate: goUp: level: {}, checking up segment designated bridge {}, designated port {}, bridge {}, with root port {}",
                  level,down.getDesignatedBridge(),down.getDesignatedPort(), bridge.getId(), bridge.getRootPort());
            Integer upBridgeId = down.getDesignatedBridge();
            // if segment is on the bridge then...
            if (upBridgeId.intValue() == bridge.getId().intValue()) {
                LOG.debug("calculate: goUp: level: {}, return bridge {}, designated port {}",
                          level,down.getDesignatedBridge(),down.getDesignatedPort());
                return down.getDesignatedPort();
            }
            // if segment is a root segment add mac on port
            if (upBridgeId.intValue() == m_domain.getRootBridgeId().intValue()) {
                LOG.debug("calculate: goUp: level: {}, got root bridge, returning bridge {}, root port {}",
                          level,bridge.getId(),bridge.getRootPort());
                return bridge.getRootPort();
            }
            // iterate until you got it
            Bridge upBridge = null;
            for (Bridge cbridge: m_domain.getBridges()) {
                if (cbridge.getId().intValue() == bridge.getId().intValue())
                    continue;
                LOG.debug("calculate: goUp: level: {}, searching bridge {}, parsing bridge {}",
                          level,down.getDesignatedBridge(),cbridge.getId());
                if (cbridge.getId().intValue() == upBridgeId.intValue()) {
                    LOG.debug("calculate: goUp: level: {}, searching bridge {}, found bridge {}, with root port {}",
                              level,down.getDesignatedBridge(),cbridge.getId(), cbridge.getRootPort());
                    upBridge=cbridge;
                    break;
                }
            }
            if (upBridge == null) {
                LOG.debug("calculate: goUp: level: {}, searching bridge {}, no bridge found!!!",
                          level,down.getDesignatedBridge());
                return null;
            }
            SharedSegment up = m_domain.getSharedSegment(upBridge.getId(),upBridge.getRootPort());
            if (up == null) {
                LOG.debug("calculate: goUp: level: {}, no shared segment found on bridge {}, root port {}!!!",
                          level,down.getDesignatedBridge());
                return null;
            }
        return goUp(up, bridge,++level);
    }
    
    @Override
    public boolean isReady() {
        if (!m_linkd.getQueryManager().hasUpdatedBft(getNodeId())) {
            LOG.info("isReady: node: {}, without updated bft. Topology Discovery Not Ready", getNodeId());
            return false;
        }
        return true;

    }
}

