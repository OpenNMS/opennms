package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BridgeTopology;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {

    volatile Map<Integer,Map<Integer,Set<String>>> m_bftMap = new HashMap<Integer, Map<Integer,Set<String>>>();

    volatile Map<Integer,Map<Integer,Integer>> m_nodebridgeportifindex = new HashMap<Integer, Map<Integer,Integer>>();
    
    volatile Map<Integer, Map<Integer,Integer>> m_nodebridgeportvlan =new HashMap<Integer, Map<Integer,Integer>>();

    @Override
    public void store(BridgeElement bridge) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(BridgeMacLink maclink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(BridgeStpLink stpLink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(BridgeBridgeLink bridgeLink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void walked(int nodeid, Date now) {
        // TODO Auto-generated method stub

    }

    @Override
    public BridgeTopology getTopology(int nodeid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean topologyChanged(int nodeid) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Integer> getUpdatedNodes(int nodeid) {
        // TODO Auto-generated method stub
        return new ArrayList<Integer>();
    }

    @Override
    public Date getUpdateTime(int nodeid) {
        // TODO Auto-generated method stub
        return null;
    }

    
    /*     FIXME move to BridgeTopologyDao
    @Override
    public void storeBridgeToIfIndexMap(int nodeid, Map<Integer,Integer> bridgeportifindex) {
        m_nodebridgeportifindex.put(nodeid, bridgeportifindex);
    }

    @Override
    public void storeBridgetoVlanMap(int nodeId, Set<Integer> bridgeports, Integer vlanid) {
        Map<Integer,Integer> portvlan = new HashMap<Integer, Integer>();
        if (m_nodebridgeportvlan.containsKey(nodeId)) 
            portvlan = m_nodebridgeportvlan.get(nodeId);
        for (Integer bridgeport: bridgeports) {
            if (portvlan.containsKey(bridgeport)) {
                if (portvlan.get(bridgeport) == vlanid) {
                    continue;
                }   else { 
                    // port is a trunk
                    portvlan.remove(bridgeport);
                    continue;
                }
            }
            portvlan.put(bridgeport, vlanid);
        }
        m_nodebridgeportvlan.put(nodeId, portvlan);
    }
*/
    
    /*
    protected void saveBridgeTopology(final BridgeTopologyLink bridgelink) {
            if (bridgelink == null)
                    return;

            OnmsNode node = m_nodeDao.get(bridgelink.getBridgeTopologyPort().getNodeid());
            if (node == null)
                    return;
            OnmsNode designatenode = null;
            if (bridgelink.getDesignateBridgePort() != null) {
                    designatenode = m_nodeDao.get(bridgelink.getDesignateBridgePort().getNodeid());
            }
            
            if (bridgelink.getMacs().isEmpty() && designatenode != null) {
                    BridgeBridgeLink link = new BridgeBridgeLink();
                    link.setNode(node);
                    link.setBridgePort(bridgelink.getBridgeTopologyPort().getBridgePort());
                    if (m_nodebridgeportifindex.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                        link.setBridgePortIfIndex(m_nodebridgeportifindex.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                    if (m_nodebridgeportvlan.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                        link.setVlan(m_nodebridgeportvlan.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                    link.setDesignatedNode(designatenode);
                    link.setDesignatedPort(bridgelink.getDesignateBridgePort().getBridgePort());
                    if (m_nodebridgeportifindex.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                        link.setDesignatedPortIfIndex(m_nodebridgeportifindex.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                    if (m_nodebridgeportvlan.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                        link.setDesignatedVlan(m_nodebridgeportvlan.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                    saveBridgeBridgeLink(link);
                    return;
            } 

            for (String mac: bridgelink.getMacs()) {
                    BridgeMacLink maclink1 = new BridgeMacLink();
                    maclink1.setNode(node);
                    maclink1.setBridgePort(bridgelink.getBridgeTopologyPort().getBridgePort());
                    if (m_nodebridgeportifindex.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                        maclink1.setBridgePortIfIndex(m_nodebridgeportifindex.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                    if (m_nodebridgeportvlan.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                        maclink1.setVlan(m_nodebridgeportvlan.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                    maclink1.setMacAddress(mac);
                    saveBridgeMacLink(maclink1);
                    if (designatenode == null)
                            continue;
                    BridgeMacLink maclink2 = new BridgeMacLink();
                    maclink2.setNode(designatenode);
                    maclink2.setBridgePort(bridgelink.getDesignateBridgePort().getBridgePort());
                    if (m_nodebridgeportifindex.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                        maclink2.setBridgePortIfIndex(m_nodebridgeportifindex.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                    if (m_nodebridgeportvlan.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                        maclink2.setVlan(m_nodebridgeportvlan.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                    maclink2.setMacAddress(mac);
                    saveBridgeMacLink(maclink2);
            }
    }
    */


}
