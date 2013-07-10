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

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Map;

import org.opennms.core.utils.DefaultSocketWrapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.SocketWrapper;
import org.opennms.core.utils.TimeoutSocketFactory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of a generic LDAP service on remote interfaces. The class
 * implements the ServiceMonitor interface that allows it to be used along with
 * other plug-ins by the service poller framework.
 *
 * @author <A HREF="jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
public class LdapMonitor extends AbstractServiceMonitor {
    
    
    public static final Logger LOG = LoggerFactory.getLogger(LdapMonitor.class);

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 1;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on
                                                        // read()

    /**
     * Default search base for an LDAP search
     */
    private static final String DEFAULT_BASE = "base";

    /**
     * Default search filter for an LDAP search
     */
    private static final String DEFAULT_FILTER = "(objectclass=*)";

    /**
     * A class to add a timeout to the socket that the LDAP code uses to access
     * an LDAP server
     */
    private class TimeoutLDAPSocket extends TimeoutSocketFactory implements LDAPSocketFactory {
        public TimeoutLDAPSocket(int timeout) {
            super(timeout, getSocketWrapper());
        }
    }

    protected SocketWrapper getSocketWrapper() {
        return new DefaultSocketWrapper();
    }

    protected int determinePort(final Map<String, Object> parameters) {
        return ParameterMap.getKeyedInteger(parameters, "port", LDAPConnection.DEFAULT_PORT);
    }

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for service availability.
     *
     * During the poll an attempt is made to connect the service.
     *
     * Provided that the interface's response is valid we set the service status
     * to SERVICE_AVAILABLE and return.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        int serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
        String reason = null;

        final TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        // get the parameters
        //
        final int ldapVersion = ParameterMap.getKeyedInteger(parameters, "version", LDAPConnection.LDAP_V3);
        final int ldapPort = determinePort(parameters);
        final String searchBase = ParameterMap.getKeyedString(parameters, "searchbase", DEFAULT_BASE);
        final String searchFilter = ParameterMap.getKeyedString(parameters, "searchfilter", DEFAULT_FILTER);

        final String password = (String) parameters.get("password");
        final String ldapDn = (String) parameters.get("dn");

        final Object addressObject = iface.getAddress();
        String address = null;
        if (addressObject instanceof InetAddress)
            address = InetAddressUtils.str(((InetAddress) addressObject));
        else if (addressObject instanceof String)
            address = (String) addressObject;

        // first just try a connection to the box via socket. Just in case there
        // is
        // a no way to route to the address, don't iterate through the retries,
        // as a
        // NoRouteToHost exception will only be thrown after about 5 minutes,
        // thus tying
        // up the thread
        Double responseTime = null;
        Socket socket = null;
        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress((InetAddress) iface.getAddress(), ldapPort), tracker.getConnectionTimeout());
            socket.setSoTimeout(tracker.getSoTimeout());
            LOG.debug("LdapMonitor: connected to host: {} on port: {}", address, ldapPort);

            // We're connected, so upgrade status to unresponsive
            serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;
        

            if (socket != null)
                socket.close();

            // lets detect the service
            LDAPConnection lc = new LDAPConnection(new TimeoutLDAPSocket(tracker.getSoTimeout()));

            
            for (tracker.reset(); tracker.shouldRetry() && !(serviceStatus == PollStatus.SERVICE_AVAILABLE); tracker.nextAttempt()) {
                LOG.debug("polling LDAP on {}, {}", address, tracker);

                // connect to the ldap server
                tracker.startAttempt();
                try {
                    lc.connect(address, ldapPort);
                    LOG.debug("connected to LDAP server {} on port {}", address, ldapPort);
                } catch (LDAPException e) {
			LOG.debug("could not connect to LDAP server {} on port {}", address, ldapPort);
                	reason = "could not connect to LDAP server " + address + " on port " + ldapPort;
                    continue;
                }

                // bind if possible
                if (ldapDn != null && password != null) {
                    try {
                        lc.bind(ldapVersion, ldapDn, password.getBytes());
                        LOG.debug("bound to LDAP server version {} with distinguished name {}", ldapVersion, ldapDn);
                        LOG.debug("poll: responseTime= {}ms", tracker.elapsedTimeInMillis());
                    } catch (LDAPException e) {
                        try {
                            lc.disconnect();
                        } catch (LDAPException ex) {
                            LOG.debug(ex.getMessage());
                        }

                        LOG.debug("could not bind to LDAP server version {} with distinguished name {}", ldapVersion, ldapDn);
                        reason = "could not bind to LDAP server version " + ldapVersion + " with distinguished name " + ldapDn;
                        continue;
                    }
                }

                // do a quick search and see if any results come back
                boolean attributeOnly = true;
                String attrs[] = { LDAPConnection.NO_ATTRS };
                int searchScope = LDAPConnection.SCOPE_ONE;

                LOG.debug("running search {} from {}", searchFilter, searchBase);
                LDAPSearchResults results = null;

                try {
                    results = lc.search(searchBase, searchScope, searchFilter, attrs, attributeOnly);

                    if (results != null && results.hasMore()) {
                        responseTime = tracker.elapsedTimeInMillis();
                        LOG.debug("search yielded {} result(s)", results.getCount());
                        serviceStatus = PollStatus.SERVICE_AVAILABLE;
                    } else {
                        LOG.debug("no results found from search");
                        reason = "No results found from search";
                        serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
                    }
                } catch (LDAPException e) {
                    try {
                        lc.disconnect();
                    } catch (LDAPException ex) {
                        LOG.debug(ex.getMessage());
                    }

                    LOG.debug("could not perform search {} from {}", searchFilter, searchBase);
                    reason = "could not perform search " + searchFilter + " from " + searchBase;
                    continue;
                }

                try {
                    lc.disconnect();
                    LOG.debug("disconected from LDAP server {} on port {}", address, ldapPort);
                } catch (LDAPException e) {
                    LOG.debug(e.getMessage());
                }
            }
        } catch (ConnectException e) {
		LOG.debug("connection refused to host {}", address, e);
        	reason = "connection refused to host " + address;
        } catch (NoRouteToHostException e) {
		LOG.debug("No route to host {}", address, e);
        	reason = "No route to host " + address;
        } catch (InterruptedIOException e) {
		LOG.debug("did not connect to host with {}", tracker);
        	reason = "did not connect to host with "+tracker;
        } catch (Throwable t) {
		LOG.debug("An undeclared throwable exception caught contacting host {}", address, t);
        	reason = "An undeclared throwable exception caught contacting host " + address;
        }
        
        return PollStatus.get(serviceStatus, reason, responseTime);
    }

}
