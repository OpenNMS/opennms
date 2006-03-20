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
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.poller.monitors.NetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.protocols.snmp.SnmpSession;

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
    private static final String SQL_GET_NODEID = "SELECT nodeid,ifindex,issnmpprimary FROM ipinterface WHERE ipAddr=? AND ismanaged!='D'";

    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "SNMP";

    /**
     * Default thresholding interval (in milliseconds).
     * 
     */
    private static final int DEFAULT_INTERVAL = 300000; // 300s or 5m
    
    /**
     * Default age before which a data point is considered "out of date"
     */
    
    private static final int DEFAULT_RANGE = 0; // 300s or 5m
    

    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String NODE_ID_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.NodeId";

    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String RRD_REPOSITORY_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.RrdRepository";

    /**
     * Interface attribute key used to store a map of node level ThresholdEntity
     * objects keyed by datasource name.
     */
    static final String NODE_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.NodeThresholdMap";

    /**
     * Interface attribute key used to store a map of interface level
     * ThresholdEntity objects keyed by datasource name.
     */
    static final String BASE_IF_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.IfThresholdMap";

    /**
     * We must maintain a map of interface level ThresholdEntity objects on a
     * per interface basis in order to maintain separate exceeded counts and the
     * like for each of a node's interfaces. This interface attribute key used
     * to store a map of interface level ThresholdEntity object maps keyed by
     * ifLabel. So it wil refer to a map of maps indexed by ifLabel.
     */
    static final String ALL_IF_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.AllIfThresholdMap";

    /**
     * Local host name
     */
    private String m_host;

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
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        // Get local host name (used when generating threshold events)
        //
        try {
            m_host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            if (log.isEnabledFor(Priority.WARN))
                log.warn("initialize: Unable to resolve local host name.", e);
            m_host = "unresolved.host";
        }

        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("initialize: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
        }

        if (log.isDebugEnabled())
            log.debug("initialize: successfully instantiated JNI interface to RRD...");

        return;
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
    public void initialize(NetworkInterface iface, Map parameters) {
        Category log = ThreadCategory.getInstance(getClass());

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");

        InetAddress ipAddr = (InetAddress) iface.getAddress();

        // Retrieve the name of the thresholding group associated
        // with this interface.
        String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", "default");

        // Get the threshold group's RRD repository path
        // 
        String repository = null;
        try {
            repository = ThresholdingConfigFactory.getInstance().getRrdRepository(groupName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
        }

        // Add RRD repository as an attribute of the interface for retrieval
        // by the check() method.
        //
        iface.setAttribute(RRD_REPOSITORY_KEY, repository);

        // Retrieve the collection of Threshold objects associated with
        // the defined thresholding group and build two maps, one consisting
        // of node level ThresholdEntity objects and another consisting of
        // interface level ThresholdEntity objects both keyed by datasource
        // name.
        //
        // Each ThresholdEntity can wrap one high Threshold and one low
        // Threshold castor-generated object for a single datasource.
        // If more than one high or more than one low threshold is defined
        // for a single datasource a warning messages is generated. Only
        // the first threshold in such a scenario will be used for thresholding.
        //
        Map nodeMap = new HashMap();
        Map baseIfMap = new HashMap();
        try {
            Iterator iter = ThresholdingConfigFactory.getInstance().getThresholds(groupName).iterator();
            while (iter.hasNext()) {
                Threshold thresh = (Threshold) iter.next();

                // See if map entry already exists for this datasource
                // If not, create a new one.
                boolean newEntity = false;
                ThresholdEntity thresholdEntity = null;
                if (thresh.getDsType().equals("node")) {
                    thresholdEntity = (ThresholdEntity) nodeMap.get(thresh.getDsName());
                } else if (thresh.getDsType().equals("if")) {
                    thresholdEntity = (ThresholdEntity) baseIfMap.get(thresh.getDsName());
                }

                // Found entry?
                if (thresholdEntity == null) {
                    // Nope, create a new one
                    newEntity = true;
                    thresholdEntity = new ThresholdEntity();
                }

                try {
                    // Set high/low threshold
                    if (thresh.getType().equals(ThresholdEntity.HIGH_THRESHOLD))
                        thresholdEntity.setHighThreshold(thresh);
                    else if (thresh.getType().equals(ThresholdEntity.LOW_THRESHOLD))
                        thresholdEntity.setLowThreshold(thresh);
                } catch (IllegalStateException e) {
                    log.warn("Encountered duplicate " + thresh.getType() + " for datasource " + thresh.getDsName(), e);
                }

                // Add new entity to the map
                if (newEntity) {
                    if (thresh.getDsType().equals("node"))
                        nodeMap.put(thresh.getDsName(), thresholdEntity);
                    else if (thresh.getDsType().equals("if"))
                        baseIfMap.put(thresh.getDsName(), thresholdEntity);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
        }

        // Add node and interface thresholding maps as attributes of the
        // interface
        // for retrieval by the check() method.
        //
        iface.setAttribute(NODE_THRESHOLD_MAP_KEY, nodeMap);
        iface.setAttribute(BASE_IF_THRESHOLD_MAP_KEY, baseIfMap);

        // Now create an empty map which will hold interface level
        // ThresholdEntity objects for each of the node's interfaces.
        // This map will be keyed by the interface's iflabel and will
        // contain as a value a map of ThresholdEntity objects keyed
        // by datasource name.
        //
        iface.setAttribute(ALL_IF_THRESHOLD_MAP_KEY, new HashMap());

        // Get database connection in order to retrieve the nodeid and
        // ifIndex from the database for this interface.
        //
        java.sql.Connection dbConn = null;
        try {
            dbConn = DatabaseConnectionFactory.getInstance().getConnection();
        } catch (SQLException sqlE) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("initialize: Failed getting connection to the database.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        }

        int nodeId = -1;
        int primaryIfIndex = -1;
        char isSnmpPrimary = DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE;

        // All database calls wrapped in try/finally block so we make
        // certain that the connection will be closed when we are
        // finished.
        //
        try {
            // Prepare & execute the SQL statement to get the 'nodeid',
            // 'ifIndex' and 'isSnmpPrimary' fields from the ipInterface table.
            //
            PreparedStatement stmt = null;
            try {
                stmt = dbConn.prepareStatement(SQL_GET_NODEID);
                stmt.setString(1, ipAddr.getHostAddress()); // interface address
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nodeId = rs.getInt(1);
                    if (rs.wasNull())
                        nodeId = -1;
                    primaryIfIndex = rs.getInt(2);
                    if (rs.wasNull())
                        primaryIfIndex = -1;
                    String str = rs.getString(3);
                    if (str != null)
                        isSnmpPrimary = str.charAt(0);
                }
                rs.close();
            } catch (SQLException sqle) {
                if (log.isDebugEnabled())
                    log.debug("initialize: SQL exception!!", sqle);
                throw new RuntimeException("SQL exception while attempting to retrieve node id for interface " + ipAddr.getHostAddress());
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // Ignore
                }
            }

            if (log.isDebugEnabled())
                log.debug("initialize: db retrieval info: nodeid = " + nodeId + ", address = " + ipAddr.getHostAddress() + ", ifIndex = " + primaryIfIndex + ", isSnmpPrimary = " + isSnmpPrimary);

            // RuntimeException is thrown if any of the following are true:
            // - node id is invalid
            // - primaryIfIndex is invalid
            // - Interface is not the primary SNMP interface for the node
            //
            if (nodeId == -1)
                throw new RuntimeException("Unable to retrieve node id for interface " + ipAddr.getHostAddress());

            if (primaryIfIndex == -1)
                // allow this for nodes without ipAddrTables
                // throw new RuntimeException("Unable to retrieve ifIndex for interface " + ipAddr.getHostAddress());
                if (log.isDebugEnabled())
                    log.debug("initialize: db retrieval info: node " + nodeId + " does not have a legitimate primaryIfIndex. Assume node does not supply ipAddrTable and continue...");

            if (isSnmpPrimary != DbIpInterfaceEntry.SNMP_PRIMARY)
                throw new RuntimeException("Interface " + ipAddr.getHostAddress() + " is not the primary SNMP interface for nodeid " + nodeId);
        } finally {
            // Done with the database so close the connection
            try {
                dbConn.close();
            } catch (SQLException sqle) {
                if (log.isEnabledFor(Priority.INFO))
                    log.info("initialize: SQLException while closing database connection", sqle);
            }
        }

        // Add nodeId as an attribute of the interface for retrieval
        // by the check() method.
        //
        iface.setAttribute(NODE_ID_KEY, new Integer(nodeId));

        // Debug
        //
        if (log.isDebugEnabled()) {
            log.debug("initialize: dumping node thresholds defined for " + ipAddr.getHostAddress() + "/" + groupName + ":");
            Iterator iter = nodeMap.values().iterator();
            while (iter.hasNext()) {
                log.debug((ThresholdEntity) iter.next());
            }

            log.debug("initialize: dumping interface thresholds defined for " + ipAddr.getHostAddress() + "/" + groupName + ":");
            iter = baseIfMap.values().iterator();
            while (iter.hasNext()) {
                log.debug((ThresholdEntity) iter.next());
            }
        }

        if (log.isDebugEnabled())
            log.debug("initialize: initialization completed for " + ipAddr.getHostAddress());
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
    public int check(NetworkInterface iface, EventProxy eproxy, Map parameters) {
        Category log = ThreadCategory.getInstance(getClass());

        int thresholdingStatus = THRESHOLDING_UNKNOWN;
        InetAddress primary = (InetAddress) iface.getAddress();
        SnmpSession session = null;

        // Get configuration parameters
        //
        String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", "default");
        int interval = ParameterMap.getKeyedInteger(parameters, "interval", DEFAULT_INTERVAL);
        int range = ParameterMap.getKeyedInteger(parameters, "range", DEFAULT_RANGE);
        
        if (log.isDebugEnabled())
            log.debug("check: service= " + SERVICE_NAME + " address= " + primary.getHostAddress() + " thresholding-group=" + groupName + " interval=" + interval + "ms range=" + range + " mS");

        // RRD Repository attribute
        //
        String repository = (String) iface.getAttribute(RRD_REPOSITORY_KEY);
        if (log.isDebugEnabled())
            log.debug("check: rrd repository=" + repository);

        // Nodeid attribute
        //
        Integer nodeId = (Integer) iface.getAttribute(NODE_ID_KEY);

        // node and interface ThresholdEntity map attributes
        //
        Map nodeMap = (Map) iface.getAttribute(NODE_THRESHOLD_MAP_KEY);
        Map baseIfMap = (Map) iface.getAttribute(BASE_IF_THRESHOLD_MAP_KEY);
        Map allIfMap = (Map) iface.getAttribute(ALL_IF_THRESHOLD_MAP_KEY);

        // -----------------------------------------------------------
        // 
        // Perform node-level threshold checking
        //
        // -----------------------------------------------------------

        // Get File object representing the node directory
        File nodeDirectory = new File(repository + File.separator + nodeId.toString());
        if (!RrdFileConstants.isValidRRDNodeDir(nodeDirectory)) {
            log.error("Node directory for " + nodeId + "/" + primary.getHostAddress() + " does not exist or is not a valid RRD node directory.");
            log.error("Threshold checking failed for primary SNMP interface " + primary.getHostAddress());
            return THRESHOLDING_FAILED;
        }

        // Create empty Events object to hold any threshold
        // events generated during the thresholding check...
        Events events = new Events();

        // Date stamp for all outgoing events
        Date dateStamp = new Date();

        try {
            checkNodeDir(nodeDirectory, nodeId, primary, interval, range, dateStamp, nodeMap, events);
        } catch (IllegalArgumentException e) {
            log.error("check: Threshold checking failed for primary SNMP interface " + primary.getHostAddress(), e);
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
                    checkIfDir(files[i], nodeId, primary, interval, dateStamp, baseIfMap, allIfMap, events);
                } catch (IllegalArgumentException e) {
                    log.error("check: Threshold checking failed for primary SNMP interface " + primary.getHostAddress(), e);
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
                log.error("check: Failed sending threshold events via event proxy...", e);
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
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary SNMP interface address
     * @param interval
     *            Configured thresholding interval
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param thresholdMap
     *            Map of node level ThresholdEntity objects keyed by datasource
     *            name.
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * 
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     */
    private void checkNodeDir(File directory, Integer nodeId, InetAddress primary, int interval, int range, Date date, Map thresholdMap, Events events) throws IllegalArgumentException {
        Category log = ThreadCategory.getInstance(getClass());

        // Sanity Check
        if (directory == null || nodeId == null || primary == null || date == null || thresholdMap == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log.isDebugEnabled())
            log.debug("checkNodeDir: threshold checking node dir: " + directory.getAbsolutePath());

        // Iterate over directory contents and threshold
        // check any RRD files which represent datasources
        // in the threshold maps.
        //
        File[] files = directory.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);

        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {
            // File name has format: <datsource>.rrd
            // Must strip off ".rrd" portion.
            String filename = files[i].getName();
            String datasource = filename.substring(0, filename.indexOf(".rrd"));

            // Lookup the ThresholdEntity object corresponding
            // to this datasource.
            //
            
       
            
            ThresholdEntity threshold = (ThresholdEntity) thresholdMap.get(datasource);
            if (threshold != null) {
                if (log.isDebugEnabled())
                    log.debug("checkNodeDir: threshold checking datasource: " + datasource);
                // 
                // fetch the last in range datasource from the RRD file
                //
                Double dsValue = null;
                try {
                	if (range != 0) {
                		if (log.isDebugEnabled())
                            log.debug("checking values within " + range + " mS of last possible PDP");
                		dsValue = RrdUtils.fetchLastValueInRange(files[i].getAbsolutePath(), interval, range);
                	} else {
                		if (log.isDebugEnabled())
                            log.debug("checking value of last possible PDP only");
                		dsValue = RrdUtils.fetchLastValue(files[i].getAbsolutePath(), interval);
                	}
                } catch (NumberFormatException nfe) {
                    log.warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double, skipping evaluation.");
                } catch (RrdException e) {
                    log.error("An error occurred retriving the last value for datasource '" + datasource + "'", e);
                }

	        if (log.isDebugEnabled())
       		     log.debug("checkNodeDir: got a dsValue of : " + dsValue);
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
                            events.addEvent(createEvent(nodeId, primary, null, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_EVENT_UEI, date));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.LOW_TRIGGERED) {
                            events.addEvent(createEvent(nodeId, primary, null, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_EVENT_UEI, date));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.HIGH_REARMED) {
                            events.addEvent(createEvent(nodeId, primary, null, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, date));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.LOW_REARMED) {
                            events.addEvent(createEvent(nodeId, primary, null, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, date));
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
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary SNMP interface address
     * @param interval
     *            Configured thresholding interval
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param baseIfThresholdMap
     *            Map of configured interface level ThresholdEntity objects
     *            keyed by datasource name.
     * @param allIfThresholdMap
     *            Map of threshold maps indexed by ifLabel
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * 
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     */
    private void checkIfDir(File directory, Integer nodeId, InetAddress primary, int interval, Date date, Map baseIfThresholdMap, Map allIfThresholdMap, Events events) throws IllegalArgumentException {
        Category log = ThreadCategory.getInstance(getClass());

        // Sanity Check
        if (directory == null || nodeId == null || primary == null || date == null || baseIfThresholdMap == null || allIfThresholdMap == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log.isDebugEnabled())
            log.debug("checkIfDir: threshold checking interface dir: " + directory.getAbsolutePath());

        String ifLabel = directory.getName();
        if (log.isDebugEnabled())
            log.debug("checkIfDir: ifLabel=" + ifLabel);

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
        Map thresholdMap = (Map) allIfThresholdMap.get(ifLabel);
        if (thresholdMap == null) {
            // Doesn't exist yet, go ahead and create it
            // Must maintain a separate threshold map for
            // each interface.
            thresholdMap = new HashMap();

            // Iterate over base interface threshold map and clone each
            // ThresholdEntity object and add it to the threshold map.
            // for this interface.
            // 
            Iterator iter = baseIfThresholdMap.values().iterator();
            while (iter.hasNext()) {
                ThresholdEntity entity = (ThresholdEntity) iter.next();
                thresholdMap.put(entity.getDatasourceName(), entity.clone());
            }

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
            if (log.isDebugEnabled())
                log.debug("checkIfDir: no RRD files in dir: " + directory);
            return;
        }

        Map ifDataMap = null;
        for (int i = 0; i < files.length; i++) {
            // File name has format: <datsource>.rrd
            // Must strip off ".rrd" portion.
            String filename = files[i].getName();
            String datasource = filename.substring(0, filename.indexOf(".rrd"));

            // Lookup the ThresholdEntity object corresponding
            // to this datasource.
            //
            if (log.isDebugEnabled())
                log.debug("checkIfDir: looking up datasource: " + datasource);
            ThresholdEntity threshold = (ThresholdEntity) thresholdMap.get(datasource);
            if (threshold != null) {
                // Use RRD JNI interface to "fetch" value of the
                // datasource from the RRD file
                //
                Double dsValue = null;
                try {
                    dsValue = RrdUtils.fetchLastValue(files[i].getAbsolutePath(), interval);
                } catch (NumberFormatException nfe) {
                    log.warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double, skipping evaluation.");
                } catch (RrdException e) {
                    log.error("An error occurred retriving the last value for datasource '" + datasource + "'", e);
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
                                dbConn = DatabaseConnectionFactory.getInstance().getConnection();
                            } catch (SQLException sqlE) {
                                if (log.isEnabledFor(Priority.ERROR))
                                    log.error("checkIfDir: Failed getting connection to the database.", sqlE);
                                throw new UndeclaredThrowableException(sqlE);
                            }

                            // Make certain we close the connection
                            //
                            try {
                                ifDataMap = IfLabel.getInterfaceInfoFromIfLabel(dbConn, nodeId.intValue(), ifLabel);
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
                                log.warn("Failed to retrieve interface info from database using ifLabel '" + ifLabel);
                                ifDataMap = new HashMap();
                            } finally {
                                // Done with the database so close the
                                // connection
                                try {
                                    if (dbConn != null)
                                        dbConn.close();
                                } catch (SQLException sqle) {
                                    if (log.isEnabledFor(Priority.INFO))
                                        log.info("checkIfDir: SQLException while closing database connection", sqle);
                                }
                            }
                            // Adding ifLabel value to the map for potential use
                            // by
                            // the createEvent() method
                            ifDataMap.put("iflabel", ifLabel);

                            // Debug - dump data map
                            //
                            if (log.isDebugEnabled()) {
                                Iterator iter = ifDataMap.keySet().iterator();
                                while (iter.hasNext()) {
                                    String key = (String) iter.next();
                                    String value = (String) ifDataMap.get(key);
                                }
                            }
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.HIGH_TRIGGERED) {
                            events.addEvent(createEvent(nodeId, primary, ifDataMap, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_EVENT_UEI, date));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED || result == ThresholdEntity.LOW_TRIGGERED) {
                            events.addEvent(createEvent(nodeId, primary, ifDataMap, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_EVENT_UEI, date));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.HIGH_REARMED) {
                            events.addEvent(createEvent(nodeId, primary, ifDataMap, dsValue.doubleValue(), threshold.getHighThreshold(), EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, date));
                        }

                        if (result == ThresholdEntity.HIGH_AND_LOW_REARMED || result == ThresholdEntity.LOW_REARMED) {
                            events.addEvent(createEvent(nodeId, primary, ifDataMap, dsValue.doubleValue(), threshold.getLowThreshold(), EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, date));
                        }
                    }
                }
            }
        }
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
    private Event createEvent(Integer nodeId, InetAddress primary, Map ifDataMap, double dsValue, Threshold threshold, String uei, java.util.Date date) {
        Category log = ThreadCategory.getInstance(getClass());

        if (nodeId == null || primary == null || threshold == null)
            throw new IllegalArgumentException("nodeid, primary, and threshold cannot be null.");

        if (log.isDebugEnabled()) {
            log.debug("createEvent: nodeId=" + nodeId + " primaryAddr=" + primary + " ds=" + threshold.getDsName() + " uei=" + uei);

            if (ifDataMap != null) {
                log.debug("createEvent: specific interface data:" + " ifAddr=" + (String) ifDataMap.get("ipaddr") + " macAddr=" + (String) ifDataMap.get("snmpphysaddr") + " ifName=" + (String) ifDataMap.get("snmpifname") + " ifDescr=" + (String) ifDataMap.get("snmpifdescr") + " ifIndex=" + (String) ifDataMap.get("snmpifindex") + " ifLabel=" + (String) ifDataMap.get("iflabel"));
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
            log.warn("Failed to resolve local hostname", uhE);
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
}
