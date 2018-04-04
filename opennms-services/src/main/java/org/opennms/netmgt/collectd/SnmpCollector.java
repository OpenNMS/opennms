/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.collection.api.AbstractServiceCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.InvalidCollectionAgentException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The SnmpCollector class ...
 * </P>
 *
 * @author <A HREF="mailto:brozow@opennms.org">Matt Brozowski</A>
 */
public class SnmpCollector extends AbstractServiceCollector {

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
    public static final String SNMP_STORAGE_PRIMARY = "primary";

    static final String SNMP_STORAGE_ALL = "all";

    static final String SNMP_STORAGE_SELECT = "select";

    /**
     * This defines the default maximum number of variables the collector is
     * permitted to pack into a single outgoing PDU. This value is intentionally
     * kept relatively small in order to communicate successfully with the
     * largest possible number of agents.
     * 
     * @deprecated If not configured in SNMP collector configuration, use agent's
     * setting for defaults are now determined there.
     */
    static final int DEFAULT_MAX_VARS_PER_PDU = 30;

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
    static final String IF_MAP_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifMap";

    /**
     * Interface attribute key used to store a NodeInfo object which holds data
     * about the node being polled.
     */
    static final String NODE_INFO_KEY = "org.opennms.netmgt.collectd.SnmpCollector.nodeInfo";

    /**
     * Interface attribute key used to store the data collection scheme to be
     * followed. Possible values are:
     * <ul>
     * <li>SNMP_STORAGE_PRIMARY = "primary"</li>
     * <li>SNMP_STORAGE_ALL = "all"</li>
     * <li>SNMP_STORAGE_SELECT = "select"</li>
     * </ul>
     */
    static final String SNMP_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.snmpStorage";

    private LocationAwareSnmpClient m_client;

    /**
     * Returns the name of the service that the plug-in collects ("SNMP").
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    @Override
    public void initialize() {
    	initSnmpPeerFactory();
    }

    private void initSnmpPeerFactory() {
        try {
            SnmpPeerFactory.init();
        } catch (IOException e) {
            LOG.error("initSnmpPeerFactory: Failed to load SNMP configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void validateAgent(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        ((SnmpCollectionAgent)agent).validateAgent();
    }

    /**
     * {@inheritDoc}
     *
     * Perform data collection.
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        try {
            final ServiceParameters params = new ServiceParameters(parameters);
            params.logIfAliasConfig();

            if (m_client == null) {
                m_client = BeanUtils.getBean("daoContext", "locationAwareSnmpClient", LocationAwareSnmpClient.class);
            }

            if (!(agent instanceof SnmpCollectionAgent)) {
                throw new InvalidCollectionAgentException(String.format("Expected agent of type: %s, but got: %s",
                        SnmpCollectionAgent.class.getCanonicalName(), agent.getClass().getCanonicalName()));
            }
            OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection((SnmpCollectionAgent)agent, params, m_client);

            final EventProxy eventProxy = EventIpcManagerFactory.getIpcManager();
            final ForceRescanState forceRescanState = new ForceRescanState(agent, eventProxy);

            SnmpCollectionSet collectionSet = snmpCollection.createCollectionSet((SnmpCollectionAgent)agent);
            collectionSet.setCollectionTimestamp(new Date());
            if (!collectionSet.hasDataToCollect()) {
                LOG.info("agent {} defines no data to collect.  Skipping.", agent);
                // should we return here?
            }

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
            } else {
                collectionSet.checkForSystemRestart();
            }
            return collectionSet;
        } catch (CollectionException e) {
            throw e;
        } catch (Throwable t) {
            throw new CollectionException("Unexpected error during node SNMP collection for: " + agent.getHostAddress(), t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return DataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }
}
