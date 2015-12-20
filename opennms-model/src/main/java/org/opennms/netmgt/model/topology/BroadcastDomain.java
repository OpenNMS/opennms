package org.opennms.netmgt.model.topology;

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
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME
// check synchronized and volatile
// 
//FIXME 
// use spanning tree when there is no way of finding link...
//FIXME
// getEffectiveBFT is broken
//
public class BroadcastDomain {

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastDomain.class);

    Set<Bridge> m_bridges = new HashSet<Bridge>();
    List<SharedSegment> m_topology = new ArrayList<SharedSegment>();
    
    Integer m_rootBridgeId;
    List<BridgeMacLink> m_rootBridgeBFT = new ArrayList<BridgeMacLink>();
    Map<Integer,Date> m_lastUpdate = new HashMap<Integer, Date>();
    boolean m_calculating = false;
    boolean m_topologyChanged = false;
    
    Map<Integer, List<BridgeMacLink>> m_notYetParsedBFTMap = new HashMap<Integer, List<BridgeMacLink>>();
    List<BridgeStpLink> m_STPLinks = new ArrayList<BridgeStpLink>();
    
    private class SimpleConnection {
        final List<BridgeMacLink> m_links;
        final BridgeBridgeLink m_dlink;
        
        SimpleConnection(List<BridgeMacLink> links, BridgeBridgeLink dlink){
            m_links = links;
            m_dlink = dlink;
        }

        public List<BridgeMacLink> getLinks() {
            return m_links;
        }

        public BridgeBridgeLink getDlink() {
            return m_dlink;
        }
    }

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
        Map<Integer, List<BridgeMacLink>> m_throughSet = new HashMap<Integer, List<BridgeMacLink>>();
        SimpleConnection m_simpleconnection; 
        
        public BridgeTopologyHelper(Bridge xBridge, List<BridgeMacLink> xBFT, Integer yBridgeId, List<BridgeMacLink> yBFT) {
            super();
            Set<String> xmacs = new HashSet<String>();
            Set<String> ymacs = new HashSet<String>();
     
            for (BridgeMacLink xlink: xBFT) {
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) 
                    xmactoport.put(xlink.getMacAddress(), xlink);
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    xmacs.add(xlink.getMacAddress());
            }
            for (BridgeMacLink ylink: yBFT) {
                if (yBridgeId.intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    ymactoport.put(ylink.getMacAddress(), ylink);
                    if (!m_throughSet.containsKey(ylink.getBridgePort()))
                        m_throughSet.put(ylink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSet.get(ylink.getBridgePort()).add(ylink);
                }
                if (yBridgeId.intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    ymacs.add(ylink.getMacAddress());
            }
            
            boolean cond1X = condition1BridgeX(xmacs);
            boolean cond1Y = condition1BridgeY(ymacs);
            
            if (!cond1X || !cond1Y) {
                Set<String> commonlearnedmacs = new HashSet<String>(xmactoport.keySet()); 
                commonlearnedmacs.retainAll(new HashSet<String>(ymactoport.keySet()));
                LOG.debug("BridgeTopologyHelper: common (learned mac) size: {} ",commonlearnedmacs.size());
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
            LOG.debug("BridgeTopologyHelper: m_xy: {}", m_xy);
            LOG.debug("BridgeTopologyHelper: m_yx: {}", m_yx);
            
            BridgeMacLink xylink = null;
            BridgeMacLink yxlink = null;
            List<BridgeMacLink> connectionsOnSegment=new ArrayList<BridgeMacLink>();
            for (BridgeMacLink xlink: xBFT) {
                if (xlink.getBridgePort() == m_xy) {
                    if (!ymactoport.containsKey(xlink.getMacAddress()) || m_yx == ymactoport.get(xlink.getMacAddress()).getBridgePort())
                        connectionsOnSegment.add(xlink);
                    if (xylink == null)
                        xylink = xlink;
                }
            }
            int ylinks = connectionsOnSegment.size();
            LOG.debug("BridgeTopologyHelper: added {}, Y links on segment", ylinks);
            for (BridgeMacLink ylink: yBFT) {
                if (ylink.getBridgePort() == m_yx) {
                    if (!xmactoport.containsKey(ylink.getMacAddress()) || m_xy == xmactoport.get(ylink.getMacAddress()).getBridgePort())
                        connectionsOnSegment.add(ylink);
                    if (yxlink == null)
                        yxlink = ylink;
                }
            }
            int xlinks = connectionsOnSegment.size() - ylinks;
            LOG.debug("BridgeTopologyHelper: added {}, X links on segment", xlinks);
    
            BridgeBridgeLink blink = new BridgeBridgeLink();
            if (xylink != null && yxlink != null ) {
                blink.setNode(xylink.getNode());
                blink.setBridgePort(xylink.getBridgePort());
                blink.setBridgePortIfIndex(xylink.getBridgePortIfIndex());
                blink.setBridgePortIfName(xylink.getBridgePortIfName());
                
                blink.setDesignatedNode(yxlink.getNode());
                blink.setDesignatedPort(yxlink.getBridgePort());
                blink.setDesignatedPortIfIndex(yxlink.getBridgePortIfIndex());
                blink.setDesignatedPortIfName(yxlink.getBridgePortIfName());
            }
            m_simpleconnection = new SimpleConnection(connectionsOnSegment, blink);
            m_throughSet.remove(m_yx);            
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
                LOG.debug("condition3: parsing common BFT mac: {}",mac);
                if (mac1 == null) {
                    mac1=mac;
                    yp1=ymactoport.get(mac).getBridgePort();
                    xp1=xmactoport.get(mac).getBridgePort();
                    LOG.debug("condition3: mac1: {} xp1: {} yp1: {} ", mac1,xp1,yp1);
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort() == yp1 && xmactoport.get(mac).getBridgePort() == xp1)
                    continue;
                if (mac2 == null) {
                    mac2=mac;
                    yp2=ymactoport.get(mac).getBridgePort();
                    xp2=xmactoport.get(mac).getBridgePort();
                    LOG.debug("condition3: mac2: {} xp2: {} yp2: {} ", mac2,xp2,yp2);
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort() == yp2 && xmactoport.get(mac).getBridgePort() == xp2)
                    continue;
                Integer yp3 = ymactoport.get(mac).getBridgePort();
                Integer xp3 = xmactoport.get(mac).getBridgePort();
                LOG.debug("condition3: mac3: {} x3: {} yp3: {} ", mac,xp3,yp3);

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
            LOG.debug("condition2BridgeX: checking m_xy common macs {} ", commonlearnedmacs );
            String mac1=null;
            Integer p1=null;
            Integer xy1=null;
            boolean allmaconthesameport=true;
            for (String mac: commonlearnedmacs) {
                if (mac1 == null) {
                    mac1 = mac;
                    p1 = ymactoport.get(mac).getBridgePort();
                    xy1= xmactoport.get(mac).getBridgePort();
                    continue;
                }
                if (ymactoport.get(mac).getBridgePort() == p1)
                    continue;
                if (xmactoport.get(mac).getBridgePort() == xy1) {
                    m_xy=xy1;
                    return;
                }
                allmaconthesameport=false;
            }
            if (allmaconthesameport)
                m_xy=xy1;
        }
        
        // condition 2Y
        // if exists m_y, m_1 and m_2, p1 and p2 on X : m_y belongs to FDB(xy,X) 
        //                                              m_1 belongs to FDB(p1,X) FDB(yx,Y)
        //                                              m_2 belongs to FDB(p2,X) FDB(yx,Y)
        private void condition2BridgeY(Set<String> commonlearnedmacs) {
            LOG.debug("condition2BridgeY: checking m_yx common macs {} ", commonlearnedmacs );
            String mac1=null;
            Integer p1=null;
            Integer yx1=null;
            boolean allmaconthesameport=true;
            for (String mac: commonlearnedmacs) {
                if (mac1 == null) {
                    mac1 = mac;
                    p1 = xmactoport.get(mac).getBridgePort();
                    yx1= ymactoport.get(mac).getBridgePort();
                    continue;
                }
                if (xmactoport.get(mac).getBridgePort() == p1)
                    continue;
                if (ymactoport.get(mac).getBridgePort() == yx1) {
                    m_yx=yx1;
                    return;
                }
                allmaconthesameport=false;
            }
            if (allmaconthesameport)
                m_yx=yx1;
        }
        
        // there is a mac of X found on Y BFT
        private boolean condition1BridgeX(Set<String> xmacs) {
            LOG.debug("condition1BridgeX: bridge X macs {} ", xmacs );
            for (String xmac: xmacs) {
                if (ymactoport.containsKey(xmac)) {
                    m_yx = ymactoport.get(xmac).getBridgePort();
                    return true;
                }
            }
            return false;
        }
            
        // there is a mac of Y found on X BFT
        private boolean condition1BridgeY(Set<String> ymacs) {
            LOG.debug("condition1BridgeY: bridge Y macs {} ", ymacs );
            for (String ymac: ymacs) {
                if (xmactoport.containsKey(ymac)) {
                    m_xy = xmactoport.get(ymac).getBridgePort();
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
        
        public Map<Integer,List<BridgeMacLink>> getSecondBridgeTroughSet() {
            return m_throughSet;
        }

    }
    
    private class Bridge {

        final Integer m_id;
        List<BridgeElement> m_bridgeIds = new ArrayList<BridgeElement>();
        Integer m_rootPort;
        boolean m_isRootBridge=false;

        public Bridge(Integer id) {
            super();
            m_id = id;
        }

        public List<BridgeElement> getBridgeElements() {
            return m_bridgeIds;
        }
        
        public void setBridgeElements(List<BridgeElement> bridgeIds) {
            m_bridgeIds = bridgeIds;
        }
        
        public boolean hasBridgeId(String bridgeId) {
            if (bridgeId == null)
                return false;
            for (BridgeElement element: m_bridgeIds) {
                if (bridgeId.equals(element.getBaseBridgeAddress()))
                        return true;
            }
            return false;
        }
                
        public Set<String> getOtherStpRoots() {
            Set<String> stpRoots = new HashSet<String>();
            for (BridgeElement element: m_bridgeIds) {
                if (InetAddressUtils.isValidStpBridgeId(element.getStpDesignatedRoot())) {
                    String stpRoot = InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot());
                    if ( stpRoot.equals(element.getBaseBridgeAddress()))
                            continue;
                    stpRoots.add(stpRoot);
                }
            }
            return stpRoots;
        }

        public Bridge(BridgeElement bridgeElement) {
            super();
            m_id = bridgeElement.getNode().getId();
            m_bridgeIds.add(bridgeElement);
        }


        public Integer getRootPort() {
            return m_rootPort;
        }

        public void setRootPort(Integer rootPort) {
            m_rootPort = rootPort;
        }

        public boolean isRootBridge() {
            return m_isRootBridge;
        }

        public void setRootBridge(boolean isRootBridge) {
            m_isRootBridge = isRootBridge;
        }

        public void addBridgeElement(BridgeElement bridgeElement) {
            if (bridgeElement.getNode().getId() != m_id)
                return; 
            for (BridgeElement curBridgeElement: m_bridgeIds) {
                if (curBridgeElement.getBaseBridgeAddress().equals(bridgeElement.getBaseBridgeAddress())) {
                    curBridgeElement = bridgeElement;
                    return;
                }
            }
            m_bridgeIds.add(bridgeElement);
        }

        public Integer getId() {
            return m_id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Bridge other = (Bridge) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (m_id == null) {
                if (other.m_id != null)
                    return false;
            } else if (!m_id.equals(other.m_id))
                return false;
            return true;
        }

        private BroadcastDomain getOuterType() {
            return BroadcastDomain.this;
        }
        
        
    }
    
    public Set<String> getMacsOnDomain() {
        Set<String>macs = new HashSet<String>();
        for (SharedSegment segment: m_topology) 
            macs.addAll(segment.getMacsOnSegment());
        return macs;
    }

    public void addBridgeElement(BridgeElement bridgeElement) {
        LOG.info("addBridgeElement: loading element with address {} on node {}", bridgeElement.getBaseBridgeAddress(),bridgeElement.getNode().getId());
        for (Bridge bridge: m_bridges) {
            if (bridge.getId() == bridgeElement.getNode().getId()) {
                bridge.addBridgeElement(bridgeElement);
                return;
            }
        }
        m_bridges.add(new Bridge(bridgeElement));
    }

    public void addTopologyEntry(SharedSegment segment) {
        m_topology.add(segment);
        for (Integer nodeId : segment.getBridgeIdsOnSegment()) {
            m_bridges.add(new Bridge(nodeId));
        }
    }
    
    public void addSTPEntry(BridgeStpLink stplink ) {
        m_STPLinks.add(stplink);
    }
    
    public boolean containsAtleastOne(Set<Integer> nodeids) {
        for (Bridge bridge: m_bridges) {
            for (Integer nodeid:nodeids) {
                if (bridge.getId().intValue() == nodeid.intValue())
                    return true;
            }
        }
        return false;
    }
    
    public boolean containBridgeId(Integer nodeid) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeid.intValue())
                return true;
        }
        return false;
    }

    public void loadBFT(int nodeId, List<BridgeMacLink> maclinks,List<BridgeStpLink> stplinks, List<BridgeElement> elements) {
        m_topologyChanged=true;
        LOG.info("loadBFT: start: loading bridge forwarding table for node: {}, with size: {}", nodeId, maclinks.size());
        m_notYetParsedBFTMap.put(nodeId, maclinks);
        LOG.info("loadBFT: set bridge elements on node {}", nodeId);
        Bridge added = null;
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeId) {
                added = bridge;
            }
        }
        if (added == null) {
            added = new Bridge(nodeId);
            m_bridges.add(added);
        }
        added.setBridgeElements(elements);

        if (stplinks != null && !stplinks.isEmpty()) {
            //remove all stp link in the list to let them
            // substituted with the new incoming list
            List<BridgeStpLink> allstplinks = new ArrayList<BridgeStpLink>();
            for (BridgeStpLink link: m_STPLinks) {
                if (link.getNode().getId().intValue() == nodeId)
                    continue;
                allstplinks.add(link);
            }
            allstplinks.addAll(stplinks);
            m_STPLinks=allstplinks;
        }
        LOG.info("loadBFT: stop: loaded bridge forwarding table for node {}", nodeId);

    }
    
    public synchronized Date getLastUpdate(Integer nodeid) {
        return m_lastUpdate.get(nodeid);
    }
    
    public synchronized void setLastUpdate(Integer nodeid, Date now) {
        m_lastUpdate.put(nodeid, now);
    }

    public synchronized List<SharedSegment> getTopology() {
        return m_topology;
    }
        
    public synchronized void removeBridge(int bridgeId) {
        Bridge bridge = null;
        for (Bridge curbridge: m_bridges) {
            if (curbridge.getId() == bridgeId) {
                bridge=curbridge;
                break;
            }
        }
        // if not in domain: return
        if (bridge==null)
            return;
        m_topologyChanged=true;
        // if last bridge in domain: clear all and return
        if (m_bridges.size() == 1) {
            m_topology.clear();
            m_bridges.clear();
            m_rootBridgeId =  null;
            m_rootBridgeBFT.clear();
            return;
        }
        
        cleanTopologyForBridge(bridge);
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge cur: m_bridges) {
            if (cur.getId().intValue() == bridgeId) 
                continue;
            bridges.add(bridge);
        }
        m_bridges = bridges;
        m_lastUpdate.remove(bridgeId);

    }
    
    private synchronized void cleanTopologyForBridge(Bridge bridge) {
        Integer bridgeId = bridge.getId();
        // if is root: rearrange topology with a new root before deleting.
        if (bridge.isRootBridge()) {
            for (SharedSegment segment: getSharedSegmentOnBridge(bridgeId)) {
                Integer newRootId = segment.getFirstNoDesignatedBridge();
                if (newRootId == null)
                    continue;
                m_rootBridgeBFT = getEffectiveBFT(newRootId);
                m_rootBridgeId = newRootId;
                for (Bridge curBridge: m_bridges) {
                    if (curBridge.getId().intValue() == m_rootBridgeId.intValue()) {
                        curBridge.setRootBridge(true);
                        curBridge.setRootPort(null);
                    }
                }
                hierarchySetUp();
                break;
            }
        }

        //all the topology will be merged with the segment for bridge designated port
        SharedSegment topsegment = getSharedSegment(bridgeId, bridge.getRootPort());
        if (topsegment != null)
            topsegment.removeBridge(bridgeId);
        
        for (SharedSegment segment: removeSharedSegmentOnBridge(bridgeId)) {
            if (topsegment != null)
                topsegment.mergeBridge(segment,bridgeId);
        }        
    }
    
    public synchronized boolean isEmpty() {
        return m_bridges.isEmpty();
    }
        
    public synchronized boolean isCalculating() {
        return m_calculating;
    }

    public synchronized boolean isTopologyChanged() {
        return m_topologyChanged;
    }

    public synchronized Set<Integer> getUpdatedNodes() {
        return m_lastUpdate.keySet();
    }

    public synchronized void calculate() {
        LOG.info("calculate: start:  calculate topology");
        m_calculating = true;
        m_topologyChanged=false;
        
        for (Bridge curbridge: m_bridges) {
            if (m_notYetParsedBFTMap.keySet().contains(curbridge.getId()) ) {
                LOG.info("calculate: clean topology for bridge:  {}", curbridge.getId());
                cleanTopologyForBridge(curbridge);
            }
        }
        
        Bridge electedRoot = selectRootBridge();
        List<BridgeMacLink> electedRootBFT = m_notYetParsedBFTMap.remove(electedRoot.getId()); 
        if (electedRootBFT != null) {
            LOG.info("calculate: elected root bridge: {}, has new bft", electedRoot.getId());
            if (m_topology.isEmpty()) {
                LOG.info("calculate: creating shared segment for root bridge in empty topology:  {}", electedRoot.getId());
                Map<Integer, SharedSegment> rootleafs=new HashMap<Integer, SharedSegment>();
                for (BridgeMacLink link: electedRootBFT) {
                    if (link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED)
                        continue;
                    if (!rootleafs.containsKey(link.getBridgePort()))
                        rootleafs.put(link.getBridgePort(), new SharedSegment(link.getNode().getId(),link.getBridgePort()));
                    rootleafs.get(link.getBridgePort()).add(link);
                }
                for (SharedSegment rootleaf: rootleafs.values()) {
                    LOG.info("calculate: adding shared segment to topology: root bridge {} port: {}, mac size: {}, bft size: {}",rootleaf.getDesignatedBridge(),
                             rootleaf.getDesignatedPort(), rootleaf.getMacsOnSegment().size(), rootleaf.getBridgeMacLinks().size());
                    m_topology.add(rootleaf);
                }
                LOG.info("calculate: created: shared segment for root bridge in topology:  {}", electedRoot.getId());
            } else {
                LOG.info("calculate: find topology for elected root bridge:  {}", electedRoot.getId());
                findBridgesTopo(getRootBridge(), m_rootBridgeBFT, electedRoot.getId(),electedRootBFT);
            }
            m_rootBridgeBFT = electedRootBFT;
            LOG.info("calculate: set root bridge {} bft size:  {}", electedRoot.getId(), m_rootBridgeBFT.size());
        } 
        m_rootBridgeId = electedRoot.getId();
        electedRoot.setRootBridge(true);
        electedRoot.setRootPort(null);
        hierarchySetUp();

        for (Integer nodeid: m_notYetParsedBFTMap.keySet()) 
            findBridgesTopo(getRootBridge(), m_rootBridgeBFT, nodeid, m_notYetParsedBFTMap.remove(nodeid));
       
        m_calculating = false;
        LOG.info("calculate: stop:  calculate topology");

    }
        
    // here we assume that rbridge exists in topology
    // while xBridge is to be added
    private void findBridgesTopo(Bridge rBridge, List<BridgeMacLink> rBFT, Integer xBridgeId, List<BridgeMacLink> xBFT) {
        LOG.info("findBridgesTopo: calculate topology for nodeid {}, against {}", xBridgeId,rBridge.getId());
        Bridge xBridge = null;
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == xBridgeId.intValue()) {
                xBridge=bridge;
                break;
            }
        }
        if (xBridge == null) {
            LOG.error("findBridgesTopo: not found bridge for nodeid {} exiting....", xBridgeId);
        }
        
        BridgeTopologyHelper rx = new BridgeTopologyHelper(rBridge, rBFT, xBridgeId,xBFT);
        Integer rxDesignatedPort = rx.getFirstBridgeConnectionPort();
        if (rxDesignatedPort == null) {
            LOG.info("findBridgesTopo: cannot found top simple connection:  nodeid {}",rBridge.getId());
            return;
        }
        Integer xrDesignatedPort = rx.getSecondBridgeConnectionPort();
        if (xrDesignatedPort == null) {
             LOG.info("findBridgesTopo: cannot found bottom simple connection:  nodeid {}",xBridgeId);
             return;
        }
        LOG.info("findBridgesTopo: found simple connection:  nodeid {}, port {} <--> nodeid {}, port {}",rBridge.getId(),rxDesignatedPort,xBridgeId,xrDesignatedPort);
        LOG.info("findBridgesTopo: set root port {} for nodeid {}",xrDesignatedPort,xBridgeId);
        xBridge.setRootPort(xrDesignatedPort);
        xBridge.setRootBridge(false);
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = getSharedSegment(rBridge.getId(),rxDesignatedPort);
        if (topSegment == null) {
            LOG.warn("findBridgesTopo: not found: top segment:  for nodeid {}, port {}",rBridge.getId(),rxDesignatedPort);
            m_topology.clear();
            return;
        }

        for (Bridge yBridge: getBridgeOnSharedSegment(topSegment)) {
            LOG.debug("findBridgesTopo: checking Bridge {} to see how is connected to topSegment", yBridge.getId());
            Integer yBridgeId = yBridge.getId();
            // X is a leaf of top segment: of course
            if (yBridgeId.intValue() == rBridge.getId().intValue()) {
                LOG.info("findBridgesTopo: nodeid {} is a leaf of top segment {}, sure", xBridgeId,yBridge.getId());
                continue;
            } 
            Integer yrDesignatedPort = yBridge.getRootPort();
            LOG.info("findBridgesTopo: found Y designated port:  nodeid {}, port {}",yBridgeId,yrDesignatedPort);
            BridgeTopologyHelper   yx = new BridgeTopologyHelper(yBridge, getEffectiveBFT(yBridgeId),xBridgeId, xBFT);
            Integer  xyDesignatedPort = yx.getSecondBridgeConnectionPort();
            Integer  yxDesignatedPort = yx.getFirstBridgeConnectionPort();
            LOG.info("findBridgesTopo: found simple connection:  nodeid {}, port {} <--> nodeid {}, port {}",xBridgeId,xyDesignatedPort,yBridgeId,yxDesignatedPort);
            // X is a leaf of Y then iterate
            if (xyDesignatedPort == xrDesignatedPort && yxDesignatedPort != yrDesignatedPort) {
                LOG.info("findBridgesTopo: nodeid {} is a leaf of {}, going down",xBridgeId,yBridge.getId());
                findBridgesTopo(yBridge, getEffectiveBFT(yBridge.getId()), xBridgeId, xBFT);
                return;
            }
            // Y is a leaf of X then remove Y from topSegment
            if (yxDesignatedPort == yrDesignatedPort && xyDesignatedPort != xrDesignatedPort) {
                //create a SharedSegment with root port
                LOG.info("findBridgesTopo: removing from top segment nodeid {} that is a leaf of {} ", yBridge.getId(),xBridgeId);
                SharedSegment leafSegment = new SharedSegment(xBridgeId, xyDesignatedPort);
                leafSegment.assign(yx.getSimpleConnection().getLinks(),yx.getSimpleConnection().getDlink());
                m_topology.add(leafSegment);
                topSegment.removeBridge(yBridge.getId());
            }            
            // this is a clear violation  of the topology tree rule
            if (xyDesignatedPort != xrDesignatedPort && yxDesignatedPort != yrDesignatedPort) {
                LOG.error("findBridgesTopo: topology mismatch. Clearing...topology");
                m_topology.clear();
                return;
            }            
        }
        // if we are here is because X is NOT a leaf of any bridge found
        // on topSegment so X is connected to top Segment by it's root 
        // port or rx is a direct connection
        topSegment.assign(rx.getSimpleConnection().getLinks(),rx.getSimpleConnection().getDlink());
        LOG.info("findBridgesTopo: updating top segment to topology: bridge {} port: {}, mac size: {}, bft size: {}",topSegment.getDesignatedBridge(),
                 topSegment.getDesignatedPort(), topSegment.getMacsOnSegment().size(), topSegment.getBridgeMacLinks().size());
       for (Integer xbridgePort: rx.getSecondBridgeTroughSet().keySet()) {
            SharedSegment xleafSegment = new SharedSegment(xBridgeId, xbridgePort);
            xleafSegment.setBridgeMacLinks(rx.getSecondBridgeTroughSet().get(xbridgePort));
            LOG.info("findBridgesTopo: adding shared segment to topology: root bridge {} port: {}, mac size: {}, bft size: {}",xleafSegment.getDesignatedBridge(),
                     xleafSegment.getDesignatedPort(), xleafSegment.getMacsOnSegment().size(), xleafSegment.getBridgeMacLinks().size());

            m_topology.add(xleafSegment);
        }
        LOG.info("findBridgesTopo: topology calculated for nodeid: {}", xBridgeId);
    }

    private List<SharedSegment> getSharedSegmentOnBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
        }
        return segmentsOnBridge;
    }

    private List<SharedSegment> removeSharedSegmentOnBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        List<SharedSegment> topology = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
            else 
                topology.add(segment);
        }
        m_topology = topology;
        return segmentsOnBridge;
    }
    
    private Set<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        Set<Integer> nodeidsOnSegment = new HashSet<Integer>();
        if (segment.noMacsOnSegment()) {
            for (BridgeBridgeLink link : segment.getBridgeBridgeLinks()) {
                nodeidsOnSegment.add(link.getNode().getId());
                nodeidsOnSegment.add(link.getDesignatedNode().getId());
            }
        } else {
            for (BridgeMacLink link: segment.getBridgeMacLinks()) {
                nodeidsOnSegment.add(link.getNode().getId());
            }
        }
        Set<Bridge> bridgesOn = new HashSet<BroadcastDomain.Bridge>();
        for (Bridge bridge: m_bridges) {
            if (nodeidsOnSegment.contains(bridge.getId()))
                bridgesOn.add(bridge);
        }
        return bridgesOn;
    }

    private SharedSegment getSharedSegment(Integer bridgeId, Integer bridgePort) {
        if (bridgeId == null || bridgePort == null)
            return null;
        for (SharedSegment segment: m_topology) {
            if (segment.containsPort(bridgeId, bridgePort)) 
                return segment;
        }
        return null;
    }
    
    private Bridge selectRootBridge() {
        LOG.info("selectRootBridge: start");
        Bridge rootBridge= null;
        //if there is only one bridge....
        if (m_bridges.size() == 1) {
            LOG.debug("selectRootBridge: only one bridge in topology set root");
            rootBridge = m_bridges.iterator().next();
        }
        //if null try set the stp roots
        if (rootBridge == null) {
            LOG.debug("selectRootBridge: searching for stp root in topology");
            Set<String> rootBridgeIds=new HashSet<String>();
            for (Bridge bridge: m_bridges) {
                rootBridgeIds.addAll(bridge.getOtherStpRoots());
            }
            //well only one root bridge should be defined....
            //otherwise we need to skip calculation
            //so here is the place were we can
            //manage multi stp domains...
            //ignoring for the moment....
            for (String rootBridgeId: rootBridgeIds) {
                LOG.debug("selectRootBridge: searching for stp root found: {}", rootBridgeId);
                for (Bridge bridge: m_bridges) {
                    LOG.debug("selectRootBridge: searching for stp root found: {}, parsing bridge {}", rootBridgeId,bridge.getId());
                    if (bridge.hasBridgeId(rootBridgeId)) {
                        LOG.debug("selectRootBridge: found stp root bridge: {}", bridge.getId());
                        rootBridge = bridge;
                        break;
                    }
                }
            }
        }

        // no spanning tree root?
        // then find root among switches with
        // updated bft with max bft size
        if (m_rootBridgeId != null) {
            LOG.debug("selectRootBridge: mantaining old root bridge: {}", m_rootBridgeId);
            return getRootBridge();
        }
        // why I'm here?
        // not root bridge defined (this mean no calculation yet done...
        // so checking the best into not parsed
        if (rootBridge == null) {
            int size = 0;
            Integer rootNodeid = null;
            for (Integer nodeid:  m_notYetParsedBFTMap.keySet()) {
                int cursize = m_notYetParsedBFTMap.get(nodeid).size();
                if (size < cursize) {
                    rootNodeid = nodeid;
                    size = cursize;
                }
            }
            if (rootNodeid != null ) {
                for (Bridge bridge: m_bridges) {
                    if (bridge.getId().intValue() == rootNodeid.intValue()) {
                        rootBridge = bridge;
                        break;
                    }
                }
            }
        }
        
        // still not found...get the first
        if (rootBridge == null)
            rootBridge=m_bridges.iterator().next();
        
        LOG.info("selectRootBridge: rootBridge is {}", rootBridge.getId());
        return rootBridge;

    }
        
    private Bridge getRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge;
        }
        return null;
    }
    
    private List<BridgeMacLink> getEffectiveBFT(Integer bridgeId) {
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        OnmsNode node=new OnmsNode();
        node.setId(bridgeId);
        for (SharedSegment segment: getSharedSegmentOnBridge(bridgeId)) {
          //  if (segment.getDesignatedBridge() != bridgeId)
          //      continue;
            Integer bridgePort = null;
            if (segment.noMacsOnSegment()) {
                for (BridgeBridgeLink link: segment.getBridgeBridgeLinks()) {
                    if (link.getNode().getId() == bridgeId) {
                        bridgePort = link.getBridgePort();
                        break;
                    }
                    if (link.getDesignatedNode().getId() == bridgeId) {
                        bridgePort = link.getDesignatedPort();
                        break;
                    }
                }
            } else {
                for (BridgeMacLink link: segment.getBridgeMacLinks()) {
                    if (link.getNode().getId() == bridgeId) {
                        bridgePort = link.getBridgePort();
                        links.add(link);
                    }
                }
            }
            
            for (String mac: getForwardingSet(segment, bridgeId)) {
                BridgeMacLink link = new BridgeMacLink();
                link.setNode(node);
                link.setBridgePort(bridgePort);
                link.setMacAddress(mac);
                links.add(link);
            }
        }
        return links;
    }

    private Set<String> getForwardingSet(SharedSegment segment, Integer bridgeId) {
        Set<String> macs = new HashSet<String>();
        for (Bridge bridge: getBridgeOnSharedSegment(segment)) {
            if (bridge.getId() == bridgeId)
                continue;
            for (SharedSegment s2: getSharedSegmentOnBridge(bridge.getId())) {
                macs.addAll(s2.getMacsOnSegment());
                macs.addAll(getForwardingSet(s2, bridge.getId()));
            }
        }
       return macs;
    }

    
    private void hierarchySetUp() {
        //top level
        for (SharedSegment segment: getSharedSegmentOnBridge(m_rootBridgeId)) {
            LOG.debug("hierarchySetUp: set up tree with root: {}",m_rootBridgeId);
            segment.setDesignatedBridge(m_rootBridgeId);
            segment.setDesignatedPort(segment.getPortForBridge(m_rootBridgeId));
            LOG.debug("hierarchySetUp: set up tree with root: {} port: {}",
                      segment.getDesignatedBridge(),segment.getDesignatedPort());
            tier(segment, m_rootBridgeId);
        }
    }
    
    private void tier(SharedSegment segment, Integer rootid) {
        LOG.debug("tier: tier one level under bridge: {}, segment: {}",rootid,segment);
        for (Integer bridgeid: segment.getBridgeIdsOnSegment()) {
            LOG.debug("tier: found bridge: {}",bridgeid);
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            for (SharedSegment s2: getSharedSegmentOnBridge(bridgeid)) {
                s2.setDesignatedBridge(bridgeid);
                s2.setDesignatedPort(s2.getPortForBridge(bridgeid));
                tier(s2,bridgeid);
            }
        }
    }

}
