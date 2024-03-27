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
package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the ICMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class IcmpMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpMonitor.class);

    private Supplier<PingerFactory> pingerFactory = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "pingerFactory", PingerFactory.class));

    /**
     * {@inheritDoc}
     *
     * <P>
     * Poll the specified address for ICMP service availability.
     * </P>
     *
     * <P>
     * The ICMP service monitor relies on Discovery for the actual generation of
     * IMCP 'ping' requests. A JSDT session with two channels (send/recv) is
     * utilized for passing poll requests and receiving poll replies from
     * discovery. All exchanges are SOAP/XML compliant.
     * </P>
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        Number rtt = null;
        InetAddress host = svc.getAddress();

        try {
            
            // get parameters
            //
            int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
            long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
            int packetSize = ParameterMap.getKeyedInteger(parameters, "packet-size", PingConstants.DEFAULT_PACKET_SIZE);
            final int dscp = ParameterMap.getKeyedDecodedInteger(parameters, "dscp", 0);
            final boolean allowFragmentation = ParameterMap.getKeyedBoolean(parameters, "allow-fragmentation", true);

            rtt = pingerFactory.get().getInstance(dscp, allowFragmentation).ping(host, timeout, retries,packetSize);
        } catch (Throwable e) {
            LOG.debug("failed to ping {}", host, e);
            return PollStatus.unavailable(e.getMessage());
        }
        
        if (rtt != null) {
            return PollStatus.available(rtt.doubleValue());
        } else {
            // TODO add a reason code for unavailability
            return PollStatus.unavailable(null);
        }

    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = Suppliers.ofInstance(pingerFactory);
    }
}
