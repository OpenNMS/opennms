/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.CollectionMath;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

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
@Distributable
final public class StrafePingMonitor extends AbstractServiceMonitor {
    private static final int DEFAULT_MULTI_PING_COUNT = 20;
    private static final long DEFAULT_PING_INTERVAL = 50;
    private static final int DEFAULT_FAILURE_PING_COUNT = 20;

    /**
     * Constructs a new monitor.
     *
     * @throws java.io.IOException if any.
     */
    public StrafePingMonitor() throws IOException {
    }

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
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
        PollStatus serviceStatus = PollStatus.unavailable();
        InetAddress host = (InetAddress) iface.getAddress();
        List<Number> responseTimes = null;

        try {

            // get parameters
            //
            long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
            int count = ParameterMap.getKeyedInteger(parameters, "ping-count", DEFAULT_MULTI_PING_COUNT);
            long pingInterval = ParameterMap.getKeyedLong(parameters, "wait-interval", DEFAULT_PING_INTERVAL);
            int failurePingCount = ParameterMap.getKeyedInteger(parameters, "failure-ping-count", DEFAULT_FAILURE_PING_COUNT);
            
            responseTimes = new ArrayList<Number>(PingerFactory.getInstance().parallelPing(host, count, timeout, pingInterval));

            if (CollectionMath.countNull(responseTimes) >= failurePingCount) {
            	if (log().isDebugEnabled()) {
            		log().debug("Service " +svc.getSvcName()+ " on interface " +svc.getIpAddr()+ " is down, but continuing to gather latency data");
            	}
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
            returnval.put("response-time", CollectionMath.average(responseTimes));

            serviceStatus.setProperties(returnval);
        } catch (Throwable e) {
            log.debug("failed to ping " + host, e);
        }

        return serviceStatus;
    }

}
