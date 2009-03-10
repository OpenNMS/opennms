package org.opennms.web.svclayer.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.InventoryNode;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RWSResourceList;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.RancidNodeAuthentication;
import org.opennms.web.inventory.*;
import org.springframework.beans.factory.InitializingBean;

public class InventoryService implements InitializingBean {
    RWSConfig m_rwsConfig;
    NodeDao m_nodeDao;
    
    
    public void afterPropertiesSet() throws Exception {
        //FIXME this should be done by spring
            RWSClientApi.init();
    }
    
    
    public InventoryService() {
        super();
    }
    
    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }
    public void setRwsConfig(RWSConfig rwsConfig) {
        m_rwsConfig = rwsConfig;
    }
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public Map<String, Object> getRancidNode(int nodeid, boolean adminRole) throws RancidApiException{
        
        log().debug("getRancidNode start");
        
        OnmsNode node = m_nodeDao.get(nodeid);
        String rancidName = node.getLabel();

        log().debug("getRancidNode: " + rancidName);


        Map<String, Object> nodeModel = new TreeMap<String, Object>();
        nodeModel.put("id", rancidName);
        nodeModel.put("db_id", nodeid);
        nodeModel.put("status_general", node.getType());
        
        List<RancidNodeWrapper> ranlist = new ArrayList<RancidNodeWrapper>();
        
        // Group list 
        ConnectionProperties cp = new ConnectionProperties(m_rwsConfig.getBaseUrl().getServer_url(),m_rwsConfig.getBaseUrl().getDirectory(),m_rwsConfig.getBaseUrl().getTimeout());
        RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(cp);
        
        List<String> grouplist = groups.getResource();
        Iterator<String> iter1 = grouplist.iterator();
        
      
        String groupname;
        boolean first = true;
        while (iter1.hasNext()){
            groupname = iter1.next();
            log().debug("getRancidNode " + rancidName + " group " + groupname);        
            
            try {
                if (first){
                    RancidNode rn = RWSClientApi.getRWSRancidNodeTLO(cp, groupname, rancidName);
                    nodeModel.put("devicename", rn.getDeviceName());
                    nodeModel.put("status", rn.getState());
                    nodeModel.put("devicetype", rn.getDeviceType());
                    nodeModel.put("comment", rn.getComment());
                    nodeModel.put("groupname", groupname);
                    first = false;
                } 
                RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(cp ,groupname, rancidName);
                String vs = rn.getHeadRevision();
                InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);

                RancidNodeWrapper rnw = new RancidNodeWrapper(rn.getDeviceName(), groupname, rn.getDeviceType(), rn.getComment(), rn.getHeadRevision(),
                  rn.getTotalRevisions(), in.getCreationDate(), rn.getRootConfigurationUrl());

                ranlist.add(rnw); 
                
            }
            catch (RancidApiException e){
                log().debug("No inventory information associated to " + rancidName);
            }
        }
        
        //Groups invariant            
        nodeModel.put("grouptable", ranlist);
        nodeModel.put("url", cp.getUrl());
        
        //CLOGIN
        if (adminRole) {

            RancidNodeAuthentication rn5 = RWSClientApi.getRWSAuthNode(cp,rancidName);
            nodeModel.put("isadmin", "true");
            nodeModel.put("cloginuser", rn5.getUser());
            nodeModel.put("cloginpassword", rn5.getPassword());
            nodeModel.put("cloginconnmethod", rn5.getConnectionMethodString());
            nodeModel.put("cloginenablepass", rn5.getEnablePass());
            String autoen = "0";
            if (rn5.isAutoEnable()){
                autoen = "1";
            }
            nodeModel.put("cloginautoenable", autoen);
        }
        
        return nodeModel;
    

    }

    private static Category log() {
        return Logger.getLogger("Rancid");
    }

}
