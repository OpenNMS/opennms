/*
 * This file is part of the OpenNMS(R) Application.
 * 
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * Modifications:
 * 
 * 2007 Jan 29: Formatting, convert to use new methods for evaluating thresholds and building events, use log() method. - dj@opennms.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.                                                            
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *   
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.threshd;

/**
 * @author mjamison
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.dao.support.RrdFileConstants;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>
 * The JMXThresholder class ...
 * </P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @deprecated No longer used - see ThresholdingVisitor
 * @version $Id: $
 */
public abstract class JMXThresholder implements ServiceThresholder {
    /**
     * SQL statement to retrieve interface's 'ipinterface' table information.
     */
    private static final String SQL_GET_NODEID = "SELECT nodeid FROM ipinterface WHERE ipAddr=? AND ismanaged!='D'";

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
    static final String NODE_ID_KEY = "org.opennms.netmgt.collectd.JMXThresholder.NodeId";

    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String RRD_REPOSITORY_KEY = "org.opennms.netmgt.collectd.JMXThresholder.RrdRepository";

    /**
     * Interface attribute key used to store a map of node level ThresholdEntity
     * objects keyed by datasource name.
     */
    static final String NODE_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.JMXThresholder.NodeThresholdMap";

    /**
     * Interface attribute key used to store a map of interface level
     * ThresholdEntity objects keyed by datasource name.
     */
    static final String BASE_IF_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.JMXThresholder.IfThresholdMap";

    /**
     * We must maintain a map of interface level ThresholdEntity objects on a
     * per interface basis in order to maintain separate exceeded counts and the
     * like for each of a node's interfaces. This interface attribute key used
     * to store a map of interface level ThresholdEntity object maps keyed by
     * ifLabel. So it wil refer to a map of maps indexed by ifLabel.
     */
    static final String ALL_IF_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.JMXThresholder.AllIfThresholdMap";

    private String serviceName = null;
    
    private boolean useFriendlyName = false;
    
    /**
     * <p>Setter for the field <code>serviceName</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setServiceName(String name) {
        serviceName = name;
    }

    /**
     * <P>
     * Returns the name of the service that the plug-in collects ("SNMP").
     * </P>
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return serviceName.toUpperCase();
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * Initialize the service thresholder.
     * </P>
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    public void initialize(Map parameters) {
        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            log().error("initialize: Unable to initialize RrdUtils: " + e, e);
            throw new RuntimeException("Unable to initialize RrdUtils: " + e, e);
        }

        log().debug("initialize: successfully instantiated RRD strategy");

        return;
    }

    /**
     * <p>reinitialize</p>
     */
    public void reinitialize() {
        //Nothing to do 
    }
    /**
     * Responsible for freeing up any resources held by the thresholder.
     */
    public void release() {
        // Nothing to release...
    }

    /**
     * {@inheritDoc}
     *
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for thresholding.
     */
    public void initialize(ThresholdNetworkInterface iface, Map parameters) {
        // Get interface address from NetworkInterface
        if (iface.getType() != NetworkInterface.TYPE_IPV4) {
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");
        }

        InetAddress ipAddr = (InetAddress) iface.getAddress();

        // Retrieve the name of the thresholding group associated
        // with this interface.
        String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", serviceName);

        // Get the threshold group's RRD repository path
        String repository = null;
        try {
            repository = ThresholdingConfigFactory.getInstance().getRrdRepository(groupName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
        }

        // Add RRD repository as an attribute of the interface for retrieval
        // by the check() method.
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
        Map nodeMap   = new HashMap();
        Map baseIfMap = new HashMap();
        try {
            for (Basethresholddef thresh : ThresholdingConfigFactory.getInstance().getThresholds(groupName)) {
                // See if map entry already exists for this datasource
                // If not, create a new one.
                boolean newEntity = false;
                ThresholdEntity thresholdEntity = null;
                try {
                    BaseThresholdDefConfigWrapper wrapper=BaseThresholdDefConfigWrapper.getConfigWrapper(thresh);
                    if (wrapper.getDsType().equals("node")) {
                        thresholdEntity = (ThresholdEntity) nodeMap.get(wrapper.getDatasourceExpression());
                    } else if (wrapper.getDsType().equals("if")) {
                        thresholdEntity = (ThresholdEntity) baseIfMap.get(wrapper.getDatasourceExpression());
                    }
    
                    // Found entry?
                    if (thresholdEntity == null) {
                        // Nope, create a new one
                        newEntity = true;
                        thresholdEntity = new ThresholdEntity();
                    }
    
                    try {
                        thresholdEntity.addThreshold(wrapper);
                    } catch (IllegalStateException e) {
                        log().warn("Encountered duplicate " + thresh.getType() + " for datasource " + wrapper.getDatasourceExpression(), e);
                    }
 
                    // Add new entity to the map
                    if (newEntity) {
                        if (thresh.getDsType().equals("node")) {
                            nodeMap.put(wrapper.getDatasourceExpression(), thresholdEntity);
                        } else if (thresh.getDsType().equals("if")) {
                            baseIfMap.put(wrapper.getDatasourceExpression(), thresholdEntity);
                        }
                    }
                } catch (ThresholdExpressionException e) {
                    log().warn("Could not parse threshold expression: "+e.getMessage(), e);
                }

            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
        }

        // Add node and interface thresholding maps as attributes of the
        // interface for retrieval by the check() method.
        iface.setAttribute(NODE_THRESHOLD_MAP_KEY,    nodeMap);
        iface.setAttribute(BASE_IF_THRESHOLD_MAP_KEY, baseIfMap);

        // Now create an empty map which will hold interface level
        // ThresholdEntity objects for each of the node's interfaces.
        // This map will be keyed by the interface's iflabel and will
        // contain as a value a map of ThresholdEntity objects keyed
        // by datasource name.
        iface.setAttribute(ALL_IF_THRESHOLD_MAP_KEY, new HashMap());

        final DBUtils d = new DBUtils(getClass());
        // Get database connection in order to retrieve the nodeid and
        // ifIndex from the database for this interface.
        Connection dbConn = null;
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
            d.watch(dbConn);
        } catch (SQLException e) {
            log().error("initialize: Failed getting connection to the database: " + e, e);
            throw new UndeclaredThrowableException(e);
        }

        int nodeId = -1;

        // All database calls wrapped in try/finally block so we make
        // certain that the connection will be closed when we are
        // finished.
        try {
            // Prepare & execute the SQL statement to get the 'nodeid',
            // 'ifIndex' and 'isSnmpPrimary' fields from the ipInterface table.
            PreparedStatement stmt = null;
            try {
                stmt = dbConn.prepareStatement(SQL_GET_NODEID);
                d.watch(stmt);
                stmt.setString(1, ipAddr.getHostAddress()); // interface address
                ResultSet rs = stmt.executeQuery();
                d.watch(rs);
                if (rs.next()) {
                    nodeId = rs.getInt(1);
                    if (rs.wasNull()) {
                        nodeId = -1;
                    }
                }
            } catch (SQLException e) {
                if (log().isDebugEnabled()) {
                    log().debug("initialize: SQL exception!!: " + e, e);
                }
                throw new RuntimeException("SQL exception while attempting to retrieve node id for interface " + ipAddr.getHostAddress() + ": " + e, e);
            }

            // RuntimeException is thrown if any of the following are true:
            // - node id is invalid
            // - primaryIfIndex is invalid
            // - Interface is not the primary SNMP interface for the node
            if (nodeId == -1) {
                throw new RuntimeException("Unable to retrieve node id for interface " + ipAddr.getHostAddress());
            }

        } finally {
            d.cleanUp();
        }

        // Add nodeId as an attribute of the interface for retrieval
        // by the check() method.
        iface.setAttribute(NODE_ID_KEY, new Integer(nodeId));

        // Debug
        if (log().isDebugEnabled()) {
            log().debug("initialize: dumping node thresholds defined for " + ipAddr.getHostAddress() + "/" + groupName + ":");
            Iterator iter = nodeMap.values().iterator();
            while (iter.hasNext()) {
                log().debug((ThresholdEntity) iter.next());
            }

            log().debug("initialize: dumping interface thresholds defined for " + ipAddr.getHostAddress() + "/" + groupName + ":");
            iter = baseIfMap.values().iterator();
            while (iter.hasNext()) {
                log().debug((ThresholdEntity) iter.next());
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("initialize: initialization completed for " + ipAddr.getHostAddress());
        }
        
        return;
    }

    /**
     * {@inheritDoc}
     *
     * Responsible for releasing any resources associated with the specified
     * interface.
     */
    public void release(ThresholdNetworkInterface iface) {
        // Nothing to release...
    }

    /**
     * {@inheritDoc}
     *
     * Perform threshold checking.
     */
    public int check(ThresholdNetworkInterface iface, EventProxy eproxy, Map parameters) {
        Category log = log();
        String dsDir = serviceName;

        String port         = ParameterMap.getKeyedString( parameters, "port",           null);
        String friendlyName = ParameterMap.getKeyedString( parameters, "friendly-name",  port);
        int range = ParameterMap.getKeyedInteger( parameters, "range",  DEFAULT_RANGE);
        
        if (useFriendlyName) {
            dsDir = friendlyName;
        }

        InetAddress primary = (InetAddress) iface.getAddress();

        // Get configuration parameters
        String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", serviceName);
        int    interval  = ParameterMap.getKeyedInteger(parameters, "interval", DEFAULT_INTERVAL);

        if (log.isDebugEnabled()) {
            log.debug("check: service= " + serviceName.toUpperCase() + " address= " + primary.getHostAddress() + " thresholding-group=" + groupName + " interval=" + interval + "mS range =  " + range + " mS");
        }

        // RRD Repository attribute
        String repository = (String) iface.getAttribute(RRD_REPOSITORY_KEY);
        if (log.isDebugEnabled()) {
            log.debug("check: rrd repository=" + repository);
        }

        // Nodeid attribute
        Integer nodeId = (Integer) iface.getAttribute(NODE_ID_KEY);

        // node and interface ThresholdEntity map attributes
        Map nodeMap   = (Map) iface.getAttribute(NODE_THRESHOLD_MAP_KEY);
        Map baseIfMap = (Map) iface.getAttribute(BASE_IF_THRESHOLD_MAP_KEY);
        Map allIfMap  = (Map) iface.getAttribute(ALL_IF_THRESHOLD_MAP_KEY);

        // -----------------------------------------------------------
        // 
        // Perform node-level threshold checking
        //
        // -----------------------------------------------------------

        // Get File object representing the node directory
        File nodeDirectory = new File(repository + File.separator + nodeId.toString() + File.separator + dsDir);
        //if (!RrdFileConstants.isValidRRDNodeDir(nodeDirectory)) {
        //    log.error("Node directory for " + nodeDirectory + " does not exist or is not a valid RRD node directory.");
        //    log.error("Threshold checking failed for primary " + serviceName + " interface " + primary.getHostAddress());
        //}

        // Create empty Events object to hold any threshold
        // events generated during the thresholding check...
        Events events = new Events();

        // Date stamp for all outgoing events
        Date dateStamp = new Date();

        try {
            checkNodeDir(nodeDirectory, nodeId, primary, range, interval, dateStamp, nodeMap, events);
        } catch (IllegalArgumentException e) {
            log.info("check: Threshold checking failed for primary " + serviceName + " interface " + primary.getHostAddress() + ": " + e, e);
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
        File[] files = nodeDirectory.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    // Found interface directory...
                    checkIfDir(files[i], nodeId, primary, interval, range, dateStamp, baseIfMap, allIfMap, events);
                } catch (IllegalArgumentException e) {
                    log.info("check: Threshold checking failed for primary " + serviceName + " interface " + primary.getHostAddress() + ": " + e, e);
                    return THRESHOLDING_FAILED;
                }
            }
        }

        // Send created events
        if (events.getEventCount() > 0) {
            try {
                Log eventLog = new Log();
                eventLog.setEvents(events);
                eproxy.send(eventLog);
            } catch (EventProxyException e) {
                log.warn("check: Failed sending threshold events via event proxy: " + e, e);
                return THRESHOLDING_FAILED;
            }
        }

        // return the status of the threshold check
        return THRESHOLDING_SUCCEEDED;
        
    }
    
    private Map<String, Double> getThresholdValues(File directory, int range, int interval, Collection<String> requiredDatasources) {
        Category log = log();
        Map<String, Double> values=new HashMap<String,Double>();
        for(String ds: requiredDatasources) {
            File dsFile=new File(directory,ds+RrdUtils.getExtension());
            Double thisValue=null;
            if(dsFile.exists()) {
                try {
                    if (range != 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("checking values within " + range + " mS of last possible PDP");
                        }
                        thisValue = RrdUtils.fetchLastValueInRange(dsFile.getAbsolutePath(), ds, interval, range);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("checking value of last possible PDP only");
                        }
                        thisValue = RrdUtils.fetchLastValue(dsFile.getAbsolutePath(), ds, interval);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Unable to convert retrieved value for datasource '" + ds + "' to a double, skipping evaluation.");
                } catch (RrdException e) {
                    log.info("An error occurred retriving the last value for datasource '" + ds + "': " + e, e);
                }
            }
        
            if (thisValue == null || thisValue.isNaN()) {
                return null;
            }
            values.put(ds,thisValue);
        }
        return values;
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
     * @param range
     *            Age before which PDP is considered out of date
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
    private void checkNodeDir(File directory, Integer nodeId, InetAddress primary, int interval, int range,  Date date, Map thresholdMap, Events events) throws IllegalArgumentException {
        Category log = log();

        // Sanity Check
        if (directory == null || nodeId == null || primary == null || date == null || thresholdMap == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log.isDebugEnabled()) {
            log.debug("checkNodeDir: threshold checking node dir: " + directory.getAbsolutePath());
        }
        
        for(Object threshKey  :thresholdMap.keySet()) {
            ThresholdEntity threshold = (ThresholdEntity) thresholdMap.get(threshKey);
            Collection<String> requiredDatasources=threshold.getRequiredDatasources();
            Map<String, Double> values=getThresholdValues(directory, range, interval, requiredDatasources);
            if(values==null) {
                continue; //Not all values were available
            }
            List<Event> eventList = threshold.evaluateAndCreateEvents(values, date);
            if (eventList.size() == 0) {
                // Nothing to do, so continue
                continue;
            }
            
            completeEventListAndAddToEvents(events, eventList, nodeId, primary, null);
        }
        
 
    }


    /**
     * Performs threshold checking on an JMX RRD interface directory.
     * 
     * @param directory
     *            RRD repository directory
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary JMX interface address
     * @param interval
     *            Configured thresholding interval
     * @param range
     *            Age before which PDP is considered out of date           
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
    private void checkIfDir(File directory, Integer nodeId, InetAddress primary, int interval, int range, Date date, Map baseIfThresholdMap, Map allIfThresholdMap, Events events) throws IllegalArgumentException {
        // Sanity Check
        if (directory == null || nodeId == null || primary == null || date == null || baseIfThresholdMap == null || allIfThresholdMap == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log().isDebugEnabled()) {
            log().debug("checkIfDir: threshold checking interface dir: " + directory.getAbsolutePath());
        }

        String ifLabel = directory.getName();
        if (log().isDebugEnabled()) {
            log().debug("checkIfDir: ifLabel=" + ifLabel);
        }

        // This is an interface directory extract the
        // interface label from the full path name of the file
        /*
         * String path = directory.getAbsolutePath(); String path = directory
         * int fileSepIndex = path.lastIndexOf(File.separatorChar); if
         * (fileSepIndex >= 0) ifLabel = path.substring(fileSepIndex+1,
         * path.length()); else ifLabel = path;
         */

        // Attempt to retrieve the threshold map for this interface
        // using the ifLabel for the interface
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
                thresholdMap.put(entity.getDataSourceExpression(), entity.clone());
            }

            // Add the new threshold map for this interface
            // to the all interfaces map.
            allIfThresholdMap.put(ifLabel, thresholdMap);
        }
        
        Map<String, String> ifDataMap = new HashMap<String, String>();
        for(Object threshKey  :thresholdMap.keySet()) {
            ThresholdEntity threshold = (ThresholdEntity) thresholdMap.get(threshKey);
            Collection<String> requiredDatasources=threshold.getRequiredDatasources();
            Map<String, Double> values=getThresholdValues(directory, range, interval, requiredDatasources);
            if(values==null) {
                continue; //Not all values were available
            }
            List<Event> eventList = threshold.evaluateAndCreateEvents(values, date);
            if (eventList.size() == 0) {
                // Nothing to do, so continue
                continue;
            }
            if (ifDataMap.size() == 0 && ifLabel != null) {
                populateIfDataMap(nodeId, ifLabel, ifDataMap);
            }
            completeEventListAndAddToEvents(events, eventList, nodeId, primary, ifDataMap);
        }
    }

    private void completeEventListAndAddToEvents(Events events, List<Event> eventList, Integer nodeId, InetAddress primary, Map<String, String> ifDataMap) {
        for (Event event : eventList) {
            event.setNodeid(nodeId);
            event.setService(serviceName());

            // Set event interface
            if (ifDataMap == null || ifDataMap.get("ipaddr") == null) {
                // Node level datasource
                if (primary != null) {
                    event.setInterface(primary.getHostAddress());
                }
            } else {
                /*
                 * Interface-level datasource
                 * 
                 * NOTE: Non-IP interfaces will have an
                 * address of "0.0.0.0".
                 */
                String ifAddr = ifDataMap.get("ipaddr");
                event.setInterface(ifAddr);
            }
            
            // Add appropriate parms
            Parms eventParms = event.getParms();
            
            Parm eventParm;
            Value parmValue;
            
            if (ifDataMap != null && ifDataMap.get("iflabel") != null) {
                eventParm = new Parm();
                eventParm.setParmName("ifLabel");
                parmValue = new Value();
                parmValue.setContent(ifDataMap.get("iflabel"));
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
    
            events.addEvent(event);
        }
        
    }

    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private void populateIfDataMap(Integer nodeId, String ifLabel, Map<String, String> ifDataMap) {
        Connection dbConn;
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
        } catch (SQLException e) {
            log().error("checkIfDir: Failed getting connection to the database: " + e, e);
            throw new UndeclaredThrowableException(e);
        }

        // Make certain we close the connection
        try {
            ifDataMap.putAll(IfLabel.getInterfaceInfoFromIfLabel(dbConn, nodeId.intValue(), ifLabel));
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
            log().warn("Failed to retrieve interface info from database using ifLabel '" + ifLabel + "': " + e, e);
        } finally {
            // Done with the database so close the connection
            try {
                if (dbConn != null) {
                    dbConn.close();
                }
            } catch (SQLException e) {
                log().info("checkIfDir: SQLException while closing database connection: " + e, e);
            }
        }
        
        // Add ifLabel value to the map for potential when creating events
        ifDataMap.put("iflabel", ifLabel);
    }

    /**
     * <p>Setter for the field <code>useFriendlyName</code>.</p>
     *
     * @param useFriendlyName a boolean.
     */
    public void setUseFriendlyName(boolean useFriendlyName) {
        this.useFriendlyName = useFriendlyName;
    }
}
