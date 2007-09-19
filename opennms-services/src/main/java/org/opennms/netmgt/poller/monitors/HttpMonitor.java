//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2004 May 05: Switch from SocketChannel to Socket with connection timeout.
// 2003 Jul 21: Explicitly closed socket.
// 2003 Jul 18: Enabled retries for monitors.
// 2003 Jul 02: Fixed a ClassCastException.
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
// 2002 Nov 14: Used non-blocking I/O socket channel classes.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the HTTP service on remote interfaces. The class implements the ServiceMonitor interface
 * that allows it to be used along with other plug-ins by the service poller framework.
 * 
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 *  
 */
@Distributable
public class HttpMonitor extends IPv4Monitor {

    /**
     * Default HTTP ports.
     */
    private static final int[] DEFAULT_PORTS = { 80, 8080, 8888};

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default URL to 'GET'
     */
    private static final String DEFAULT_URL = "/";

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting for data from the
     * monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on read()
    
    /**
     * Poll the specified address for HTTP service availability.
     * 
     * During the poll an attempt is made to connect on the specified port(s) (by default TCP
     * ports 80, 8080, 8888). If the connection request is successful, an HTTP 'GET' command is
     * sent to the interface. The response is parsed and a return code extracted and verified.
     * Provided that the interface's response is valid we set the service status to
     * SERVICE_AVAILABLE and return.
     * @param parameters
     *            The package parameters (timeout, retry, and others) to be used for this poll.
     * @return The availability of the interface and if a transition event should be suppressed.
     *  
     */
    public PollStatus poll(MonitoredService svc, Map parameters) {
        NetworkInterface iface = svc.getNetInterface();

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4) {
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");
        }


        String cmd = buildCommand(iface, parameters);

        // Cycle through the port list
        //
        int serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
        String reason = null;
        int currentPort = -1;
        long nanoResponseTime = -1;
        for (int portIndex = 0; portIndex < getPorts(parameters).length && serviceStatus != PollStatus.SERVICE_AVAILABLE; portIndex++) {
            currentPort = getPorts(parameters)[portIndex];

            if (log().isDebugEnabled()) {
                log().debug("Port = " + currentPort + ", Address = " + getIpv4Addr(iface) + ", Timeout = " + getTimeout(parameters) + ", Retry = " + getRetries(parameters));
            }

            for (int attempts = 0; attempts <= getRetries(parameters) && serviceStatus != PollStatus.SERVICE_AVAILABLE; attempts++) {
                Socket socket = null;
                try {
                    socket = createSocket(iface, parameters, currentPort);
                    socket.connect(new InetSocketAddress(getIpv4Addr(iface), currentPort), getTimeout(parameters));
                    socket = wrapSocket(socket);
                    log().debug("HttpMonitor: connected to host: " + getIpv4Addr(iface) + " on port: " + currentPort);

                    // We're connected, so upgrade status to unresponsive
                    serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;

                    //
                    // Issue HTTP 'GET' command and check the return code in the response
                    //
                    long nanoSentTime = System.nanoTime();
                    socket.getOutputStream().write(cmd.getBytes());

                    //
                    // Get a buffered input stream that will read a line
                    // at a time
                    //
                    BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = lineRdr.readLine();
                    nanoResponseTime = System.nanoTime() - nanoSentTime;
                    if (line == null) {
                        continue;
                    }

                    if (log().isDebugEnabled()) {
                        log().debug("poll: response= " + line);
                        log().debug("poll: responseTime= " + (nanoResponseTime / 1000000) + "ms");
                    }

                    if (line.startsWith("HTTP/")) {
                        StringTokenizer t = new StringTokenizer(line);
                        t.nextToken();

                        int serverResponseValue = -1;
                        try {
                            serverResponseValue = Integer.parseInt(t.nextToken());
                        } catch (NumberFormatException nfE) {
                            log().info("Error converting response code from host = " + getIpv4Addr(iface) + ", response = " + line);
                        }
                        
                        if (SnmpPeerFactory.matchNumericListOrRange(String.valueOf(serverResponseValue), getResponse(parameters))) {
                            serviceStatus = PollStatus.SERVICE_AVAILABLE;
                        } else {
                            serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
                            StringBuffer sb = new StringBuffer();
                            sb.append("HTTP response value: ");
                            sb.append(serverResponseValue);
                            sb.append(". Expecting: ");
                            sb.append(getResponse(parameters));
                            sb.append(".");
                            reason = sb.toString();
                        }
                    }

                    if (serviceStatus == PollStatus.SERVICE_AVAILABLE && getResponseText(parameters) != null && getResponseText(parameters).length() > 0) {
                        // This loop will rip through the rest of the Response Header
                        //
                        do {
                            line = lineRdr.readLine();
                            
                            if (isVerbose(parameters)) {
                                log().debug("\theader: "+line);
                            }

                        } while (line != null && line.length() != 0);
                        if (line == null) {
                            continue;
                        }

                        // Now lets rip through the Entity-Body (i.e., content) looking
                        // for the required text.
                        //
                        boolean bResponseTextFound = false;
                        int nullCount = 0;
                        do {
                            line = lineRdr.readLine();
                            
                            if (isVerbose(parameters)) {
                                log().debug("\tbody: "+line);
                            }
                            
                            if (line != null) {
                                if (getResponseText(parameters).charAt(0) == '~') {
                                    if (line.matches(getResponseText(parameters).substring(1))) {
                                        bResponseTextFound = true;
                                    }
                                } else {
                                    int responseIndex = line.indexOf(getResponseText(parameters));
                                    if (responseIndex != -1) {
                                        bResponseTextFound = true;
                                    }
                                }
                            } else {
                                nullCount++;
                            }
                            
                        } while (nullCount < 2 && !bResponseTextFound);

                        // Set the status back to failed
                        //
                        if (!bResponseTextFound) {
                            serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
                            reason = "Matching text: ["+getResponseText(parameters)+"] not found in body of HTTP response";
                        }
                    }
                } catch (NoRouteToHostException e) {
                    log().warn("checkStatus: No route to host exception for address " + getIpv4Addr(iface) + ": " + e.getMessage());
                    portIndex = getPorts(parameters).length; // Will cause outer for(;;) to terminate
                    reason = "No route to host exception";
                    break; // Break out of inner for(;;)
                } catch (InterruptedIOException e) {
                    // Ignore
                    log().info("checkStatus: did not connect to host within timeout: " + getTimeout(parameters) + " attempt: " + attempts);
                    reason = "HTTP connection timeout";
                } catch (ConnectException e) {
                    // Connection Refused. Continue to retry.
                    log().warn("Connection exception for " + getIpv4Addr(iface) + ":" + getPorts(parameters)[portIndex] + ":"+ e.getMessage());
                    reason = "HTTP connection exception on port: "+getPorts(parameters)[portIndex]+": "+e.getMessage();
                } catch (IOException e) {
                    // Ignore
                    //
                    e.fillInStackTrace();
                    log().warn("IOException while polling address " + getIpv4Addr(iface), e);
                    reason = "IOException while polling address: "+getIpv4Addr(iface)+": "+e.getMessage();
                } finally {
                    try {
                        // Close the socket
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.fillInStackTrace();
                        log().warn("Error closing socket connection", e);
                    }
                }

            } // end for (attempts)
        } // end for (ports)

        // Add the 'qualifier' parm to the parameter map. This parm will
        // contain the port on which the service was found if AVAILABLE or
        // will contain a comma delimited list of the port(s) which were
        // tried if the service is UNAVAILABLE
        //
        if (serviceStatus == PollStatus.SERVICE_UNAVAILABLE) {
            //
            // Build port string
            //
            StringBuffer testedPorts = new StringBuffer();
            for (int i = 0; i < getPorts(parameters).length; i++) {
                if (i == 0) {
                    testedPorts.append(getPorts(parameters)[0]);
                } else {
                    testedPorts.append(',').append(getPorts(parameters)[i]);
                }
            }

            // Add to parameter map
            parameters.put("qualifier", testedPorts.toString());
            reason += "/Ports: "+testedPorts.toString();
            log().debug("checkStatus: Reason: \""+reason+"\"");
        } else if (serviceStatus == PollStatus.SERVICE_AVAILABLE) {
            parameters.put("qualifier", Integer.toString(currentPort));
        }

        //
        // return the status of the service
        //
        PollStatus ps = PollStatus.get(serviceStatus, reason);
        ps.setProperty("response-time", nanoResponseTime / 1000000.0);
        return ps;
    }
    
    

    protected Socket wrapSocket(final Socket socket) throws IOException {
        return socket;
    }

    protected Socket createSocket(final NetworkInterface iface, final Map<String, String> parameters, final int currentPort) throws IOException, SocketException {
        Socket socket;
        socket = new Socket();
        socket.setSoTimeout(getTimeout(parameters));
        return socket;
    }

    private boolean isVerbose(final Map<String, String> parameters) {
        final String verbose = ParameterMap.getKeyedString(parameters, "verbose", null);
        return (verbose != null && verbose.equalsIgnoreCase("true")) ? true : false;
    }

    private String buildCommand(final NetworkInterface iface, final Map<String, String> parameters) {
        
        /*
         * Sorting this map just in case the poller gets changed and the Map
         * is no longer a TreeMap.
         */
        Map<String, String> sortedParameters = new TreeMap<String, String>(parameters);
        // Following a successful poll 'currentPort' will contain the port on
        // the remote host that was successfully queried
        //
        String cmd = "GET " + getUrl(parameters) + " HTTP/1.1\r\n";
        cmd += "Connection: CLOSE \r\n";

        if (getVirtualHost(parameters) != null) {
            cmd = cmd + "Host: " + getVirtualHost(parameters) +"\r\n";
        } else {
            cmd += "Host: " + getIpv4Addr(iface).getHostName() +"\r\n";
        }
        
        cmd += "User-Agent: "+getUserAgent(parameters) +"\r\n";
        
        if (getBasicAuthentication(parameters) != null) {
            cmd += "Authorization: Basic "+getBasicAuthentication(parameters) +"\r\n";
        }

        for (Iterator<String> it = sortedParameters.keySet().iterator(); it.hasNext();) {
            String parmKey = (String) it.next();
            if (parmKey.matches("header[0-9]+$")) {
                cmd += getHeader(parameters, parmKey)+"\r\n";
            }
        }
        
        cmd = cmd + "\r\n";
        log().debug("checkStatus: cmd:\n" + cmd);
        return cmd;
    }

    private String getUserAgent(final Map<String, String> parameters) {
        String agent = ParameterMap.getKeyedString(parameters, "user-agent", null);
        if (agent == null || "".equals(agent)) {
            return "OpenNMS HttpMonitor";
        }
        return agent;
    }

    protected String getBasicAuthentication(final Map<String, String> parameters) {
        String credentials = ParameterMap.getKeyedString(parameters, "basic-authentication", null);
        if (credentials != null && !"".equals(credentials)) {
            return new String(Base64.encodeBase64(credentials.getBytes()));
        } else {
            String user = ParameterMap.getKeyedString(parameters, "user", null);
            if (user == null || "".equals(user)) {
                return null;
            }
            
            String passwd = ParameterMap.getKeyedString(parameters, "password", "");
            
            return new String(Base64.encodeBase64((user+":"+passwd).getBytes()));
        }
    }

    private InetAddress getIpv4Addr(final NetworkInterface iface) {
        return (InetAddress) iface.getAddress();
    }

    private String getHeader(final Map<String, String> parameters, String key) {
        return ParameterMap.getKeyedString(parameters, key, null);
    }
    
/*    private String getHeader(Map parameters, int num) {
        return ParameterMap.getKeyedString(parameters, "header"+String.valueOf(num), null);
    }

*/
    private String getVirtualHost(final Map<String, String> parameters) {
        String virtualHost = ParameterMap.getKeyedString(parameters, "host-name", null);
        if (virtualHost == null || "".equals(virtualHost)) {
            //try deprecated parameter
            virtualHost = ParameterMap.getKeyedString(parameters, "host name", null);
        }
        if (virtualHost == null || "".equals(virtualHost)) {
            return null;
        }
        return virtualHost;
    }

    private String getResponseText(final Map<String, String> parameters) {
        String responseText = ParameterMap.getKeyedString(parameters, "response-text", null);
        if (responseText == null) {
            //try depricated parameter
            responseText = ParameterMap.getKeyedString(parameters, "response text", null);
        }
        return responseText;
    }

    private String getResponse(final Map<String, String> parameters) {
        return ParameterMap.getKeyedString(parameters, "response", getDefaultResponseRange(getUrl(parameters)));
    }

    private String getUrl(final Map<String, String> parameters) {
        return ParameterMap.getKeyedString(parameters, "url", DEFAULT_URL);
    }

    protected int[] getPorts(final Map<String, String> parameters) {
        return ParameterMap.getKeyedIntegerArray(parameters, "port", DEFAULT_PORTS);
    }

    private int getTimeout(final Map<String, String> parameters) {
        return ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
    }

    private int getRetries(final Map<String, String> parameters) {
        return ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
    }

    private String getDefaultResponseRange(String url) {
        if (url == null || url.equals(DEFAULT_URL)) {
            return "100-499";
        }
        return "100-399";
    }

    /**
     * Set to true if "response" property has a valid return code specified.
       By default response will be deemed valid if the return code
       falls in the range: 99 < rc < 500
       This is based on the following information from RFC 1945 (HTTP 1.0)
          HTTP 1.0 GET return codes:
              1xx: Informational - Not used, future use
              2xx: Success
              3xx: Redirection
              4xx: Client error
              5xx: Server error

     * @param response
     * @return
     */
/*    private boolean isResponseParameterStrict(int response) {
        return (response > 99 && response < 600);
    }
*/
}
