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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.SocketUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.support.ParameterSubstitutingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * validity of an SSL certificate on a remote interface. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * It also has some limited support for STARTTLS. You can specify a preliminary
 * message and expected response (optional, but required for some protocols,
 * notably XMPP) as well as the actual STARTTLS verb and the expected response.
 * Assuming the exchanges pass (only the latter is required for all protocols)
 * normal TLS negotiation then takes place to determine certificate expiration
 * validity.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:dschlenk@convergeone.com">David Schlenk</a>
 */
public class SSLCertMonitor extends ParameterSubstitutingMonitor {
    
    
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

    public static final String PARAMETER_RESOLVE_SERVER_NAME = "resolve-server-name";

    public static final String PARAMETER_STLS_INIT = "starttls-preamble";

    public static final String PARAMETER_STLS_INIT_RESP = "starttls-preamble-response";

    public static final String PARAMETER_STLS_START = "starttls-start";

    public static final String PARAMETER_STLS_START_RESP = "starttls-start-response";
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

        final String stlsInitiate = PropertiesUtils.substitute(resolveKeyedString(parameters, PARAMETER_STLS_INIT, ""),
                                                             getServiceProperties(svc));

        final String stlsInitExpectedResp = PropertiesUtils.substitute(resolveKeyedString(parameters, PARAMETER_STLS_INIT_RESP, ""),
                                                             getServiceProperties(svc));

        final String tlsStart = PropertiesUtils.substitute(resolveKeyedString(parameters, PARAMETER_STLS_START, ""),
                                                             getServiceProperties(svc));

        final String tlsStartResp = PropertiesUtils.substitute(resolveKeyedString(parameters, PARAMETER_STLS_START_RESP, ""),
                                                             getServiceProperties(svc));

        final boolean resolveHostName = ParameterMap.getKeyedBoolean(parameters, PARAMETER_RESOLVE_SERVER_NAME, false);

        // Calculate validity range
        Calendar calValid = this.getCalendarInstance();
        Calendar calCurrent = this.getCalendarInstance();
        calValid.setTimeInMillis(calCurrent.getTimeInMillis());
        calValid.add(Calendar.DAY_OF_MONTH, validityDays);

        Calendar calBefore = this.getCalendarInstance();
        Calendar calAfter = this.getCalendarInstance();

        PollStatus serviceStatus = PollStatus.unavailable();

        // Get the address instance
        InetAddress ipAddr;
        if (resolveHostName) {
            // Look up the hostname provided in server-name parameter
            try {
                ipAddr = InetAddress.getByName(serverName);
            }
            catch (UnknownHostException e) {
                LOG.error("Failed to resolve hostname in server-name: {}", serverName);
                serviceStatus = PollStatus.unavailable("Failed to resolve server-name '" + serverName + "' to an addresss and resolve-server-name is true");
                return serviceStatus;
            }
        }
        else {
            ipAddr = svc.getAddress();
        }

        final String hostAddress = InetAddressUtils.str(ipAddr);
        LOG.debug("poll: address={}, port={}, serverName={}, resolve={} {}", hostAddress, port, serverName, resolveHostName, tracker);

        // Give it a whirl
        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
            Socket socket = null;
            BufferedReader r = null;
            BufferedWriter wr = null;
            try {
                tracker.startAttempt();

                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddr, port), tracker.getConnectionTimeout());
                socket.setSoTimeout(tracker.getSoTimeout());
                r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                LOG.debug("Connected to host: {} on port: {}", ipAddr, port);

                // We're connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.unresponsive();

                // xmpp (and probably others) make you find out if the server supports STARTTLS
                // at the protocol level before actually trying to start it
                boolean stlsSupported = SocketUtils.validResponse(stlsInitiate, stlsInitExpectedResp, r, wr) &&
                    SocketUtils.validResponse(tlsStart, tlsStartResp, r, wr);
                if (!stlsSupported) {
                    serviceStatus = PollStatus.unavailable("STARTTLS requested, but server does not support STARTTLS.");
                    return serviceStatus;
                }

                SSLSocket sslSocket = SocketUtils.wrapSocketInSslContext(socket, null, null);
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
                        String subject = "";
                        if (certx.getSubjectDN() != null && certx.getSubjectDN().getName() != null) {
                            subject = certx.getSubjectDN().getName();
                        }
                        String issuer = "";
                        if (certx.getIssuerDN() != null && certx.getIssuerDN().getName() != null) {
                            issuer = certx.getIssuerDN().getName();
                        }
                        String fprint = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(certx.getEncoded())).toLowerCase();
                        StringBuilder reasonBuilder = new StringBuilder();
                        if (certx.getNotBefore() == null || certx.getNotAfter() == null) {
                            reasonBuilder.append("Unable to check for expiration: one or both of notBefore and notAfter are null for certificate with fingerprint '")
                                .append(fprint).append("' issued to ").append(subject).append(" by ").append(issuer).append(".");
                            serviceStatus = PollStatus.unavailable(reasonBuilder.toString());
                            break;
                        }
                        LOG.debug("Checking validity against dates: [current: {}, valid: {}], NotBefore: {}, NotAfter: {}", calCurrent.getTime(), calValid.getTime(), certx.getNotBefore(), certx.getNotAfter());
                        calBefore.setTime(certx.getNotBefore());
                        calAfter.setTime(certx.getNotAfter());
                        if (calCurrent.before(calBefore)) {
                            reasonBuilder.append("Certificate with fingerprint '").append(fprint).append("' issued to ")
                                    .append(subject).append(" by ").append(issuer)
                                    .append(" is not yet valid. Current time is before start time. It is valid from ")
                                    .append(certx.getNotBefore().toString()).append(" until ").append(certx.getNotAfter()).append(".");
                            LOG.debug(reasonBuilder.toString());
                            serviceStatus = PollStatus.unavailable(reasonBuilder.toString());
                            break;
                        } else if (calCurrent.before(calAfter)) {
                            if (calValid.before(calAfter)) {
                                reasonBuilder.append("Certificate with fingerprint '").append(fprint).append("' issued to ")
                                        .append(subject).append(" by ").append(issuer)
                                        .append(" is valid. It is valid from ")
                                        .append(certx.getNotBefore().toString()).append(" until ").append(certx.getNotAfter()).append(".");
                                LOG.debug(reasonBuilder.toString());
                                serviceStatus = PollStatus.available(tracker.elapsedTimeInMillis());
                                break;
                            } else {
                                reasonBuilder.append("Certificate with fingerprint '").append(fprint).append("' issued to ")
                                        .append(subject).append(" by ").append(issuer)
                                        .append(" is valid, but will expire within ").append(validityDays).append(" days. It is valid from ")
                                        .append(certx.getNotBefore().toString()).append(" until ").append(certx.getNotAfter()).append(".");
                                LOG.debug(reasonBuilder.toString());
                                serviceStatus = PollStatus.unavailable(reasonBuilder.toString());
                                break;
                            }
                        } else {
                            reasonBuilder.append("Certificate with fingerprint '").append(fprint).append("' issued to ")
                                    .append(subject).append(" by ").append(issuer)
                                    .append(" is no longer valid. It was valid from ").append(certx.getNotBefore().toString())
                                    .append(" until ").append(certx.getNotAfter()).append(".");
                            LOG.debug(reasonBuilder.toString());
                            serviceStatus = PollStatus.unavailable(reasonBuilder.toString());
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
            } catch (CertificateEncodingException e) {
                String reason = "CertificateEncodingException while polling address: " + ipAddr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (NoSuchAlgorithmException e) {
                String reason = "NoSuchAlgorithException (SHA-1) while polling address: " + ipAddr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } finally {
                try {
                    if (r != null) {
                        r.close();
                    }
                    if (wr != null) {
                        wr.close();
                    }
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
