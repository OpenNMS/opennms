/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.nsclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <P>
 * This class is designed to be used by plugins, services and programs to
 * perform checks against an NSClient service.
 * <P>
 * To use it you must first create an instance of the manager with the host,
 * port and/or password. Then you can set the timeout for the socket, if you
 * want to override DEFAULT_SOCKET_TIMEOUT. Once you have set up the manager,
 * you call the init() method to connect to the service. Once connected you
 * use the processCheckCommand() method to receive a NsclientPacket object
 * containing the response and the result code. Here's an example of using
 * this manager: <CODE> NsclientCheckParams params = new
 * NsclientCheckParams(critPerc, warnPerc, parameter); NsclientManager client =
 * new NsclientManager(host.getHostAddress(), port); client.init();
 * NsclientPacket
 * pack=client.processCheckCommand(NsclientManager.convertStringToType(command),
 * params); </CODE>
 * <P>
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 */
public class NsclientManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(NsclientManager.class);

    /**
     * The default socket timeout.
     */
    public static int DEFAULT_SOCKET_TIMEOUT = 5000;

    /**
     * The default NSClient TCP port.
     */
    public static int DEFAULT_PORT = 1248;

    /**
     * Stores the host name the manager is connect(ing/ed) to.
     */
    private String m_HostName = null;

    /**
     * Stores the port the manager is connect(ing/ed) to. Set to DEFAULT_PORT.
     */
    private int m_PortNumber = DEFAULT_PORT;

    /**
     * The password to use when requesting a check. Default is "None"
     */
    private String m_Password = "None";

    /**
     * Stores the socket used to connect to the service.
     */
    private Socket m_Socket = null;

    /**
     * This is used for receiving input from the server.
     */
    private BufferedInputStream m_BufInStream = null;

    private ByteArrayOutputStream m_ByteArrayOutStream = null;

    private int m_Timeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * Default check type. Not supported.
     */
    public static final String CHECK_NONE = "0";

    /**
     * The ID for checking the remote client version.
     */
    public static final String CHECK_CLIENTVERSION = "1";

    /**
     * The ID for checking the remote CPU usage.
     */
    public static final String CHECK_CPULOAD = "2";

    /**
     * The ID for checking the remote uptime.
     */
    public static final String CHECK_UPTIME = "3";

    /**
     * The ID for checking the remote used disk space.
     */
    public static final String CHECK_USEDDISKSPACE = "4";

    /**
     * The ID for checking the state of a remote service.
     */
    public static final String CHECK_SERVICESTATE = "5";

    /**
     * The ID for checking the state of a remote process.
     */
    public static final String CHECK_PROCSTATE = "6";

    /**
     * The ID for checking the state of the remote memory usage.
     */
    public static final String CHECK_MEMUSE = "7";

    /**
     * The ID for checking the value of a remote Perfmon counter.
     */
    public static final String CHECK_COUNTER = "8";

    /**
     * The ID for checking the age of a remote file.
     */
    public static final String CHECK_FILEAGE = "9";

    /**
     * This check type is used by the NSClient developers as a utility for an
     * easy remote method of looking up potential COUNTER instances.
     */
    public static final String CHECK_INSTANCES = "10";
    
    /**
     * The ID for checking the size of a directory.  NSClient++ only.
     */
    public static final String CHECK_FILESIZE = "CheckFileSize";

    /**
     * Stores the String -> CHECK_ id mappings for lookups.
     */
    public static final Map<String, String> CheckStrings = new HashMap<String, String>();
    /**
     * This static block initializes the global check strings map with the
     * default values used for performing string->type->string conversions.
     */
    static {
        CheckStrings.put("NONE", CHECK_NONE);
        CheckStrings.put("CLIENTVERSION", CHECK_CLIENTVERSION);
        CheckStrings.put("CPULOAD", CHECK_CPULOAD);
        CheckStrings.put("UPTIME", CHECK_UPTIME);
        CheckStrings.put("USEDDISKSPACE", CHECK_USEDDISKSPACE);
        CheckStrings.put("SERVICESTATE", CHECK_SERVICESTATE);
        CheckStrings.put("PROCSTATE", CHECK_PROCSTATE);
        CheckStrings.put("MEMUSE", CHECK_MEMUSE);
        CheckStrings.put("COUNTER", CHECK_COUNTER);
        CheckStrings.put("FILEAGE", CHECK_FILEAGE);
        CheckStrings.put("INSTANCES", CHECK_INSTANCES);
    }

    /**
     * This method uses CheckStrings to convert from a short value such as
     * CHECK_CLIENTVERSION to the a string, for example "CLIENTVERSION"
     *
     * @param type
     *            the CHECK_ type to look up in the CheckStrings map.
     * @return a string containing "NONE" if the short is not found in the
     *         map, or the string in the map that corresponds to type.
     * @see CheckStrings
     * @see convertStringToType
     */
    public static String convertTypeToString(String type) {
        for (Map.Entry<String, String> e : CheckStrings.entrySet()) {
            String val = e.getValue();
            if (val.equals(type)) {
                return e.getKey();
            }
        }
        return "NONE";
    }

    /**
     * This method uses the CheckStrings HashMap to convert from a string to a
     * short value. For example if you passed "CLIENTVERSION" you would
     * receive the value of CHECK_CLIENTVERSION in return.
     *
     * @param type
     *            A string to look up a CHECK_ value from the CheckStrings
     *            HashMap.
     * @return a short id corresponding to the CHECK_ value that matches the
     *         string.
     * @see CheckStrings
     * @see convertTypeToString
     */
    public static String convertStringToType(String type) {
        for (Map.Entry<String, String> e : CheckStrings.entrySet()) {
            if (type.equalsIgnoreCase(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * This method is used for setting the password used to perform service
     * checks.
     *
     * @param pass
     *            the password to use when performing service checks.
     */
    public void setPassword(String pass) {
        m_Password = pass;
    }

    /**
     * This method is used for overriding the port that is used to connect to
     * the remote service. This method must be called before calling the
     * init() method or it will have no effect.
     *
     * @param port
     *            the remote service port.
     */
    public void setPortNumber(int port) {
        m_PortNumber = port;
    }

    /**
     * Returns the port being used to connect to the remote service.
     *
     * @return the port being used to connect to the remote service.
     * @see init
     */
    public int getPortNumber() {
        return m_PortNumber;
    }

    /**
     * This method is used to set the host name to connect to for performing
     * remote service checks. This method must be called before calling the
     * init() method or it will have no effect.
     *
     * @param host
     *            the host name to connect to.
     * @see init
     */
    public void setHostName(String host) {
        m_HostName = host;
    }

    /**
     * Returns the host name being used to connect to the remote service.
     *
     * @return the host name being used to connect to the remote service.
     */
    public String getHostName() {
        return m_HostName;
    }

    /**
     * This method is used to set the TCP socket timeout to be used when
     * connecting to the remote service. This must be called before calling
     * <code>init</code> or it will have no effect.
     *
     * @param timeout
     *            the TCP socket timeout.
     */
    public void setTimeout(int timeout) {
        m_Timeout = timeout;
    }

    /**
     * Returns the TCP socket timeout used when connecting to the remote
     * service.
     *
     * @return the tcp socket timeout.
     */
    public int getTimeout() {
        return m_Timeout;
    }

    /**
     * Constructor.
     *
     * @param host
     *            sets the host name to connect to.
     */
    public NsclientManager(String host) {
        m_HostName = host;
    }

    /**
     * Constructor. The password defaults to "None"
     *
     * @param host
     *            sets the host name to connect to.
     * @param port
     *            sets the port number to connect using.
     */
    public NsclientManager(String host, int port) {
        m_HostName = host;
        m_PortNumber = port;
    }

    /**
     * Constructor.
     *
     * @param host
     *            sets the host name to connect to.
     * @param port
     *            sets the port number to connect using.
     * @param pass
     *            sets the password to use when performing checks.
     */
    public NsclientManager(String host, int port, String pass) {
        m_HostName = host;
        m_PortNumber = port;
        m_Password = pass;
    }

    /**
     * Constructor. The port number defaults to <code>DEFAULT_PORT</code>.
     *
     * @param host
     *            sets the host name to connect to.
     * @param pass
     *            sets the password to use when performing checks.
     */
    public NsclientManager(String host, String pass) {
        m_HostName = host;
        m_Password = pass;
    }

    /**
     * Constructor. Made private to prevent construction without parameters.
     */
    @SuppressWarnings("unused")
    private NsclientManager() {
        // nothing to do, don't allow it.
    }

    /**
     * This method creates a new socket and attempts to connect to the remote
     * service. The input and output streams are created after the socket is
     * connected.
     *
     * @throws org.opennms.protocols.nsclient.NsclientException
     *             if the hostname is unknown if the connection is refused if
     *             there is no route to the host if the host did not respond
     *             if there was an unexpected IO error. The thrown exception
     *             contains the causing exception.
     */
    public void init() throws NsclientException {
        try {
            // set up socket
            m_Socket = new Socket();
            m_Socket.connect(new InetSocketAddress(m_HostName, m_PortNumber), m_Timeout);
            m_Socket.setSoTimeout(m_Timeout);

            // get buffer streams for read/write.
            m_BufInStream = new BufferedInputStream(m_Socket.getInputStream());
            m_ByteArrayOutStream = new ByteArrayOutputStream();

            // handle exceptions.
        } catch (UnknownHostException e) {
            closeSocketAndThrow(new NsclientException("Unknown host: " + m_HostName, e));
        } catch (ConnectException e) {
            closeSocketAndThrow(new NsclientException("Connection refused to " + m_HostName + ":" + m_PortNumber, e));
        } catch (NoRouteToHostException e) {
            closeSocketAndThrow(new NsclientException("Unable to connect to host: " + m_HostName + ", no route to host.", e));
        } catch (InterruptedIOException e) {
            closeSocketAndThrow(new NsclientException("Unable to connect to host: " + m_HostName + ":" + m_PortNumber + ", exceeded timeout of " + m_Timeout, e));
        } catch (IOException e) {
            closeSocketAndThrow(new NsclientException("An unexpected I/O exception occured connecting to host: " + m_HostName + ":" + m_PortNumber, e));
        }
    }

    private void closeSocketAndThrow(final NsclientException e) throws NsclientException {
        if (m_Socket != null && !m_Socket.isClosed()) {
            try {
                m_Socket.close();
            } catch (final IOException ioe) {
                LOG.debug("unable to close socket after a previous failure", ioe);
            }
        }
        throw e;
    }
    
    /**
     * Closes the socket.
     */
    public void close() {
        try {
            m_Socket.close();
        } catch (final Exception e) {
            LOG.debug("unable to close socket", e);
        }
    }

    /**
     * This method sends the request string to the remote service.
     * 
     * @param request
     *            the request string to be sent to the remote service
     * @return a <code>NsclientPacket</code> containing the response from
     *         the remote service.
     * @throws NsclientException
     *             is thrown if there is an IO error with send/receiving
     *             to/from the socket.
     */
    private NsclientPacket sendCheckRequest(String request)
            throws NsclientException {
        byte[] buffer = new byte[1024];
        m_ByteArrayOutStream.reset();

        try {
        	if (!request.endsWith("\n")) {
        		request += "\n";
        	}
            m_Socket.getOutputStream().write(request.getBytes());
            m_Socket.getOutputStream().flush();
            int read = m_BufInStream.read(buffer);

            if (read > 0)
                m_ByteArrayOutStream.write(buffer, 0, read);

            return new NsclientPacket(m_ByteArrayOutStream.toString());
        } catch (Throwable e) {
            throw new NsclientException("Unknown exception: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method determines which check method to call to create a request,
     * send to the server and process the results. It merely determines the
     * method to be called based on the type param.
     *
     * @param type
     *            the short ID of the type of check to be processed.
     * @param param
     *            the object containing the parameters for performing checks
     *            on the respones from the remote service.
     * @return the NsclientPacket as processed by the check command method
     *         that is called.
     * @throws org.opennms.protocols.nsclient.NsclientException
     *             this method rethrows org.opennms.netmgt.poller.nsclient.NsclientExceptions caused by the check
     *             commands.
     */
    public NsclientPacket processCheckCommand(String type,
            NsclientCheckParams param) throws NsclientException {
        try {
        	if (type.equals(CHECK_CLIENTVERSION)) {
                return checkClientVersion(param);
        	}
        	else if (type.equals(CHECK_CPULOAD)) {
                return checkCpuLoad(param);
        	}
        	else if (type.equals(CHECK_UPTIME)) {
                return checkUptime(param);
        	}
        	else if (type.equals(CHECK_SERVICESTATE)) {
                return checkServiceState(param);
        	}
        	else if (type.equals(CHECK_USEDDISKSPACE)) {
                return checkUsedDiskSpace(param);
        	}
        	else if (type.equals(CHECK_PROCSTATE)) {
                return checkProcState(param);
        	}
        	else if (type.equals(CHECK_MEMUSE)) {
                return checkMemoryUsage(param);
        	}
        	else if (type.equals(CHECK_COUNTER)) {
                return checkPerfCounter(param);
        	}
        	else if (type.equals(CHECK_FILEAGE)) {
                return checkFileAge(param);
            }
        	else if (type.equals(CHECK_INSTANCES)) {
        		return checkInstances(param);
        	}
            return null;
        } catch (NsclientException e) {
            throw e;
        }
    }
    
    /**
     * This method performs a check of the client version on the remote
     * service. From the <code>NsclientCheckParams</code> object passed to
     * this method only the 'parameter' string is used, this contains the four
     * digit version number which should be formatted like: 2.0.1.0 If the
     * parameter does not contain for period delimited digits, the check will
     * return the packet with with
     * <code>NsclientPacket.RES_STATE_UNKNOWN</code> for a result code.
     * 
     * @param param
     *            The param string member of this value contains the minimum
     *            client version.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkClientVersion(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;

        // get the client version response.
        try {
            pack = sendCheckRequest(m_Password + "&" + CHECK_CLIENTVERSION);
        } catch (NsclientException e) {
            throw e;
        }

        // Check for "ERROR" string.
        if (pack.getResponse().matches(".*ERROR.*")) {
            pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
            return pack;
        }

        // if we're not checking the clientversion, just return OK.
        if (param.getParamString() == null
                || param.getParamString().equals("")) {
            pack.setResultCode(NsclientPacket.RES_STATE_OK);
            return pack;
        } else {
            // otherwise, if we are checking, split it up into four octets and
            // compare.
            pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
            String[] minimum = param.getParamString().split("\\.");
            String[] remote;
            if (pack.getResponse().contains(" ")) {
            	// NSClient++ response with the format: NSClient++ 0.2.7 2007-03-06
            	remote = pack.getResponse().split(" ")[1].split("\\.");
            } else {
            	// Oldskool NSClient responds with the format: 2.0.1.0
            	remote = pack.getResponse().split("\\.");
            }

            // make sure they both contain the same number of version digits.
            if (remote.length != 4) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // then convert them to arrays.
            Integer[] remVer = new Integer[4];
            Integer[] minVer = new Integer[4];

            try {
                for (int i = 0; i < 4; i++) {
                    if (minimum[i] != null) {
                        minVer[i] = Integer.parseInt(minimum[i]);
                    }
                    if (remote[i] != null) {
                        remVer[i] = Integer.parseInt(remote[i]);
                    }
                }
            } catch (NumberFormatException nfe) {
                return handleNumberFormatException(pack, nfe);
            }

            if (minVer[0] == null || remVer[0].compareTo(minVer[0]) > 0) {
                pack.setResultCode(NsclientPacket.RES_STATE_OK);
            } else if (remVer[0].compareTo(minVer[0]) == 0) {
                if (minVer[1] == null || remVer[1].compareTo(minVer[1]) > 0) {
                    pack.setResultCode(NsclientPacket.RES_STATE_OK);
                } else if (remVer[1].compareTo(minVer[1]) == 0) {
                    if (minVer[2] == null || remVer[2].compareTo(minVer[2]) > 0) {
                        pack.setResultCode(NsclientPacket.RES_STATE_OK);
                    } else if (remVer[2].compareTo(minVer[2]) == 0) {
                        if (minVer[3] == null || remVer[3].compareTo(minVer[3]) > 0) {
                            pack.setResultCode(NsclientPacket.RES_STATE_OK);
                        } else if (remVer[3].compareTo(minVer[3]) == 0) {
                            pack.setResultCode(NsclientPacket.RES_STATE_OK);
                        }
                    }
                }
            }

            return pack;
        }
    }

    /**
     * This method is used to perform a check of the CPU percent in use it
     * higher than the warning and critical percent thresholds.
     * 
     * @param param
     *            The param warning and critical percent members are used for
     *            validating CPU load results.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkCpuLoad(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;
        try {
            // get the packet from the server and assume it is okay. We'll
            // rule it out as we go.
            pack = sendCheckRequest(m_Password + "&" + CHECK_CPULOAD
                    + "&1&1&1");
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }
            // if a warning percent was configured, check it.
            if (param.getWarningPercent() != 0) {
                if (Integer.parseInt(pack.getResponse()) > param.getWarningPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_WARNING);
                }
            }

            // if a critical percent was configured, check it, overriding
            // warning percent.
            if (param.getCriticalPercent() != 0) {
                if (Integer.parseInt(pack.getResponse()) > param.getCriticalPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
                }
            }

            return pack;
        } catch (NumberFormatException nfe) {
            return handleNumberFormatException(pack, nfe);
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method simply performs a check of the uptime from the remove
     * service and returns the results. No response validation is performed.
     * 
     * @param param
     *            The param member is not currently in use.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkUptime(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;
        try {
            pack = sendCheckRequest(m_Password + "&" + CHECK_UPTIME);
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            return pack;
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method performs a check of the state of NT services on the remote
     * service. The services to check are contained in the 'parameter' string
     * in a comma delimited format (that is prepared to the client format
     * using the <code>prepList</code> method.) The default result code is
     * <code>NsclientPacket.RES_STATE_OK</code> unless one of the services
     * responds as 'Stopped' - in which case the result code is set to
     * <code>NsclientPacket.RES_STATE_CRIT</code>
     * 
     * @param param
     *            The param string member should contain a comma delimited
     *            list of NT services on the remote service.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkServiceState(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;
        try {
            pack = sendCheckRequest(m_Password + "&" + CHECK_SERVICESTATE
                    + "&ShowAll&" + prepList(param.getParamString()));
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // check up response from "1& Service1: State - Service2: State"
            String[] services = pack.getResponse().replaceFirst("^\\d&\\s+",
                                                                "").split(
                                                                          "\\s+-\\s+");
            for (int i = 0; i < services.length; i++) {
            	String stateValue = services[i].split(":\\s+")[1].trim();
                if (stateValue.equals("Stopped") || stateValue.equals("Unknown") ||
                        stateValue.equals("Not found")) {
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
                }
            }

            return pack;
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method performs a check of the state of NT process on the remote
     * service. The processes to check are contained in the 'parameter' string
     * in a comma delimited format (that is prepared to the client format
     * using the <code>prepList</code> method.) The default result code is
     * <code>NsclientPacket.RES_STATE_OK</code> unless one of the processes
     * responds as 'not running' - in which case the result code is set to
     * <code>NsclientPacket.RES_STATE_CRIT</code>
     * 
     * @param param
     *            The param string member should contain a comma delimited
     *            list of NT processes on the remote service.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkProcState(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;
        try {
            pack = sendCheckRequest(m_Password + "&" + CHECK_PROCSTATE
                    + "&ShowAll&" + prepList(param.getParamString()));
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // check up response from "1& Prc1: State - Proc2: State"
            String[] services = pack.getResponse().replaceFirst("^\\d&\\s+",
                                                                "").split(
                                                                          "\\s+-\\s+");
            for (int i = 0; i < services.length; i++) {
                if (services[i].split(":\\s+")[1].matches("not running\\s*"))
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
            }

            return pack;
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method performs a check of the disk space available on the drive
     * specified in the 'parameter' string. The warning and critical
     * thresholds defined by 'warningPercent' and 'criticalPercent' are used
     * to validate the percent of used disk space.
     * 
     * @param param
     *            The param string should contain a drive letter, warning and
     *            critical should contain non-zero percentages.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkUsedDiskSpace(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;

        try {
            // send/receive the request
            pack = sendCheckRequest(m_Password + "&" + CHECK_USEDDISKSPACE
                    + "&" + param.getParamString());
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // parse out the response.
            String[] results = pack.getResponse().split("&");
            double freeDisk = Double.parseDouble(results[0]);
            double totalDisk = Double.parseDouble(results[1]);
            double usedPerc = ((totalDisk - freeDisk) / totalDisk) * 100;

            // check to see if the drives even exist.
            if (freeDisk < 0 || totalDisk < 0) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // Process checks.
            if (param.getWarningPercent() != 0) {
                if (usedPerc > param.getWarningPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_WARNING);
                }
            }
            if (param.getCriticalPercent() != 0) {
                if (usedPerc > param.getCriticalPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
                }
            }

            return pack;
        } catch (NumberFormatException nfe) {
            return handleNumberFormatException(pack, nfe);
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method performs a check of the memory space used on the remote
     * server. The warning and critical thresholds defined by 'warningPercent'
     * and 'criticalPercent' are used to validate the percent of used memory.
     * 
     * @param param
     *            The params warning and critical should contain non-zero
     *            percentages.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkMemoryUsage(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;
        try {
            // send/receive the request
            pack = sendCheckRequest(m_Password + "&" + CHECK_MEMUSE + "&7");
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // parse out the response
            String[] results = pack.getResponse().split("&");
            float memCommitLimit = Float.parseFloat(results[0].replace(",", "."));
            float memCommitByte = Float.parseFloat(results[1].replace(",", "."));
            float memUsedPerc = (memCommitByte / memCommitLimit) * 100;

            // if a warning percent was configured, check it.
            if (param.getWarningPercent() != 0) {
                if (memUsedPerc > (float) param.getWarningPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_WARNING);
                }
            }

            // if a critical percent was configured, check it, overriding
            // warning percent.
            if (param.getCriticalPercent() != 0) {
                if (memUsedPerc > (float) param.getCriticalPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
                }
            }
            return pack;
        } catch (NumberFormatException nfe) {
            return handleNumberFormatException(pack, nfe);
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method performs a check of a perfmon object as defined by the
     * 'parameter' string. An example of this string would be:
     * \Memory(_Total)\Pool Paged Bytes - the warning and critical members of
     * param will define thresholds used to validate the perfmon object value.
     * 
     * @param param
     *            The param string should contain a perfmon OID, warning and
     *            critical should contain non-zero values.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkPerfCounter(NsclientCheckParams param)
            throws NsclientException {
        NsclientPacket pack = null;

        try {
            // send/receive the request
            pack = sendCheckRequest(m_Password + "&" + CHECK_COUNTER + "&"
                    + prepList(param.getParamString()));
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // parse out the response.
            float counterValue = Float.parseFloat(pack.getResponse().replace(",", "."));

            // if a warning percent was configured, check it.
            if (param.getWarningPercent() != 0) {
                if (counterValue > (float) param.getWarningPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_WARNING);
                }
            }

            // if a critical percent was configured, check it, overriding
            // warning percent.
            if (param.getCriticalPercent() != 0) {
                if (counterValue > (float) param.getCriticalPercent()) {
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
                }
            }

            return pack;
        } catch (NumberFormatException nfe) {
            return handleNumberFormatException(pack, nfe);
        } catch (NsclientException e) {
            throw e;
        }
    }

    /**
     * This method performs a check of a file's age as defined by the
     * 'parameter' string.
     * 
     * @param param
     *            The param string should contain a full path to a file,
     *            warning and critical should contain non-zero ages in
     *            minutes.
     * @return the processed <code>NsclientPacket</code>.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkFileAge(NsclientCheckParams param) throws NsclientException {
        NsclientPacket pack = null;
        String responseValue = "";
        try {
            // send/receive the request
            pack = sendCheckRequest(m_Password + "&" + CHECK_FILEAGE + "&"
                    + param.getParamString());
            pack.setResultCode(NsclientPacket.RES_STATE_OK);

            // Check for "ERROR" string.
            if (pack.getResponse().matches(".*ERROR.*")) {
                pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
                return pack;
            }

            // this will store our date.
            // SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy
            // h:mm:ss a");
            String[] results = pack.getResponse().split("&");
            responseValue = results[0];
            double minutes = Double.parseDouble(responseValue);

            // check the age of the file, if it's newer than the
            // warning/critical, change the state.
            if (param.getWarningPercent() != 0) {
                if (minutes < param.getWarningPercent())
                    pack.setResultCode(NsclientPacket.RES_STATE_WARNING);
            }
            if (param.getCriticalPercent() != 0) {
                if (minutes < param.getCriticalPercent())
                    pack.setResultCode(NsclientPacket.RES_STATE_CRIT);
            }

            return pack;
        } catch (NumberFormatException nfe) {
            return handleNumberFormatException(pack, nfe);
        } catch (NsclientException e) {
            throw e;
        }
    }
    
    /**
     * This method requests a list of instances for a perfmon object as
     * defined by the 'parameter' string. Examples of this string would be:
     * Processor or PhysicalDisk- the available instances are returned by the
     * agent as a comma-separated list.  The warning and critical members of
     * param are ignored.
     * 
     * @param param
     *            The param string should contain a perfmon object name. Warning and
     *            critical values are ignored.
     * @return the processed <code>NsclientPacket</code> containing a comma-separated instance list.
     * @throws NsclientException
     *             this method rethrows the exception thrown by
     *             <code>sendCheckRequest</code>
     */
    private NsclientPacket checkInstances(NsclientCheckParams param) throws NsclientException {
    	NsclientPacket pack = null;
    	// String responseValue = "";
    	try {
    		// send/receive the request
    		pack = sendCheckRequest(m_Password + "&" + CHECK_INSTANCES + "&" + param.getParamString());
    		pack.setResultCode(NsclientPacket.RES_STATE_OK);
    		// Check for "ERROR" string.
    		if (pack.getResponse().matches(".*ERROR.*")) {
    			pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
    			return pack;
    		}
    		
    		// If we did not receive an ERROR report, then we are done here
    		LOG.debug("checkInstances: received result '{}'", pack.getResponse());
    		return pack;
    	} catch (NsclientException e) {
    		throw e;
    	}
    }

    private String prepList(String list) {
        return list.replaceAll(",", "&");
    }
    
    private NsclientPacket handleNumberFormatException(NsclientPacket pack, NumberFormatException e) throws NsclientException {
        pack.setResultCode(NsclientPacket.RES_STATE_UNKNOWN);
        LOG.info("Unable to parse numeric value returned ({})", pack.getResponse(), e);
        return pack;
    }

}
