//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.SnmpResponseHandler;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpUInt32;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public final class SnmpMonitor extends IPv4Monitor {
    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "SNMP";

    /**
     * <P>
     * The default port on which the host is checked to see if it supports SNMP.
     * </P>
     */
    private static int DEFAULT_PORT = 161;

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2"; // MIB-II
                                                                                // System
                                                                                // Object
                                                                                // Id

    /**
     * Interface attribute key used to store the interface's JoeSNMP SnmpPeer
     * object.
     */
    static final String SNMP_PEER_KEY = "org.opennms.netmgt.collectd.SnmpCollector.SnmpPeer";

    /**
     * Constant for less-than operand
     */
    static final String LESS_THAN = "<";

    static final String GREATER_THAN = ">";

    static final String LESS_THAN_EQUALS = "<=";

    static final String GREATER_THAN_EQUALS = ">=";

    static final String EQUALS = "=";

    static final String NOT_EQUAL = "!=";

    static final String MATCHES = "~";

    /**
     * <P>
     * Returns the name of the service that the plug-in monitors ("SNMP").
     * </P>
     * 
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
     * <P>
     * Initialize the service monitor.
     * </P>
     * 
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    public void initialize(PollerConfig pollerConfig, Map parameters) {
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.reload();
        } catch (MarshalException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        return;
    }

    /**
     * <P>
     * Called by the poller framework when an interface is being added to the
     * scheduler. Here we perform any necessary initialization to prepare the
     * NetworkInterface object for polling.
     * </P>
     * 
     * @param iface
     *            The network interface to be initialized.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */
    public void initialize(NetworkInterface iface) {
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");

        InetAddress ipAddr = (InetAddress) iface.getAddress();

        // Retrieve configured SNMP parms for this interface
        //
        // Instantiate new SnmpPeer object for this interface
        //
        SnmpPeer peer = SnmpPeerFactory.getInstance().getPeer(ipAddr, SnmpSMI.SNMPV1);
        if (log.isDebugEnabled()) {
            String nl = System.getProperty("line.separator");
            log.debug("initialize: SnmpPeer configuration: address: " + peer.getPeer() + nl + "      version: " + ((peer.getParameters().getVersion() == SnmpSMI.SNMPV1) ? "SNMPv1" : "SNMPv2") + nl + "      timeout: " + peer.getTimeout() + nl + "      retries: " + peer.getRetries() + nl + "      read commString: " + peer.getParameters().getReadCommunity() + nl + "      write commString: " + peer.getParameters().getWriteCommunity());
        }

        // Add the snmp config object as an attribute of the interface
        //
        if (log.isDebugEnabled())
            log.debug("initialize: setting SNMP peer attribute for interface " + ipAddr.getHostAddress());

        iface.setAttribute(SNMP_PEER_KEY, peer);

        log.debug("initialize: interface: " + ipAddr.getHostAddress() + " initialized.");

        return;
    }

    /**
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * 
     * @param iface
     *            The network interface to test the service on.
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * 
     * @return The availability of the interface and if a transition event
     *         should be supressed.
     * 
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        int status = SERVICE_UNAVAILABLE;
        InetAddress ipaddr = (InetAddress) iface.getAddress();
        SnmpSession session = null;

        // Retrieve this interface's SNMP peer object
        //
        SnmpPeer peer = (SnmpPeer) iface.getAttribute(SNMP_PEER_KEY);
        if (peer == null)
            throw new RuntimeException("SnmpPeer object not available for interface " + ipaddr);

        // Get configuration parameters
        //
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", peer.getTimeout());
        int retries = ParameterMap.getKeyedInteger(parameters, "retries", peer.getRetries());
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
        String oid = ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OBJECT_IDENTIFIER);
        String operator = ParameterMap.getKeyedString(parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(parameters, "operand", null);

        // set timeout and retries on SNMP peer object
        //
        peer.setTimeout(timeout);
        peer.setRetries(retries);

        if (log.isDebugEnabled())
            log.debug("poll: service= SNMP address= " + ipaddr.getHostAddress() + " port= " + port + " oid=" + oid + " timeout= " + timeout + " retries= " + retries + " operator = " + operator + " operand = " + operand);

        // Establish SNMP session with interface
        //
        try {
            peer.setPort(port);
            if (log.isDebugEnabled()) {
                String nl = System.getProperty("line.separator");
                log.debug("SnmpMonitor.poll: SnmpPeer configuration: address: " + peer.getPeer() + nl + "      version: " + ((peer.getParameters().getVersion() == SnmpSMI.SNMPV1) ? "SNMPv1" : "SNMPv2") + nl + "      timeout: " + peer.getTimeout() + nl + "      retries: " + peer.getRetries() + nl + "      read commString: " + peer.getParameters().getReadCommunity() + nl + "      write commString: " + peer.getParameters().getWriteCommunity());
            }
            session = new SnmpSession(peer);
        } catch (SocketException e) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("poll: Error creating the SnmpSession to collect from " + ipaddr.getHostAddress(), e);
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ex) {
                    if (log.isInfoEnabled())
                        log.info("poll: an error occured closing the SNMP session", ex);
                }
            }

            return SERVICE_UNAVAILABLE;
        }

        // Need to be certain that we close the SNMP session when the data
        // retrieval is completed...wrapping in a try/finally block
        //
        try {
            // Create SNMP response handler, send SNMP GetNext request and
            // block waiting for response.
            //
            SnmpResponseHandler handler = new SnmpResponseHandler();
            SnmpPduPacket out = new SnmpPduRequest(SnmpPduPacket.GETNEXT, new SnmpVarBind[] { new SnmpVarBind(new SnmpObjectId(oid)) });

            synchronized (handler) {
                session.send(out, handler);
                try {
                    // wait for response for no longer than
                    // (timeout)*(retries)+1000ms
                    handler.wait(timeout * retries + 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (handler.getResult() != null) {
                log.debug("poll: SNMP poll succeeded, addr=" + ipaddr.getHostAddress() + " oid=" + oid + " value=" + handler.getResult().getValue());
                try {
                    status = (meetsCriteria(handler.getResult().getValue(), operator, operand) ? SERVICE_AVAILABLE : SERVICE_UNAVAILABLE);
                } catch (NumberFormatException e) {
                    log.error("Number operator used on a non-number " + e.getMessage());
                    status = SERVICE_AVAILABLE;
                } catch (IllegalArgumentException e) {
                    log.error("Invalid Snmp Criteria: " + e.getMessage());
                    status = SERVICE_UNAVAILABLE;
                }
            } else {
                log.debug("poll: SNMP poll failed, addr=" + ipaddr.getHostAddress() + " oid=" + oid);
                status = SERVICE_UNAVAILABLE;
            }
        } catch (Throwable t) {
            log.warn("poll: Unexpected exception during SNMP poll of interface " + ipaddr.getHostAddress(), t);
            status = SERVICE_UNAVAILABLE;
        } finally {
            // Regardless of what happens with the collection, close the session
            // when we're finished collecting data.
            //
            try {
                session.close();
            } catch (Exception e) {
                if (log.isEnabledFor(Priority.WARN))
                    log.warn("collect: An error occured closing the SNMP session for " + ipaddr.getHostAddress(), e);
            }
        }

        return status;
    }

    /**
     * Verifies that the result of the SNMP query meets the criteria specified
     * by the operator and the operand from the configuartion file.
     * 
     * @param result
     * @param operator
     * @param operand
     * @return
     */
    public boolean meetsCriteria(SnmpSyntax result, String operator, String operand) {
        if (result == null)
            return false;

        if (operator == null || operand == null)
            return true;

        String value = result.toString();
        if (EQUALS.equals(operator))
            return operand.equals(value);
        else if (NOT_EQUAL.equals(operator))
            return !operand.equals(value);
        else if (MATCHES.equals(operator))
            return Pattern.compile(operand).matcher(value).find();

        BigInteger val = null;
        switch (result.typeId()) {
        case SnmpSMI.SMI_INTEGER:
            val = BigInteger.valueOf(((SnmpInt32) result).getValue());
            break;
        case SnmpSMI.SMI_COUNTER64:
            val = ((SnmpCounter64) result).getValue();
            break;
        case SnmpSMI.SMI_GAUGE32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_COUNTER32:
            val = BigInteger.valueOf(((SnmpUInt32) result).getValue());
            break;
        default:
            val = new BigInteger(result.toString());
            break;
        }

        BigInteger intOperand = new BigInteger(operand);
        if (LESS_THAN.equals(operator)) {
            return val.compareTo(intOperand) < 0;
        } else if (LESS_THAN_EQUALS.equals(operator)) {
            return val.compareTo(intOperand) <= 0;
        } else if (GREATER_THAN.equals(operator)) {
            return val.compareTo(intOperand) > 0;
        } else if (GREATER_THAN_EQUALS.equals(operator)) {
            return val.compareTo(intOperand) >= 0;
        } else {
            throw new IllegalArgumentException("operator " + operator + " is unknown");
        }
    }
}
