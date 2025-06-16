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
package org.opennms.netmgt.provision.scan.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.ScanContext;
import org.opennms.netmgt.provision.Scanner;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.util.Assert;

/**
 * <p>AbstractSnmpScanner class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class AbstractSnmpScanner implements Scanner {
    
    private String m_name = null;
    private SnmpAgentConfigFactory m_snmpAgentConfigFactory = null;
    private List<SnmpExchange> m_exchangeCollection = null;
    
    /**
     * <p>Constructor for AbstractSnmpScanner.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    protected AbstractSnmpScanner(String name) {
        m_name = name;
        m_exchangeCollection = new ArrayList<>();
    }
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>setSnmpAgentConfigFactory</p>
     *
     * @param snmpPeerFactory a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     */
    public void setSnmpAgentConfigFactory(SnmpAgentConfigFactory snmpPeerFactory) {
        m_snmpAgentConfigFactory = snmpPeerFactory;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.Scanner#init()
     */
    /**
     * <p>init</p>
     */
    @Override
    public void init() {
        Assert.notNull(m_snmpAgentConfigFactory, "snmpAgentConfigFactory must be set");

        
        onInit();
        
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.Scanner#scan(org.opennms.netmgt.provision.ScanContext)
     */
    /** {@inheritDoc} */
    @Override
    public void scan(ScanContext context) throws InterruptedException {
        InetAddress agentAddress = context.getAgentAddress("SNMP");
        if (agentAddress == null) {
            return;
        }

        SnmpAgentConfig agentConfig = m_snmpAgentConfigFactory.getAgentConfig(agentAddress, null);

        try(SnmpWalker walker = SnmpUtils.createWalker(agentConfig, getName(), createCollectionTracker(context))) {
            walker.start();
            walker.waitFor();
        }
    }

    /**
     * @param context
     * @return
     */
    private CollectionTracker createCollectionTracker(final ScanContext scanContext) {
        List<Collectable> trackers = new ArrayList<>();
        for(SnmpExchange exchange : m_exchangeCollection) {
            trackers.add(exchange.createTracker(scanContext));
        }
        return new AggregateTracker(trackers);
     }
    
    public static interface Storer {
        public void storeResult(ScanContext scanContext, SnmpResult res);
    }
    
    public interface SnmpExchange {
        public CollectionTracker createTracker(ScanContext context);
        public void andStoreIn(Storer storer);
    }
    
    /**
     * <p>getSingleInstance</p>
     *
     * @param base a {@link java.lang.String} object.
     * @param inst a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.scan.snmp.AbstractSnmpScanner.SnmpExchange} object.
     */
    protected SnmpExchange getSingleInstance(final String base, final String inst) {
        SnmpExchange exchange = new SnmpExchange() {
            Storer m_storer;
            @Override
            public CollectionTracker createTracker(final ScanContext scanContext) {
                return new SingleInstanceTracker(base, inst) {
                    @Override
                    protected void storeResult(SnmpResult res) {
                        m_storer.storeResult(scanContext, res);
                    }
                    
                };
            }
            @Override
            public void andStoreIn(Storer storer) {
                m_storer = storer;
            }
        };
        
        m_exchangeCollection.add(exchange);
        return exchange;
    }
    


}
