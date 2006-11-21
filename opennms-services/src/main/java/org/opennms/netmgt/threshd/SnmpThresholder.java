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
// Modifications:
//
// 2005 Nov 29: Added a method to allow for labels in Threshold events
// 2005 Jan 03: minor mod to support lame SNMP hosts
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 22: Added a threshold rearm event.
// 2002 Jul 08: Modified code to allow for Threshold-based event notifications.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

package org.opennms.netmgt.threshd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>
 * The SnmpThresholder class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
final class SnmpThresholder implements ServiceThresholder {
    /**
     * SQL statement to retrieve interface's 'ipinterface' table information.
     */
    static final String SQL_GET_NODEID = "SELECT nodeid,ifindex,issnmpprimary FROM ipinterface WHERE ipAddr=? AND ismanaged!='D'";

    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "SNMP";

    /**
     * <P>
     * Returns the name of the service that the plug-in collects ("SNMP").
     * </P>
     * 
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
     * <P>
     * Initialize the service thresholder.
     * </P>
     * 
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    public void initialize(Map parameters) {

        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            log().error("initialize: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
        }

        log().debug("initialize: successfully instantiated JNI interface to RRD...");

    }

    /**
     * Responsible for freeing up any resources held by the thresholder.
     */
    public void release() {
        // Nothing to release...
    }

    /**
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for thresholding.
     * 
     * @param iface
     *            Network interface to be prepped for thresholding.
     * @param parameters
     *            Key/value pairs associated with the package to which the
     *            interface belongs..
     * 
     */
    public void initialize(NetworkInterface netIface, Map parms) {
        
        
        SnmpThresholdInterface snmpIface = SnmpThresholdInterface.get(netIface);
        SnmpThresholdConfiguration thresholdConfiguration = SnmpThresholdConfiguration.get(netIface, parms);
        
        if (!snmpIface.isIPV4())
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");

        // Debug
        //
        if (log().isDebugEnabled()) {
            log().debug("initialize: dumping node thresholds defined for " + snmpIface.getIpAddress() + "/" + thresholdConfiguration.getGroupName() + ":");
            Iterator iter = thresholdConfiguration.getNodeResourceType().getThresholdMap().values().iterator();
            while (iter.hasNext()) {
                log().debug((ThresholdEntity) iter.next());
            }

            log().debug("initialize: dumping interface thresholds defined for " + snmpIface.getIpAddress() + "/" + thresholdConfiguration.getGroupName() + ":");
            iter = thresholdConfiguration.getIfResourceType().getThresholdMap().values().iterator();
            while (iter.hasNext()) {
                log().debug((ThresholdEntity) iter.next());
            }
        }

        if (log().isDebugEnabled())
            log().debug("initialize: initialization completed for " + snmpIface.getIpAddress());
        return;
    }

    /**
     * Responsible for releasing any resources associated with the specified
     * interface.
     * 
     * @param iface
     *            Network interface to be released.
     */
    public void release(NetworkInterface iface) {
        // Nothing to release...
    }

    /**
     * Perform threshold checking.
     * 
     * @param iface
     *            Network interface to be data collected.
     * @param eproxy
     *            Eventy proxy for sending events.
     * @param parameters
     *            Key/value pairs from the package to which the interface
     *            belongs.
     */
    public int check(NetworkInterface netIface, EventProxy eproxy, Map parms) {
        
        SnmpThresholdInterface snmpIface = SnmpThresholdInterface.get(netIface);
        SnmpThresholdConfiguration thresholdConfiguration = SnmpThresholdConfiguration.get(netIface, parms);
        SnmpThresholderState thresholderState = SnmpThresholderState.get(netIface, parms);

        // Get configuration parameters
        //
        if (log().isDebugEnabled())
        	log().debug("check: service= " + SERVICE_NAME + " address= " + snmpIface.getIpAddress() + " thresholding-group=" + thresholdConfiguration.getGroupName() + " interval=" + thresholdConfiguration.getInterval() + "ms range=" + thresholdConfiguration.getRange() + " mS");

        // RRD Repository attribute
        //
        if (log().isDebugEnabled())
            log().debug("check: rrd repository=" + thresholdConfiguration.getRrdRepository());


        // -----------------------------------------------------------
        // 
        // Perform node-level threshold checking
        //
        // -----------------------------------------------------------

        // Get File object representing the node directory
        File nodeDirectory = new File(thresholdConfiguration.getRrdRepository(), snmpIface.getNodeId().toString());
        if (!RrdFileConstants.isValidRRDNodeDir(nodeDirectory)) {
            log().info("Node directory for " + snmpIface.getNodeId() + "/" + snmpIface.getIpAddress() + " does not exist or is not a valid RRD node directory.");
            log().info("Threshold checking failed for primary SNMP interface " + snmpIface.getIpAddress());
            return THRESHOLDING_FAILED;
        }

        // Create empty Events object to hold any threshold
        // events generated during the thresholding check...
        Events events = new Events();

        // Date stamp for all outgoing events
        Date dateStamp = new Date();

        try {
        	checkNodeDir(nodeDirectory, snmpIface, thresholdConfiguration, dateStamp, events);
        } catch (IllegalArgumentException e) {
            log().info("check: Threshold checking failed for primary SNMP interface " + snmpIface.getIpAddress(), e);
            return THRESHOLDING_FAILED;
        }

        // -----------------------------------------------------------
        // 
        // Perform interface-level threshold checking
        //
        // -----------------------------------------------------------

        // Iterate over node directory contents and call
        // checkInterfaceDirectory() for any/all RRD interface
        // directories.
        //
        File[] files = nodeDirectory.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    // Found interface directory...
                    checkIfDir(files[i], snmpIface, thresholdConfiguration, dateStamp, thresholderState.getAllInterfaceMap(), events);
                } catch (IllegalArgumentException e) {
                    log().info("check: Threshold checking failed for primary SNMP interface " + snmpIface.getIpAddress(), e);
                    return THRESHOLDING_FAILED;
                }
            }
        }

        // Send created events
        //
        if (events.getEventCount() > 0) {
            try {
                Log eventLog = new Log();
                eventLog.setEvents(events);
                eproxy.send(eventLog);
            } catch (EventProxyException e) {
                log().info("check: Failed sending threshold events via event proxy...", e);
                return THRESHOLDING_FAILED;
            }
        }

        // return the status of the threshold check
        //
        return THRESHOLDING_SUCCEEDED;
    }

    /**
     * Performs threshold checking on an SNMP RRD node directory.
     * 
     * @param directory
     *            RRD repository directory
     * @param snmpIface TODO
     * @param thresholdConfiguration TODO
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * @param interval
     *            Configured thresholding interval
     * @param range
     *            Time interval before last possible PDP is considered
     *            "out of date"
     * @param thresholdMap
     *            Map of node level ThresholdEntity objects keyed by datasource
     *            name.
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary SNMP interface address
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     */
    private void checkNodeDir(File directory, SnmpThresholdInterface snmpIface, SnmpThresholdConfiguration thresholdConfiguration, Date date, Events events) throws IllegalArgumentException {
        ThresholdResourceType resourceType = thresholdConfiguration.getNodeResourceType();
        Map<String, ThresholdEntity> thresholdMap = resourceType.getThresholdMap();
        // Sanity Check
        if (directory == null || snmpIface.getNodeId() == null || snmpIface.getInetAddress() == null || date == null || thresholdMap == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log().isDebugEnabled())
            log().debug("checkNodeDir: threshold checking node dir: " + directory.getAbsolutePath());

        // Iterate over directory contents and threshold
        // check any RRD files which represent datasources
        // in the threshold maps.
        //
        File[] files = directory.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);

        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {
            // File name has format: <datsource><extension>
            // Must strip off <extension> portion.
            String filename = files[i].getName();
            String datasource = filename.substring(0, filename.indexOf(RrdUtils.getExtension()));

            // Lookup the ThresholdEntity object corresponding
            // to this datasource.
            //
            ThresholdEntity threshold = (ThresholdEntity) thresholdMap.get(datasource);
            if (threshold != null) {

                // Get the value to use for the ds-label from this threshold
                String dsLabelValue = "Unknown";
                String propertiesFile = directory + "/strings.properties";
                Properties stringProps = new Properties();
                try {
                        stringProps.load(new FileInputStream(propertiesFile));
                        dsLabelValue = stringProps.getProperty(threshold.getDatasourceLabel());
                } catch (FileNotFoundException e) {
                        log().debug ("Label: No strings.properties file found for node id: " + snmpIface.getNodeId() + " looking here: " + propertiesFile);
                } catch (NullPointerException e) {
                        log().debug ("Label: No data source label for node id: " + snmpIface.getNodeId() );
                } catch (java.io.IOException e) {
                        log().debug ("Label: I/O exception when looking for strings.properties file for node id: "+ snmpIface.getNodeId() + " looking here: " + propertiesFile);
                }


                // Use RRD JNI interface to "fetch" value of the
                // datasource from the RRD file
                //
                Double dsValue = null;
		
                try {
                	if (thresholdConfiguration.getRange() != 0) {
                		if (log().isDebugEnabled())
                            log().debug("checking values within " + thresholdConfiguration.getRange() + " mS of last possible PDP");
                		dsValue = RrdUtils.fetchLastValueInRange(files[i].getAbsolutePath(), thresholdConfiguration.getInterval(), thresholdConfiguration.getRange());
                	} else {
                		if (log().isDebugEnabled())
                            log().debug("checking value of last possible PDP only");
                		dsValue = RrdUtils.fetchLastValue(files[i].getAbsolutePath(), thresholdConfiguration.getInterval());
                	}
                } catch (NumberFormatException nfe) {
                    log().warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double, skipping evaluation.");
                } catch (RrdException e) {
                    log().info("An error occurred retriving the last value for datasource '" + datasource + "'", e);
                }

                if (dsValue != null && !dsValue.isNaN()) {
                    // Evaluate the threshold
                    // 
                    // ThresholdEntity.evaluate() returns an integer value
                    // which indicates which threshold values were
                    // triggered and require an event to be generated (if any).
                    // 
                    int result = threshold.evaluate(dsValue.doubleValue());
                    if (result != ThresholdEntity.NONE_TRIGGERED) {
                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.HIGH_TRIGGERED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), null, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_EVENT_UEI, date, dsLabelValue));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.LOW_TRIGGERED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), null, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_EVENT_UEI, date, dsLabelValue));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.HIGH_REARMED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), null, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, date, dsLabelValue));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.LOW_REARMED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), null, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, date, dsLabelValue));
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs threshold checking on an SNMP RRD interface directory.
     * 
     * @param directory
     *            RRD repository directory
     * @param snmpIface TODO
     * @param thresholdConfiguration TODO
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param allIfThresholdMap
     *            Map of threshold maps indexed by ifLabel
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary SNMP interface address
     * @param interval
     *            Configured thresholding interval
     * @param range
     *            Time interval before last possible PDP is considered
     *            "out of date"
     * @param baseIfThresholdMap
     *            Map of configured interface level ThresholdEntity objects
     *            keyed by datasource name.
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     */
    private void checkIfDir(File directory, SnmpThresholdInterface snmpIface, SnmpThresholdConfiguration thresholdConfiguration, Date date, Map allIfThresholdMap, Events events) throws IllegalArgumentException {
        ThresholdResourceType resourceType = thresholdConfiguration.getIfResourceType();
        // Sanity Check
        if (directory == null || snmpIface.getNodeId() == null || snmpIface.getInetAddress() == null || date == null || resourceType.getThresholdMap() == null || allIfThresholdMap == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log().isDebugEnabled())
            log().debug("checkIfDir: threshold checking interface dir: " + directory.getAbsolutePath());

        String ifLabel = directory.getName();
        if (log().isDebugEnabled())
            log().debug("checkIfDir: ifLabel=" + ifLabel);

        // This is an interface directory extract the
        // interface label from the full path name of the file
        //
        /*
         * String path = directory.getAbsolutePath(); String path = directory
         * int fileSepIndex = path.lastIndexOf(File.separatorChar); if
         * (fileSepIndex >= 0) ifLabel = path.substring(fileSepIndex+1,
         * path.length()); else ifLabel = path;
         */

        // Attempt to retrieve the threshold map for this interface
        // using the ifLabel for the interface
        // 
        Map<String, ThresholdEntity> thresholdMap = (Map<String, ThresholdEntity>) allIfThresholdMap.get(ifLabel);
        if (thresholdMap == null) {
            // Doesn't exist yet, go ahead and create it
            // Must maintain a separate threshold map for
            // each interface.
            thresholdMap = getAttributeMap(resourceType);

            // Add the new threshold map for this interface
            // to the all interfaces map.
            allIfThresholdMap.put(ifLabel, thresholdMap);
        }

        // Iterate over directory contents and threshold
        // check any RRD files which represent datasources
        // in the threshold maps.
        //
        File[] files = directory.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);

        if (files == null || files.length == 0) {
            if (log().isDebugEnabled())
                log().debug("checkIfDir: no RRD files in dir: " + directory);
            return;
        }

        Map ifDataMap = null;
        for (int i = 0; i < files.length; i++) {
            // File name has format: <datsource><extension>
            // Must strip off <extension> portion.
            String filename = files[i].getName();
            String datasource = filename.substring(0, filename.indexOf(RrdUtils.getExtension()));

            // Lookup the ThresholdEntity object corresponding
            // to this datasource.
            //
            if (log().isDebugEnabled())
                log().debug("checkIfDir: looking up datasource: " + datasource);
            ThresholdEntity threshold = (ThresholdEntity) thresholdMap.get(datasource);
            if (threshold != null) {

		String dsLabelValue = "Unknown";

                // Use RRD JNI interface to "fetch" value of the
                // datasource from the RRD file
                //
                Double dsValue = null;
                try {
                	if (thresholdConfiguration.getRange() != 0) {
                		if (log().isDebugEnabled())
                            log().debug("checking values within " + thresholdConfiguration.getRange() + " mS of last possible PDP");
                		dsValue = RrdUtils.fetchLastValueInRange(files[i].getAbsolutePath(), thresholdConfiguration.getInterval(), thresholdConfiguration.getRange());
                	} else {
                		if (log().isDebugEnabled())
                            log().debug("checking value of last possible PDP only");
                		dsValue = RrdUtils.fetchLastValue(files[i].getAbsolutePath(), thresholdConfiguration.getInterval());
                	}
                } catch (NumberFormatException nfe) {
                    log().warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double, skipping evaluation.");
                } catch (RrdException e) {
                    log().info("An error occurred retriving the last value for datasource '" + datasource + "'", e);
                }

                if (dsValue != null && !dsValue.isNaN()) {
                    // Evaluate the threshold
                    // 
                    // ThresholdEntity.evaluate() returns an integer value
                    // which indicates which threshold values were
                    // triggered and require an event to be generated (if any).
                    // 
                    int result = threshold.evaluate(dsValue.doubleValue());
                    if (result != ThresholdEntity.NONE_TRIGGERED) {
                        // ifLabel will either be set to null for node level
                        // datasource values
                        // or to a specific interface in the case of an
                        // interface level datasource.
                        //
                        // ifLabel has the following format:
                        // <ifName|ifDescr>-<macAddr>
                        // 
                        // Call IfLabel.getInterfaceInfoFromLabel() utility
                        // method to retrieve
                        // data from the 'snmpInterfaces' table for this
                        // interface. This method
                        // will return a Map of database values keyed by field
                        // name.
                        //
                        if (ifLabel != null && ifDataMap == null) {
                            // Get database connection
                            //
                            java.sql.Connection dbConn = null;
                            try {
                                dbConn = DataSourceFactory.getInstance().getConnection();
                            } catch (SQLException sqlE) {
                                log().error("checkIfDir: Failed getting connection to the database.", sqlE);
                                throw new UndeclaredThrowableException(sqlE);
                            }

                            // Make certain we close the connection
                            //
                            try {
                                ifDataMap = IfLabel.getInterfaceInfoFromIfLabel(dbConn, snmpIface.getNodeId().intValue(), ifLabel);
                            } catch (SQLException e) {
                                // Logging a warning message but processing will
                                // continue for
                                // this thresholding event, when the event is
                                // created it
                                // will be created with an interface value set
                                // to the primary
                                // SNMP interface address and an event source
                                // set to
                                // <datasource>:<ifLabel>.
                                //
                                log().warn("Failed to retrieve interface info from database using ifLabel '" + ifLabel);
                                ifDataMap = new HashMap();
                            } finally {
                                // Done with the database so close the
                                // connection
                                try {
                                    if (dbConn != null)
                                        dbConn.close();
                                } catch (SQLException sqle) {
                                    log().info("checkIfDir: SQLException while closing database connection", sqle);
                                }
                            }
                            // Adding ifLabel value to the map for potential use
                            // by
                            // the createEvent() method
                            ifDataMap.put("iflabel", ifLabel);

                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.HIGH_TRIGGERED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), ifDataMap, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_EVENT_UEI, date, dsLabelValue));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.LOW_TRIGGERED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), ifDataMap, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_EVENT_UEI, date, dsLabelValue));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.HIGH_REARMED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), ifDataMap, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, date, dsLabelValue));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.LOW_REARMED) {
                            events.addEvent(createEvent(snmpIface.getNodeId(), snmpIface.getInetAddress(), ifDataMap, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, date, dsLabelValue));
                        }
                    }
                }
            }
        }
    }

    private Map<String, ThresholdEntity> getAttributeMap(ThresholdResourceType resourceType) {
        Map<String, ThresholdEntity> thresholdMap;
        thresholdMap = new HashMap<String, ThresholdEntity>();

        // Iterate over base interface threshold map and clone each
        // ThresholdEntity object and add it to the threshold map.
        // for this interface.
        // 
        Iterator iter = resourceType.getThresholdMap().values().iterator();
        while (iter.hasNext()) {
            ThresholdEntity entity = (ThresholdEntity) iter.next();
            thresholdMap.put(entity.getDatasourceName(), (ThresholdEntity)entity.clone());
        }
        return thresholdMap;
    }

    /**
     * Creates a new threshold event from the specified parms.
     * 
     * @param nodeId
     *            node identifier of the affected node
     * @param primary
     *            IP address of the affected primary SNMP interface
     * @param ifDataMap
     *            Map of this node's interface information
     * @param dsValue
     *            Data source value which triggered the threshold event
     * @param threshold
     *            Configured threshold
     * @param uei
     *            Event identifier
     * @param date
     *            source of event's timestamp
     * 
     * @return new threshold event to be sent to Eventd
     */
    private Event createEvent(Integer nodeId, InetAddress primary, Map ifDataMap, double dsValue, Threshold threshold, String uei, java.util.Date date, String label) {
        if (nodeId == null || primary == null || threshold == null)
            throw new IllegalArgumentException("nodeid, primary, and threshold cannot be null.");

        if (log().isDebugEnabled()) {
            log().debug("createEvent: nodeId=" + nodeId + " primaryAddr=" + primary + " ds=" + threshold.getDsName() + " uei=" + uei);

            if (ifDataMap != null) {
                log().debug("createEvent: specific interface data:" + " ifAddr=" + (String) ifDataMap.get("ipaddr") + " macAddr=" + (String) ifDataMap.get("snmpphysaddr") + " ifName=" + (String) ifDataMap.get("snmpifname") + " ifDescr=" + (String) ifDataMap.get("snmpifdescr") + " ifIndex=" + (String) ifDataMap.get("snmpifindex") + " ifLabel=" + (String) ifDataMap.get("iflabel"));
            }
        }

        // create the event to be sent
        Event newEvent = new Event();
        newEvent.setUei(uei);
        newEvent.setNodeid(nodeId.longValue());
        newEvent.setService(this.serviceName());

        // set the source of the event to the datasource name
        newEvent.setSource("OpenNMS.Threshd." + threshold.getDsName());

        // Set event interface
        //
        if (ifDataMap == null || ifDataMap.get("ipaddr") == null) {
            // Node level datasource
            //
            if (primary != null)
                newEvent.setInterface(primary.getHostAddress());
        } else {
            // Interface level datasource
            // 
            // NOTE: Non-IP interfaces will have an
            // address of "0.0.0.0".
            //
            String ifAddr = (String) ifDataMap.get("ipaddr");
            newEvent.setInterface(ifAddr);
        }

        // Set event host
        //
        try {
            newEvent.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhE) {
            newEvent.setHost("unresolved.host");
            log().warn("Failed to resolve local hostname", uhE);
        }

        // Set event time
        newEvent.setTime(EventConstants.formatToString(date));

        // Add appropriate parms
        //
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        // Add datasource name
        eventParm = new Parm();
        eventParm.setParmName("ds");
        parmValue = new Value();
        parmValue.setContent(threshold.getDsName());
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add last known value of the datasource
        // fetched from its RRD file
        //
        eventParm = new Parm();
        eventParm.setParmName("value");
        parmValue = new Value();
        parmValue.setContent(Double.toString(dsValue));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add configured threshold value
        eventParm = new Parm();
        eventParm.setParmName("threshold");
        parmValue = new Value();
        parmValue.setContent(Double.toString(threshold.getValue()));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add configured trigger value
        eventParm = new Parm();
        eventParm.setParmName("trigger");
        parmValue = new Value();
        parmValue.setContent(Integer.toString(threshold.getTrigger()));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add configured rearm value
        eventParm = new Parm();
        eventParm.setParmName("rearm");
        parmValue = new Value();
        parmValue.setContent(Double.toString(threshold.getRearm()));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add datasource label
	if (label != null) {
        	eventParm = new Parm();
        	eventParm.setParmName("label");
        	parmValue = new Value();
        	parmValue.setContent(label);
        	eventParm.setValue(parmValue);
        	eventParms.addParm(eventParm);
	}

        // Add interface parms if available
        if (ifDataMap != null && ifDataMap.get("iflabel") != null) {
            // Add ifLabel
            eventParm = new Parm();
            eventParm.setParmName("ifLabel");
            parmValue = new Value();
            parmValue.setContent((String) ifDataMap.get("iflabel"));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
        }

        if (ifDataMap != null && ifDataMap.get("snmpifindex") != null) {
            // Add ifIndex
            eventParm = new Parm();
            eventParm.setParmName("ifIndex");
            parmValue = new Value();
            parmValue.setContent((String) ifDataMap.get("snmpifindex"));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
        }

        // Add Parms to the event
        newEvent.setParms(eventParms);

        return newEvent;
    }

	Category log() {
	    return ThreadCategory.getInstance(getClass());
	}
}
