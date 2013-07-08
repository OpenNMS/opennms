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

package org.opennms.netmgt.collectd;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The SnmpCollector class ...
 * </P>
 *
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 */
public class SnmpCollector implements ServiceCollector {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollector.class);
    
    /**
     * Name of monitored service.
     */
    static final String SERVICE_NAME = "SNMP";

    /**
     * The character to replace non-alphanumeric characters in Strings where
     * needed.
     */
    static final char nonAnRepl = '_';

    /**
     * The String of characters which are exceptions for
     * AlphaNumeric.parseAndReplaceExcept in if Aliases
     */
    static final String AnReplEx = "-._";

    /**
     * Value of MIB-II ifAlias oid
     */
    static final String IFALIAS_OID = ".1.3.6.1.2.1.31.1.1.1.18";

    /**
     * SQL statement to retrieve snmpifaliases and snmpifindexes for a given
     * node.
     */
    static final String SQL_GET_SNMPIFALIASES = "SELECT snmpifalias "
        + "FROM snmpinterface "
        + "WHERE nodeid=? "
        + "AND snmpifindex = ? "
        + "AND snmpifalias != ''";

    /**
     * SQL statement to retrieve most recent forced rescan eventid for a node.
     */
    static final String SQL_GET_LATEST_FORCED_RESCAN_EVENTID = "SELECT eventid "
        + "FROM events "
        + "WHERE (nodeid=? OR ipaddr=?) "
        + "AND eventuei='" + EventConstants.FORCE_RESCAN_EVENT_UEI + "' "
        + "ORDER BY eventid DESC " + "LIMIT 1";

    /**
     * SQL statement to retrieve most recent rescan completed eventid for a
     * node.
     */
    static final String SQL_GET_LATEST_RESCAN_COMPLETED_EVENTID = "SELECT eventid "
        + "FROM events "
        + "WHERE nodeid=? "
        + "AND eventuei='" + EventConstants.RESCAN_COMPLETED_EVENT_UEI + "' "
        + "ORDER BY eventid DESC " + "LIMIT 1";

    /**
     * Object identifier used to retrieve interface count. This is the MIB-II
     * interfaces.ifNumber value.
     */
    static final String INTERFACES_IFNUMBER = ".1.3.6.1.2.1.2.1";

    /**
     * Object identifier used to retrieve system uptime. This is the MIB-II
     * system.sysUpTime value.
     */
    static final String NODE_SYSUPTIME = ".1.3.6.1.2.1.1.3";

    /**
     * Valid values for the 'snmpStorageFlag' attribute in datacollection-config
     * XML file. "primary" = only primary SNMP interface should be collected and
     * stored "all" = all primary SNMP interfaces should be collected and stored
     */
    public static String SNMP_STORAGE_PRIMARY = "primary";

    static String SNMP_STORAGE_ALL = "all";

    static String SNMP_STORAGE_SELECT = "select";

    /**
     * This defines the default maximum number of variables the collector is
     * permitted to pack into a single outgoing PDU. This value is intentionally
     * kept relatively small in order to communicate successfully with the
     * largest possible number of agents.
     * 
     * @deprecated If not configured in SNMP collector configuration, use agent's
     * setting for defaults are now determined there.
     */
    static int DEFAULT_MAX_VARS_PER_PDU = 30;

    /* -------------------------------------------------------------- */
    /* Attribute key names */
    /* -------------------------------------------------------------- */

    /**
     * Interface attribute key used to store the interface's JoeSNMP SnmpPeer
     * object.
     */
    static final String SNMP_PEER_KEY = "org.opennms.netmgt.collectd.SnmpCollector.SnmpPeer";

    /**
     * Interface attribute key used to store the number of interfaces configured
     * on the remote host.
     */
    static final String INTERFACE_COUNT_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifCount";

    /**
     * Interface attribute key used to store the map of IfInfo objects which
     * hold data about each interface on a particular node.
     */
    static String IF_MAP_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifMap";

    /**
     * Interface attribute key used to store a NodeInfo object which holds data
     * about the node being polled.
     */
    static String NODE_INFO_KEY = "org.opennms.netmgt.collectd.SnmpCollector.nodeInfo";

    /**
     * Interface attribute key used to store the data collection scheme to be
     * followed. Possible values are:
     * <ul>
     * <li>SNMP_STORAGE_PRIMARY = "primary"</li>
     * <li>SNMP_STORAGE_ALL = "all"</li>
     * <li>SNMP_STORAGE_SELECT = "select"</li>
     * </ul>
     */
    static String SNMP_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.snmpStorage";

    /**
     * Returns the name of the service that the plug-in collects ("SNMP").
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Initialize the service collector. During initialization the SNMP
     * collector:
     * <ul>
     * <li>Initializes various configuration factories.</li>
     * <li>Verifies access to the database.</li>
     * <li>Verifies access to RRD file repository.</li>
     * <li>Verifies access to JNI RRD shared library.</li>
     * <li>Determines if SNMP to be stored for only the node's primary
     * interface or for all interfaces.</li>
     * </ul>
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    @Override
    public void initialize(Map<String, String> parameters) {
    	initSnmpPeerFactory();
        //initDataCollectionConfig();
        initDatabaseConnectionFactory();
        
        // Get path to RRD repository
        //initializeRrdRepository();

    }

    /*private void initializeRrdRepository() {

        initializeRrdDirs();

        initializeRrdInterface();
    }

    private void initializeRrdDirs() {
        File f = new File(DataCollectionConfigFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new CollectionInitializationException("Unable to create RRD file "
                                           + "repository.  Path doesn't already exist and could not make directory: " + DataCollectionConfigFactory.getInstance().getRrdPath());
            }
        }
    }

    private void initializeRrdInterface() {
        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            log().error("initializeRrdInterface: Unable to initialize RrdUtils", e);
            throw new CollectionInitializationException("Unable to initialize RrdUtils", e);
        }
    }*/

    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (IOException e) {
            LOG.error("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            LOG.error("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            LOG.error("initDatabaseConnectionFactory: Failed loading database driver", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /*
    private void initDataCollectionConfig() {
        try {
            DataCollectionConfigFactory.init();
        } catch (Throwable e) {
            log().fatal("initDataCollectionConfig: Failed to load data collection configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }
    */

    private void initSnmpPeerFactory() {
        try {
            SnmpPeerFactory.init();
        } catch (IOException e) {
            LOG.error("initSnmpPeerFactory: Failed to load SNMP configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Responsible for freeing up any resources held by the collector.
     */
    @Override
    public void release() {
        // Nothing to release...
    }

    /**
     * {@inheritDoc}
     *
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for data collection.
     * @throws CollectionInitializationException 
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        agent.validateAgent();
        
        // XXX: Experimental code that creates an OnmsSnmpCollection only once
//        ServiceParameters params = new ServiceParameters(parameters);
//        agent.setAttribute("SNMP_COLLECTION", new OnmsSnmpCollection(agent, params));
//        
//        params.logIfAliasConfig();
    }

    /**
     * {@inheritDoc}
     *
     * Responsible for releasing any resources associated with the specified
     * interface.
     */
    @Override
    public void release(CollectionAgent agent) {
        agent.setAttribute("SNMP_COLLECTION", null);
    }

    /**
     * {@inheritDoc}
     *
     * Perform data collection.
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eventProxy, Map<String, Object> parameters) throws CollectionException {
        try {
            // XXX: Experimental code that reuses the OnmsSnmpCollection
            // OnmsSnmpCollection snmpCollection = (OnmsSnmpCollection)agent.getAttribute("SNMP_COLLECTION");
            // ServiceParameters params = snmpCollection.getServiceParameters();
            
            // XXX: This code would be commented out in light if the experimental code above was enabled
            final ServiceParameters params = new ServiceParameters(parameters);
            params.logIfAliasConfig();
            OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, params);

            final ForceRescanState forceRescanState = new ForceRescanState(agent, eventProxy);

            SnmpCollectionSet collectionSet = snmpCollection.createCollectionSet(agent);
            collectionSet.setCollectionTimestamp(new Date());
            if (!collectionSet.hasDataToCollect()) {
                logNoDataToCollect(agent);
                // should we return here?
            }
            
            Collectd.instrumentation().beginCollectingServiceData(collectionSet.getCollectionAgent().getNodeId(), collectionSet.getCollectionAgent().getHostAddress(), serviceName());
            try {
                collectionSet.collect();
                
                /*
                 * FIXME: Should we even be doing this? I say we get rid of this force rescan thingie
                 * {@see http://issues.opennms.org/browse/NMS-1057}
                 */
                if (System.getProperty("org.opennms.netmgt.collectd.SnmpCollector.forceRescan", "false").equalsIgnoreCase("true")
                        && collectionSet.rescanNeeded()) {
                    /*
                     * TODO: the behavior of this object may have been re-factored away.
                     * Verify that this is correct and remove this unused object if it
                     * is no longer needed.  My gut thinks this should be investigated.
                     */
                    forceRescanState.rescanIndicated();
                }
                /**
                 * Persistence is now done by the BasePersister visitor
                 * @see CollectableService#doCollection()
                 * @see CollectionSet#visit(BasePersister visitor)
                 */
                //persistData(params, collectionSet);
                return collectionSet;
            } finally {
                Collectd.instrumentation().endCollectingServiceData(collectionSet.getCollectionAgent().getNodeId(), collectionSet.getCollectionAgent().getHostAddress(), serviceName());
            }
        } catch (CollectionException e) {
            Collectd.instrumentation().reportCollectionException(agent.getNodeId(), agent.getHostAddress(), serviceName(), e);
            
            throw e;
        } catch (Throwable t) {
            throw new CollectionException("Unexpected error during node SNMP collection for: " + agent.getHostAddress(), t);
        }
    }

    /*private void persistData(ServiceParameters params, SnmpCollectionSet collectionSet) {
        Collectd.instrumentation().beginPersistingServiceData(collectionSet.getCollectionAgent().getNodeId(), collectionSet.getCollectionAgent().getHostAddress(), serviceName());
        try {
            collectionSet.saveAttributes(params);
        } finally {
            Collectd.instrumentation().endPersistingServiceData(collectionSet.getCollectionAgent().getNodeId(), collectionSet.getCollectionAgent().getHostAddress(), serviceName());
        }
    }*/

    /*private void collectData(SnmpCollectionSet collectionSet) throws CollectionWarning {
        Collectd.instrumentation().beginCollectingServiceData(collectionSet.getCollectionAgent().getNodeId(), collectionSet.getCollectionAgent().getHostAddress(), serviceName());
        try {
            collectionSet.collect();
        } finally {
            Collectd.instrumentation().endCollectingServiceData(collectionSet.getCollectionAgent().getNodeId(), collectionSet.getCollectionAgent().getHostAddress(), serviceName());
        }
    }*/

    private void logNoDataToCollect(CollectionAgent agent) {
        LOG.info("agent {} defines no data to collect.  Skipping.", agent);
    }

    

    // Unused
//    int unexpected(CollectionAgent agent, Throwable t) {
//        log().error("Unexpected error during node SNMP collection for " + agent.getHostAddress(), t);
//        return ServiceCollector.COLLECTION_FAILED;
//    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return DataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }
}
