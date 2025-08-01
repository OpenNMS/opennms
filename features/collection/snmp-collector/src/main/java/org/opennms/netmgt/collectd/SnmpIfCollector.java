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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SnmpIfCollector class is responsible for performing the actual SNMP data
 * collection for a node over a specified network interface. The SnmpIfCollector
 * implements the SnmpHandler class in order to receive notifications when an
 * SNMP reply is received or error occurs.
 *
 * The SnmpIfCollector is provided a list of MIB objects to collect and an
 * interface over which to collect the data. Data collection can be via SNMPv1
 * GetNext requests or SNMPv2 GetBulk requests depending upon the parms used to
 * construct the collector.
 *
 */
public class SnmpIfCollector extends AggregateTracker {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpIfCollector.class);
    
    private Map<SnmpInstId, SNMPCollectorEntry> m_results = new TreeMap<SnmpInstId, SNMPCollectorEntry>();
    
    /**
     * Holds the IP Address of the primary SNMP iterface.
     */
    private String m_primaryIf;

    private List<SnmpAttributeType> m_objList;

    private SnmpCollectionSet m_collectionSet;
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
    	final StringBuilder buffer = new StringBuilder();
    	
    	buffer.append(getClass().getName());
    	buffer.append("@");
    	buffer.append(Integer.toHexString(hashCode()));
    	
    	buffer.append(": Primary Interface: " + m_primaryIf);
    	buffer.append(", object list: " + m_objList);
    	buffer.append(", CollectionSet: ");
    	if (m_collectionSet == null) {
    		buffer.append("(null)");
    	} else {
    		buffer.append(m_collectionSet.getClass().getName());
    		buffer.append("@");
        	buffer.append(Integer.toHexString(m_collectionSet.hashCode()));
    	}
    	
    	return buffer.toString();
    }

    /**
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</EM> using the notifyAll() method.
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param objList TODO
     * @param collectionSet TODO
     */
    public SnmpIfCollector(InetAddress address, List<SnmpAttributeType> objList, SnmpCollectionSet collectionSet) {
        super(SnmpAttributeType.getCollectionTrackers(objList));
        
        LOG.debug("COLLECTING on list of {} items", objList.size());
        LOG.debug("List is {}", objList);
        // Process parameters
        //
        m_primaryIf = InetAddressUtils.str(address);
        m_objList = objList;
        m_collectionSet = collectionSet;
    }

    /**
     * Returns the list of all entry maps that can be used to access all the
     * information from the service polling.
     *
     * @return a {@link java.util.List} object.
     */
    public List<SNMPCollectorEntry> getEntries() {
        return new ArrayList<SNMPCollectorEntry>(m_results.values());
    }
    
	/** {@inheritDoc} */
    @Override
	protected void reportGenErr(String msg) {
        LOG.warn("{} : genErr collecting ifData. {}", m_primaryIf, msg);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("{} : noSuchName collecting ifData. {}", m_primaryIf, msg);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportTooBigErr(String msg) {
        LOG.info("{} : request tooBig. {}", m_primaryIf, msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("{} : fatal error. {}", m_primaryIf, ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("{} : non-fatal error ({}) encountered: {}", m_primaryIf, status, status.retry()? "Retrying." : "Giving up.");
    }

    /** {@inheritDoc} */
    @Override
    protected void storeResult(SnmpResult res) {
        if(res.getBase().toString().equals(AbstractSnmpCollector.IFALIAS_OID) && (res.getValue().isNull() || res.getValue().toDisplayString() == null || res.getValue().toDisplayString().equals(""))) {
            LOG.debug("Skipping storeResult. Null or zero length ifAlias");
            return;
        }
        SNMPCollectorEntry entry = m_results.get(res.getInstance());
        if (entry == null) {
            LOG.debug("Creating new SNMPCollectorEntry entry");
            entry = new SNMPCollectorEntry(m_objList, m_collectionSet);
            m_results.put(res.getInstance(), entry);
        }
        entry.storeResult(res);

    }
    
    /**
     * <p>hasData</p>
     *
     * @return a boolean.
     */
    public boolean hasData() {
        return !m_results.isEmpty();
    }
    
    /**
     * <p>getCollectionSet</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionSet} object.
     */
    public CollectionSet getCollectionSet() {
        return m_collectionSet;
    }
}
