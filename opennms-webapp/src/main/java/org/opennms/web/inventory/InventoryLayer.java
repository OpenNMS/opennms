//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

package org.opennms.web.inventory;

import org.opennms.rancid.*;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.netmgt.config.RWSConfigFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Category;
import javax.servlet.http.HttpServletRequest;


import java.util.*;


public class InventoryLayer {
    
    //static String _URL = "http://www.rionero.com:9080";
             
    private static RWSConfig rwsCfgFactory;
    
    private static ConnectionProperties m_cp;
   
    
    static public void init(){
        
        try {
            log().debug("Setting Up RWS client");
            RWSClientApi.init();

            //nel config
            RWSConfigFactory.init();
            rwsCfgFactory = RWSConfigFactory.getInstance();
            
           
            m_cp = rwsCfgFactory.getBase();
            
            log().debug("RWS Url " + m_cp.getUrl());            
        }
        catch (Exception e) {
            
        }
    }
    
 
        
//    static public boolean hasRancidInfo(String nodeLabe){
//        return true;        
//    }
//    
    static public Map<String, Object> getInventoryElement(String rancidName) throws RancidApiException{
        
        try {
        
            String group = "laboratorio";
            
            
            RancidNode rn = RWSClientApi.getRWSRancidNode(m_cp, group, rancidName);
    
            
    
            InventoryNode in = new InventoryNode(rn);
            InventoryElement ie = new InventoryElement(in);
            
            ie.setElementName("Router1");
            ie.setVendor("Cisco");
            ie.setSysOid("1.1.1.1.1.1.1");
            ie.setModelType("x123");
            ie.setSerialNumber("17266577871");
            ie.setProductPartNumber("21211212");
            ie.setHardwareVersion("1.1");
            ie.setRamSize(8);
            ie.setNwRamSize(4);
            ie.setElementId(4);
            
            Map<String, Object> nodeModel = new TreeMap<String, Object>();
    //      Integer ii = Integer(ie.getElementId(4));
    //      nodeModel.put("id", ii.toString());
    
            nodeModel.put("status", ie.getParent().getParent().getState());
            nodeModel.put("name", ie.getElementName());
            nodeModel.put("elementid", ie.getElementId());
            nodeModel.put("vendor", ie.getVendor());
            nodeModel.put("sysoid", ie.getSysOid());
            nodeModel.put("modeltype", ie.getModelType());
            nodeModel.put("serialnumber", ie.getSerialNumber());
            nodeModel.put("productpartnumber", ie.getProductPartNumber());
            nodeModel.put("hardwareversion", ie.getHardwareVersion());
            nodeModel.put("ramsize", ie.getRamSize());        
            nodeModel.put("nwramsize", ie.getNwRamSize());
            nodeModel.put("group", group);;
            nodeModel.put("url", m_cp.getUrl());

    
            
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
   
   static public Map<String, Object> getRancidNode(String rancidName, HttpServletRequest request) throws RancidApiException{
        
            
            log().debug("getRancidNode " + rancidName);
            Map<String, Object> nodeModel = new TreeMap<String, Object>();
            
            List<RancidNodeWrapper> ranlist = new ArrayList<RancidNodeWrapper>();
            
            // Group list            
            RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(m_cp);
            
            List<String> grouplist = groups.getResource();
            Iterator<String> iter1 = grouplist.iterator();
            
          
            String groupname;
            boolean first = true;
            while (iter1.hasNext()){
                groupname = iter1.next();
                log().debug("getRancidNode " + rancidName + " group " + groupname);

                try {
                    RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(m_cp ,groupname, rancidName);
                    String vs = rn.getHeadRevision();
                    InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);

                    RancidNodeWrapper rnw = new RancidNodeWrapper(rn.getDeviceName(), groupname, rn.getDeviceType(), rn.getComment(), rn.getHeadRevision(),
                      rn.getTotalRevisions(), in.getCreationDate(), rn.getRootConfigurationUrl());
                    if (first) {
                        nodeModel.put("devicename", rn.getDeviceName());
                        nodeModel.put("status", rn.getState());
                        nodeModel.put("devicetype", rn.getDeviceType());
                        nodeModel.put("comment", rn.getComment());
                        first = false;
                    }
                    ranlist.add(rnw); 

                }
                catch (RancidApiException e){
                    log().debug("Exception in getRancidNode getRWSRancidNodeInventory ");
                }
            }
            
            //Groups invariant            
            nodeModel.put("grouptable", ranlist);
            nodeModel.put("url", m_cp.getUrl());
            
            //CLOGIN
            if (request.isUserInRole(Authentication.ADMIN_ROLE)) {

                RancidNodeAuthentication rn5 = RWSClientApi.getRWSAuthNode(m_cp,rancidName);
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
    static public Map<String, Object> getRancidNodeAdmin(String rancidName, HttpServletRequest request) throws RancidApiException{
      
       
       log().debug("getRancidNodeAdmin start");

       log().debug("getRancidNode: " + rancidName);

       //OnmsNode node = m_nodeDao.get(nodeid);

       Map<String, Object> nodeModel = new TreeMap<String, Object>();
       nodeModel.put("id", rancidName);
       //nodeModel.put("status_general", node.getType());
       
       List<RancidNodeWrapper> ranlist = new ArrayList<RancidNodeWrapper>();
       
       // Group list 
       RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(m_cp);
       
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
                   nodeModel.put("group", groupname);

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
               log().debug("Exception in getRancidNode getRWSRancidNodeInventory ");
           }
       }
       
       //Groups invariant            
       nodeModel.put("grouptable", ranlist);
       nodeModel.put("url", m_cp.getUrl());
       
       //CLOGIN
       if (request.isUserInRole(Authentication.ADMIN_ROLE)) {

           RancidNodeAuthentication rn5 = RWSClientApi.getRWSAuthNode(m_cp,rancidName);
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

   static public Map<String, Object> getRancidNodeList(String rancidName, String groupname) throws RancidApiException{
       
       try {
           
           log().debug("getRancidNodeList " + rancidName + " " + groupname);

           
           Map<String, Object> nodeModel = new TreeMap<String, Object>();
           
           List<InventoryWrapper> ranlist = new ArrayList<InventoryWrapper>();
           
           RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(m_cp, groupname, rancidName);

           nodeModel.put("devicename", rn.getDeviceName());
           
           //*********
           // version
         
           RWSResourceList versionList;
           
           versionList = RWSClientApi.getRWSResourceConfigList(m_cp, groupname, rancidName);
           
           List<String> versionListStr= versionList.getResource();
           
           Iterator<String> iter1 = versionListStr.iterator();
           
           String vs;
           
           while (iter1.hasNext()) {
               vs = iter1.next();
               InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);
               InventoryWrapper inwr = new InventoryWrapper(in.getVersionId(), in.getCreationDate(), groupname, in.getConfigurationUrl());
               ranlist.add(inwr);
           }
           
           nodeModel.put("grouptable", ranlist);
           nodeModel.put("url", m_cp.getUrl());
           
           
           return nodeModel;
       }
       catch (RancidApiException e) {
           throw e;
       }
   }
static public Map<String, Object> getRancidNodeList(String rancidName) throws RancidApiException{
       
       try {
           
           log().debug("getRancidNodeList " + rancidName);

           
           Map<String, Object> nodeModel = new TreeMap<String, Object>();
           
           RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(m_cp);
           
           List<InventoryWrapper> ranlist = new ArrayList<InventoryWrapper>();
           
           List<String> grouplist = groups.getResource();
           Iterator<String> iter2 = grouplist.iterator();
           
           boolean first = true;
           String groupname;
           while (iter2.hasNext()) {
               groupname = iter2.next();
           
               RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(m_cp, groupname, rancidName);
    
               if (first){
                   nodeModel.put("devicename", rn.getDeviceName());
                   first = false;
               }
               
               //*********
               // version
             
               RWSResourceList versionList;
               
               versionList = RWSClientApi.getRWSResourceConfigList(m_cp, groupname, rancidName);
               
               List<String> versionListStr= versionList.getResource();
               
               Iterator<String> iter1 = versionListStr.iterator();
               
               String vs;
               
               while (iter1.hasNext()) {
                   vs = iter1.next();
                   InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);
                   InventoryWrapper inwr = new InventoryWrapper(in.getVersionId(), in.getCreationDate(), groupname, in.getConfigurationUrl());
                   ranlist.add(inwr);
               }
           }
           
           nodeModel.put("grouptable", ranlist);
           nodeModel.put("url", m_cp.getUrl());
           
           
           return nodeModel;
       }
       catch (RancidApiException e) {
           throw e;
       }
   }
   
   static public Map<String, Object> getInventoryNode(String rancidName, String group, String version) throws RancidApiException{
       
       try {
           
           log().debug("getInventoryNode " + rancidName);

    
           RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(m_cp, group, rancidName);
       
           InventoryNode in = (InventoryNode)rn.getNodeVersions().get(version);
           
           //InventoryWrapper tmpw = new InventoryWrapper(version, in.getExpirationDate(), group, in.getConfigurationUrl());
                       
           Map<String, Object> nodeModel = new TreeMap<String, Object>();
   
           nodeModel.put("devicename", rancidName);
           nodeModel.put("groupname", group);
           nodeModel.put("version", version);
           nodeModel.put("status", in.getParent().getState());
           nodeModel.put("creationdate", in.getCreationDate());
           nodeModel.put("swconfigurationurl", in.getSoftwareImageUrl());
           log().debug("getInventoryNode date" + in.getCreationDate());
           nodeModel.put("configurationurl", in.getConfigurationUrl());
           nodeModel.put("url", m_cp.getUrl());
           
           return nodeModel;
       }
       catch (RancidApiException e) {
           throw e;
       }
   }
   private static Category log() {
       return Logger.getLogger("Rancid");
   }
}