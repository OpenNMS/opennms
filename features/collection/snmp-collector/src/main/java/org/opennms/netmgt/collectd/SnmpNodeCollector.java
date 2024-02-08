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

import java.net.InetAddress;
import java.util.Collection;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.SnmpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SnmpNodeCollector class is responsible for performing the actual SNMP
 * data collection for a node over a specified network interface. The
 * SnmpNodeCollector implements the SnmpHandler class in order to receive
 * notifications when an SNMP reply is received or error occurs.
 *
 * The SnmpNodeCollector is provided a list of MIB objects to collect and an
 * interface over which to collect the data. Data collection can be via SNMPv1
 * GetNext requests or SNMPv2 GetBulk requests depending upon the parms used to
 * construct the collector.
 *
 */
public class SnmpNodeCollector extends AggregateTracker {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpNodeCollector.class);
    
    /**
     * Used to store the collected MIB data.
     */
    private SNMPCollectorEntry m_collectorEntry;

    /**
     * Holds the IP Address of the primary SNMP iterface.
     */
    private String m_primaryIf;

    private SnmpCollectionSet m_collectionSet;

    /**
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</EM> using the notifyAll() method.
     *
     * @param address TODO
     * @param objList
     *            The list of object id's to be collected.
     * @param collectionSet TODO
     */
    public SnmpNodeCollector(InetAddress address, Collection<SnmpAttributeType> objList, SnmpCollectionSet collectionSet) {
        super(SnmpAttributeType.getCollectionTrackers(objList));
        
        m_primaryIf = InetAddressUtils.str(address);
        m_collectionSet = collectionSet;
        m_collectorEntry = new SNMPCollectorEntry(objList, m_collectionSet);


    }


    /**
     * Returns the list of all entry maps that can be used to access all the
     * information from the service polling.
     *
     * @return a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    public SNMPCollectorEntry getEntry() {
        return m_collectorEntry;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void reportGenErr(String msg) {
        LOG.warn("genErr collecting data for node {}: {}", m_primaryIf, msg);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("noSuchName collecting data for node {}: {}", m_primaryIf, msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Fatal error collecting data for node {}: {}", m_primaryIf, ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) collecting data for node {}: {}", status, m_primaryIf, status.retry()? "Retrying." : "Giving up.");
    }

    /** {@inheritDoc} */
    @Override
    protected void storeResult(SnmpResult res) {
        m_collectorEntry.storeResult(res);
    }


    /**
     * <p>getCollectionSet</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SnmpCollectionSet} object.
     */
    public SnmpCollectionSet getCollectionSet() {
        return m_collectionSet;
    }
}
