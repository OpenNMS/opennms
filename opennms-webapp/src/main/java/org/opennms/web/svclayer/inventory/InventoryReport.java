package org.opennms.web.svclayer.inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;


import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.rws.GroupSet;
import org.opennms.netmgt.config.rws.GroupXSet;
import org.opennms.netmgt.config.rws.NodeSet;
import org.opennms.netmgt.config.rws.InventoryElement2RP;
import org.opennms.netmgt.config.rws.InventoryMemoryRP;
import org.opennms.netmgt.config.rws.InventorySoftwareRP;
import org.opennms.netmgt.config.rws.Nbisinglenode;
import org.opennms.netmgt.config.rws.RwsNbinventoryreport;
import org.opennms.netmgt.config.rws.RwsRancidlistreport;
import org.opennms.netmgt.config.rws.TupleRP;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.InventoryElement2;
import org.opennms.rancid.InventoryMemory;
import org.opennms.rancid.InventoryNode;
import org.opennms.rancid.InventorySoftware;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RWSResourceList;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.Tuple;

public class InventoryReport {
    public static RwsNbinventoryreport runInventoryReport(ConnectionProperties m_cp, String _date, String field){
        
        log().debug("InventoryService runNodeBaseInventoryReport Date ["+ _date +"] key [" + field + "]"); 
        boolean withKey = false;
        if (field.compareTo("")!=0){
            withKey = true;
        }

        try {
        
            //parse date
            SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
            Date tmp_date = format.parse(_date);
            log().debug("InventoryService runNodeBaseInventoryReport date[" + tmp_date.toString() + "]"); 
            
            RwsNbinventoryreport rnbi = new RwsNbinventoryreport();
            
            //get the list of groups
            RWSResourceList groupList = RWSClientApi.getRWSResourceGroupsList(m_cp);
            List<String> groupListStr= groupList.getResource();
            Iterator<String> iterGroup = groupListStr.iterator();
            int totalGroups = 0;
            while (iterGroup.hasNext()){
                String groupName = iterGroup.next();
                GroupSet groupSet = new GroupSet(); 
                boolean groupIsEmpty = true;
                boolean groupIsIncremented = false;
                log().debug("InventoryService runNodeBaseInventoryReport group [" + groupName + "]"); 
                RWSResourceList deviceList = RWSClientApi.getRWSResourceDeviceList(m_cp, groupName);
                List<String> deviceListStr= deviceList.getResource();
                Iterator<String> iterDevice = deviceListStr.iterator();
                int totalNodes = 0;
                while (iterDevice.hasNext()){
                    groupIsEmpty = false;
                    if (!groupIsIncremented){
                        totalGroups++;
                        groupIsIncremented = true;
                    }
                    String deviceName = iterDevice.next();
                    totalNodes++;
                    log().debug("InventoryService runNodeBaseInventoryReport device [" + deviceName + "]");
                    String versionMatch="";
                    try {
                        RWSResourceList versionList = RWSClientApi.getRWSResourceConfigList(m_cp, groupName, deviceName);
                        List<String> versionListStr= versionList.getResource();
                        Iterator<String> iterVersion = versionListStr.iterator();
                        
                        RancidNode rancidNode;
                        rancidNode = RWSClientApi.getRWSRancidNodeInventory(m_cp ,groupName, deviceName);
                        
                        boolean found = false;
                        
                        while (iterVersion.hasNext() && !found)  {
                            versionMatch = iterVersion.next();
                            InventoryNode invNode = (InventoryNode)rancidNode.getNodeVersions().get(versionMatch);
                            log().debug("InventoryService runNodeBaseInventoryReport InventoryNode version[" + invNode.getVersionId() + "] date ["+invNode.getCreationDate()+"] config ["+ invNode.getConfigurationUrl() +"]"); 
                            if (tmp_date.compareTo(invNode.getCreationDate()) >  0 ) {
                                found = true;
                                log().debug("InventoryService runNodeBaseInventoryReport Date found is ["+invNode.getCreationDate()+"] version is [" + versionMatch + "]"); 
                            }
                        }
                        if (found == false) {
                            // skip device
                            log().debug("InventoryService runNodeBaseInventoryReport device has no inventory at this date["+deviceName+ "]"); 
                            continue;
                        }
                    } catch (Exception e){
                        //no inventory, skip node....
                        log().debug("InventoryService runNodeBaseInventoryReport device has no inventory ["+deviceName+ "]"); 
                        if (!withKey){
                            Nbisinglenode nbisn = new Nbisinglenode();
                            nbisn.setDevicename(deviceName);
                            nbisn.setGroupname(groupName);
                            nbisn.setComment("No inventory associated");
                            groupSet.addNbisinglenode(nbisn);
                        }
                        continue;
                    }
                    if (versionMatch.compareTo("") == 0){
                        log().debug("InventoryService runNodeBaseInventoryReport device skipped ["+deviceName+ "]"); 
                        continue;
                    }
                   
                    //we have groupname devicename and version
        
                    try {
                        NodeBaseInventory nodeBaseInv = getNodeBaseInventory(m_cp, deviceName, groupName, versionMatch);
                        
                        //marshall xml and save to disk
                        log().debug("InventoryService runNodeBaseInventoryReport MARSHALL [" + deviceName + "] group ["+groupName+"] Version ["+ versionMatch +"]"); 
                        log().debug("InventoryService runNodeBaseInventoryReport data [" + nodeBaseInv.expand()); 
                               
                        
                        
                        Nbisinglenode nbisn = new Nbisinglenode();
                        boolean includeNbisn = false;
                        
                        
                        nbisn.setConfigurationurl(nodeBaseInv.getConfigurationurl());
                        nbisn.setCreationdate(nodeBaseInv.getCreationdate());
                        nbisn.setDevicename(nodeBaseInv.getDevicename());
                        nbisn.setGroupname(nodeBaseInv.getGroupname());
                        nbisn.setStatus(nodeBaseInv.getStatus());
                        nbisn.setSwconfigurationurl(nodeBaseInv.getSwconfigurationurl());
                        nbisn.setVersion(nodeBaseInv.getVersion());
            
                        List<InventoryElement2RP> ie2rpList = new ArrayList<InventoryElement2RP>();
                        Iterator<InventoryElement2> ie2rpIter = nodeBaseInv.getIe().iterator();
                        InventoryElement2RP ie2rp = new InventoryElement2RP();
            
                        while (ie2rpIter.hasNext()){
                            InventoryElement2 ie2 = ie2rpIter.next();
                            Iterator<Tuple> iterTuple = ie2.getTupleList().iterator();
                            Iterator<InventoryMemory> iterMemory = ie2.getMemoryList().iterator();
                            Iterator<InventorySoftware> iterSoftware = ie2.getSoftwareList().iterator();
                            
                            TupleRP tmp2 = new TupleRP();
                            while (iterTuple.hasNext()){
                                Tuple tmp1 = iterTuple.next();
                                tmp2.setName(tmp1.getName());
                                //filter here
                                if (withKey && tmp1.getDescription().contains(field)){
                                    includeNbisn = true;
                                }
                                tmp2.setDescription(tmp1.getDescription());
                                ie2rp.addTupleRP(tmp2);
                            }
                            
                            InventoryMemoryRP tmp3 = new InventoryMemoryRP();
                            while (iterMemory.hasNext()){
                                InventoryMemory tmp1 = iterMemory.next();
                                //filter here
                                if (withKey && tmp1.getSize().contains(field)){
                                    includeNbisn = true;
                                }
                                tmp3.setSize(tmp1.getSize());
                                if (withKey && tmp1.getType().contains(field)){
                                    includeNbisn = true;
                                }
                                tmp3.setType(tmp1.getType());
                                ie2rp.addInventoryMemoryRP(tmp3);
                            }
                            InventorySoftwareRP tmp4 = new InventorySoftwareRP();
                            while (iterSoftware.hasNext()){
                                InventorySoftware tmp1 = iterSoftware.next();
                                //filter here
                                if(withKey && tmp1.getType().contains(field)){
                                    includeNbisn = true;
                                }
                                tmp4.setType(tmp1.getType());
                                if(withKey && tmp1.getVersion().contains(field)){
                                    includeNbisn = true;
                                }
                                tmp4.setVersion(tmp1.getVersion());
                                ie2rp.addInventorySoftwareRP(tmp4);
                            }
                            
                            ie2rpList.add(ie2rp);
                            
                        }
                        nbisn.setInventoryElement2RP(ie2rpList);
                        // if withKey is false then include it in any case
                        // includeNbsin is true the fiels has been found
                        // data must be included
                        if(!withKey || includeNbisn){
                            groupSet.addNbisinglenode(nbisn);
                        }
                        //else skip 
                    }catch (Exception e){
                        log().debug("InventoryService runNodeBaseInventoryReport device has inventory errors ["+deviceName+ "]"); 
                        continue;

                    }
                    groupSet.setTotalNodes(totalNodes);
                }
                rnbi.addGroupSet(groupSet);
                rnbi.setTotalGroups(totalGroups);
            }
            log().debug("InventoryService runNodeBaseInventoryReport ended");
            return rnbi;
        }
        catch (Exception e) {
            log().debug("InventoryService runNodeBaseInventoryReport has given exception "+ e.getMessage() );
            return null;
        }
    }
    
    public static RwsRancidlistreport runRacidListReport(ConnectionProperties m_cp, String _date) {
        
        RwsRancidlistreport rlist = new RwsRancidlistreport();
        
        try {
            
            //parse date
            SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
            Date tmp_date = format.parse(_date);
            log().debug("InventoryService runRacidListReport date[" + tmp_date.toString() + "]"); 
            
            RwsNbinventoryreport rnbi = new RwsNbinventoryreport();
            
            //get the list of groups
            RWSResourceList groupList = RWSClientApi.getRWSResourceGroupsList(m_cp);
            List<String> groupListStr= groupList.getResource();
            Iterator<String> iterGroup = groupListStr.iterator();
            int totalGroups = 0;
                        
            while (iterGroup.hasNext()){
                String groupName = iterGroup.next();
                GroupSet groupSet = new GroupSet(); 
                log().debug("InventoryService runRacidListReport group [" + groupName + "]"); 
                RWSResourceList deviceList = RWSClientApi.getRWSResourceDeviceList(m_cp, groupName);
                List<String> deviceListStr= deviceList.getResource();
                Iterator<String> iterDevice = deviceListStr.iterator();
                int totalNodes = 0;
                
                GroupXSet gs = new GroupXSet();
                boolean groupHasDevices = false;
                boolean groupTotalIncremented = false;
                
                while (iterDevice.hasNext()){
                    String deviceName = iterDevice.next();
                    //totalNodes++;
                    log().debug("InventoryService runRacidListReport device [" + deviceName + "]");
                    String versionMatch="";
                    try {
                        RWSResourceList versionList = RWSClientApi.getRWSResourceConfigList(m_cp, groupName, deviceName);
                        List<String> versionListStr= versionList.getResource();
                        Iterator<String> iterVersion = versionListStr.iterator();
                        
                        RancidNode rancidNode;
                        rancidNode = RWSClientApi.getRWSRancidNodeInventory(m_cp ,groupName, deviceName);
                        
                        boolean found = false;
                        
                        InventoryNode invNode = new InventoryNode(rancidNode);
                        while (iterVersion.hasNext() && !found)  {
                            versionMatch = iterVersion.next();
                            invNode = (InventoryNode)rancidNode.getNodeVersions().get(versionMatch);
                            log().debug("InventoryService runRacidListReport InventoryNode version[" + invNode.getVersionId() + "] date ["+invNode.getCreationDate()+"] config ["+ invNode.getConfigurationUrl() +"]"); 
                            if (tmp_date.compareTo(invNode.getCreationDate()) >  0 ) {
                                found = true;
                                log().debug("InventoryService runRacidListReport Date found is ["+invNode.getCreationDate()+"] version is [" + versionMatch + "]"); 
                            }
                        }
                        if (found == false) {
                            // skip device
                            log().debug("InventoryService runRacidListReport device has no inventory at this date["+deviceName+ "]"); 
                            continue;
                        } else{
                            NodeSet ns = new NodeSet();
                            ns.setDevicename(deviceName);
                            ns.setGroupname(groupName);
                            ns.setVersion(versionMatch);
                            ns.setConfigurationurl(invNode.getConfigurationUrl());
                            ns.setSwconfigurationurl(invNode.getSoftwareImageUrl());
                            ns.setStatus(rancidNode.getState());
                            
                            gs.addNodeSet(ns);
                            
                            groupHasDevices = true;
                            if (!groupTotalIncremented){
                                totalGroups++;
                                groupTotalIncremented = true;
                            }
                            totalNodes ++;
                        }
                    } catch (Exception e){
                        //no inventory, skip node....
                        log().debug("InventoryService runRacidListReport device has no inventory ["+deviceName+ "]"); 
//                        Nbisinglenode nbisn = new Nbisinglenode();
//                        nbisn.setDevicename(deviceName);
//                        nbisn.setGroupname(groupName);
//                        nbisn.setComment("No inventory associated");
//                        groupSet.addNbisinglenode(nbisn);
                        continue;
                    }
                    if (versionMatch.compareTo("") == 0){
                        log().debug("InventoryService runNodeBaseInventoryReport device skipped ["+deviceName+ "]"); 
                        continue;
                    }
                }
                if (groupHasDevices){
                    gs.setTotalNodes(totalNodes);
                    rlist.addGroupXSet(gs);
                }
            }
            rlist.setTotalGroups(totalGroups);
            return rlist;
        }
        catch (Exception e) {
            log().debug("InventoryService runNodeBaseInventoryReport has given exception "+ e.getMessage() );
            return null;
        }
    }
        
    public static NodeBaseInventory getNodeBaseInventory(ConnectionProperties cp, String node, String group, String version) throws RancidApiException{
        
        // get the latest version from the given date
        
        log().debug("getNodeBaseInventory " + node +" "+group + " "+version);
                
        RancidNode rn = RWSClientApi.getRWSRancidNodeInventory(cp, group, node);
        
        InventoryNode in = (InventoryNode)rn.getNodeVersions().get(version);
        
        NodeBaseInventory nbi = new NodeBaseInventory();
        
        nbi.setDevicename(node);
        nbi.setGroupname(group);
        nbi.setVersion(version);
        nbi.setStatus(in.getParent().getState());
        nbi.setCreationdate(in.getCreationDate());
        nbi.setSwconfigurationurl(in.getSoftwareImageUrl());
        nbi.setConfigurationurl(in.getConfigurationUrl());
        
        nbi.setIe( RWSClientApi.getRWSRancidNodeInventoryElement2(cp, rn, version));
        
        return nbi;
    }
    
    private static Category log() {
        return Logger.getLogger("Rancid");
    }

}
