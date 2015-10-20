/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.jmx.JmxUtils;
import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.impl.connection.connectors.DefaultConnectionManager;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Map;

@Distributable
/**
 * This class computes the response time of making a connection to
 * the remote server.  If the connection is successful the reponse time
 * RRD is updated.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class JMXMonitor extends AbstractServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(JMXMonitor.class);

    private class Timer {

        private long startTime;

        private Timer() {
            reset();
        }

        public void reset() {
            this.startTime = System.nanoTime();
        }

        public long getStartTime() {
            return startTime;
        }
    }

    protected abstract String getConnectionName();

    /**
     * {@inheritDoc}
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> map) {

        final NetworkInterface<InetAddress> iface = svc.getNetInterface();
        final InetAddress ipv4Addr = iface.getAddress();

        PollStatus serviceStatus = PollStatus.unavailable();
        try {
            final Timer timer = new Timer();
            final JmxConnectionManager connectionManager = new DefaultConnectionManager(ParameterMap.getKeyedInteger(map, "retry", 3));
            final JmxConnectionManager.RetryCallback retryCallback = new JmxConnectionManager.RetryCallback() {
                @Override
                public void onRetry() {
                    timer.reset();
                }
            };

            try (JmxServerConnectionWrapper connection = connectionManager.connect(getConnectionName(), InetAddrUtils.str(ipv4Addr), JmxUtils.convertToStringMap(map), retryCallback)) {

                connection.getMBeanServerConnection().getMBeanCount();
                long nanoResponseTime = System.nanoTime() - timer.getStartTime();
                serviceStatus = PollStatus.available(nanoResponseTime / 1000000.0);
            } catch (JmxServerConnectionException mbse) {
                // Number of retries exceeded
                String reason = "IOException while polling address: " + ipv4Addr;
                LOG.debug(reason);
                serviceStatus = PollStatus.unavailable(reason);
            }
        } catch (Throwable e) {
            String reason = "Monitor - failed! " + InetAddressUtils.str(ipv4Addr);
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        }
        return serviceStatus;
    }
}
