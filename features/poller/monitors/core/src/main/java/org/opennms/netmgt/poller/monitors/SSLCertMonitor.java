/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.SocketUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * validity of an SSL certificate on a remote interface. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
@Distributable
public class SSLCertMonitor extends AbstractServiceMonitor {
    
    
    public static final Logger LOG = LoggerFactory.getLogger(SSLCertMonitor.class);

    /**
     * Default port to test for a valid SSL certificate.
     */
    private static final int DEFAULT_PORT = -1;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting for data from the
     * monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on read()

    /**
     * Default number of days before the certificate expires that we mark the service as failed.
     */
    private static final int DEFAULT_DAYS = 7;

    public static final String PARAMETER_PORT = "port";

    public static final String PARAMETER_DAYS = "days";

    public static final String PARAMETER_SERVER_NAME = "server-name";

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for HTTP service availability.
     *
     * During the poll an attempt is made to connect on the specified port. If
     * the connection request is successful, check the X509Certificates provided
     * by our peer and check that our time is between the certificates start and
     * end time.
     * Provided that the interface's response is valid we set the service status to
     * SERVICE_AVAILABLE and return.
     */
    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {
        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        // Port
        int port = ParameterMap.getKeyedInteger(parameters, PARAMETER_PORT, DEFAULT_PORT);
        if (port == DEFAULT_PORT) {
            throw new RuntimeException("Required parameter 'port' is not present in supplied properties.");
        }

        // Remaining days
        int validityDays = ParameterMap.getKeyedInteger(parameters, PARAMETER_DAYS, DEFAULT_DAYS);
        if (validityDays <= 0) {
            throw new RuntimeException("Required parameter 'days' must be a positive value.");
        }

        // Server name (optional)
        final String serverName = PropertiesUtils.substitute(ParameterMap.getKeyedString(parameters, PARAMETER_SERVER_NAME, ""),
                                                             getServiceProperties(svc));

        // Calculate validity range
        Calendar calValid = this.getCalendarInstance();
        Calendar calCurrent = this.getCalendarInstance();
        calValid.setTimeInMillis(calCurrent.getTimeInMillis());
        calValid.add(Calendar.DAY_OF_MONTH, validityDays);

        Calendar calBefore = this.getCalendarInstance();
        Calendar calAfter = this.getCalendarInstance();

        // Get the address instance
        InetAddress ipAddr = svc.getAddress();

        final String hostAddress = InetAddressUtils.str(ipAddr);
        LOG.debug("poll: address={}, port={}, serverName={}, {}", hostAddress, port, serverName, tracker);

        // Give it a whirl
        PollStatus serviceStatus = PollStatus.unavailable();
        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            Socket socket = null;
            try {
                tracker.startAttempt();

                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddr, port), tracker.getConnectionTimeout());
                socket.setSoTimeout(tracker.getSoTimeout());
                LOG.debug("Connected to host: {} on port: {}", ipAddr, port);
                SSLSocket sslSocket = SocketUtils.wrapSocketInSslContext(socket, null, null);

                // We're connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.unresponsive();

                // Use the server name as as SNI host name if available
                if (!Strings.isNullOrEmpty(serverName)) {
                    final SSLParameters sslParameters = sslSocket.getSSLParameters();
                    sslParameters.setServerNames(ImmutableList.of(new SNIHostName(serverName)));
                    sslSocket.setSSLParameters(sslParameters);

                    // Check certificates host name
                    if (!new StrictHostnameVerifier().verify(serverName, sslSocket.getSession())) {
                        serviceStatus = PollStatus.unavailable("Host name verification failed - certificate common name is invalid");
                        continue;
                    }
                }

                Certificate[] certs = sslSocket.getSession().getPeerCertificates();
                for (int i = 0; i < certs.length && !serviceStatus.isAvailable(); i++) {
                    if (certs[i] instanceof X509Certificate) {
                        X509Certificate certx = (X509Certificate) certs[i];
                        LOG.debug("Checking validity against dates: [current: {}, valid: {}], NotBefore: {}, NotAfter: {}", calCurrent.getTime(), calValid.getTime(), certx.getNotBefore(), certx.getNotAfter());
                        calBefore.setTime(certx.getNotBefore());
                        calAfter.setTime(certx.getNotAfter());
                        if (calCurrent.before(calBefore)) {
                            LOG.debug("Certificate is invalid, current time is before start time");
                            serviceStatus = PollStatus.unavailable("Certificate is invalid, current time is before start time");
                            break;
                        } else if (calCurrent.before(calAfter)) {
                            if (calValid.before(calAfter)) {
                                LOG.debug("Certificate is valid, and does not expire before validity check date");
                                serviceStatus = PollStatus.available(tracker.elapsedTimeInMillis());
                                break;
                            } else {
                                String reason = "Certificate is valid, but will expire within " + validityDays + " days (" + certx.getNotAfter() + ").";
                                LOG.debug(reason);
                                serviceStatus = PollStatus.unavailable(reason);
                                break;
                            }
                        } else {
                            LOG.debug("Certificate has expired.");
                            serviceStatus = PollStatus.unavailable("Certificate has expired.");
                            break;
                        }
                    }
                }

            } catch (NoRouteToHostException e) {
                String reason = "No route to host exception for address " + hostAddress;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
                break; // Break out of for(;;)
            } catch (InterruptedIOException e) {
                String reason = "did not connect to host with " + tracker;
                LOG.debug(reason);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (ConnectException e) {
                String reason = "Connection exception for address: " + ipAddr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (IOException e) {
                String reason = "IOException while polling address: " + ipAddr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.fillInStackTrace();
                    LOG.debug("poll: Error closing socket.", e);
                }
            }
        }

        return serviceStatus;
    }

    protected Calendar getCalendarInstance() {
        return GregorianCalendar.getInstance();
    }
}
