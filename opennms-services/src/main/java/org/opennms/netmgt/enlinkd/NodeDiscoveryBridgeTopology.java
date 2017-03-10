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
        Map<Integer, List<BridgeMacLink>> m_throughSetX= new HashMap<Integer, List<BridgeMacLink>>();
        Map<Integer, List<BridgeMacLink>> m_throughSetY= new HashMap<Integer, List<BridgeMacLink>>();
        Set<String> m_xythrowsetmacs = new HashSet<String>();
        SimpleConnection m_simpleconnection; 
        public BridgeTopologyHelper(Bridge xBridge, List<BridgeMacLink> xBFT,Bridge yBridge, List<BridgeMacLink> yBFT) {
            super();
            Map<String,BridgeMacLink> xmactoport = new HashMap<String, BridgeMacLink>();
            Map<String,BridgeMacLink> ymactoport = new HashMap<String, BridgeMacLink>();
            Set<String> xmacs = new HashSet<String>();
            Set<String> ymacs = new HashSet<String>();             		
            LOG.debug("BridgeTopologyHelper: nodes [{}, {}]: search simple connection.\n xbft\n{}\n ybft\n{}",
                    xBridge.getId(),
                    yBridge.getId(),
                    BroadcastDomain.printTopologyBFT(xBFT),
                    BroadcastDomain.printTopologyBFT(yBFT));
            
            for (BridgeMacLink xlink: xBFT) {
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    xmactoport.put(xlink.getMacAddress(), xlink);
                }
                if (xBridge.getId().intValue() == xlink.getNode().getId().intValue() && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    xmacs.add(xlink.getMacAddress());
            }
            for (BridgeMacLink ylink: yBFT) {
                if (yBridge.getId().intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    ymactoport.put(ylink.getMacAddress(), ylink);
                }
                if (yBridge.getId().intValue() == ylink.getNode().getId().intValue() && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) 
                    ymacs.add(ylink.getMacAddress());
            }
            if (m_domain.getBridgeMacAddresses(xBridge.getId()) != null)
             xmacs.addAll(m_domain.getBridgeMacAddresses(xBridge.getId()));
            if (m_domain.getBridgeMacAddresses(yBridge.getId()) != null)
                ymacs.addAll(m_domain.getBridgeMacAddresses(yBridge.getId()));

            // there is a mac of Y found on X BFT
            Integer xy = condition1(ymacs, xmactoport);
            if (xy != null) {
            	m_xy=xy;
            }

            // there is a mac of X found on Y BFT
            Integer yx = condition1(xmacs, ymactoport);
            if (yx != null) {
            	m_yx=yx;
            }
            	            
            if (m_xy == null || m_yx == null) {
                Set<String> commonlearnedmacs = new HashSet<String>(xmactoport.keySet()); 
                commonlearnedmacs.retainAll(new HashSet<String>(ymactoport.keySet()));
                LOG.debug("BridgeTopologyHelper: bridges [{},{}] common (learned mac): {}",
                		xBridge.getId(),
                		yBridge.getId(),
                		commonlearnedmacs);
                if (m_yx != null && m_xy == null) 
                    m_xy = condition2(commonlearnedmacs,m_yx,ymactoport,xmactoport);
                if (m_yx == null && m_xy != null)
                    m_yx = condition2(commonlearnedmacs,m_xy,xmactoport,ymactoport);
                if (m_yx == null || m_xy == null) {
                    List<Integer> ports = condition3(commonlearnedmacs,xmactoport,ymactoport);
                    m_xy = ports.get(0);
                    m_yx= ports.get(1);
                }
                if (m_xy == null || m_xy == null)
                    return;
            }    
            LOG.debug("BridgeTopologyHelper: simple connection: [nodeid {}, port {}] <--> [nodeid {}, port {}]", 
            		xBridge.getId(), 
            		m_xy,
            		yBridge.getId()
            		, m_yx);
            
            BridgeMacLink xylink = null;
            BridgeMacLink yxlink = null;
            Set<String> macsOnSegment=new HashSet<String>();
            for (BridgeMacLink xlink: xBFT) {
                if (xlink.getBridgePort() == m_xy && xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    if (ymactoport.get(xlink.getMacAddress()) != null 
                    		&& m_yx == ymactoport.get(xlink.getMacAddress()).getBridgePort()) {
                    	macsOnSegment.add(xlink.getMacAddress());
                        LOG.debug("BridgeTopologyHelper: simple connection: link added: [bridge:[{}],port:{},mac:{}].", 
                        		xlink.getNode().getId(),
                        		xlink.getBridgePort(),
                        		xlink.getMacAddress());
                    } else {
                    	m_xythrowsetmacs.add(xlink.getMacAddress());
                        LOG.debug("BridgeTopologyHelper: simple connection: throghset: mac added: [bridge:[{}],port:{},mac:{}].", 
                        		xlink.getNode().getId(),
                        		xlink.getBridgePort(),
                        		xlink.getMacAddress());
                    }
                    if (xylink == null)
                        xylink = xlink;
                } else if (xlink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    LOG.debug("BridgeTopologyHelper: throgh set: link added: [bridge:[{}],port:{},mac:{}].", 
                    		xlink.getNode().getId(),
                    		xlink.getBridgePort(),
                    		xlink.getMacAddress());
                    if (!m_throughSetX.containsKey(xlink.getBridgePort()))
                    	m_throughSetX.put(xlink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSetX.get(xlink.getBridgePort()).add(xlink);
                }
            }
            
            for (BridgeMacLink ylink: yBFT) {
                if (ylink.getBridgePort() == m_yx && ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    if ( xmactoport.get(ylink.getMacAddress()) != null &&
                    		m_xy == xmactoport.get(ylink.getMacAddress()).getBridgePort()) {
                    	macsOnSegment.add(ylink.getMacAddress());
                        LOG.debug("BridgeTopologyHelper: simple connection: link added: [bridge:[{}],port:{},mac:{}].", 
                        		ylink.getNode().getId(),
                        		ylink.getBridgePort(),
                        		ylink.getMacAddress());
                    } else {
                    	m_xythrowsetmacs.add(ylink.getMacAddress());
                        LOG.debug("BridgeTopologyHelper: simple connection: throghset: mac added: [bridge:[{}],port:{},mac:{}].", 
                        		ylink.getNode().getId(),
                        		ylink.getBridgePort(),
                        		ylink.getMacAddress());
                    }
                    if (yxlink == null)
                        yxlink = ylink;
                } else if (ylink.getBridgeDot1qTpFdbStatus() == BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED) {
                    LOG.debug("BridgeTopologyHelper: throgh set: link added: [bridge:[{}],port:{},mac:{}].", 
                    		ylink.getNode().getId(),
                    		ylink.getBridgePort(),
                    		ylink.getMacAddress());
                    if (!m_throughSetY.containsKey(ylink.getBridgePort()))
                    	m_throughSetY.put(ylink.getBridgePort(), new ArrayList<BridgeMacLink>());
                    m_throughSetY.get(ylink.getBridgePort()).add(ylink);
                }
            }
            LOG.debug("BridgeTopologyHelper: links:{}, found on simple connection.", 
            		macsOnSegment.size());
    
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
            m_simpleconnection = new SimpleConnection(macsOnSegment, blink);
        }

        private List<Integer> condition3(Set<String> commonlearnedmacs,Map<String,BridgeMacLink> xbft,Map<String,BridgeMacLink> ybft) {
        
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
                LOG.debug("BridgeTopologyHelper: condition3: parsing common BFT mac: {}",mac);
                if (mac1 == null) {
                    mac1=mac;
                    yp1=ybft.get(mac).getBridgePort();
                    xp1=xbft.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition3: mac1: {} xp1: {} yp1: {} ", mac1,xp1,yp1);
                    continue;
                }
                if (ybft.get(mac).getBridgePort() == yp1 && xbft.get(mac).getBridgePort() == xp1)
                    continue;
                if (mac2 == null) {
                    mac2=mac;
                    yp2=ybft.get(mac).getBridgePort();
                    xp2=xbft.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition3: mac2: {} xp2: {} yp2: {} ", mac2,xp2,yp2);
                    continue;
                }
                if (ybft.get(mac).getBridgePort() == yp2 && xbft.get(mac).getBridgePort() == xp2)
                    continue;
                Integer yp3 = ybft.get(mac).getBridgePort();
                Integer xp3 = xbft.get(mac).getBridgePort();
                LOG.debug("BridgeTopologyHelper: condition3: mac3: {} x3: {} yp3: {} ", mac,xp3,yp3);

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
        
        // condition 2
        // if exists m_x, m_1 and m_2, p1 and p2 on Y : m_x belongs to FDB(yx,Y) 
        //                                              m_1 belongs to FDB(p1,Y) FDB(xy,X)
        //                                              m_2 belongs to FDB(p2,Y) FDB(xy,X)
        private Integer condition2(Set<String> commonlearnedmacs, Integer yx, Map<String,BridgeMacLink> ybft, Map<String,BridgeMacLink> xbft) {
            String mac1=null;
            String mac2=null;
            Integer p1=null;
            Integer xy1=null;
            Integer p2=null;
            Integer xy2=null;
            for (String mac: commonlearnedmacs) {
                if (mac1 == null) {
                    mac1 = mac;
                    p1 = ybft.get(mac).getBridgePort();
                    xy1= xbft.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition2: mac1: {} xy1: {} p1: {} ", mac1,xy1,p1);
                    continue;
                }
                if (ybft.get(mac).getBridgePort().intValue() == p1.intValue())
                    continue;
                if (xbft.get(mac).getBridgePort().intValue() == xy1.intValue()) {
                    LOG.debug("BridgeTopologyHelper: condition2: xy1 bridge port {}",xy1);
                    return xy1;
                }
                if (mac2 == null) {
                    mac2 = mac;
                    p2 = ybft.get(mac).getBridgePort();
                    xy2= xbft.get(mac).getBridgePort();
                    LOG.debug("BridgeTopologyHelper: condition2: mac2: {} xy2: {} p2: {} ", mac2,xy2,p2);
                    continue;
                }
                if (ybft.get(mac).getBridgePort().intValue() == p2.intValue())
                    continue;
                if (xbft.get(mac).getBridgePort().intValue() == xy2.intValue()) {
                    LOG.debug("BridgeTopologyHelper: condition2: xy2 bridge port {}",xy2);
                    return xy2;
                }
            }
            if (xy2 == null) 
                return xy1;
            return null;
        }
        
        private Integer condition1(Set<String> bridgemacaddressess, Map<String,BridgeMacLink> otherbridgeft) {
            for (String mac: bridgemacaddressess) {
                if (otherbridgeft.containsKey(mac)) {
                    LOG.debug("BridgeTopologyHelper: condition1: base address {} --> port: {} ",
                    		mac,otherbridgeft.get(mac).getBridgePort());

                    return otherbridgeft.get(mac).getBridgePort();
                }
            }
            LOG.debug("BridgeTopologyHelper: condition1: [base address: {}]. Not found.",
            		bridgemacaddressess);
            return null;
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

        public Set<String> getFirstBridgeTroughSet() {
        	Set<String> macs = new HashSet<String>();
        	for (List<BridgeMacLink> links: m_throughSetX.values())
        		for (BridgeMacLink link: links)
        			macs.add(link.getMacAddress());
        	macs.addAll(m_xythrowsetmacs);
            return macs;
        }

        public Set<String> getSecondBridgeTroughSet() {
        	Set<String> macs = new HashSet<String>();
        	for (List<BridgeMacLink> links: m_throughSetY.values())
        		for (BridgeMacLink link: links)
        			macs.add(link.getMacAddress());
        	macs.addAll(m_xythrowsetmacs);
            return macs;
        }
        
        public Map<Integer,List<BridgeMacLink>> getSecondBridgeTroughSetBft() {
        	return m_throughSetY;
        }

    }

    Map<Bridge,List<BridgeMacLink>> m_notYetParsedBFTMap;
    BroadcastDomain m_domain;
    //List<BridgeStpLink> m_STPLinks = new ArrayList<BridgeStpLink>();

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

    private Set<Integer> getAllNodesWithUpdatedBFTOnDomain(Set<String>incomingSet, Map<Integer,List<BridgeMacLink>> nodeBftMap) {
        Set<Integer> nodeswithupdatedbftonbroadcastdomain= new HashSet<Integer>();
        nodeswithupdatedbftonbroadcastdomain.add(getNodeId());

        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. Start", getNodeId());
        for (Integer curNodeId: nodeBftMap.keySet()) {
            if (curNodeId.intValue() == getNodeId())
                continue;
            Set<String>retainedSet = new HashSet<String>();
            for (BridgeMacLink link: nodeBftMap.get(curNodeId)) {
                retainedSet.add(link.getMacAddress());
            }
            LOG.debug("run: node: [{}], parsing updated bft node: [{}], macs {}", getNodeId(), curNodeId,retainedSet);
            retainedSet.retainAll(incomingSet);
            LOG.debug("run: node: [{}], node: [{}] - common mac address set: {}", getNodeId(), curNodeId, retainedSet);
            if (retainedSet.size() > 10
                    || retainedSet.size() >= incomingSet.size() * 0.1) {
                nodeswithupdatedbftonbroadcastdomain.add(curNodeId);
                LOG.debug("run: node: [{}], node: [{}] - put on same broadcast domain, common macs: {} ", getNodeId(), 
                         curNodeId,
                         retainedSet);
            }
        }
        LOG.info("run: node: [{}], getting nodes with updated bft on broadcast domain. End", getNodeId());
        return nodeswithupdatedbftonbroadcastdomain;
    	
    }

    @Override
    public void run() {

        if (!m_linkd.getQueryManager().hasUpdatedBft(getNodeId())) {
            LOG.info("run: node: [{}], no bft.Exiting Bridge Topology Discovery", getNodeId());
            return;
        }

    	List<BridgeMacLink> links =  m_linkd.
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
            for (BridgeMacLink link : links) {
                incomingSet.add(link.getMacAddress());
            }            
        }
        LOG.debug("run: node: [{}]. macs found: {}", getNodeId(), incomingSet);

        LOG.info("run: node: [{}], getting broadcast domain. Start", getNodeId());
        
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
            LOG.debug("run: node: [{}], parsing domain with nodes: {}, macs: {}", getNodeId(), domain.getBridgeNodesOnDomain(),domain.getMacsOnDomain());
            Set<String>retainedSet = new HashSet<String>(
                                                          domain.getMacsOnDomain());
            retainedSet.retainAll(incomingSet);
            LOG.debug("run: node: [{}], retained: {}", getNodeId(), retainedSet);
            // should contain at list 10 or 10% of the all size
            if (retainedSet.size() > 10
                    || retainedSet.size() >= incomingSet.size() * 0.1) {
                LOG.debug("run: node: [{}], domain {} found!",getNodeId(), domain.getBridgeNodesOnDomain());
                    m_domain = domain;
            }
        }
        if (m_domain == null) {
            LOG.debug("run: node: [{}] Creating a new Domain", getNodeId());
            m_domain = new BroadcastDomain();
            m_linkd.getQueryManager().save(m_domain);
        }
        LOG.debug("run: node: [{}], Found Broadcast Bomain. Print Topology. {} ", getNodeId(),m_domain.printTopology());
        LOG.info("run: node: [{}], getting broadcast domain. End", getNodeId());
                
        Map<Integer,List<BridgeMacLink>> nodeBftMap = m_linkd.getQueryManager().getUpdateBftMap();

        Set<Integer> nodeswithupdatedbftonbroadcastdomain = getAllNodesWithUpdatedBFTOnDomain(incomingSet,nodeBftMap);
 
        boolean stop = false;
        for (BroadcastDomain domain : m_linkd.getQueryManager().getAllBroadcastDomains()) {
        	if (m_domain == domain)
        		continue;
            LOG.debug("run: node [{}]: cleaning broadcast domain {}.",
            		getNodeId(),
                      domain.getBridgeNodesOnDomain());
            for (Integer curNodeId: nodeswithupdatedbftonbroadcastdomain) {
                if (domain.containBridgeId(curNodeId)) {
                    LOG.debug("run: node [{}]: node [{}]: removing from broadcast domain {}!",
                    		getNodeId(),
                    		  curNodeId,
                              domain.getBridgeNodesOnDomain());
                    if (!domain.getLock(this)) {
                        LOG.info("run: node [{}]: node [{}]: broadcast domain {}: is locked. cannot clear topology.", 
                        		getNodeId(),
                                 curNodeId,                                  
                                 domain.getBridgeNodesOnDomain());
                        stop=true;
                        continue;
                    }
                    domain.clearTopologyForBridge(curNodeId);
                    domain.removeBridge(curNodeId);
                    m_linkd.getQueryManager().store(domain,now);
                    domain.releaseLock(this);
                    m_linkd.getQueryManager().cleanBroadcastDomains();
                }
            }
        }
        
        if (stop || !m_domain.getLock(this)) {
            LOG.info("run: node [{}]: broadcast domain locked. scheduling with time interval {}", 
                    getNodeId(), 
                    getInitialSleepTime());
        	schedule();
        	return;        	
        }
        
        m_notYetParsedBFTMap = new HashMap<Bridge, List<BridgeMacLink>>();        
        for (Integer nodeid: nodeswithupdatedbftonbroadcastdomain) {
            sendStartEvent(nodeid);
            m_domain.addBridge(new Bridge(nodeid));
            LOG.debug("run: node: [{}], getting update bft for node [{}] on domain", getNodeId(),nodeid);
            List<BridgeMacLink> bft = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(nodeid);
            if (bft == null || bft.isEmpty()) {
                LOG.debug("run: node: [{}], no update bft for node [{}] on domain", getNodeId(),nodeid);
                continue;
            }
            m_notYetParsedBFTMap.put(m_domain.getBridge(nodeid), bft);
        }
        
        for (Integer nodeid: nodeBftMap.keySet()) {
        	if (nodeswithupdatedbftonbroadcastdomain.contains(nodeid))
        		continue;
            LOG.info("run: node [{}]: bridge [{}] with updated bft. Not even more on broadcast domain {}: clear topology.", 
            		getNodeId(),
                     nodeid,                                  
                     m_domain.getBridgeNodesOnDomain());
            m_domain.clearTopologyForBridge(nodeid);
        	m_domain.removeBridge(nodeid);
        }
        m_linkd.getQueryManager().cleanBroadcastDomains();
        
        m_domain.setBridgeElements(m_linkd.getQueryManager().getBridgeElements(m_domain.getBridgeNodesOnDomain()));
        

        if (m_notYetParsedBFTMap.isEmpty()) {
            LOG.info("run: node: [{}], broadcast domain has no topology updates. No more action is needed.", getNodeId());
            return;
        }
        
        calculate();
        LOG.info("run: node: [{}], saving Topology.", getNodeId());
        m_linkd.getQueryManager().store(m_domain,now);
        LOG.info("run: node: [{}], saved Topology.", getNodeId());
        
        for (Integer curNode: nodeswithupdatedbftonbroadcastdomain)
            sendCompletedEvent(curNode);
        m_domain.releaseLock(this);
        LOG.info("run: node: {}, releaseLock broadcast domain: {}.", getNodeId(),
                 m_domain.getBridgeNodesOnDomain());
    }
            
    @Override
    protected void runCollection() {
    }

    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

    protected  void calculate() {
        LOG.debug("calculate: node[{}]: Print Topology {}", 
        		getNodeId(),
        		m_domain.printTopology());
        LOG.info("calculate: node: [{}]: start: broadcast domain {} topology calculation.", 
        		getNodeId(),
        		m_domain.getBridgeNodesOnDomain());

        Bridge electedRoot = m_domain.electRootBridge();
        
        if (electedRoot == null && m_domain.hasRootBridge()) {
            LOG.debug("calculate: node [{}]: electRootBridge: mantaining old root bridge: {}", 
                    getNodeId(),
                    m_domain.getRootBridgeId());
            electedRoot = m_domain.getRootBridge();        	
        } else if (electedRoot == null) {
	        // no spanning tree root?
	        // why I'm here?
	        // not root bridge defined (this mean no calculation yet done...
	        // so checking the best into not parsed
	        int size=0;
	        
	        Bridge rootBridge = null;
	        for (Bridge bridge:  m_notYetParsedBFTMap.keySet()) {
	            LOG.debug("calculate: node [{}]: bridge [{}]: max bft size \"{}\" in topology",
	                    getNodeId(),
	                    bridge.getId(), 
	                    m_notYetParsedBFTMap.get(bridge).size());
	            if (size < m_notYetParsedBFTMap.get(bridge).size()) {
	                rootBridge = bridge;
	                size = m_notYetParsedBFTMap.get(bridge).size();
	            }
	        }
	        if (rootBridge != null ) {
	            LOG.debug("calculate: node [{}]: bridge [{}]: elected root with max bft size \"{}\" in topology",
	                    getNodeId(),
	                    rootBridge.getId(), 
	                    size);
	            electedRoot = rootBridge;
	        }
        } 
        
        if (electedRoot == null ) {
        	electedRoot = m_domain.getBridges().iterator().next();
    	   LOG.debug("calculate: node [{}]: electRootBridge: first root bridge: {}", 
                       getNodeId(),
                       electedRoot.getId());
        }

        if (electedRoot == null || electedRoot.getId() == null) {
            LOG.error("calculate: [{}]: electedRootBridge should not be null",
                    getNodeId()
            		);
            return;
        }

        List<BridgeMacLink> rootBft = m_notYetParsedBFTMap.remove(electedRoot);

        if (m_domain.hasRootBridge() && m_domain.getRootBridge().getId() == electedRoot.getId() && rootBft == null) {
            LOG.debug("calculate: node [{}]: elected root bridge: [{}], old root bridge. no updated bft",
                    getNodeId(), 
            		electedRoot.getId());
            rootBft = m_domain.calculateRootBFT();
        } else if (m_domain.hasRootBridge() && m_domain.getRootBridge().getId() == electedRoot.getId() && rootBft != null ) {
            LOG.debug("calculate: node [{}]: elected root bridge: [{}], old root bridge. updated bft",
                    getNodeId(), 
                     electedRoot.getId());
            if (m_domain.getTopology().isEmpty()) {
                LOG.debug("calculate: node [{}]: elected root bridge: [{}], clean topology found. Adding root shared segments",
                        getNodeId(), 
                         electedRoot.getId());
                loadFirstLevelSharedSegment(rootBft);
            }
         } else if ( rootBft != null ) {
            LOG.debug("calculate: node [{}]: elected root bridge: [{}], new root. updated bft",
                    getNodeId(), 
                     electedRoot.getId());
           if (m_domain.getTopology().isEmpty()) {
               LOG.debug("calculate: node [{}]: new elected root bridge: [{}], is the first bridge in topology. Adding root shared segments",
                       getNodeId(), 
                        electedRoot.getId());
                loadFirstLevelSharedSegment(rootBft);
                electedRoot.setRootBridge(true);
                electedRoot.setRootPort(null);
           } else {
                m_domain.clearTopologyForBridge(electedRoot.getId());
                calculate(m_domain.getRootBridge(), m_domain.calculateRootBFT(),
                                electedRoot, rootBft);
                m_domain.hierarchySetUp(electedRoot);
           }
        } else {
           LOG.debug("calculate: node [{}]: elected root bridge: [{}], is new root bridge with old bft",
                    getNodeId(), 
                     electedRoot.getId());
           m_domain.hierarchySetUp(electedRoot);
           rootBft = m_domain.calculateRootBFT();
        }

        LOG.debug("calculate: node[{}]: Root Bridge [{}] elected.", 
        		getNodeId(),
        		electedRoot.getId(),
        		m_domain.printTopology());

        for (Bridge xBridge: m_notYetParsedBFTMap.keySet()) {
            m_domain.clearTopologyForBridge(xBridge.getId());
            LOG.debug("calculate: node[{}]: Removed bridge: [{}].", 
            		getNodeId(),
            		xBridge.getId());
       }

        LOG.debug("calculate: node[{}]: Print Topology {}", 
        		getNodeId(),
        		m_domain.printTopology());

        Set<Bridge> nodetobeparsed = new HashSet<Bridge>(m_notYetParsedBFTMap.keySet());
        for (Bridge xBridge: nodetobeparsed) {
            LOG.info("calculate: node: [{}]: bridge [{}]: calculate topology: start.",
            		getNodeId(),
            		xBridge.getId());
            calculate(electedRoot, rootBft, xBridge, new ArrayList<BridgeMacLink>(m_notYetParsedBFTMap.remove(xBridge)));
            LOG.info("calculate: node: [{}]: bridge [{}]: calculate topology: stop.",
            		getNodeId(),
            		xBridge.getId());
        }
        LOG.info("calculate: node: [{}]: stop: broadcast domain {} topology calculated.", 
        		getNodeId(),
        		m_domain.getBridgeNodesOnDomain());
        LOG.debug("calculate: node[{}]: Print Topology {}", 
        		getNodeId(),
        		m_domain.printTopology());


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
            LOG.debug("calculate: node [{}]: add shared segment[designated bridge:[{}],"
            		+ "designated port:{}, macs: {}]",
            		 getNodeId(),
                     rootleaf.getDesignatedBridge(),
                     rootleaf.getDesignatedPort(),
                     rootleaf.getMacsOnSegment());
            m_domain.add(rootleaf);
        }      
   }
    
    private void calculate(Bridge root,  List<BridgeMacLink> rootbft, Bridge xBridge, List<BridgeMacLink> xbft) {
        //FIXME        checkStp(root, xBridge);
        BridgeTopologyHelper rx = new BridgeTopologyHelper(root, rootbft, xBridge,xbft);
        Integer rxDesignatedPort = rx.getFirstBridgeConnectionPort();
        if (rxDesignatedPort == null) {
            LOG.warn("calculate: node [{}]: cannot found simple connection for bridges: [{},{}]", 
            		getNodeId(),
            		root.getId(), 
            		xBridge.getId());
            m_domain.clearTopology();
            return;
        }
        Integer xrDesignatedPort = rx.getSecondBridgeConnectionPort();
        if (xrDesignatedPort == null) {
             LOG.warn("calculate: node [{}]: cannot found simple connectionfor bridges: [{},{}]",
             		getNodeId(),
             		xBridge.getId(), 
             		root.getId());
             m_domain.clearTopology();
             return;
        }
        LOG.debug("calculate: node [{}]: level 1: bridge: [{}]. setting root port {} ",
        		getNodeId(),
        		xBridge.getId(),
        		xrDesignatedPort);
        xBridge.setRootPort(xrDesignatedPort);
        xBridge.setRootBridge(false);
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = m_domain.getSharedSegment(root.getId(),rxDesignatedPort);
        if (topSegment == null) {
            LOG.warn("calculate: node [{}]: nodeid [{}], port {}. top segment not found.",
            		getNodeId(),
            		m_domain.getRootBridgeId(),
            		rxDesignatedPort);
            m_domain.clearTopology();
            return;
        }
        if (!findBridgesTopo(rx,topSegment, xBridge, xbft,0))
            m_domain.clearTopology();
    }

    // here we assume that rbridge exists in topology
    // while xBridge is to be added
    private boolean findBridgesTopo(BridgeTopologyHelper rx,SharedSegment topSegment, Bridge xBridge, List<BridgeMacLink> xBFT, int level) {
        level++;
        LOG.debug("calculate: node [{}]: level {}: bridge: [{}], topo top segment found: [ids {}, designated bridge [{}, port {}], macs {}",
        		getNodeId(),
        		level,
        		xBridge.getId(),
        		topSegment.getBridgeIdsOnSegment(),
                topSegment.getDesignatedBridge(),
        		topSegment.getDesignatedPort(),
        		topSegment.getMacsOnSegment());
        if (level == 30) {
            LOG.warn("calculate: node [{}]: level {}: bridge: [{}], too many iteration on topology exiting.....",
            		getNodeId(),
            		level,
            		xBridge.getId());
            return false;
        }

        Set<Integer> portsAdded=new HashSet<Integer>();
        Set<String> throughSets=new HashSet<String>();
        Map<Integer,List<BridgeMacLink>> bftSets=new HashMap<Integer,List<BridgeMacLink>>();
        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            bftSets.put(yBridge.getId(), m_domain.calculateBFT(yBridge));
        }
        
        for (Bridge yBridge: m_domain.getBridgeOnSharedSegment(topSegment)) {
            Integer yBridgeId = yBridge.getId();
            // X is a leaf of top segment: of course
            if (yBridgeId.intValue() == topSegment.getDesignatedBridge().intValue()) {
                continue;
            } 
            LOG.debug("calculate: node [{}]: level {}: bridge: [{}]: Bridge [{}] on top segment. Searching simple connection",
            		getNodeId(),
                      level,
                      xBridge.getId(), 
                      yBridge.getId());
            Integer yrDesignatedPort = yBridge.getRootPort();
            LOG.debug("calculate: node [{}]: level {}: bridge: {}, Bridge: [{}, designated port: {}]",
            		getNodeId(),
                     level,xBridge.getId(),yBridgeId,yrDesignatedPort);
            BridgeTopologyHelper   yx = new BridgeTopologyHelper(yBridge,bftSets.get(yBridgeId) ,xBridge, xBFT);
            Integer  xyDesignatedPort = yx.getSecondBridgeConnectionPort();
            Integer  yxDesignatedPort = yx.getFirstBridgeConnectionPort();
            // X is a leaf of Y then iterate
            if (xyDesignatedPort == rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.debug("calculate: node [{}]: level {}: Bridge: [{}] is a leaf of Bridge: [{}], going one level down",
                		getNodeId(),
                		level,xBridge.getId(),yBridge.getId());
                if (!findBridgesTopo(yx,m_domain.getSharedSegment(yBridgeId, yxDesignatedPort), xBridge, xBFT,level))
                	return false;
                return true;
            }
            // Y is a leaf of X then remove Y from topSegment
            if (yxDesignatedPort == yrDesignatedPort && xyDesignatedPort != rx.getSecondBridgeConnectionPort()) {
                //create a SharedSegment with root port
                LOG.debug("calculate: node [{}]: level {}: bridge: [{},designated port [{}]]: is 'upper' Bridge: [{}]. Adding shared segment.", 
                		getNodeId(), 
                		level,xBridge.getId(),yBridge.getId(),xyDesignatedPort);
                SharedSegment leafSegment = new SharedSegment(m_domain,yx.getSimpleConnection().getDlink(),yx.getSimpleConnection().getMacs());
                leafSegment.setDesignatedBridge(xBridge.getId());
                m_domain.add(leafSegment);
                portsAdded.add(xyDesignatedPort);
                
                LOG.debug("calculate: node [{}]: level {}: bridge [{}]. Removing Bridge [{}]: through set {} from top segment.", 
                		getNodeId(), 
                         level,
                         xBridge.getId(),
                         yBridge.getId(), 
                         yx.getFirstBridgeTroughSet());
                topSegment.removeMacs(yx.getFirstBridgeTroughSet());
                LOG.debug("calculate: node [{}]: level {}: bridge [{}]. Remove bridge [{}] from top segment.", 
                		getNodeId(), 
                         level,
                         xBridge.getId(),
                         yBridge.getId());
                topSegment.removeBridge(yBridgeId);
            }            
            // this is a clear violation  of the topology tree rule
            if (xyDesignatedPort != rx.getSecondBridgeConnectionPort() && yxDesignatedPort != yrDesignatedPort) {
                LOG.warn("calculate: node [{}]: level {}: bridge [{}]. Topology mismatch. Clearing...topology",
                		getNodeId(), 
                		level,
                		xBridge.getId());
                return false;
            }            
            throughSets.addAll(yx.getFirstBridgeTroughSet());
        }
        // if we are here is because X is NOT a leaf of any bridge found
        // on topSegment so X is connected to top Segment by it's root 
        // port or rx is a direct connection
        LOG.debug("calculate: node [{}]: level {}: bridge: [{}]. removing through set {} from top segment", 
        		getNodeId(), 
                 level, xBridge.getId(), rx.getSecondBridgeTroughSet());
        topSegment.removeMacs(rx.getSecondBridgeTroughSet());
        LOG.debug("calculate: node [{}]: level {}: bridge: [{}]. assign macs {} to top segment", 
        		getNodeId(), 
                 level,xBridge.getId(),rx.getSimpleConnection().getMacs());
        topSegment.assign(rx.getSimpleConnection().getMacs(),rx.getSimpleConnection().getDlink());
        LOG.debug("calculate: node [{}]: level {}: bridge: [{}]. removing through sets {} from top segment", 
        		getNodeId(), 
                 level,xBridge.getId(),throughSets);
        topSegment.removeMacs(throughSets);
        LOG.debug("calculate: node [{}]: level {}: bridge: [{}]. resulting top segment: [ids {}, designated bridge [{}, port: {}], mac : {}]",
        		getNodeId(), 
                 level, xBridge.getId(),
                 topSegment.getBridgeIdsOnSegment(), topSegment.getDesignatedBridge(),
                 topSegment.getDesignatedPort(), topSegment.getMacsOnSegment());
        for (Integer xbridgePort : rx.getSecondBridgeTroughSetBft().keySet()) {
            if (portsAdded.contains(xbridgePort))
                continue;
            SharedSegment xleafSegment = new SharedSegment(m_domain,rx.getSecondBridgeTroughSetBft().get(xbridgePort));
            xleafSegment.setDesignatedBridge(xBridge.getId());  
            m_domain.add(xleafSegment);
            LOG.debug("calculate: node [{}]: level {}: bridge: [{}]. Add shared segment. "
            		+ "[ designated bridge:[{}], "
            		+ "port:{}, "
            		+ "mac: {}]",
            		getNodeId(), 
                     level,
                     xBridge.getId(),
                     xleafSegment.getDesignatedBridge(),
                     xleafSegment.getDesignatedPort(),
                     xleafSegment.getMacsOnSegment());
        }
        return true;
    }

}

