/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The LatencyThresholder class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * FIXME: This thresholder does not support ranges yet.
 * 
 */
final class LatencyThresholder implements ServiceThresholder {
    
    private static final Logger LOG = LoggerFactory.getLogger(LatencyThresholder.class);
    
    /**
     * SQL statement to retrieve interface's 'ipinterface' table information.
     */
    private static final String SQL_GET_NODEID = "SELECT nodeid FROM ipinterface WHERE ipAddr=? AND ismanaged!='D'";

    /**
     * Default thresholding interval (in milliseconds).
     * 
     */
    static final int DEFAULT_INTERVAL = 300000; // 300s or 5m

    /**
     * Default age before which a data point is considered "out of date"
     */
    
    static final int DEFAULT_RANGE = 0; 


    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String RRD_REPOSITORY_KEY = "org.opennms.netmgt.collectd.LatencyThresholder.RrdRepository";

    /**
     * Interface attribute key used to store configured thresholds
     */
    static final String THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.LatencyThresholder.ThresholdMap";

    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String NODE_ID_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.NodeId";

    /**
     * Specific service that this thresholder is responsible for latency
     * threshold checking.
     */
    private String m_svcName;

    /**
     * <P>
     * Returns the name of the service that the plug-in threshold checks.
     * </P>
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return m_svcName;
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
    @Override
    public void initialize(Map<?,?> parameters) {
        // Service name
        //
        m_svcName = (String) parameters.get("svcName");
        LOG.debug("initialize: latency thresholder for service '{}'", m_svcName);
    }
    
    /**
     * <p>reinitialize</p>
     */
    @Override
    public void reinitialize() {
        //Nothing to do 
    }

    /**
     * Responsible for freeing up any resources held by the thresholder.
     */
    @Override
    public void release() {
        // Nothing to release...
    }

    /**
     * {@inheritDoc}
     *
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for thresholding.
     */
    @Override
    public void initialize(ThresholdNetworkInterface iface, Map<?,?> parameters) {
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new RuntimeException("Unsupported interface type, only TYPE_INET currently supported");
        InetAddress ipAddr = (InetAddress) iface.getAddress();
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

        // Get database connection in order to retrieve the nodeid and
        // ifIndex from the database for this interface.
        //
        java.sql.Connection dbConn = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
            d.watch(dbConn);
        } catch (SQLException sqlE) {
            LOG.error("initialize: Failed getting connection to the database.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        }

        // Use IP address to lookup the node id
        //
        // NOTE: All database calls wrapped in try/finally block so we make
        // certain that the connection will be closed when we are
        // finished.
        //
        int nodeId = -1;

        final String hostAddress = InetAddressUtils.str(ipAddr);
		try {
            // Prepare & execute the SQL statement to get the 'nodeid',
            // 'ifIndex' and 'isSnmpPrimary' fields from the ipInterface table.
            //
            PreparedStatement stmt = null;
            try {
                stmt = dbConn.prepareStatement(SQL_GET_NODEID);
                d.watch(stmt);
                stmt.setString(1, hostAddress); // interface address
                ResultSet rs = stmt.executeQuery();
                d.watch(rs);
                if (rs.next()) {
                    nodeId = rs.getInt(1);
                    if (rs.wasNull())
                        nodeId = -1;
                }
            } catch (SQLException sqle) {
                LOG.debug("initialize: SQL exception!!", sqle);
                throw new RuntimeException("SQL exception while attempting to retrieve node id for interface " + hostAddress);
            }

            LOG.debug("initialize: db retrieval info: nodeid = {}, address = {}", nodeId, hostAddress);

            if (nodeId == -1)
                throw new RuntimeException("Unable to retrieve node id for interface " + hostAddress);
        } finally {
            d.cleanUp();
        }

        // Add nodeId as an attribute of the interface for retrieval
        // by the check() method.
        //
        iface.setAttribute(NODE_ID_KEY, new Integer(nodeId));

        // Retrieve the collection of Threshold objects associated with
        // the defined thresholding group and build maps of
        // ThresholdEntity objects keyed by datasource name. The
        // datasource type of the threshold determines which
        // map the threshold entity is added to.
        //
        // Each ThresholdEntity can wrap one high Threshold and one low
        // Threshold castor-generated object for a single datasource.
        // If more than one high or more than one low threshold is defined
        // for a single datasource a warning messages is generated. Only
        // the first threshold in such a scenario will be used for thresholding.
        //

        // Create empty map for storing threshold entities
        Map<String, ThresholdEntity> thresholdMap = new HashMap<String, ThresholdEntity>();

        try {
            for (Basethresholddef thresh : ThresholdingConfigFactory.getInstance().getThresholds(groupName)) {
                // See if map entry already exists for this datasource
                // If not, create a new one.
                boolean newEntity = false;
                ThresholdEntity thresholdEntity = null;

                // All latency thresholds are per interface so confirm that
                // the datasource type is set to "if"
                //
                if (!thresh.getDsType().equals("if") && !thresh.getDsType().equals("expr")) {
                    LOG.warn("initialize: invalid datasource type, latency thresholder only supports interface level datasources.");
                    continue; // continue with the next threshold...
                }
                try {
                    BaseThresholdDefConfigWrapper wrapper=BaseThresholdDefConfigWrapper.getConfigWrapper(thresh);
                    // First attempt to lookup the entry in the map
                    thresholdEntity = thresholdMap.get(wrapper.getDatasourceExpression());

                    // Found entry?
                    if (thresholdEntity == null) {
                        // Nope, create a new one
                        newEntity = true;
                        thresholdEntity = new ThresholdEntity();
                    }

                    try {
                        thresholdEntity.addThreshold(wrapper);
                    } catch (IllegalStateException e) {
                        LOG.warn("Encountered duplicate {} for datasource {}", thresh.getType(), wrapper.getDatasourceExpression(), e);
                    }

                    // Add new entity to the map
                    if (newEntity) {
                        thresholdMap.put(wrapper.getDatasourceExpression(), thresholdEntity);
                    }
                } catch (ThresholdExpressionException e) {
                    LOG.warn("Could not parse threshold expression", e);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
        }

        // Add threshold maps as attributes for retrieval by the check() method.
        //
        iface.setAttribute(THRESHOLD_MAP_KEY, thresholdMap);

        // Debug
        //
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialize: dumping interface thresholds defined for {}/{}:", hostAddress, groupName);
            Iterator<ThresholdEntity> iter = thresholdMap.values().iterator();
            while (iter.hasNext())
                LOG.debug(iter.next().toString());
        }

        LOG.debug("initialize: initialization completed for {}", hostAddress);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * Responsible for releasing any resources associated with the specified
     * interface.
     */
    @Override
    public void release(ThresholdNetworkInterface iface) {
        // Nothing to release...
    }

    /**
     * {@inheritDoc}
     *
     * Perform threshold checking.
     */
    @Override
    public int check(ThresholdNetworkInterface iface, EventProxy eproxy, Map<?,?> parameters) {
		LatencyInterface latIface = new LatencyInterface(iface, m_svcName);
		LatencyParameters latParms = new LatencyParameters(parameters, m_svcName);
        
        try {
            
            // Get configuration parameters
            //
            // NodeId attribute
            LOG.debug("check: service={} interface={} nodeId={} thresholding-group={} interval={}ms", m_svcName, latIface.getHostAddress(), latIface.getNodeId(), latParms.getGroupName(), latParms.getInterval());
            
            // RRD Repository attribute
            //
            // Create empty Events object to hold any threshold
            // events generated during the thresholding check...
            Events events = checkRrdDir(latIface, latParms);
            
            // Send created events
            //
            sendEvents(eproxy, events);
            
            // return the status of the threshold check
            //
            return THRESHOLDING_SUCCEEDED;
            
        } catch(ThresholdingException e) {
            LOG.error(e.getMessage());
            return e.getFailureCode();
        } catch (EventProxyException e) {
            LOG.error("check: Failed sending threshold events via event proxy...", e);
            return THRESHOLDING_FAILED;
        }
    }

	private void sendEvents(EventProxy eproxy, Events events) throws EventProxyException {
        if (events != null && events.getEventCount() > 0) {
            Log eventLog = new Log();
            eventLog.setEvents(events);
            eproxy.send(eventLog);
        }
    }

    /**
     * Performs threshold checking on an directory which contains one or more
     * RRD files containing latency/response time information. ThresholdEntity
     * objects are stored for performing threshold checking.
     * @param latIface TODO
     * @param latParms TODO
     * @param parameters 
     * @param iface 
     * @param directory
     *            RRD repository directory
     * @param m_nodeId
     *            Node identifier of interface being checked
     * @param ipAddr
     *            IP address of the interface being checked
     * @param interval
     *            Configured thresholding interval
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param thresholdMap
     *            Map of configured interface level ThresholdEntity objects
     *            keyed by datasource name.
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     * @throws ThresholdingException 
     */
    Events checkRrdDir(LatencyInterface latIface, LatencyParameters latParms) throws IllegalArgumentException, ThresholdingException {
		Map<String,ThresholdEntity> thresholdMap = latIface.getThresholdMap();

        // Sanity Check
        if (latIface.getInetAddress() == null || thresholdMap == null) {
            throw new ThresholdingException("check: Threshold checking failed for " + m_svcName + "/" + latIface.getHostAddress(), THRESHOLDING_FAILED);
        }
        
        Events events = new Events();
        Date date = new Date();

        for (Iterator<String> it = thresholdMap.keySet().iterator(); it.hasNext();) {
            String datasource = it.next();
            ThresholdEntity threshold = thresholdMap.get(datasource);
            if (threshold != null) {
                Double dsValue = threshold.fetchLastValue(latIface, latParms);
                Map<String, Double> dsValues=new HashMap<String, Double>();
                dsValues.put(datasource, dsValue);
                List<Event> eventList = threshold.evaluateAndCreateEvents(dsValues, date);
                if (eventList.size() == 0) {
                    // Nothing to do, so continue
                    continue;
                }

                completeEventListAndAddToEvents(events, eventList, latIface);
                

                /*
                threshold.evaluateThreshold(dsValue, events, date, latIface);
                */
            }
        }

        return events;
    }

    private void completeEventListAndAddToEvents(Events events, List<Event> eventList, LatencyInterface latIface) throws ThresholdingException {
        for (Event event : eventList) {
            event.setNodeid((long) latIface.getNodeId());
            event.setInterfaceAddress(latIface.getInetAddress());
            event.setService(latIface.getServiceName());

            events.addEvent(event);
        }
    }
}
