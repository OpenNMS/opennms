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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.CollectionMath;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
@Component
final public class StrafePingMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(StrafePingMonitor.class);
    private static final int DEFAULT_MULTI_PING_COUNT = 20;
    private static final long DEFAULT_PING_INTERVAL = 50;
    private static final int DEFAULT_FAILURE_PING_COUNT = 20;

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
     * IMCP 'ping' requests. A JSDT session with two channels (send/receive) is
     * utilized for passing poll requests and receiving poll replies from
     * discovery. All exchanges are SOAP/XML compliant.
     * </P>
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus serviceStatus = PollStatus.unavailable(null);
        InetAddress host = svc.getAddress();
        List<Number> responseTimes = null;

        try {

            // get parameters
            //
            long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
            int count = ParameterMap.getKeyedInteger(parameters, "ping-count", DEFAULT_MULTI_PING_COUNT);
            long pingInterval = ParameterMap.getKeyedLong(parameters, "wait-interval", DEFAULT_PING_INTERVAL);
            int failurePingCount = ParameterMap.getKeyedInteger(parameters, "failure-ping-count", DEFAULT_FAILURE_PING_COUNT);
            final int packetSize = ParameterMap.getKeyedInteger(parameters, "packet-size", PingConstants.DEFAULT_PACKET_SIZE);
            final int dscp = ParameterMap.getKeyedDecodedInteger(parameters, "dscp", 0);
            final boolean allowFragmentation = ParameterMap.getKeyedBoolean(parameters, "allow-fragmentation", true);

            responseTimes = new ArrayList<>(pingerFactory.get().getInstance(dscp, allowFragmentation).parallelPing(host, count, timeout, pingInterval, packetSize));

            if (CollectionMath.countNull(responseTimes) >= failurePingCount) {
                LOG.debug("Service {} on interface {} is down, but continuing to gather latency data", svc.getSvcName(), svc.getIpAddr());
                serviceStatus = PollStatus.unavailable("the failure ping count (" + failurePingCount + ") was reached");
            } else {
            	serviceStatus = PollStatus.available();
            }
            
            Collections.sort(responseTimes, new Comparator<Number>() {

                @Override
                public int compare(Number arg0, Number arg1) {
                    if (arg0 == null) {
                        return -1;
                    } else if (arg1 == null) {
                        return 1;
                    } else if (arg0.doubleValue() == arg1.doubleValue()) {
                        return 0;
                    } else {
                        return arg0.doubleValue() < arg1.doubleValue() ? -1 : 1;
                    }
                }

            });

            Map<String, Number> returnval = new LinkedHashMap<String, Number>();
            for (int i = 0; i < responseTimes.size(); i++) {
                returnval.put("ping" + (i + 1), responseTimes.get(i));
            }
            returnval.put("loss", CollectionMath.countNull(responseTimes));
            returnval.put("median", CollectionMath.median(responseTimes));
            returnval.put(PollStatus.PROPERTY_RESPONSE_TIME, CollectionMath.average(responseTimes));

            serviceStatus.setProperties(returnval);
        } catch (Throwable e) {
            LOG.debug("failed to ping {}", host, e);
            serviceStatus = PollStatus.unavailable(String.format("Failed to ping %s: %s", host, e.getMessage()));
        }

        return serviceStatus;
    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = Suppliers.ofInstance(pingerFactory);
    }

}
