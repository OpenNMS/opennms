/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
