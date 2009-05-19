package org.opennms.web.svclayer.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;


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
import org.opennms.report.availability.ReportMailer;
import org.opennms.report.datablock.PDFWriter;

public class InventoryReport implements Runnable {
    
    static int RANCIDLIST = 1;
    static int NBINVENTORY = 2;
    
    String theDate;
    String theField;
    int theType;
    String reportFormat;
    ConnectionProperties cProperties;
    String reportEmail;
    
    public InventoryReport(ConnectionProperties m_cp, String _date, String _field, String _format, String _reportemail){
        cProperties = m_cp;
        theDate = _date;
        theField = _field;
        reportFormat = _format;
        theType = NBINVENTORY;
        reportEmail = _reportemail;
    }
    public InventoryReport(ConnectionProperties m_cp, String _date, String _format, String _reportemail){
        cProperties = m_cp;
        theDate = _date;
        reportFormat = _format;
        theType = RANCIDLIST;
        reportEmail = _reportemail;
    }
    
    public void run() {

        if (theType == NBINVENTORY){
            log().debug("InventoryService runNodeBaseInventoryReport Date ["+ theDate +"] key [" + theField + "]"); 
            boolean withKey = false;
            if (theField.compareTo("")!=0){
                withKey = true;
            }
            try {            
                //parse date
                SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
                Date tmp_date = new Date();
                try {
                    tmp_date = format.parse(theDate);
                }
                catch (ParseException pe){
                    tmp_date = Calendar.getInstance().getTime();
                }
                log().debug("InventoryService runNodeBaseInventoryReport date[" + tmp_date.toString() + "]"); 

                RwsNbinventoryreport rnbi = new RwsNbinventoryreport();

                //get the list of groups
                RWSResourceList groupList = RWSClientApi.getRWSResourceGroupsList(cProperties);
                List<String> groupListStr= groupList.getResource();
                Iterator<String> iterGroup = groupListStr.iterator();
                int totalGroups = 0;
                while (iterGroup.hasNext()){
                    String groupName = iterGroup.next();
                    GroupSet groupSet = new GroupSet(); 
                    boolean groupIsEmpty = true;
                    boolean groupIsIncremented = false;
                    log().debug("InventoryService runNodeBaseInventoryReport group [" + groupName + "]"); 
                    RWSResourceList deviceList = RWSClientApi.getRWSResourceDeviceList(cProperties, groupName);
                    List<String> deviceListStr= deviceList.getResource();
                    Iterator<String> iterDevice = deviceListStr.iterator();
                    int totalNodes = 0;
                    while (iterDevice.hasNext()){
                        //groupIsEmpty = false;
                        if (!groupIsIncremented){
                            totalGroups++;
                            groupIsIncremented = true;
                        }
                        String deviceName = iterDevice.next();
                        log().debug("InventoryService runNodeBaseInventoryReport device [" + deviceName + "]");
                        String versionMatch="";
                        try {
                            RWSResourceList versionList = RWSClientApi.getRWSResourceConfigList(cProperties, groupName, deviceName);
                            List<String> versionListStr= versionList.getResource();
                            Iterator<String> iterVersion = versionListStr.iterator();

                            RancidNode rancidNode;
                            rancidNode = RWSClientApi.getRWSRancidNodeInventory(cProperties ,groupName, deviceName);

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
                            //                            if (!withKey){
                            //                                Nbisinglenode nbisn = new Nbisinglenode();
                            //                                nbisn.setDevicename(deviceName);
                            //                                nbisn.setGroupname(groupName);
                            //                                nbisn.setComment("No inventory associated");
                            //                                groupSet.addNbisinglenode(nbisn);
                            //                            }
                            continue;
                        }
                        if (versionMatch.compareTo("") == 0){
                            log().debug("InventoryService runNodeBaseInventoryReport device skipped ["+deviceName+ "]"); 
                            continue;
                        }

                        //we have groupname devicename and version

                        try {
                            NodeBaseInventory nodeBaseInv = getNodeBaseInventory(cProperties, deviceName, groupName, versionMatch);

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

                            while (ie2rpIter.hasNext()){

                                InventoryElement2RP ie2rp = new InventoryElement2RP();

                                InventoryElement2 ie2 = ie2rpIter.next();
                                Iterator<Tuple> iterTuple = ie2.getTupleList().iterator();
                                Iterator<InventoryMemory> iterMemory = ie2.getMemoryList().iterator();
                                Iterator<InventorySoftware> iterSoftware = ie2.getSoftwareList().iterator();

                                while (iterTuple.hasNext()){
                                    TupleRP tmp2 = new TupleRP();
                                    Tuple tmp1 = iterTuple.next();
                                    tmp2.setName(tmp1.getName());
                                    //filter here
                                    if (withKey && tmp1.getDescription().contains(theField)){
                                        includeNbisn = true;
                                    }
                                    tmp2.setDescription(tmp1.getDescription());
                                    ie2rp.addTupleRP(tmp2);
                                }

                                while (iterMemory.hasNext()){
                                    InventoryMemoryRP tmp3 = new InventoryMemoryRP();
                                    InventoryMemory tmp1 = iterMemory.next();
                                    //filter here
                                    if (withKey && tmp1.getSize().contains(theField)){
                                        includeNbisn = true;
                                    }
                                    tmp3.setSize(tmp1.getSize());
                                    if (withKey && tmp1.getType().contains(theField)){
                                        includeNbisn = true;
                                    }
                                    tmp3.setType(tmp1.getType());
                                    ie2rp.addInventoryMemoryRP(tmp3);
                                }
                                while (iterSoftware.hasNext()){
                                    InventorySoftwareRP tmp4 = new InventorySoftwareRP();
                                    InventorySoftware tmp1 = iterSoftware.next();
                                    //filter here
                                    if(withKey && tmp1.getType().contains(theField)){
                                        includeNbisn = true;
                                    }
                                    tmp4.setType(tmp1.getType());
                                    if(withKey && tmp1.getVersion().contains(theField)){
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
                                groupIsEmpty = false;
                                totalNodes++;
                                groupSet.addNbisinglenode(nbisn);
                            }
                            //else skip 
                        }catch (Exception e){
                            log().debug("InventoryService runNodeBaseInventoryReport device has inventory errors ["+deviceName+ "]"); 
                            continue;

                        }
                        if (!groupIsEmpty){
                            groupSet.setTotalNodes(totalNodes);
                        }
                    }
                    rnbi.addGroupSet(groupSet);
                    rnbi.setTotalGroups(totalGroups);
                    rnbi.setDateInventory(format.format(tmp_date));
                }
                log().debug("InventoryService runNodeBaseInventoryReport object filled");
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
                String datestamp = fmt.format(new java.util.Date()) ;
                String xmlFileName = ConfigFileConstants.getHome() + "/share/reports/NODEINVENTORY" + datestamp + ".xml";

                // Generate source XML
                FileWriter writer = new FileWriter(xmlFileName);
                Marshaller marshaller = new Marshaller(writer);
                marshaller.setSuppressNamespaces(true);
                marshaller.marshal(rnbi);
                writer.close();
                log().debug("runNodeBaseInventoryReport marshal done");

                if (reportFormat.compareTo("pdftype") == 0){

                    log().debug("runNodeBaseInventoryReport generating pdf is still not supported :( sending xml");
                    log().debug("runNodeBaseInventoryReport xml sending email");
                    ReportMailer mailer = new ReportMailer(reportEmail,xmlFileName);
                    mailer.send();


                } else {

                    log().debug("runNodeBaseInventoryReport generating html");

                    String htmlFileName=ConfigFileConstants.getHome() + "/share/reports/NODEINVENTORY" + datestamp + ".html";

                    File file = new File(htmlFileName);
                    FileOutputStream hmtlFileWriter = new FileOutputStream(file);
                    PDFWriter htmlWriter = new PDFWriter(ConfigFileConstants.getFilePathString() + "/rws-nbinventoryreport.xsl");
                    File fileR = new File(xmlFileName);
                    FileReader fileReader = new FileReader(fileR);
                    htmlWriter.generateHTML(fileReader, hmtlFileWriter);
                    log().debug("runNodeBaseInventoryReport html sending email");
                    ReportMailer mailer = new ReportMailer(reportEmail,htmlFileName);
                    mailer.send();

                }
            }
            catch (Exception e){
                log().debug("InventoryService runNodeBaseInventoryReport exception "+ e.getMessage() );
            }

        }
        else if (theType == RANCIDLIST){
            RwsRancidlistreport rlist = new RwsRancidlistreport();

            try {

                //parse date
                SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
                Date tmp_date = new Date();
                try {
                    tmp_date = format.parse(theDate);
                }
                catch (ParseException pe){
                    tmp_date = Calendar.getInstance().getTime();
                }
                log().debug("InventoryService runRacidListReport date[" + tmp_date.toString() + "]"); 

                //get the list of groups
                RWSResourceList groupList = RWSClientApi.getRWSResourceGroupsList(cProperties);
                List<String> groupListStr= groupList.getResource();
                Iterator<String> iterGroup = groupListStr.iterator();
                int totalGroups = 0;

                while (iterGroup.hasNext()){
                    String groupName = iterGroup.next();
                    log().debug("InventoryService runRacidListReport group [" + groupName + "]"); 
                    RWSResourceList deviceList = RWSClientApi.getRWSResourceDeviceList(cProperties, groupName);
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
                            RWSResourceList versionList = RWSClientApi.getRWSResourceConfigList(cProperties, groupName, deviceName);
                            List<String> versionListStr= versionList.getResource();
                            Iterator<String> iterVersion = versionListStr.iterator();

                            RancidNode rancidNode;
                            rancidNode = RWSClientApi.getRWSRancidNodeInventory(cProperties ,groupName, deviceName);

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
                            //                            Nbisinglenode nbisn = new Nbisinglenode();
                            //                            nbisn.setDevicename(deviceName);
                            //                            nbisn.setGroupname(groupName);
                            //                            nbisn.setComment("No inventory associated");
                            //                            groupSet.addNbisinglenode(nbisn);
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
                // Generate source XML
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
                String datestamp = fmt.format(new java.util.Date()) ;
                String xmlFileName = ConfigFileConstants.getHome() + "/share/reports/RANCIDLISTREPORT" + datestamp + ".xml";

                FileWriter writer = new FileWriter(xmlFileName);
                Marshaller marshaller = new Marshaller(writer);
                marshaller.setSuppressNamespaces(true);
                marshaller.marshal(rlist);
                writer.close();
                log().debug("runRancidListReport marshal done");

                if (reportFormat.compareTo("pdftype") == 0){

                    log().debug("runRancidListReport generating pdf is still not supported :( sending xml");
                    
                    log().debug("runRancidListReport xml sending email");
                    ReportMailer mailer = new ReportMailer(reportEmail,xmlFileName);
                    mailer.send();
                    


                } else {

                    log().debug("runRancidListReport generating html");

                    String htmlFileName=ConfigFileConstants.getHome() + "/share/reports/RANCIDLISTREPORT" + datestamp + ".html";

                    File file = new File(htmlFileName);
                    FileOutputStream hmtlFileWriter = new FileOutputStream(file);
                    PDFWriter htmlWriter = new PDFWriter(ConfigFileConstants.getFilePathString() + "/rws-rancidlistreport.xsl");
                    File fileR = new File(xmlFileName);
                    FileReader fileReader = new FileReader(fileR);
                    htmlWriter.generateHTML(fileReader, hmtlFileWriter);
                    log().debug("runRancidListReport html sending email");
                    ReportMailer mailer = new ReportMailer(reportEmail,htmlFileName);
                    mailer.send();

                }


            }
            catch (Exception e) {
                log().debug("InventoryService runRancidListReport has given exception "+ e.getMessage() );
                //
            }
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
