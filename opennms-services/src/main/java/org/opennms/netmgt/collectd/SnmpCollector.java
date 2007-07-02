
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
//reserved.
//OpenNMS(R) is a derivative work, containing both original code, included
//code and modified
//code that was published under the GNU General Public License. Copyrights
//for modified
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Modifications:

//2006 Aug 15: Make error messages more descriptive - dj@opennms.org
//2005 Jan 03: Added support for lame SNMP hosts
//2003 Oct 20: Added minval and maxval code for mibObj RRDs
//2003 Jan 31: Cleaned up some unused imports.

//Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
//reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/


package org.opennms.netmgt.collectd;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.EventProxy;

/**
 * <P>
 * The SnmpCollector class ...
 * </P>
 * 
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpCollector implements ServiceCollector {
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
        + "AND eventuei='uei.opennms.org/internal/capsd/forceRescan' "
        + "ORDER BY eventid DESC " + "LIMIT 1";

    /**
     * SQL statement to retrieve most recent rescan completed eventid for a
     * node.
     */
    static final String SQL_GET_LATEST_RESCAN_COMPLETED_EVENTID = "SELECT eventid "
        + "FROM events "
        + "WHERE nodeid=? "
        + "AND eventuei='uei.opennms.org/internal/capsd/rescanCompleted' "
        + "ORDER BY eventid DESC " + "LIMIT 1";

    /**
     * Object identifier used to retrieve interface count. This is the MIB-II
     * interfaces.ifNumber value.
     */
    static final String INTERFACES_IFNUMBER = ".1.3.6.1.2.1.2.1";

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
     * Interface attribute key used to store configured value for the maximum
     * number of variables permitted in a single outgoing SNMP PDU request.
     */
    static String MAX_VARS_PER_PDU_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.maxVarsPerPdu";

    /**
     * Returns the name of the service that the plug-in collects ("SNMP").
     * 
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
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
     * 
     * @param parameters
     *            Not currently used.
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    public void initialize(Map parameters) {

        // Initialize the SnmpPeerFactory
        initSnmpPeerFactory();

        // Initialize the DataCollectionConfigFactory
        initDataCollectionConfig();

        // Make sure we can connect to the database
        initDatabaseConnectionFactory();

        // Get path to RRD repository
        initializeRrdRepository();

    }

    private void initializeRrdRepository() {

        initializeRrdDirs();

        initializeRrdInterface();
    }

    private void initializeRrdDirs() {
        /*
         * If the RRD file repository directory does NOT already exist, create
         * it.
         */
        File f = new File(DataCollectionConfigFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file "
                                           + "repository.  Path doesn't already exist and could not make directory: " + DataCollectionConfigFactory.getInstance().getRrdPath());
            }
        }
    }

    private void initializeRrdInterface() {
        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            log().error("initializeRrdInterface: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
        }
    }

    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (IOException e) {
            log().fatal("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            log().fatal("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log().fatal("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initDataCollectionConfig() {
        try {
            DataCollectionConfigFactory.init();
        } catch (MarshalException e) {
            log().fatal("initDataCollectionConfig: Failed to load data collection configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initDataCollectionConfig: Failed to load data collection configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().fatal("initDataCollectionConfig: Failed to load data collection configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initSnmpPeerFactory() {
        try {
            SnmpPeerFactory.init();
        } catch (MarshalException e) {
            log().fatal("initSnmpPeerFactory: Failed to load SNMP configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initSnmpPeerFactory: Failed to load SNMP configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().fatal("initSnmpPeerFactory: Failed to load SNMP configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Responsible for freeing up any resources held by the collector.
     */
    public void release() {
        // Nothing to release...
    }

    /**
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for data collection.
     * 
     * @param agent
     *            Network interface to be prepped for collection.
     * @param parameters
     *            Key/value pairs associated with the package to which the
     *            interface belongs..
     */
    public void initialize(CollectionAgent agent, Map parameters) {
        agent.validateAgent();
    }

    /**
     * Responsible for releasing any resources associated with the specified
     * interface.
     * 
     * @param agent
     *            Network interface to be released.
     */
    public void release(CollectionAgent agent) {
        agent.setAttribute("SNMP_COLLECTION", null);
    }

    /**
     * Perform data collection.
     * 
     * @param agent
     *            Network interface to be data collected.
     * @param eventProxy
     *            Eventy proxy for sending events.
     * @param parameters
     *            Key/value pairs from the package to which the interface
     *            belongs.
     */
    public int collect(CollectionAgent agent, EventProxy eventProxy, Map<String, String> parameters) {
        try {

            final ServiceParameters params = new ServiceParameters(parameters);
            params.logIfAliasConfig();
            OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(agent, params);

            final ForceRescanState forceRescanState = new ForceRescanState(agent, eventProxy);

            CollectionSet collectionSet = snmpCollection.createCollectionSet(agent);
            if (!collectionSet.hasDataToCollect()) {
                logNoDataToCollect(agent);
            }

            collectionSet.collect();

            if (collectionSet.rescanNeeded()) {
                forceRescanState.rescanIndicated();
            }

            collectionSet.saveAttributes(params);

            // return the status of the collection
            return ServiceCollector.COLLECTION_SUCCEEDED;
        } catch (CollectionError e) {
            return e.reportError();
        } catch (Throwable t) {
            t.printStackTrace();
            log().error("received Throwable: " + t, t);
            return this.unexpected(agent, t);
        }
    }

    private void logNoDataToCollect(CollectionAgent agent) {
        log().info("agent "+agent+" defines no data to collect.  Skipping.");
    }

    Category log() {
        return ThreadCategory.getInstance(SnmpCollector.class);
    }

    int unexpected(CollectionAgent agent, Throwable t) {
        log().error(
                    "Unexpected error during node SNMP collection for "
                    + agent.getHostAddress(), t);
        return ServiceCollector.COLLECTION_FAILED;
    }
}
