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

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.poller.Distributable;
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
@Distributable
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
