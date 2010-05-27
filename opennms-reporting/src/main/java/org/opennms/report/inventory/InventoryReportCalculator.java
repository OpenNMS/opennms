package org.opennms.report.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.InventoryElement2;
import org.opennms.rancid.InventoryMemory;
import org.opennms.rancid.InventoryNode;
import org.opennms.rancid.InventorySoftware;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.Tuple;
import org.springframework.beans.factory.InitializingBean;

public class InventoryReportCalculator implements InitializingBean {
    String m_baseDir;
    // output file name

    private String m_outputFileName;

    ConnectionProperties m_cp;
    public String getOutputFileName() {
        return m_outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        m_outputFileName = outputFileName;
    }

    RWSConfig m_rwsConfig;
    
    String theDate;
    String user;
    String theField;
    Date reportRequestDate;
    
    RwsNbinventoryreport rnbi;

    
    public String getTheField() {
        return theField;
    }

    public void setTheField(String theField) {
        this.theField = theField;
    }

    public String getTheDate() {
        return theDate;
    }

    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }

    public void setRwsConfig(RWSConfig rwsConfig) {
        m_rwsConfig = rwsConfig;
    }

    public String getBaseDir() {
        return m_baseDir;
    }

    public void setBaseDir(String baseDir) {
        m_baseDir = baseDir;
    }

    private static Logger log() {
        return Logger.getLogger("Rancid");
    }

    public void afterPropertiesSet() throws Exception {
        RWSClientApi.init();
        m_cp = m_rwsConfig.getBase();
    }

    private List<String> getGroups() {
        
        try {
            return RWSClientApi.getRWSResourceGroupsList(m_cp).getResource();
        } catch (RancidApiException e) {
            log().error("getGroups: has given exception "+ e.getMessage() + ". Skipped");
        }
        return new ArrayList<String>();
    }
    
    private List<String> getDeviceListOnGroup(String groupName) {
        try {
            return RWSClientApi.getRWSResourceDeviceList(m_cp, groupName).getResource();
        } catch (RancidApiException e) {
            log().error("getDeviceListOnGroup: group [" + groupName + "]. Skipped"); 
        }
        return new ArrayList<String>();
    }
    
    private List<String> getVersionListOnDevice(String deviceName, String groupName) {
        try {
            return RWSClientApi.getRWSResourceConfigList(m_cp, groupName, deviceName).getResource();
        } catch (RancidApiException e) {
            log().error("getVersionListOnDevice:  device has no inventory ["+deviceName+ "]. " + e.getLocalizedMessage()); 
        }

        return new ArrayList<String>();
    }
    
    private RancidNode getFullNode(String groupName, String deviceName) {
        try {
            return RWSClientApi.getRWSRancidNodeInventory(m_cp ,groupName, deviceName);
        } catch (RancidApiException e) {
            log().error("getFullNode:  device has no inventory ["+deviceName+ "]. " + e.getLocalizedMessage()); 
        }
        return null;
    }


    public void calculate() {
        
        rnbi = new RwsNbinventoryreport();
        rnbi.setUser(user);
        rnbi.setReportRequestDate(reportRequestDate.toString());

        boolean withKey = false;
        if (theField.compareTo("")!=0){
            withKey = true;
            rnbi.setTheField(theField);
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
        Date tmp_date = new Date();
        try {
            tmp_date = format.parse(theDate);
        }
        catch (ParseException pe){
            tmp_date = Calendar.getInstance().getTime();
        }
        log().debug("calculate:report date[" + tmp_date.toString() + "]"); 
        rnbi.setReportDate(tmp_date.toString());


        int totalGroups = 0;
        int groupsMatching = 0;
        int groupWithoutNodes = 0;
        int groupsWithNodesWithoutinventoryAtAll = 0;
        int groupsWithNodesWithoutinventoryAtReportDate = 0;
  
        for(String groupName : getGroups()) {
            log().debug("calculate:report group [" + groupName + "]"); 
            totalGroups++;
            GroupSet gs = new GroupSet(); 
            gs.setGroupSetName(groupName);
            int totalNodes = 0;
            int nodeMatching = 0;
            int nodesWithoutinventoryAtAll=0;
            int nodesWithoutinventoryAtReportDate=0;
            boolean groupHasDevices = false;
            boolean groupHasNodesWithoutinventoryAtAll = false;
            boolean groupHasNodesWithoutinventoryAtrequestDate = false;

            for (String deviceName: getDeviceListOnGroup(groupName)) {
                totalNodes++;
                log().debug("calculate:report device [" + deviceName + "]");

                RancidNode rancidNode = getFullNode(groupName, deviceName);
                if ( rancidNode == null ) {
                    groupHasNodesWithoutinventoryAtAll = true;
                    nodesWithoutinventoryAtAll++;
                    continue;
                }
                
                InventoryNode invNode = new InventoryNode(rancidNode);
                boolean found = false;
                
                for (String versionMatch : getVersionListOnDevice(deviceName, groupName)) {


                    invNode = (InventoryNode)rancidNode.getNodeVersions().get(versionMatch);

                    log().debug("calculate:report parsing InventoryNode version[" + invNode.getVersionId() + "] date ["+invNode.getCreationDate()+"]"); 
                    
                    if (tmp_date.compareTo(invNode.getCreationDate()) >  0 ) {
                        found = true;
                        log().debug("calculate:report Date found is ["+invNode.getCreationDate()+"] version is [" + versionMatch + "]");
                        break;
                    }
                }  //end for on version
                if (found == false) {
                    log().debug("calculate: device has no configuration at this date["+deviceName+ "]"); 
                    groupHasNodesWithoutinventoryAtrequestDate = true;
                    nodesWithoutinventoryAtReportDate++;
                    continue;
                }
                //we have groupname devicename and version

                NodeBaseInventory nodeBaseInv = getNodeBaseInventory(deviceName, groupName, invNode.getVersionId());

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

                for (InventoryElement2 ie2 : nodeBaseInv.getIe()) {
                    InventoryElement2RP ie2rp = new InventoryElement2RP();
                    boolean addInventoryElement = false;
                    for (Tuple tuple: ie2.getTupleList()) {
                        if (withKey) {
                            if (Pattern.matches(theField,tuple.getDescription()) || Pattern.matches(theField,tuple.getName())) {
                                includeNbisn = true;
                                addInventoryElement=true;
                            }
                        } else {
                            includeNbisn = true;
                            addInventoryElement=true;
                        }
                        if (tuple.getName().equalsIgnoreCase("name")) {
                            ie2rp.setName(tuple.getDescription());
                        } else {
                            TupleRP trp = new TupleRP();
                            trp.setName(tuple.getName());
                            trp.setDescription(tuple.getDescription());
                            ie2rp.addTupleRP(trp);
                        }
                    }

                    for(InventoryMemory im: ie2.getMemoryList()) {
                        if (withKey) {
                            if (Pattern.matches(theField,"Memory") || Pattern.matches(theField,im.getType())) {
                                includeNbisn = true;
                                addInventoryElement=true;
                            }
                        } else {
                            includeNbisn = true;
                            addInventoryElement=true;
                        }
                        
                        InventoryMemoryRP imrp = new InventoryMemoryRP();
                            imrp.setType(im.getType());
                            imrp.setSize(im.getSize());
                            ie2rp.addInventoryMemoryRP(imrp);
                    }

                    for(InventorySoftware is : ie2.getSoftwareList()) {
                        if (withKey) {
                            if (Pattern.matches(theField,"Software") || Pattern.matches(theField,is.getType()) ||
                                    Pattern.matches(theField, is.getVersion())) {
                                includeNbisn = true;
                                addInventoryElement=true;
                            }
                        } else {
                            includeNbisn = true;
                            addInventoryElement=true;
                        }
                        InventorySoftwareRP isrp = new InventorySoftwareRP();
                        isrp.setType(is.getType());
                        isrp.setVersion(is.getVersion());

                        ie2rp.addInventorySoftwareRP(isrp);
                    }
                    if (addInventoryElement)   
                        ie2rpList.add(ie2rp);

                }
                nbisn.setInventoryElement2RP(ie2rpList);

                if(includeNbisn){
                    nodeMatching++;
                    groupHasDevices=true;
                    gs.addNbisinglenode(nbisn);
                }
            }
            gs.setTotalNodes(totalNodes);
            gs.setNodesMatching(nodeMatching);
            gs.setNodesWithoutinventoryAtReportDate(nodesWithoutinventoryAtReportDate);
            gs.setNodesWithoutinventoryAtAll(nodesWithoutinventoryAtAll);
            rnbi.addGroupSet(gs);
            if (groupHasDevices) groupsMatching++;
            else groupWithoutNodes++;
            if (groupHasDevices && groupHasNodesWithoutinventoryAtAll) 
                groupsWithNodesWithoutinventoryAtAll++;
            if (groupHasDevices &&groupHasNodesWithoutinventoryAtrequestDate) 
                groupsWithNodesWithoutinventoryAtReportDate++;
        } //end for groups
        rnbi.setTotalGroups(totalGroups);
        rnbi.setGroupsMatching(groupsMatching);
        rnbi.setGroupsWithNodesWithoutinventoryAtAll(groupsWithNodesWithoutinventoryAtAll);
        rnbi.setGroupsWithNodesWithoutinventoryAtReportDate(groupsWithNodesWithoutinventoryAtReportDate);
        rnbi.setGroupWithoutNodes(groupWithoutNodes);
    }
    
    public NodeBaseInventory getNodeBaseInventory(String node, String group, String version) {
        // get the latest version from the given date        
        
        log().debug("getNodeBaseInventory " + node +" "+group + " "+version);
        NodeBaseInventory nbi = new NodeBaseInventory();

        
        RancidNode rn;
        try {
            rn = RWSClientApi.getRWSRancidNodeInventory(m_cp, group, node);
        } catch (RancidApiException e) {
            log().debug("getNodeBaseInventory: inventory not found. Skipping");
            return nbi;
        }
        
        InventoryNode in = (InventoryNode)rn.getNodeVersions().get(version);
                
        nbi.setDevicename(node);
        nbi.setGroupname(group);
        nbi.setVersion(version);
        nbi.setStatus(in.getParent().getState());
        nbi.setCreationdate(in.getCreationDate());
        nbi.setSwconfigurationurl(in.getSoftwareImageUrl());
        nbi.setConfigurationurl(in.getConfigurationUrl());
        
        try {
            nbi.setIe( RWSClientApi.getRWSRancidNodeInventoryElement2(m_cp, rn, version));
        } catch (RancidApiException e) {
            log().debug("getNodeBaseInventory: inventory not found for version: " + version + ". Skipping");
        }
        
        return nbi;

    }

    public void writeXML() throws InventoryCalculationException {
        try {
            log().debug("Writing the XML");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            String datestamp = fmt.format(reportRequestDate);
            m_outputFileName = "/NODEINVENTORY" + datestamp + ".xml";


            // Create a file name of type Category-monthFormat-startDate.xml
            log().debug("Report Store XML file: " + m_outputFileName);
            File reportFile = new File(m_baseDir, m_outputFileName);
            // marshal the XML into the file.
            marshal(reportFile);
        } catch (InventoryCalculationException e) {
            log().fatal("Unable to marshal report as XML");
            throw new InventoryCalculationException(e);
        }
    }
    

    public void marshal(File outputFile)
    throws InventoryCalculationException {
        try {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            Marshaller marshaller = new Marshaller(fileWriter);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(rnbi);
            log().debug("The xml marshalled from the castor classes is saved in "
                    + outputFile.getAbsoluteFile());
            fileWriter.close();
        } catch (MarshalException me) {
            log().fatal("MarshalException ", me);
            throw new InventoryCalculationException(me);
        } catch (ValidationException ve) {
            log().fatal("Validation Exception ", ve);
            throw new InventoryCalculationException(ve);
        } catch (IOException ioe) {
            log().fatal("IO Exception ", ioe);
            throw new InventoryCalculationException(ioe);
        }

    }
}
