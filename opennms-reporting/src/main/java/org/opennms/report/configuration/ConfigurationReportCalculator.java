/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.report.configuration;

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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.InventoryNode;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>ConfigurationReportCalculator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ConfigurationReportCalculator implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationReportCalculator.class);


    String m_baseDir;
    // output file name

    private String m_outputFileName;

    ConnectionProperties m_cp;
    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputFileName() {
        return m_outputFileName;
    }

    /**
     * <p>setOutputFileName</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     */
    public void setOutputFileName(String outputFileName) {
        m_outputFileName = outputFileName;
    }

    RWSConfig m_rwsConfig;
    
    String theDate;
    String user;
    Date reportRequestDate;
    
    RwsRancidlistreport rlist;
    
    /**
     * <p>Getter for the field <code>theDate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTheDate() {
        return theDate;
    }

    /**
     * <p>Setter for the field <code>theDate</code>.</p>
     *
     * @param theDate a {@link java.lang.String} object.
     */
    public void setTheDate(String theDate) {
        this.theDate = theDate;
    }

    /**
     * <p>Getter for the field <code>user</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return user;
    }

    /**
     * <p>Setter for the field <code>user</code>.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * <p>Getter for the field <code>reportRequestDate</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getReportRequestDate() {
        return reportRequestDate;
    }

    /**
     * <p>Setter for the field <code>reportRequestDate</code>.</p>
     *
     * @param reportRequestDate a {@link java.util.Date} object.
     */
    public void setReportRequestDate(Date reportRequestDate) {
        this.reportRequestDate = reportRequestDate;
    }

    /**
     * <p>getRwsConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.RWSConfig} object.
     */
    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }

    /**
     * <p>setRwsConfig</p>
     *
     * @param rwsConfig a {@link org.opennms.netmgt.config.RWSConfig} object.
     */
    public void setRwsConfig(RWSConfig rwsConfig) {
        m_rwsConfig = rwsConfig;
    }

    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBaseDir() {
        return m_baseDir;
    }

    /**
     * <p>setBaseDir</p>
     *
     * @param baseDir a {@link java.lang.String} object.
     */
    public void setBaseDir(String baseDir) {
        m_baseDir = baseDir;
    }


    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        RWSClientApi.init();
        m_cp = m_rwsConfig.getBase();
    }

    private List<String> getGroups() {
        
        try {
            return RWSClientApi.getRWSResourceGroupsList(m_cp).getResource();
        } catch (RancidApiException e) {
            LOG.error("getGroups: has given exception {}. Skipped", e.getMessage());
        }
        return new ArrayList<String>();
    }
    
    private List<String> getDeviceListOnGroup(String groupName) {
        try {
            return RWSClientApi.getRWSResourceDeviceList(m_cp, groupName).getResource();
        } catch (RancidApiException e) {
            LOG.error("getDeviceListOnGroup: group [{}]. Skipped", groupName); 
        }
        return new ArrayList<String>();
    }
    
    private List<String> getVersionListOnDevice(String deviceName, String groupName) {
        try {
            return RWSClientApi.getRWSResourceConfigList(m_cp, groupName, deviceName).getResource();
        } catch (RancidApiException e) {
            LOG.error("getVersionListOnDevice:  device has no inventory [{}]. {}", deviceName, e.getLocalizedMessage()); 
        }

        return new ArrayList<String>();
    }
    
    private RancidNode getFullNode(String groupName, String deviceName) {
        try {
            return RWSClientApi.getRWSRancidNodeInventory(m_cp ,groupName, deviceName);
        } catch (RancidApiException e) {
            LOG.error("getFullNode:  device has no inventory [{}]. {}", deviceName, e.getLocalizedMessage()); 
        }
        return null;
    }
    
    /**
     * <p>calculate</p>
     */
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
        LOG.debug("calculate:report date[{}]", tmp_date.toString()); 
        rlist.setReportDate(tmp_date.toString());

        int totalGroups = 0;
        int groupsMatching = 0;
        int groupWithoutNodes = 0;
        int groupsWithNodesWithoutconfigurationAtAll = 0;
        int groupsWithNodesWithoutconfigurationAtReportDate = 0;


        for (String groupName : getGroups()) {
            LOG.debug("calculate:report group [{}]", groupName); 
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
                LOG.debug("calculate:report device [{}]", deviceName);
                
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

                    LOG.debug("calculate:report parsing InventoryNode version[{}] date [{}]", invNode.getVersionId(), invNode.getCreationDate()); 
                    
                    if (tmp_date.compareTo(invNode.getCreationDate()) >  0 ) {
                        found = true;
                        LOG.debug("calculate:report Date found is [{}] version is [{}]", invNode.getCreationDate(), versionMatch);
                        break;
                    }
                }  //end for on version
                if (found == false) {
                    // skip device
                    LOG.debug("calculate:report device has no inventory at this date[{}]", deviceName); 
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

    /**
     * <p>writeXML</p>
     *
     * @throws org.opennms.report.configuration.ConfigurationCalculationException if any.
     */
    public void writeXML() throws ConfigurationCalculationException {
        try {
            LOG.debug("Writing the XML");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            String datestamp = fmt.format(reportRequestDate);
            m_outputFileName = "/RANCIDLISTREPORT" + datestamp + ".xml";


            // Create a file name of type Category-monthFormat-startDate.xml
            LOG.debug("Report Store XML file: {}", m_outputFileName);
            File reportFile = new File(m_baseDir, m_outputFileName);
            // marshal the XML into the file.
            marshal(reportFile);
        } catch (ConfigurationCalculationException e) {
            LOG.error("Unable to marshal report as XML");
            throw new ConfigurationCalculationException(e);
        }
    }
    

    /**
     * <p>marshal</p>
     *
     * @param outputFile a {@link java.io.File} object.
     * @throws org.opennms.report.configuration.ConfigurationCalculationException if any.
     */
    public void marshal(File outputFile)
    throws ConfigurationCalculationException {
        try {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            Marshaller marshaller = new Marshaller(fileWriter);
            marshaller.setSuppressNamespaces(true);
            marshaller.marshal(rlist);
            LOG.debug("The xml marshalled from the castor classes is saved in {}", outputFile.getAbsoluteFile());
            fileWriter.close();
        } catch (MarshalException me) {
            LOG.error("MarshalException ", me);
            throw new ConfigurationCalculationException(me);
        } catch (ValidationException ve) {
            LOG.error("Validation Exception ", ve);
            throw new ConfigurationCalculationException(ve);
        } catch (IOException ioe) {
            LOG.error("IO Exception ", ioe);
            throw new ConfigurationCalculationException(ioe);
        }
    }


}
