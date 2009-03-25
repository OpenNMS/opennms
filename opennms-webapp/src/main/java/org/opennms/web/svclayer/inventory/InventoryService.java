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
import org.opennms.web.element.ElementUtil;
import org.opennms.web.inventory.*;
import org.springframework.beans.factory.InitializingBean;

public class InventoryService implements InitializingBean {
    RWSConfig m_rwsConfig;
    NodeDao m_nodeDao;
    ConnectionProperties m_cp;
    
    
    public void afterPropertiesSet() throws Exception {
        //FIXME this should be done by spring
            RWSClientApi.init();
            m_cp = m_rwsConfig.getBase();
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
    
    public boolean checkRWSAlive(){
        try {
            RWSResourceList rl = RWSClientApi.getRWSResourceServicesList(m_cp);
            if (rl!= null){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
    
    public boolean checkRancidNode(String deviceName){
        
        log().debug("checkRancidNode start " + deviceName);
                
        // Group list 
        try {
            RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(m_cp);
            List<String> grouplist = groups.getResource();
            Iterator<String> iter1 = grouplist.iterator();
           
            if (iter1.hasNext()){
                String groupname = iter1.next();
                log().debug("checkRancidNode " + deviceName + " group " + groupname);        
                
                try {
                    RancidNode rn = RWSClientApi.getRWSRancidNodeTLO(m_cp, groupname, deviceName);
                    if (rn!=null){
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                 catch (RancidApiException e){
                     log().debug("No inventory information associated to " + deviceName);
                     return false;
                }
            }

        }catch (Exception e){
            return false;
        }
        return true;
    }
    
    /*
     * getRancidNode will filter any exception, the page will show an empty table
     * in case of node not in DB or device name not in RWS 
     */
    public Map<String, Object> getRancidNode(int nodeid, boolean adminRole) {
        
        log().debug("getRancidNode start");
        Map<String, Object> nodeModel = new TreeMap<String, Object>();

        
        OnmsNode node = m_nodeDao.get(nodeid);
        String rancidName = node.getLabel();
        
        String foreignSource = node.getForeignSource();
        if (foreignSource != null ) {
            nodeModel.put("permitModifyClogin", false);
            nodeModel.put("foreignSource", foreignSource);
        } else {
            nodeModel.put("permitModifyClogin", true);            
        }

        log().debug("getRancidNode: " + rancidName);


        nodeModel.put("id", rancidName);
        nodeModel.put("db_id", nodeid);
        nodeModel.put("status_general", ElementUtil.getNodeStatusString(node.getType().charAt(0)));
        
        List<RancidNodeWrapper> ranlist = new ArrayList<RancidNodeWrapper>();
        
        // Group list 
        RWSResourceList groups;
        try {
            groups = RWSClientApi.getRWSResourceGroupsList(m_cp);
        } catch (RancidApiException e1) {
            log().error(e1.getLocalizedMessage());
            return nodeModel;
        }
            
        List<String> grouplist = groups.getResource();
        Iterator<String> iter1 = grouplist.iterator();
        
      
        String groupname;
        boolean first = true;
        while (iter1.hasNext()){
            groupname = iter1.next();
            log().debug("getRancidNode " + rancidName + " group " + groupname);        
            
            try {
                if (first){
                    RancidNode rn = RWSClientApi.getRWSRancidNodeTLO(m_cp, groupname, rancidName);
                    nodeModel.put("devicename", rn.getDeviceName());
                    nodeModel.put("status", rn.getState());
                    nodeModel.put("devicetype", rn.getDeviceType());
                    nodeModel.put("comment", rn.getComment());
                    nodeModel.put("groupname", groupname);
                    first = false;
                } 
                RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(m_cp ,groupname, rancidName);
                String vs = rn.getHeadRevision();
                InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);

                RancidNodeWrapper rnw = new RancidNodeWrapper(rn.getDeviceName(), groupname, rn.getDeviceType(), rn.getComment(), rn.getHeadRevision(),
                  rn.getTotalRevisions(), in.getCreationDate(), rn.getRootConfigurationUrl());

                ranlist.add(rnw); 
                
            }
            catch (RancidApiException e){
                log().debug("No device found in router.db for:" + rancidName + "on Group: " + groupname);
            }
        }
            
        //Groups invariant            
        nodeModel.put("grouptable", ranlist);
        nodeModel.put("url", m_cp.getUrl());
        
        //CLOGIN
        if (adminRole) {
            log().debug("getRancidNode: getting clogin info for: " + rancidName);        
            RancidNodeAuthentication rn5;
            try {
                rn5 = RWSClientApi.getRWSAuthNode(m_cp,rancidName);
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
            }catch (RancidApiException e){
                log().error("getRancidNode: clogin get failed with reason: " + e.getLocalizedMessage());
            }
        }
        return nodeModel;
    }

    public boolean updateStatus(String groupName, String deviceName){
        
      log().debug("InventoryService updateStatus " + groupName+"/"+deviceName);

      try {  
          RancidNode rn = RWSClientApi.getRWSRancidNodeTLO(m_cp, groupName, deviceName);
          if (rn.isStateUp()){
              log().debug("InventoryService updateStatus :down");
              rn.setStateUp(false);
          }else {
              log().debug("InventoryService updateStatus :up");
              rn.setStateUp(true);
          }
          RWSClientApi.updateRWSRancidNode(m_cp, rn);
      }
      catch (Exception e){
          log().debug("updateStatus has given exception on node "  + groupName+"/"+deviceName + " "+ e.getMessage() );
          return false;
      }
      return true;
    }
    
    public boolean updateClogin(String deviceName, String groupName, String userID, String pass, String enPass, String loginM, String autoE){
        log().debug("InventoryService updateClogin for following changes"+
                    "userID ["+ userID +"] "+
                    "pass [" + pass +"] "+
                    "enpass [" + enPass+"] "+
                    "loginM [" + loginM+"] "+
                    "autoE [" + autoE+"] "+
                    "groupName (ignored) [" + groupName+"] "+
                    "deviceName [" + deviceName + "] "); 
        try {
          RancidNodeAuthentication rna = RWSClientApi.getRWSAuthNode(m_cp, deviceName);
          rna.setUser(userID);
          rna.setPassword(pass);
          rna.setConnectionMethod(loginM);
          rna.setEnablePass(enPass);
          boolean autoeb = false;
          if (autoE.compareTo("1")==0) {
              autoeb = true;
          }
          rna.setAutoEnable(autoeb);
          RWSClientApi.createOrUpdateRWSAuthNode(m_cp,rna);
          log().debug("InventoryService ModelAndView updateClogin changes submitted");
        }
        catch (Exception e){
            log().debug("updateClogin has given exception on node "  + deviceName + " "+ e.getMessage() );
            return false;
        }
        return true;


    }


    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }

}
