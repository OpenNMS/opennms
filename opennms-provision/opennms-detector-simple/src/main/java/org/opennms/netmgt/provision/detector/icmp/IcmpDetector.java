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
package org.opennms.netmgt.provision.detector.icmp;

import java.net.InetAddress;

import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>IcmpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IcmpDetector extends SyncAbstractDetector {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpDetector.class);

    private PingerFactory pingerFactory;
    private int m_dscp;
    private boolean m_allowFragmentation;

    /**
     * <p>Constructor for IcmpDetector.</p>
     */
    public IcmpDetector() {
        super("ICMP", -1);
        init();
    }
    
    public void setDscp(final int dscp) {
        m_dscp = dscp;
    }

    public int getDscp() {
        return m_dscp;
    }

    public boolean isAllowFragmentation() {
        return m_allowFragmentation;
    }

    public void setAllowFragmentation(final boolean allowFragmentation) {
        m_allowFragmentation = allowFragmentation;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isServiceDetected(InetAddress address) {
        LOG.debug("isServiceDetected: Testing ICMP based service for address: {}...", address);

        boolean found = false;
        try {
            for(int i = 0; i < getRetries() && !found; i++) {
                final Number retval = pingerFactory.getInstance(m_dscp, m_allowFragmentation).ping(address, getTimeout(), getRetries());
                LOG.debug("isServiceDetected: Response time for address: {} is: {}.", address, retval);
                
                if (retval != null) {
                    found = true;
                }
            }
            
            LOG.info("isServiceDetected: ICMP based service for address: {} is detected: {}.", address, found);

        } catch (final InterruptedException e) {
            LOG.info("isServiceDetected: ICMP based service for address: {} is detected: {}. Received an InterruptedException.", address, false);
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            LOG.info("isServiceDetected: ICMP based service for address: {} is detected: {}. Received an Exception {}.", address, false, e);
        }
        
        return found;
    }

    @Override
    protected void onInit() {
        setTimeout(PingConstants.DEFAULT_TIMEOUT);
        setRetries(PingConstants.DEFAULT_RETRIES);
    }

    @Override
    public void dispose() {
        // pass
    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = pingerFactory;
    }
}
