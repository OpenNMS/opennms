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

import java.util.Date;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.InvalidCollectionAgentException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProxy;
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
public class SnmpCollector extends AbstractSnmpCollector {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollector.class);

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
}
