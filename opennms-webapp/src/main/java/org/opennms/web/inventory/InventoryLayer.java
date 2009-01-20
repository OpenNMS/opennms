package org.opennms.web.inventory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.rancid.InventoryElement;
import org.opennms.rancid.InventoryNode;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;

public class InventoryLayer {
    
    
    static public void init(){
        
        try {
            RWSClientApi.init();
        }
        catch (Exception e) {
            
        }
    }
        
    static public Map<String, Object> getInventoryElement(String url, String group, String rancidName) throws RancidApiException{
        
        try {
        
            RancidNode rn = RWSClientApi.getRWSRancidNode(url, group, rancidName);
    
    
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
;
    
            
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
    
    static public Map<String, Object> getInventoryNode(String url, String group, String rancidName) throws RancidApiException{
        
        try {
        
            RancidNode rn = RWSClientApi.getRWSRancidNode(url, group, rancidName);
    
    
            InventoryNode in = new InventoryNode(rn);
            
            //in.setVersionId("version1");
            Date dc = new Date();
            //in.setCreationDate(dc);
            in.setExpirationDate(dc);
            
            //software configuration
            in.setSoftwareVersion("1.1");
            in.setSoftwareImageUrl("http://rancid.net/softwareimage");
            
            //configuration
            in.setConfigurationUrl("http://rancid.net/configuration");
//            in.setRootConfigurationUrl("http://rancid.net/rootconfiguration");

            Map<String, Object> nodeModel = new TreeMap<String, Object>();
            
    //      Integer ii = Integer(ie.getElementId(4));
    //      nodeModel.put("id", ii.toString());
    
            nodeModel.put("status", in.getParent().getState());
            nodeModel.put("datecrea", in.getCreationDate());
            nodeModel.put("dateexp", in.getExpirationDate());
            nodeModel.put("softversion", in.getSoftwareVersion());
            nodeModel.put("softimage", in.getSoftwareImageUrl());
            nodeModel.put("softconfigurl", in.getConfigurationUrl());
//            nodeModel.put("rootconfigurl", in.getRootConfigurationUrl());
  
            
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
    static public Map<String, Object> getInventoryNodeList(String url, String group, String rancidName) throws RancidApiException{
        
        try {
        
            RancidNode rn = RWSClientApi.getRWSRancidNode(url, group, rancidName);
    
    
            InventoryNode in = new InventoryNode(rn);
            
            List<InventoryElement> lista = new LinkedList<InventoryElement>();
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
   static public Map<String, Object> getRancidNode(String url, String group, String rancidName) throws RancidApiException{
        
        try {
        
            RancidNode rn = RWSClientApi.getRWSRancidNode(url, group, rancidName);
            
            Map<String, Object> nodeModel = new TreeMap<String, Object>();
    //      Integer ii = Integer(ie.getElementId(4));
    //      nodeModel.put("id", ii.toString());
    
            nodeModel.put("status", rn.getState());
            nodeModel.put("devicename", rn.getDeviceName());
            nodeModel.put("devicetype", rn.getDeviceType());
            nodeModel.put("comment", rn.getComment());
            
            return nodeModel;
        }
        catch (RancidApiException e) {
            throw e;
        }
    }
}
