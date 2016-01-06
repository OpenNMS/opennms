package org.opennms.netmgt.enlinkd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
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
        
        public BridgeTopologyHelper(Bridge xBridge, List<BridgeMacLink> xBFT, Bridge yBridge, List<BridgeMacLink> yBFT) {
            super();
            LOG.debug("BridgeTopologyHelper: searching simple connection for X: {} with bft size: {}",xBridge.getId(),xBFT.size());
            LOG.debug("BridgeTopologyHelper: searching simple connection for Y: {} with bft size: {}",yBridge.getId(),yBFT.size());
            
            for (BridgeMacLink xlink: xBFT) {
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    xmactoport.put(xlink.getMacAddress(), xlink);
                    if (!m_throughSetX.containsKey(xlink.getBridgePort()))
                        m_throughSetX.put(xlink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSetX.get(xlink.getBridgePort()).add(xlink);
                }
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    xBridge.addBridgeMac(xlink.getMacAddress());
            }
            for (BridgeMacLink ylink: yBFT) {
                if (yBridge.getId().intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    ymactoport.put(ylink.getMacAddress(), ylink);
                    if (!m_throughSetY.containsKey(ylink.getBridgePort()))
                        m_throughSetY.put(ylink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSetY.get(ylink.getBridgePort()).add(ylink);
                }
                if (yBridge.getId().intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    yBridge.addBridgeMac(ylink.getMacAddress());
            }
            
            boolean cond1X = condition1BridgeX(xBridge.getBridgeMacs());
            boolean cond1Y = condition1BridgeY(yBridge.getBridgeMacs());
            
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

                blink.setNode(xylink.getNode());
                blink.setBridgePort(xylink.getBridgePort());
                blink.setBridgePortIfIndex(xylink.getBridgePortIfIndex());
                blink.setBridgePortIfName(xylink.getBridgePortIfName());
                blink.setVlan(xylink.getVlan());

                blink.setDesignatedNode(yxlink.getNode());
                blink.setDesignatedPort(yxlink.getBridgePort());
                blink.setDesignatedPortIfIndex(yxlink.getBridgePortIfIndex());
                blink.setDesignatedPortIfName(yxlink.getBridgePortIfName());
                blink.setDesignatedVlan(yxlink.getVlan());
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
                LOG.debug("BridgeTopologyHelper: condition2BridgeX: parsing common BFT mac: {}",mac);
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
                LOG.debug("BridgeTopologyHelper: condition2BridgeY: parsing common BFT mac: {}",mac);
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
        private boolean condition1BridgeX(Set<String> xmacs) {
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
        private boolean condition1BridgeY(Set<String> ymacs) {
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
    
    public BroadcastDomain getDomain() {
        return m_domain;
    }

    public void setDomain(BroadcastDomain domain) {
        m_domain = domain;
    }

    public Map<Bridge, List<BridgeMacLink>> getNotYetParsedBFTMap() {
        return m_notYetParsedBFTMap;
    }

    public void setNotYetParsedBFTMap(
            Map<Bridge, List<BridgeMacLink>> notYetParsedBFTMap) {
        m_notYetParsedBFTMap = notYetParsedBFTMap;
    }

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
        setInitialSleepTime(linkd.getInitialSleepTime()+180000);
    }

    @Override
    protected void runCollection() {
        
        Date now = new Date();
        m_domain = m_linkd.getQueryManager().getBridgeTopologyBroadcastDomain(getNodeId());
        if (m_domain == null ) {
            LOG.warn("run: no broadcast domain found for node: {}", getNodeId());
            return;
        }
        if (m_domain.isCalculating()) {
            LOG.info("run: broadcast domain is locked for calculation either on node {}....", getNodeId());
            return;
        }
        if (!m_domain.hasTopologyUpdatedBft()) {
            LOG.info("run: broadcast domain with no topology change found for node: {}. exiting...", getNodeId());
            return;
        }
        m_domain.getLock();
        m_notYetParsedBFTMap = m_domain.removeUpdateMap();
        LOG.info("run: calculating broadcast domain with topology change found for node: {}.", getNodeId());
        calculate();
        LOG.info("run: broadcast domain topology for node: {}. calculated.", getNodeId());
        m_domain.releaseLock();
        LOG.info("run: saving broadcast domain topology for node: {}.", getNodeId());
        m_linkd.getQueryManager().store(m_domain);
        LOG.info("run: saved broadcast domain topology for node: {}.", getNodeId());
        
        for (Integer nodeid: m_domain.getBridgeNodesOnDomain()) {
           LOG.info("run: reconcile topology for node: {} on Broadcast Domain",nodeid);
           m_linkd.getQueryManager().reconcileBridgeTopology(nodeid, now);
        }
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
            LOG.info("calculate: clean topology for bridge:  {} with updated bft", xBridge.getId());
            m_domain.clearTopologyForBridge(xBridge);
        }        

        Bridge electedRoot = selectRootBridge();
        if (electedRoot == null || electedRoot.getId() == null) {
            LOG.error("calculate: electedRoot should not be null");
            return;
        }
        setUpRoot(electedRoot);
        
        Set<Bridge> nodetobeparsed = new HashSet<Bridge>(m_notYetParsedBFTMap.keySet());
        for (Bridge xBridge: nodetobeparsed) {
            LOG.info("calculate: start: calculate topology for nodeid {}",xBridge.getId());
            calculate(electedRoot, m_domain.getRootBridgeBFT(), xBridge, new ArrayList<BridgeMacLink>(m_notYetParsedBFTMap.remove(xBridge)));
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
            List<BridgeMacLink> yBft = m_domain.getEffectiveBFT(yBridge);
            BridgeTopologyHelper   yx = new BridgeTopologyHelper(yBridge,yBft ,xBridge, xBFT);
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
                SharedSegment leafSegment = new SharedSegment(m_domain,xBridge.getId(), xyDesignatedPort);
                leafSegment.add(yx.getSimpleConnection().getDlink());
                leafSegment.setBridgeMacLinks(yx.getSimpleConnection().getLinks());
                m_domain.add(leafSegment);
                portsAdded.add(xyDesignatedPort);
                
                LOG.info("calculate: level {}: removing Y through set {} macs from top segment", 
                         level, yx.getFirstBridgeTroughSet().size());
                topSegment.removeMacs(yx.getFirstBridgeTroughSet());
                LOG.info("findBridgesTopo: level {}: removing Y Bridge {} from top segment", 
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
            SharedSegment xleafSegment = new SharedSegment(m_domain,xBridge.getId(),
                                                           xbridgePort);
            xleafSegment.setBridgeMacLinks(rx.getSecondBridgeTroughSet().get(xbridgePort));
            LOG.info("calculate: level {}: adding shared segment to topology: root bridge {} port: {}, mac size: {}, bft size: {}",
                     level,
                     xleafSegment.getDesignatedBridge(),
                     xleafSegment.getDesignatedPort(),
                     xleafSegment.getMacsOnSegment().size(),
                     xleafSegment.getBridgeMacLinks().size());
            m_domain.add(xleafSegment);
        }
    }
    
    private Bridge selectRootBridge() {
        //if there is only one bridge....
        if (m_domain.getBridges().size() == 1) {
            LOG.debug("calculate: selectRootBridge: only one bridge in topology, electing to root");
            return m_domain.getBridges().iterator().next();
        }
        //if null try set the stp roots
        Set<String> rootBridgeIds=new HashSet<String>();
        for (Bridge bridge: m_domain.getBridges()) {
            rootBridgeIds.addAll(bridge.getOtherStpRoots());
        }
        LOG.debug("calculate: selectRootBridge: searching for stp root in topology {}",rootBridgeIds);
        //well only one root bridge should be defined....
        //otherwise we need to skip calculation
        //so here is the place were we can
        //manage multi stp domains...
        //ignoring for the moment....
        for (String rootBridgeId: rootBridgeIds) {
            LOG.debug("calculate: selectRootBridge: searching for stp root found: {}", rootBridgeId);
            for (Bridge bridge: m_domain.getBridges()) {
                LOG.debug("calculate: selectRootBridge: searching for stp root found: {}, parsing bridge {}", rootBridgeId,bridge.getId());
                if (bridge.hasBridgeId(rootBridgeId)) {
                    LOG.debug("calculate: selectRootBridge: found stp root bridge: {}", bridge.getId());
                    return bridge;
                }
            }
        }

        // no spanning tree root?
        // why I'm here?
        // not root bridge defined (this mean no calculation yet done...
        // so checking the best into not parsed
        int size=0;
        if (m_domain.getRootBridgeBFT() != null )
            size = m_domain.getRootBridgeBFT().size();
        
        Bridge rootBridge = null;
        for (Bridge bridge:  m_notYetParsedBFTMap.keySet()) {
            if (size < m_notYetParsedBFTMap.get(bridge).size()) {
                rootBridge = bridge;
                size = m_notYetParsedBFTMap.get(bridge).size();
            }
        }
        if (rootBridge != null ) {
            LOG.debug("calculate: selectRootBridge: elected bridge {} with max bft size \"{}\" in topology",rootBridge.getId(), size);
            return rootBridge;
        }

        if (m_domain.getRootBridgeId() != null) {
            LOG.debug("calculate: selectRootBridge: mantaining old root bridge: {}", m_domain.getRootBridgeId());
            return m_domain.getRootBridge();
        }

        // still not found...get the first
        return m_domain.getBridges().iterator().next();

    }

    private void setUpRoot(Bridge electedRoot) {
        if (electedRoot.isRootBridge() && !m_notYetParsedBFTMap.containsKey(electedRoot)) {
            LOG.info("calculate: elected root bridge: {}, is old root bridge with old bft",
                     electedRoot.getId());
        } else if (electedRoot.isRootBridge() && m_notYetParsedBFTMap.containsKey(electedRoot)) {
            LOG.info("calculate: elected root bridge: {}, is old root bridge with new bft",
                     electedRoot.getId());
            m_domain.setRootBridgeBFT(new ArrayList<BridgeMacLink>(m_notYetParsedBFTMap.remove(electedRoot)));
            LOG.debug("calculate: updated new bft for root bridge {}  size:  {}",
                          electedRoot.getId(), m_domain.getRootBridgeBFT().size());
        } else if (!electedRoot.isRootBridge() && m_notYetParsedBFTMap.containsKey(electedRoot)) {
            LOG.info("calculate: elected root bridge: {}, is new root bridge and has new bft",
                         electedRoot.getId());
           List<BridgeMacLink>  electedRootBFT = new ArrayList<BridgeMacLink>(
                   m_notYetParsedBFTMap.remove(electedRoot));
           if (m_domain.getTopology().isEmpty()) {
               LOG.info("calculate: new elected root bridge:  {}, is the first bridge in topology. Adding level 0 shared segments",
                        electedRoot.getId());
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
                m_domain.setRootBridgeId(electedRoot.getId());
                electedRoot.setRootBridge(true);
                electedRoot.setRootPort(null);
           } else {
                LOG.info("calculate: find topology of new elected root bridge:  {} with old root as root bridge: {}",
                         electedRoot.getId(),m_domain.getRootBridgeId());
                calculate(m_domain.getRootBridge(), m_domain.getRootBridgeBFT(),
                                electedRoot, electedRootBFT);
                m_domain.setRootBridgeId(electedRoot.getId());
                electedRoot.setRootBridge(true);
                electedRoot.setRootPort(null);
                m_domain.hierarchySetUp();
           }
           m_domain.setRootBridgeBFT(electedRootBFT);
           LOG.debug("calculate: set bft for root bridge {}  size:  {}",
                          electedRoot.getId(), m_domain.getRootBridgeBFT().size());
        } else if (!electedRoot.isRootBridge() && !m_notYetParsedBFTMap.containsKey(electedRoot)){
            LOG.info("calculate: elected root bridge: {}, is new root bridge with old bft",
                     electedRoot.getId());
            m_domain.setRootBridgeId(electedRoot.getId());
            electedRoot.setRootBridge(true);
            electedRoot.setRootPort(null);
            m_domain.hierarchySetUp();
            m_domain.setRootBridgeBFT(m_domain.getEffectiveBFT(electedRoot));
            LOG.debug("calculate: set bft for root bridge {}  size:  {}",
                           electedRoot.getId(), m_domain.getRootBridgeBFT().size());
        }
        
    }
    
    //FIXME 
   // use spanning tree when there is no way of finding link...
    /*
      private void checkStp(Bridge xbridge, Bridge ybridge) {
          
          if (xbridge.getOtherStpRoots().isEmpty() && ybridge.getOtherStpRoots().isEmpty())
              return;
          // find a common root...
          Set<String> commonroots = new HashSet<String>();
          if (xbridge.getOtherStpRoots().isEmpty()) {
              commonroots.addAll(new HashSet<String>(xbridge.getBridgeMacs()));
              commonroots.retainAll(new HashSet<String>(ybridge.getOtherStpRoots()));
          }
          if (ybridge.getOtherStpRoots().isEmpty())
              commonroots.addAll(new HashSet<String>(ybridge.getBridgeMacs()));
              
          for (String xroot: xbridge.getOtherStpRoots())
          
          //for (BridgeStpLink stplink: m_STPLinks) {
          //    stplink.getDesignatedBridgeAddress()
         // }
          
      } */



}

