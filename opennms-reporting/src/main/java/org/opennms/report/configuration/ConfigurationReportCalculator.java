package org.opennms.report.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.InventoryNode;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.opennms.report.configuration.GroupXSet;
import org.opennms.report.configuration.NodeSet;
import org.opennms.report.configuration.RwsRancidlistreport;
import org.springframework.beans.factory.InitializingBean;

public class ConfigurationReportCalculator implements InitializingBean {

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
    Date reportRequestDate;
    
    RwsRancidlistreport rlist;
    
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

    private static Category log() {
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

        rlist = new RwsRancidlistreport();
        rlist.setUser(user);
        rlist.setReportRequestDate(reportRequestDate.toString());

        //parse date
        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
        Date tmp_date = new Date();
        try {
            tmp_date = format.parse(theDate);
        }
        catch (ParseException pe){
            tmp_date = Calendar.getInstance().getTime();
        }
        log().debug("calculate:report date[" + tmp_date.toString() + "]"); 
        rlist.setReportDate(tmp_date.toString());

        int totalGroups = 0;
        int groupsMatching = 0;
        int groupWithoutNodes = 0;
        int groupsWithNodesWithoutconfigurationAtAll = 0;
        int groupsWithNodesWithoutconfigurationAtReportDate = 0;


        for (String groupName : getGroups()) {
            log().debug("calculate:report group [" + groupName + "]"); 
            totalGroups++;
            GroupXSet gs = new GroupXSet();
            gs.setGroupXSetName(groupName);
            int totalNodes = 0;
            int nodeMatching = 0;
            int nodesWithoutConfigurationAtAll=0;
            int nodesWithoutConfigurationAtReportDate=0;
            boolean groupHasDevices = false;
            boolean groupHasNodesWithoutconfigurationAtAll = false;
            boolean groupHasNodesWithoutconfigurationAtrequestDate = false;
            for (String deviceName: getDeviceListOnGroup(groupName)) {
                totalNodes++;
                NodeSet ns = new NodeSet();
                ns.setDevicename(deviceName);
                ns.setGroupname(groupName);
                log().debug("calculate:report device [" + deviceName + "]");
                
                RancidNode rancidNode = getFullNode(groupName, deviceName);
                if ( rancidNode == null ) {
                    ns.setVersion("No Configurations found");
                    groupHasNodesWithoutconfigurationAtAll = true;
                    nodesWithoutConfigurationAtAll++;
                    gs.addNodeSet(ns);
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
                    // skip device
                    log().debug("calculate:report device has no inventory at this date["+deviceName+ "]"); 
                    groupHasNodesWithoutconfigurationAtrequestDate = true;
                    nodesWithoutConfigurationAtReportDate++;
                    ns.setVersion("No configuration found at Report Date");
                } else{
                    ns.setVersion(invNode.getVersionId());
                    ns.setConfigurationurl(invNode.getConfigurationUrl());
                    ns.setSwconfigurationurl(invNode.getSoftwareImageUrl());
                    ns.setStatus(rancidNode.getState());
                    ns.setCreationdate(invNode.getCreationDate().toString());

                    groupHasDevices = true;
                    nodeMatching++;
               }
                gs.addNodeSet(ns);
            } //end for on devices
            
            gs.setTotalNodes(totalNodes);
            gs.setNodesMatching(nodeMatching);
            gs.setNodesWithoutconfigurationAtReportDate(nodesWithoutConfigurationAtReportDate);
            gs.setNodesWithoutconfigurationAtAll(nodesWithoutConfigurationAtAll);
            rlist.addGroupXSet(gs);
            if (groupHasDevices) groupsMatching++;
            else groupWithoutNodes++;
            if (groupHasDevices && groupHasNodesWithoutconfigurationAtAll) 
                groupsWithNodesWithoutconfigurationAtAll++;
            if (groupHasDevices &&groupHasNodesWithoutconfigurationAtrequestDate) 
                groupsWithNodesWithoutconfigurationAtReportDate++;
        } //end for of groups
        rlist.setTotalGroups(totalGroups);
        rlist.setGroupWithoutNodes(groupWithoutNodes);
        rlist.setGroupsMatching(groupsMatching);
        rlist.setGroupsWithNodesWithoutconfigurationAtAll(groupsWithNodesWithoutconfigurationAtAll);
        rlist.setGroupsWithNodesWithoutconfigurationAtReportDate(groupsWithNodesWithoutconfigurationAtReportDate);
    }

    public void writeXML() throws ConfigurationCalculationException {
        try {
            log().debug("Writing the XML");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            String datestamp = fmt.format(reportRequestDate);
            m_outputFileName = "/RANCIDLISTREPORT" + datestamp + ".xml";


            // Create a file name of type Category-monthFormat-startDate.xml
            log().debug("Report Store XML file: " + m_outputFileName);
            File reportFile = new File(m_baseDir, m_outputFileName);
            // marshal the XML into the file.
            marshal(reportFile);
        } catch (ConfigurationCalculationException e) {
            log().fatal("Unable to marshal report as XML");
            throw new ConfigurationCalculationException(e);
        }
    }
    

    public void marshal(File outputFile)
    throws ConfigurationCalculationException {
        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            Marshaller marshaller = new Marshaller(fileWriter);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(rlist);
            log().debug("The xml marshalled from the castor classes is saved in "
                    + outputFile.getAbsoluteFile());
            fileWriter.close();
        } catch (MarshalException me) {
            log().fatal("MarshalException ", me);
            throw new ConfigurationCalculationException(me);
        } catch (ValidationException ve) {
            log().fatal("Validation Exception ", ve);
            throw new ConfigurationCalculationException(ve);
        } catch (IOException ioe) {
            log().fatal("IO Exception ", ioe);
            throw new ConfigurationCalculationException(ioe);
        }
    }


}
