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

import java.util.*;


public class InventoryLayer {
    
    static String _URL = "http://www.rionero.com:9080";
    
    
    static public void init(){
        
        try {
            RWSClientApi.init();
        }
        catch (Exception e) {
            
        }
    }
        
    static public boolean hasRancidInfo(String nodeLabe){
        return true;        
    }
    
    static public Map<String, Object> getInventoryElement(String rancidName) throws RancidApiException{
        
        try {
        
            String group = "laboratorio";
            
            
            RancidNode rn = RWSClientApi.getRWSRancidNode(_URL, group, rancidName);
    
            
    
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
            nodeModel.put("url", _URL);

    
            
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
    
//    static public Map<String, Object> getInventoryNode2(String rancidName) throws RancidApiException{
//        
//        try {
//        
//            String group = "laboratorio";
//            String url = "http://www.rionero.com/rws-current";
//            
//            RancidNode rn = RWSClientApi.getRWSRancidNode(url, group, rancidName);
//    
//    
//            InventoryNode in = new InventoryNode(rn);
//            
//            //in.setVersionId("version1");
//            Date dc = new Date();
//            //in.setCreationDate(dc);
//            in.setExpirationDate(dc);
//            
//            //software configuration
//            in.setSoftwareVersion("1.1");
//            in.setSoftwareImageUrl("http://rancid.net/softwareimage");
//            
//            //configuration
//            in.setConfigurationUrl("http://rancid.net/configuration");
//            in.setRootConfigurationUrl("http://rancid.net/rootconfiguration");
//                        
//            Map<String, Object> nodeModel = new TreeMap<String, Object>();
//            
//            List<InventoryNode> lista = new LinkedList();
//            lista.add(in);
////            InventoryElement i2 = new InventoryElement(in);
////            i2.setElementName("Bridge2");
////            lista.add(i2);
////            InventoryElement i3 = new InventoryElement(in);
////            i3.setElementName("Bridge3");
////            lista.add(i3);
////            InventoryElement i4 = new InventoryElement(in);
////            i4.setElementName("Bridge4");
////            lista.add(i4);
////            in.setNodeElements(lista);
//
//    //      Integer ii = Integer(ie.getElementId(4));
//    //      nodeModel.put("id", ii.toString());
//    
//
//            nodeModel.put("inventory", lista);
//            return nodeModel;
//        }
//        catch (RancidApiException e) {
//            throw e;
//        }
//    }
   

    static public Map<String, Object> getInventoryNodeList(String rancidName) throws RancidApiException{
        
        try {
        
            String group = "laboratorio";
      
            
            RancidNode rn = RWSClientApi.getRWSRancidNode(_URL, group, rancidName);
    
    
            InventoryNode in = new InventoryNode(rn);
            
            List<InventoryElement> lista = new LinkedList();
            InventoryElement i1 = new InventoryElement(in);
            i1.setElementName("Bridge1");
            lista.add(i1);
            InventoryElement i2 = new InventoryElement(in);
            i2.setElementName("Bridge2");
            lista.add(i2);
            InventoryElement i3 = new InventoryElement(in);
            i3.setElementName("Bridge3");
            lista.add(i3);
            InventoryElement i4 = new InventoryElement(in);
            i4.setElementName("Bridge4");
            lista.add(i4);
            in.setNodeElements(lista);

            
            Map<String, Object> nodeModel = new TreeMap<String, Object>();
    //      Integer ii = Integer(ie.getElementId(4));
    //      nodeModel.put("id", ii.toString());
    

            nodeModel.put("inventory", lista);
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
   
//    static public RWSResourceList getRancidPage(String rancidName ){
//        
//        try {
//        
//        String url = "http://www.rionero.com/rws-current";
//        
//        //RancidNodeAggregate rna = RWSClientApi.getRancidNodeAggregate("http://www.rionero.com/rws-current",rancidName);
//
//        Map<String, Object> nodemodel = new TreeMap<String, Object>();
//        RancidNode rn = RWSClientApi.getRancidNodeAggregate("http://www.rionero.com/rws-current",rancidName);
//        nodemodel.put("aggregate", rna);
//        nodemodel.put("groups",rna.getGroups());
//        
//        List<String> groupList = rna.getGroups();
//        
//        Iterator iter = groupList.iterator();
//        
//        String tmpg;
//        
//
//        tmpg = (String)iter.next(); 
//        nodemodel.put("group",tmpg);
//            
//        nodemodel.put("id",rna.getRancidAggregate().get(tmpg).getDeviceName());
//        nodemodel.put("devicename",rna.getRancidAggregate().get(tmpg).getDeviceName());
//        
////        <th>Rancid Name</th>
////        <th>${model.id}</th>
////    </tr>
////    <tr>
////        <th>Device Name</th>
////        <th>${model.devicename}</th>
////    </tr>   
////    <tr>
////        <th>Device Type</th>
////        <th>${model.devicetype}</th>
////    </tr>
////    <tr>
////        <th>Comment</th>
////        <th>${model.comment}</th>
////    </tr>
//        
//        return nodemodel;
//        
//        
//        //GROUPS
//        
////        //////////////////////////////
////        //////////////////////////////
////        //tutto con il rancid node aggregate
////        
////        List<String> lista = new ArrayList<String>();
////        lista.add("laboratorio");
////        lista.add("disasterrecovery");
////        lista.add("lorenteggio");
////        
////        
////        nodemodel.put("groups", lista);
////        
////        
////        //Rancid basic info with first group of list
////        
////        RancidNode rn1 = RWSClientApi.getRWSRancidNode(url, (String)lista.get(0), rancidName);
////        //      disasterrecovery
////        RancidNode rn2 = RWSClientApi.getRWSRancidNode(url, (String)lista.get(0), rancidName); 
////        //      lorenteggio
////        RancidNode rn3 = RWSClientApi.getRWSRancidNode(url, (String)lista.get(0), rancidName); 
////        
////        //mostro solo i dati del nodo trovato nel primo gruppo
////        nodeModel.put("status", rn1.getState());
////        nodeModel.put("devicename", rn1.getDeviceName());
////        nodeModel.put("devicetype", rn1.getDeviceType());
////        nodeModel.put("comment", rn1.getComment());
////        
////        //Associated info
////        InventoryNode in1 = new InventoryNode(rn1);
////        InventoryNode in2 = new InventoryNode(rn1);
////        InventoryNode in3 = new InventoryNode(rn1);
////        
////        rn2.setGroup("disasterrecovery");
////        InventoryNode in4 = new InventoryNode(rn2);
////        InventoryNode in5 = new InventoryNode(rn2);
////        InventoryNode in6 = new InventoryNode(rn2);
////        
////        rn3.setGroup("lorenteggio");
////        InventoryNode in7 = new InventoryNode(rn3);
////        InventoryNode in8 = new InventoryNode(rn3);
////        InventoryNode in9 = new InventoryNode(rn3);
////        
////        Date dc = new Date();
////        in1.setExpirationDate(dc);
////        in2.setExpirationDate(dc);
////        in3.setExpirationDate(dc);
////        in4.setExpirationDate(dc);
////        in5.setExpirationDate(dc);
////        in6.setExpirationDate(dc);
////        in7.setExpirationDate(dc);
////        in8.setExpirationDate(dc);
////        in9.setExpirationDate(dc);
////        in1.setSoftwareVersion("1.0");
////        in2.setSoftwareVersion("1.1");
////        in3.setSoftwareVersion("1.2");
////        in4.setSoftwareVersion("2.0");
////        in5.setSoftwareVersion("2.1");
////        in6.setSoftwareVersion("2.2");
////        in7.setSoftwareVersion("3.0");
////        in8.setSoftwareVersion("3.1");
////        in9.setSoftwareVersion("3.2");
////        
//////        in.setSoftwareImageUrl("http://rancid.net/softwareimage");
//////        in.setConfigurationUrl("http://rancid.net/configuration");
//////        in.setRootConfigurationUrl("http://rancid.net/rootconfiguration");
////        
////        
////        
////        HashMap<String, InventoryNode> node1 = new HashMap<String, InventoryNode>();
////        HashMap<String, InventoryNode> node2 = new HashMap<String, InventoryNode>();
////        HashMap<String, InventoryNode> node3 = new HashMap<String, InventoryNode>();
////        
////        node1.put("1.0", in1);
////        node1.put("1.1", in2);
////        node1.put("1.2", in3);
////        rn1.setNodeVersions(node1);
////        
////        
////        node2.put("2.0", in4);
////        node2.put("2.1", in5);
////        node2.put("2.2", in6);
////        rn2.setNodeVersions(node2);
////        
////        node3.put("3.0", in7);
////        node3.put("3.1", in8);
////        node3.put("3.2", in9);
////        rn3.setNodeVersions(node3);
////        
////        RancidNodeAggregate rna = new RancidNodeAggregate();
////        rna.addRancidAggregate("laboratorio", rn1);
////        rna.addRancidAggregate("disasterrecovery", rn2);
////        rna.addRancidAggregate("lorenteggio", rn3);
//        
//        nodemodel.put("rancidnodeaggregate", rna);
//        
//        return rna;
//        }
//        catch (Exceptio e) {
//            throw e;
//        }
//         
//        
//    }
    
    
   // NEW CODE
   static public Map<String, Object> getRancidNode(String rancidName, String userRole) throws RancidApiException{
        
        try {
            
         
            
            Map<String, Object> nodeModel = new TreeMap<String, Object>();
            
            List<RancidNodeWrapper> ranlist = new ArrayList<RancidNodeWrapper>();
            
            // Group list            
            RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(_URL);
            
            List<String> grouplist = groups.getResource();
            Iterator iter1 = grouplist.iterator();
            
          
            String groupname;
            boolean first = true;
            while (iter1.hasNext()){
                groupname = (String)iter1.next();
                try {
                    RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(_URL ,groupname, rancidName);
                    String vs = rn.getHeadRevision();
                    InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);

                    RancidNodeWrapper rnw = new RancidNodeWrapper(rn.getDeviceName(), groupname, rn.getDeviceType(), rn.getComment(), rn.getHeadRevision(),
                      rn.getTotalRevisions(), in.getExpirationDate(), rn.getRootConfigurationUrl());
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
                    //skip node not found in group...
                }
            }
            
            //Groups invariant            
            nodeModel.put("grouptable", ranlist);
            nodeModel.put("url", _URL);
            
            //CLOGIN
            if (userRole.compareTo("admin") == 0){
                RancidNodeAuthentication rn5 = RWSClientApi.getRWSAuthNode(_URL,rancidName);
                //System.out.println("rn5 " + rn5.getUser() + rn5.getPassword()+rn5.getConnectionMethodString());
                nodeModel.put("cloginuser", rn5.getUser());
                nodeModel.put("cloginpassword", rn5.getPassword());
                nodeModel.put("cloginconnmethod", rn5.getConnectionMethodString());
            }
            
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
   static public Map<String, Object> getRancidNodeList(String rancidName, String groupname) throws RancidApiException{
       
       try {
           
           Map<String, Object> nodeModel = new TreeMap<String, Object>();
           
           List<InventoryWrapper> ranlist = new ArrayList<InventoryWrapper>();
           
           RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(_URL, groupname, rancidName);

           nodeModel.put("devicename", rn.getDeviceName());
           
           //*********
           // version
         
           RWSResourceList versionList;
           
           versionList = RWSClientApi.getRWSResourceConfigList(_URL, groupname, rancidName);
           
           List<String> versionListStr= versionList.getResource();
           
           Iterator iter1 = versionListStr.iterator();
           
           String vs;
           
           while (iter1.hasNext()) {
               vs = (String)iter1.next();
               InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);
               InventoryWrapper inwr = new InventoryWrapper(in.getVersionId(), in.getExpirationDate(), groupname, in.getConfigurationUrl());
               ranlist.add(inwr);
           }
           
           nodeModel.put("grouptable", ranlist);
           nodeModel.put("url", _URL);
           
           
           return nodeModel;
       }
       catch (RancidApiException e) {
           throw e;
       }
   }
static public Map<String, Object> getRancidNodeList(String rancidName) throws RancidApiException{
       
       try {
           
           Map<String, Object> nodeModel = new TreeMap<String, Object>();
           
           RWSResourceList groups = RWSClientApi.getRWSResourceGroupsList(_URL);
           
           List<InventoryWrapper> ranlist = new ArrayList<InventoryWrapper>();
           
           List<String> grouplist = groups.getResource();
           Iterator iter2 = grouplist.iterator();
           
           boolean first = true;
           String groupname;
           while (iter2.hasNext()) {
               groupname = (String)iter2.next();
           
               RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(_URL, groupname, rancidName);
    
               if (first){
                   nodeModel.put("devicename", rn.getDeviceName());
                   first = false;
               }
               
               //*********
               // version
             
               RWSResourceList versionList;
               
               versionList = RWSClientApi.getRWSResourceConfigList(_URL, groupname, rancidName);
               
               List<String> versionListStr= versionList.getResource();
               
               Iterator iter1 = versionListStr.iterator();
               
               String vs;
               
               while (iter1.hasNext()) {
                   vs = (String)iter1.next();
                   InventoryNode in = (InventoryNode)rn.getNodeVersions().get(vs);
                   InventoryWrapper inwr = new InventoryWrapper(in.getVersionId(), in.getExpirationDate(), groupname, in.getConfigurationUrl());
                   ranlist.add(inwr);
               }
           }
           
           nodeModel.put("grouptable", ranlist);
           nodeModel.put("url", _URL);
           
           
           return nodeModel;
       }
       catch (RancidApiException e) {
           throw e;
       }
   }
   static public int updateCloginInfo(String group, String device, String user, String password, String method) {
       
       try {
           RancidNodeAuthentication rna = new RancidNodeAuthentication();
           rna.setUser(user);
           rna.setPassword(password);
           rna.setConnectionMethod(method);
           RWSClientApi.createOrUpdateRWSAuthNode(_URL,rna);
           
           return 0;
       }
       catch (RancidApiException e) {
           return -1;
       }
   }
   
   static public Map<String, Object> getInventoryNode(String rancidName, String group, String version) throws RancidApiException{
       
       try {
    
           RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(_URL, group, rancidName);
       
           InventoryNode in = (InventoryNode)rn.getNodeVersions().get(version);
           
           //InventoryWrapper tmpw = new InventoryWrapper(version, in.getExpirationDate(), group, in.getConfigurationUrl());
                       
           Map<String, Object> nodeModel = new TreeMap<String, Object>();
   
           nodeModel.put("devicename", rancidName);
           nodeModel.put("groupname", group);
           nodeModel.put("version", version);
           nodeModel.put("status", in.getParent().getState());
           nodeModel.put("expirationdate", in.getExpirationDate());
           nodeModel.put("configurationurl", in.getConfigurationUrl());
           nodeModel.put("url", _URL);
           
           return nodeModel;
       }
       catch (RancidApiException e) {
           throw e;
       }
   }
}

//BACKUP CODE
//// Inventory ListList<InventoryWrapper> wrinv = new ArrayList<InventoryWrapper>();id
//RWSResourceList versList = RWSClientApi.getRWSResourceConfigList(url,group,rancidName);
//
//List<String> configlist = versList.getResource();
//Iterator iter1 = configlist.iterator();
//
//RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(url, group, rancidName); 
//List<InventoryNode> invList = new ArrayList<InventoryNode>();
//
//List<InventoryWrapper> wrinv = new ArrayList<InventoryWrapper>(); 
//
////String vs;
//InventoryNode in;
//while (iter1.hasNext()){
//  vs = (String)iter1.next();
//  in = (InventoryNode)rn.getNodeVersions().get(vs);
//  InventoryWrapper tmpw = new InventoryWrapper(vs, in.getExpirationDate(), group);
//  wrinv.add(tmpw);
//                
//}