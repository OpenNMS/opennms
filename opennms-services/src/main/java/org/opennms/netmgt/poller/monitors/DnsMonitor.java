/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the DNS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable
final public class DnsMonitor extends AbstractServiceMonitor {
    /**
     * Default DNS port.
     */
    private static final int DEFAULT_PORT = 53;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 5000;
    
    /**
     * Default list of fatal response codes. Original behavior was hard-coded
     * so that only a ServFail(2) was fatal, so make that the configurable
     * default even though it makes little sense.
     */
    private static final int[] DEFAULT_FATAL_RESP_CODES = { 2 };

    /**
     * {@inheritDoc}
     *
     * <P>
     * Poll the specified address for DNS service availability.
     * </P>
     *
     * <P>
     * During the poll an DNS address request query packet is generated for
     * hostname 'localhost'. The query is sent via UDP socket to the interface
     * at the specified port (by default UDP port 53). If a response is
     * received, it is parsed and validated. If the DNS lookup was successful
     * the service status is set to SERVICE_AVAILABLE and the method returns.
     * </P>
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        // get the parameters
        //
        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);

        // Host to lookup?
        //
        String lookup = ParameterMap.getKeyedString(parameters, "lookup", null);
        if (lookup == null || lookup.length() == 0) {
            // Get hostname of local machine for future DNS lookups
        	lookup = InetAddressUtils.getLocalHostAddressAsString();
        	if (lookup == null) {
        		throw new UnsupportedOperationException("Unable to look up local host address.");
        	}
        }

        // What do we consider fatal?
        //
        final List<Integer> fatalCodes = new ArrayList<Integer>();
        for (final int code : ParameterMap.getKeyedIntegerArray(parameters, "fatal-response-codes", DEFAULT_FATAL_RESP_CODES)) {
            fatalCodes.add(code);
        }
        
        // get the address and DNS address request
        //
        final InetAddress addr = iface.getAddress();

        PollStatus serviceStatus = null;
        serviceStatus = pollDNS(timeoutTracker, port, addr, lookup, fatalCodes);

        if (serviceStatus == null) {
            serviceStatus = logDown(Level.DEBUG, "Never received valid DNS response for address: " + addr);
        }
        
        // 
        //
        // return the status of the service
        //
        return serviceStatus;
    }

    private PollStatus pollDNS(final TimeoutTracker timeoutTracker, final int port, final InetAddress address, final String lookup, final List<Integer> fatalCodes) {
    	final String addr = InetAddressUtils.str(address);
        for (timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
            try {
                final Name name = Name.fromString(lookup, Name.root);
                final SimpleResolver resolver = new SimpleResolver();
                resolver.setAddress(new InetSocketAddress(addr, port));
                resolver.setLocalAddress((InetSocketAddress)null);
                double timeout = timeoutTracker.getSoTimeout()/1000;
                resolver.setTimeout((timeout < 1 ? 1 : (int) timeout));
                final Record question = Record.newRecord(name, Type.A, DClass.IN);
                final Message query = Message.newQuery(question);

                timeoutTracker.startAttempt();
                final Message response = resolver.send(query);
                double responseTime = timeoutTracker.elapsedTimeInMillis();

                final Integer rcode = response.getHeader().getRcode();
                LogUtils.debugf(this, "received response code: %s", rcode);

                if (fatalCodes.contains(rcode)) {
                    return logDown(Level.DEBUG, "Received an invalid DNS response for address: " + addr);
                } else {
                    return logUp(Level.DEBUG, responseTime, "valid DNS request received, responseTime= " + responseTime + "ms");
                }
            } catch (final InterruptedIOException e) {
                // No response received, retry without marking the poll failed. If we get this condition over and over until 
                // the retries are exhausted, it will leave serviceStatus null and we'll get the log message at the bottom 
            } catch (final NoRouteToHostException e) {
                return logDown(Level.WARN, "No route to host exception for address: " + addr, e);
            } catch (final ConnectException e) {
                return logDown(Level.WARN, "Connection exception for address: " + addr, e);
            } catch (final IOException e) {
                return logDown(Level.WARN, "IOException while polling address: " + addr + " " + e.getMessage(), e);
            }
        }
       
        return logDown(Level.DEBUG, "Never received valid DNS response for address: " + addr);
    }

    
}
