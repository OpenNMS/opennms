//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 03: Added new parameter for determining Host: HTTP 1.1 header command
//              Encapsulated HTTP workings in new object
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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.Base64;
import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the HTTP service on remote interfaces. The class implements the ServiceMonitor interface
 * that allows it to be used along with other plug-ins by the service poller framework.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
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

    public static final String PARAMETER_VERBOSE = "verbose";
    public static final String PARAMETER_USER_AGENT = "user-agent";
    public static final String PARAMETER_BASIC_AUTHENTICATION = "basic-authentication";
    public static final String PARAMETER_USER = "user";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_RESOLVE_IP = "resolve-ip";
    public static final String PARAMETER_HOST_NAME = "host-name";
    public static final String PARAMETER_RESPONSE_TEXT = "response-text";
    public static final String PARAMETER_RESPONSE = "response";
    public static final String PARAMETER_URL = "url";
    public static final String PARAMETER_PORT = "port";

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for HTTP service availability.
     *
     * During the poll an attempt is made to connect on the specified port(s) (by default TCP
     * ports 80, 8080, 8888). If the connection request is successful, an HTTP 'GET' command is
     * sent to the interface. The response is parsed and a return code extracted and verified.
     * Provided that the interface's response is valid we set the service status to
     * SERVICE_AVAILABLE and return.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        if (iface.getType() != NetworkInterface.TYPE_INET) {
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");
        }

        // Cycle through the port list
        //
        int currentPort = -1;
        HttpMonitorClient httpClient = new HttpMonitorClient(iface, new TreeMap<String, Object>(parameters));

        for (int portIndex = 0; portIndex < determinePorts(httpClient.getParameters()).length && httpClient.getPollStatus() != PollStatus.SERVICE_AVAILABLE; portIndex++) {
            currentPort = determinePorts(httpClient.getParameters())[portIndex];

            httpClient.setTimeoutTracker(new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT));
            log().debug("Port = " + currentPort + ", Address = " + (iface.getAddress()) + ", " + httpClient.getTimeoutTracker());
            
            httpClient.setCurrentPort(currentPort);

            for(httpClient.getTimeoutTracker().reset();
                httpClient.getTimeoutTracker().shouldRetry() && httpClient.getPollStatus() != PollStatus.SERVICE_AVAILABLE; 
                httpClient.getTimeoutTracker().nextAttempt()) {
                
                try {
                    httpClient.getTimeoutTracker().startAttempt();                    
                    httpClient.connect();
                    log().debug("HttpMonitor: connected to host: " + (iface.getAddress()) + " on port: " + currentPort);

                    httpClient.sendHttpCommand();
                    
                    if (httpClient.isEndOfStream()) {
                        continue;
                    }

                    httpClient.setResponseTime(httpClient.getTimeoutTracker().elapsedTimeInMillis());
                    logResponseTimes(httpClient.getResponseTime(), httpClient.getCurrentLine());

                    if (httpClient.getPollStatus() == PollStatus.SERVICE_AVAILABLE && StringUtils.isNotBlank(httpClient.getResponseText())) {
                        httpClient.setPollStatus(PollStatus.SERVICE_UNAVAILABLE);
                        httpClient.readLinedMatching();
                        
                        if (httpClient.isEndOfStream()) {
                            continue;
                        }

                        httpClient.read();

                        if (!httpClient.isResponseTextFound()) {
                            String message = "Matching text: ["+httpClient.getResponseText()+"] not found in body of HTTP response";
                            log().debug(message);
                            httpClient.setReason("Matching text: ["+httpClient.getResponseText()+"] not found in body of HTTP response");
                        }
                    }
                    
                } catch (NoRouteToHostException e) {
                    log().warn("checkStatus: No route to host exception for address " + (iface.getAddress()), e);
                    portIndex = determinePorts(httpClient.getParameters()).length; // Will cause outer for(;;) to terminate
                    httpClient.setReason("No route to host exception");
                } catch (InterruptedIOException e) {
                    log().info("checkStatus: did not connect to host with " + httpClient.getTimeoutTracker().toString(), e);
                    httpClient.setReason("HTTP connection timeout");
                } catch (ConnectException e) {
                    log().warn("Connection exception for " + (iface.getAddress()) + ":" + determinePorts(httpClient.getParameters())[portIndex], e);
                    httpClient.setReason("HTTP connection exception on port: "+determinePorts(httpClient.getParameters())[portIndex]+": "+e.getMessage());
                } catch (IOException e) {
                    log().warn("IOException while polling address " + (iface.getAddress()), e);
                    httpClient.setReason("IOException while polling address: "+(iface.getAddress())+": "+e.getMessage());
                } finally {
                    httpClient.closeConnection();
                }

            } // end for (attempts)
        } // end for (ports)
        return httpClient.determinePollStatusResponse();

    }

    private void logResponseTimes(Double responseTime, String line) {
        if (log().isDebugEnabled()) {
            log().debug("poll: response= " + line);
            log().debug("poll: responseTime= " + responseTime + "ms");
        }
    }

    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected Socket wrapSocket(final Socket socket) throws IOException {
        return socket;
    }

    private boolean determineVerbosity(final Map<String, Object> parameters) {
        final String verbose = ParameterMap.getKeyedString(parameters, PARAMETER_VERBOSE, null);
        return (verbose != null && verbose.equalsIgnoreCase("true")) ? true : false;
    }

    private String determineUserAgent(final Map<String, Object> parameters) {
        String agent = ParameterMap.getKeyedString(parameters, PARAMETER_USER_AGENT, null);
        if (isBlank(agent)) {
            return "OpenNMS HttpMonitor";
        }
        return agent;
    }

    String determineBasicAuthentication(final Map<String, Object> parameters) {
        String credentials = ParameterMap.getKeyedString(parameters, PARAMETER_BASIC_AUTHENTICATION, null);

        if (isNotBlank(credentials)) {
            credentials = new String(Base64.encodeBase64(credentials.getBytes()));
        } else {
            
            String user = ParameterMap.getKeyedString(parameters, PARAMETER_USER, null);
            
            if (isBlank(user)) {
                credentials = null;
            } else {
                String passwd = ParameterMap.getKeyedString(parameters, PARAMETER_PASSWORD, "");
                credentials = new String(Base64.encodeBase64((user+":"+passwd).getBytes()));
            }
        }
        
        return credentials;
    }

    private String determineHttpHeader(final Map<String, Object> parameters, String key) {
        return ParameterMap.getKeyedString(parameters, key, null);
    }
    
    private String determineVirtualHost(NetworkInterface<InetAddress> iface, final Map<String, Object> parameters) {
        boolean res = ParameterMap.getKeyedBoolean(parameters, PARAMETER_RESOLVE_IP, false);
        String virtualHost = ParameterMap.getKeyedString(parameters, PARAMETER_HOST_NAME, null);

        
        if (isBlank(virtualHost)) {
            if (res) {
                virtualHost = ((InetAddress) iface.getAddress()).getCanonicalHostName();
            } else {
                virtualHost = ((InetAddress) iface.getAddress()).getHostAddress();
            }
        }
        return virtualHost;
    }


    private String determineResponseText(final Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, PARAMETER_RESPONSE_TEXT, null);
    }

    private String determineResponse(final Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, PARAMETER_RESPONSE, determineDefaultResponseRange(determineUrl(parameters)));
    }

    private String determineUrl(final Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, PARAMETER_URL, DEFAULT_URL);
    }

    /**
     * <p>determinePorts</p>
     *
     * @param parameters a {@link java.util.Map} object.
     * @return an array of int.
     */
    protected int[] determinePorts(final Map<String, Object> parameters) {
        return ParameterMap.getKeyedIntegerArray(parameters, PARAMETER_PORT, DEFAULT_PORTS);
    }

    private String determineDefaultResponseRange(String url) {
        if (url == null || url.equals(DEFAULT_URL)) {
            return "100-499";
        }
        return "100-399";
    }
    
    private boolean isNotBlank(String str) {
        return org.apache.commons.lang.StringUtils.isNotBlank(str);
    }

    private boolean isBlank(String str) {
        return org.apache.commons.lang.StringUtils.isBlank(str);
    }

    final class HttpMonitorClient {
        private double m_responseTime;
        NetworkInterface<InetAddress> m_iface;
        Map<String, Object> m_parameters;
        String m_httpCmd;
        Socket m_httpSocket;
        private BufferedReader m_lineRdr;
        private String m_currentLine;
        private int m_serviceStatus;
        private String m_reason;
        private final StringBuffer m_html = new StringBuffer();
        private int m_serverResponseCode;
        private TimeoutTracker m_timeoutTracker;
        private int m_currentPort;
        private String m_responseText;
        private boolean m_responseTextFound = false;
        
        HttpMonitorClient(NetworkInterface<InetAddress> iface, TreeMap<String, Object>parameters) {
            m_iface = iface;
            m_parameters = parameters;
            buildCommand();
            m_serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
            m_responseText = determineResponseText(parameters);
        }
        
        public void read() throws IOException {
            for (int nullCount = 0; nullCount < 2;) {
                readLinedMatching();
                if (isEndOfStream()) {
                    nullCount++;
                }
            }
        }

        public int getCurrentPort() {
            return m_currentPort;
        }

        public Map<String, Object> getParameters() {
            return m_parameters;
        }

        public boolean isResponseTextFound() {
            return m_responseTextFound;
        }
        public void setResponseTextFound(boolean found) {
            m_responseTextFound  = found;
        }

        public boolean checkCurrentLineMatchesResponseText() {
            
            if (m_responseText.charAt(0) == '~' && !m_responseTextFound) {
                m_responseTextFound = m_currentLine.matches(m_responseText.substring(1));
            } else {
                m_responseTextFound = (m_currentLine.indexOf(m_responseText) != -1 ? true : false);
            }
            return m_responseTextFound;
        }

        public String getResponseText() {
            return m_responseText;
        }

        public void setResponseText(String responseText) {
            m_responseText = responseText;
        }

        public void setCurrentPort(int currentPort) {
            m_currentPort = currentPort;
        }

        public TimeoutTracker getTimeoutTracker() {
            return m_timeoutTracker;
        }

        public void setTimeoutTracker(TimeoutTracker tracker) {
            m_timeoutTracker = tracker;
        }

        public Double getResponseTime() {
            return m_responseTime;
        }

        public void setResponseTime(double elapsedTimeInMillis) {
            m_responseTime = elapsedTimeInMillis;
        }

        private void connect() throws IOException, SocketException {
            m_httpSocket = new Socket();
            m_httpSocket.connect(new InetSocketAddress(((InetAddress) m_iface.getAddress()), m_currentPort), m_timeoutTracker.getConnectionTimeout());
            m_serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;
            m_httpSocket.setSoTimeout(m_timeoutTracker.getSoTimeout());
            m_httpSocket = wrapSocket(m_httpSocket);
        }
        
        public void closeConnection() {
            try {
                if (m_httpSocket != null) {
                    m_httpSocket.close();
                    m_httpSocket = null;
                }
            } catch (IOException e) {
                e.fillInStackTrace();
                log().warn("Error closing socket connection", e);
            }
        }

        public int getPollStatus() {
            return m_serviceStatus;
        }

        public void setPollStatus(int serviceStatus) {
            m_serviceStatus = serviceStatus;
        }

        public String getCurrentLine() {
            return m_currentLine;
        }
        

        public int getServerResponse() {
            return m_serverResponseCode;
        }

        private void determineServerInitialResponse() {
            int serverResponseValue = -1;

            if (m_currentLine != null) {

                if (m_currentLine.startsWith("HTTP/")) {
                    serverResponseValue = parseHttpResponse();

                    if (IPLike.matchNumericListOrRange(String.valueOf(serverResponseValue), determineResponse(m_parameters))) {
                        log().debug("determineServerResponse: valid server response: "+serverResponseValue+" found.");
                        m_serviceStatus = PollStatus.SERVICE_AVAILABLE;
                    } else {
                        m_serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
                        StringBuffer sb = new StringBuffer();
                        sb.append("HTTP response value: ");
                        sb.append(serverResponseValue);
                        sb.append(". Expecting: ");
                        sb.append(determineResponse(m_parameters));
                        sb.append(".");
                        m_reason = sb.toString();
                    }
                }
            }
            m_serverResponseCode = serverResponseValue;
        }
        
        private int parseHttpResponse() {
            StringTokenizer t = new StringTokenizer(m_currentLine);
            t.nextToken();

            int serverResponse = -1;
            try {
                serverResponse = Integer.parseInt(t.nextToken());
            } catch (NumberFormatException nfE) {
                log().info("Error converting response code from host = " + (m_iface.getAddress()) + ", response = " + m_currentLine);
            }
            return serverResponse;
        }

        public boolean isEndOfStream() {
            boolean eos = false;
            if (m_currentLine == null) {
                eos = true;
            }
            return eos;
        }

        public String readLinedMatching() throws IOException {
            m_currentLine = m_lineRdr.readLine();
            
            if (determineVerbosity(m_parameters)) {
                log().debug("\t<<: "+m_currentLine);
            }
            
            m_html.append(m_currentLine);
            
            if (m_responseText != null && m_currentLine != null && !m_responseTextFound) {
                if (checkCurrentLineMatchesResponseText()) {
                    log().debug("response-text: "+m_responseText+": found.");
                    m_serviceStatus = PollStatus.SERVICE_AVAILABLE;
                }
            }
            return m_currentLine;
        }

        public void sendHttpCommand() throws IOException {
            if (determineVerbosity(m_parameters)) {
                log().debug("Sending HTTP command: "+m_httpCmd);
            }
            m_httpSocket.getOutputStream().write(m_httpCmd.getBytes());
            m_lineRdr = new BufferedReader(new InputStreamReader(m_httpSocket.getInputStream()));
            readLinedMatching();
            if (determineVerbosity(m_parameters)) {
                log().debug("Server response: "+m_currentLine);
            }
            determineServerInitialResponse();
        }

        private void buildCommand() {
            /*
             * Sorting this map just in case the poller gets changed and the Map
             * is no longer a TreeMap.
             */
            StringBuilder sb = new StringBuilder();
            sb.append("GET ").append(determineUrl(m_parameters)).append(" HTTP/1.1\r\n");
            sb.append("Connection: CLOSE \r\n");
            sb.append("Host: ").append(determineVirtualHost(m_iface, m_parameters)).append("\r\n");
            sb.append("User-Agent: ").append(determineUserAgent(m_parameters)).append("\r\n");
            
            if (determineBasicAuthentication(m_parameters) != null) {
                sb.append("Authorization: Basic ").append(determineBasicAuthentication(m_parameters)).append("\r\n");
            }

            for (String string : m_parameters.keySet()) {
                String parmKey = string;
                if (parmKey.matches("header[0-9]+$")) {
                    sb.append(determineHttpHeader(m_parameters, parmKey)).append("\r\n");
                }
            }

            sb.append("\r\n");
            final String cmd = sb.toString();
            log().debug("checkStatus: cmd:\n" + cmd);
            m_httpCmd = cmd;
        }

        public void setReason(final String reason) {
            m_reason = reason;
        }
        
        public String getReason() {
            return m_reason;
        }

        public Socket getHttpSocket() {
            return m_httpSocket;
        }

        public void setHttpSocket(Socket httpSocket) {
            m_httpSocket = httpSocket;
        }

        protected PollStatus determinePollStatusResponse() {
            /*
             Add the 'qualifier' parm to the parameter map. This parm will
             contain the port on which the service was found if AVAILABLE or
             will contain a comma delimited list of the port(s) which were
             tried if the service is UNAVAILABLE
            */
            
            if (getPollStatus() == PollStatus.SERVICE_UNAVAILABLE) {
                //
                // Build port string
                //
                StringBuffer testedPorts = new StringBuffer();
                for (int i = 0; i < determinePorts(getParameters()).length; i++) {
                    if (i == 0) {
                        testedPorts.append(determinePorts(getParameters())[0]);
                    } else {
                        testedPorts.append(',').append(determinePorts(getParameters())[i]);
                    }
                }
        
                // Add to parameter map
                getParameters().put("qualifier", testedPorts.toString());
                String reason = getReason();
                reason += "/Ports: "+testedPorts.toString();
                setReason(reason);
                
                log().debug("checkStatus: Reason: \""+getReason()+"\"");
                return PollStatus.unavailable(getReason());
        
            } else if (getPollStatus() == PollStatus.SERVICE_AVAILABLE) {
                getParameters().put("qualifier", Integer.toString(getCurrentPort()));
                return PollStatus.available(getResponseTime());
            } else {
                return PollStatus.get(getPollStatus(), getReason());
            }
        }
        
    }

    
}
