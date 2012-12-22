/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.net.ssl.SSLSocket;
import org.apache.log4j.Level;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.SocketWrapper;
import org.opennms.core.utils.SslSocketWrapper;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

/**
 * This class is designed to be used by the service poller framework to test the
 * validity of an SSL certificate on a remote interface. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
@Distributable
final public class SSLCertMonitor extends AbstractServiceMonitor {

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

    private static Calendar m_calendar;

    SSLCertMonitor() {
        super();
        m_calendar = null;
    }

    SSLCertMonitor(final Calendar cal) {
        super();
        m_calendar = cal;
    }

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
        final NetworkInterface<InetAddress> iface = svc.getNetInterface();

        if (iface.getType() != NetworkInterface.TYPE_INET) {
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");
        }

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        // Port
        //
        int port = ParameterMap.getKeyedInteger(parameters, PARAMETER_PORT, DEFAULT_PORT);
        if (port == DEFAULT_PORT) {
            throw new RuntimeException("Required parameter 'port' is not present in supplied properties.");
        }

        int validityDays = ParameterMap.getKeyedInteger(parameters, PARAMETER_DAYS, DEFAULT_DAYS);
        if (validityDays <= 0) {
            throw new RuntimeException("Required parameter 'days' must be a positive value.");
        }

        //
        Calendar calValid = GregorianCalendar.getInstance();
        Calendar calCurrent = GregorianCalendar.getInstance();
        if (m_calendar != null) {
            calCurrent.setTimeInMillis(m_calendar.getTimeInMillis());
        }
        calValid.setTimeInMillis(calCurrent.getTimeInMillis());
        calValid.add(Calendar.DAY_OF_MONTH, validityDays);

        Calendar calBefore = GregorianCalendar.getInstance();
        Calendar calAfter = GregorianCalendar.getInstance();

        // Get the address instance.
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        final String hostAddress = InetAddressUtils.str(ipv4Addr);
        log().debug("poll: address=" + hostAddress + ", port=" + port + ", " + tracker);

        // Give it a whirl
        //
        PollStatus serviceStatus = PollStatus.unavailable();

        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            Socket socket = null;
            try {
                tracker.startAttempt();

                socket = new Socket();
                socket.connect(new InetSocketAddress(ipv4Addr, port), tracker.getConnectionTimeout());
                socket.setSoTimeout(tracker.getSoTimeout());
                log().debug("Connected to host: " + ipv4Addr + " on port: " + port);
                SSLSocket sslSocket = (SSLSocket) getSocketWrapper().wrapSocket(socket);

                // We're connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.unresponsive();

                Certificate[] certs = sslSocket.getSession().getPeerCertificates();
                for (int i = 0; i < certs.length && !serviceStatus.isAvailable(); i++) {
                    if (certs[i] instanceof X509Certificate) {
                        X509Certificate certx = (X509Certificate) certs[i];
                        log().debug("Checking validity against dates: [current: " + calCurrent.getTime() +
                                    ", valid: " + calValid.getTime() +"], NotBefore: " + certx.getNotBefore() +
                                    ", NotAfter: " + certx.getNotAfter());
                        calBefore.setTime(certx.getNotBefore());
                        calAfter.setTime(certx.getNotAfter());
                        if (calCurrent.before(calBefore)) {
                            serviceStatus = logDown(Level.WARN, "Certificate is invalid, current time is before start time");
                            break;
                        } else if (calCurrent.before(calAfter)) {
                            if (calValid.before(calAfter)) {
                                serviceStatus = logUp(Level.DEBUG, tracker.elapsedTimeInMillis(), "Certificate is valid, and does not expire before validity check date");
                                break;
                            } else {
                                serviceStatus = logDown(Level.ERROR, "Certificate is valid, but will expire in " + validityDays + " days.");
                                break;
                            }
                        } else {
                            serviceStatus = logDown(Level.ERROR, "Certificate has expired.");
                            break;
                        }
                    }
                }

            } catch (NoRouteToHostException e) {
                serviceStatus = logDown(Level.WARN, "No route to host exception for address " + hostAddress, e);
                break; // Break out of for(;;)
            } catch (InterruptedIOException e) {
                serviceStatus = logDown(Level.DEBUG, "did not connect to host with " + tracker);
            } catch (ConnectException e) {
                serviceStatus = logDown(Level.DEBUG, "Connection exception for address: " + ipv4Addr, e);
            } catch (IOException e) {
                serviceStatus = logDown(Level.DEBUG, "IOException while polling address: " + ipv4Addr, e);
            } finally {
                try {
                    // Close the socket
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.fillInStackTrace();
                    log().debug("poll: Error closing socket." + e);
                }
            }
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected SocketWrapper getSocketWrapper() {
        return new SslSocketWrapper();
    }

    public void setCalendar(final Calendar cal) {
        m_calendar = cal;
    }

    /**
     *
     * @return Calendar
     */
    public Calendar getCalendar() {
        return m_calendar;
    }

}
