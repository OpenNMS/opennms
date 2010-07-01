/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 13, 2004
 * 
 * 2008 Jun 14: Organize imports, code formatting, implement log(),
 *              add asserts, and use String.valueOf instead of
 *              new String when we need to get a non-null String even
 *              if the input is null (and use neither if we know the
 *              input is non-null). - dj@opennms.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

// TODO Refactor to remove code duplication in send* methods

package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
/*import org.apache.xmlrpc.secure.SecureXmlRpcClient; */
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.util.Assert;

/**
 * <p>
 * This class create an XMLRPC client and provide methods to notify the external
 * XMLRPC server if a failure occurs during processing an event.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org</A>
 * @version $Id: $
 */
public final class XmlRpcNotifier {

    /**
     * The external xmlrpc server procedure to process an event success.
     */
    private static final String XMLRPC_SERVER_SUCCESS_COMMAND = "notifySuccess";

    /**
     * The external xmlrpc server procedure to process an event failure.
     */
    private static final String XMLRPC_SERVER_FAILURE_COMMAND = "notifyFailure";

    /**
     * The external xmlrpc server procedure to listen to the receiving event
     * notice.
     */
    private static final String XMLRPC_SERVER_RECEIVE_EVENT_COMMAND = "notifyReceivedEvent";

    /**
     * The external xmlrpc server procedure to process a nodeRegainedService
     * event.
     */
    private static final String XMLRPC_SERVICE_UP_COMMAND = "sendServiceUpEvent";

    /**
     * The external xmlrpc server procedure to process a nodeLostService event.
     */
    private static final String XMLRPC_SERVICE_DOWN_COMMAND = "sendServiceDownEvent";

    /**
     * The external xmlrpc server procedure to process a nodeUp event.
     */
    private static final String XMLRPC_NODE_UP_COMMAND = "sendNodeUpEvent";

    /**
     * The external xmlrpc server procedure to process a nodeDown event.
     */
    private static final String XMLRPC_NODE_DOWN_COMMAND = "sendNodeDownEvent";

    /**
     * The external xmlrpc server procedure to process an interfaceUp event.
     */
    private static final String XMLRPC_INTERFACE_UP_COMMAND = "sendInterfaceUpEvent";

    /**
     * The external xmlrpc server procedure to process an interfaceDown event.
     */
    private static final String XMLRPC_INTERFACE_DOWN_COMMAND = "sendInterfaceDownEvent";

    /**
     * The external xmlrpc server procedure to process a generic event
     *  RPC (instead of making an RPC that's specific to the message)
     */
    private static final String XMLRPC_GENERIC_COMMAND = "sendEvent";

    /**
     * The external xmlrpc server procedure to process an SNMP trap event
     */
    private static final String XMLRPC_SNMP_TRAP_COMMAND = "sendSnmpTrapEvent";

    /**
     * The external xmlrpc servers.
     */
    private XmlrpcServer[] m_rpcServers;

    /**
     * The retry number to setup xmlrpc communication.
     */
    private int m_retries;

    /**
     * The elapse-time between retries.
     */
    private int m_elapseTime;

    /**
     * The working xmlrpc client.
     */
    private XmlRpcClient m_xmlrpcClient;

    /**
     * A boolean flag configurated to indicate to how to set the NMS server
     * Name: From user opennms server configuration or simply take from
     * InetAddress.getLocalHost().
     */
    private boolean m_verifyServer;

    /**
     * The host NMS server name
     */
    private String m_localServer;
    
    //private ExternalEventRecipient m_recipient;

    /**
     * The constructor
     *
     * @param rpcServers an array of {@link org.opennms.netmgt.config.xmlrpcd.XmlrpcServer} objects.
     * @param retries a int.
     * @param elapseTime a int.
     * @param verifyServer a boolean.
     * @param localServer a {@link java.lang.String} object.
     */
    public XmlRpcNotifier(XmlrpcServer[] rpcServers, int retries, int elapseTime, boolean verifyServer, String localServer) {
        m_rpcServers = rpcServers;
        m_retries = retries;
        m_elapseTime = elapseTime;
        createConnection();

        m_verifyServer = verifyServer;
        if (m_verifyServer) {
            m_localServer = localServer;
        }
        
//
//        // These are here temporarily until I can put in the spring xmlrpc event recipient stuff
//        InvocationHandler handler = new InvocationHandler() {
//            
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                return Boolean.TRUE;
//            }
//        };
//        m_recipient = (ExternalEventRecipient)Proxy.newProxyInstance(ExternalEventRecipient.class.getClassLoader(), 
//                                                                     new Class[] { ExternalEventRecipient.class }, 
//                                                                     handler);
    }
    

    /**
     * <p>
     * Notify the external xmlrpc server the success of processing an event.
     *
     * @param txNo
     *            the external transaction number for an event.
     * @param uei
     *            the event uei.
     * @param message
     *            the text message to indicate the success.
     * @return a boolean.
     */
    public boolean notifySuccess(long txNo, String uei, String message) {
        Assert.notNull(uei, "uei must not be null");
        Assert.notNull(message, "message must not be null");
        
        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.notifySuccess(txNo, uei, message);
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(txNo));
        params.addElement(uei);
        params.addElement(message);
        
        return sendXmlrpcRequest(XMLRPC_SERVER_SUCCESS_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of failure during
     * processing an event.
     *
     * @param txNo
     *            the external transaction number for an event.
     * @param uei
     *            the event uei.
     * @param reason
     *            the text message to explain the reason of the failure to the
     *            external xmlrpc server.
     * @return a boolean.
     */
    public boolean notifyFailure(long txNo, String uei, String reason) {
        Assert.notNull(uei, "uei must not be null");
        Assert.notNull(reason, "reason must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.notifyFailure(txNo, uei, reason);
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(txNo));
        params.addElement(uei);
        params.addElement(reason);
        
        return sendXmlrpcRequest(XMLRPC_SERVER_FAILURE_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the request has been received.
     *
     * @param txNo
     *            the external transaction number for an event.
     * @param uei
     *            the event uei.
     * @param message
     *            text message to notify the external xmlrpc server.
     * @return a boolean.
     */
    public boolean notifyReceivedEvent(long txNo, String uei, String message) {
        Assert.notNull(uei, "uei must not be null");
        Assert.notNull(message, "message must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.notifyReceivedEvent(txNo, uei, message);

        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(txNo));
        params.addElement(uei);
        params.addElement(message);
        
        return sendXmlrpcRequest(XMLRPC_SERVER_RECEIVE_EVENT_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of the 'nodeLostService'
     * event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendServiceDownEvent(Event event) {
        Assert.notNull(event, "event object must not be null");
        
        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendServiceDownEvent(getLabelForEventNode(event), event.getInterface(), event.getService(), "Not Available", getEventHost(event), event.getTime());
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(getLabelForEventNode(event)));
        params.addElement(String.valueOf(event.getInterface()));
        params.addElement(String.valueOf(event.getService()));
        params.addElement(String.valueOf("Not Available"));
        params.addElement(String.valueOf(getEventHost(event)));
        params.addElement(String.valueOf(event.getTime()));
        
        return sendXmlrpcRequest(XMLRPC_SERVICE_DOWN_COMMAND, params);
    }

    private String getEventHost(Event event) {
        return (m_verifyServer ?  m_localServer : event.getHost());
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of the
     * 'nodeRegainedService' event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendServiceUpEvent(Event event) {
        Assert.notNull(event, "event object must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendServiceUpEvent(getLabelForEventNode(event), event.getInterface(), event.getService(), "Not Available", getEventHost(event), event.getTime());

        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(getLabelForEventNode(event)));
        params.addElement(String.valueOf(event.getInterface()));
        params.addElement(String.valueOf(event.getService()));
        params.addElement(String.valueOf("Not Available"));
        params.addElement(String.valueOf(getEventHost(event)));
        params.addElement(String.valueOf(event.getTime()));
        
        return sendXmlrpcRequest(XMLRPC_SERVICE_UP_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of the 'interfaceDown'
     * event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendInterfaceDownEvent(Event event) {
        Assert.notNull(event, "event object must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendInterfaceDownEvent(getLabelForEventNode(event), event.getInterface(), getEventHost(event), event.getTime());
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(getLabelForEventNode(event)));
        params.addElement(String.valueOf(event.getInterface()));
        params.addElement(String.valueOf(getEventHost(event)));
        params.addElement(String.valueOf(event.getTime()));

        return sendXmlrpcRequest(XMLRPC_INTERFACE_DOWN_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of the 'interfaceUp'
     * event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendInterfaceUpEvent(Event event) {
        Assert.notNull(event, "event object must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendInterfaceUpEvent(getLabelForEventNode(event), event.getInterface(), getEventHost(event), event.getTime());
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(getLabelForEventNode(event)));
        params.addElement(String.valueOf(event.getInterface()));
        params.addElement(String.valueOf(event.getHost()));
        params.addElement(String.valueOf(getEventHost(event)));
        params.addElement(String.valueOf(event.getTime()));
        
        return sendXmlrpcRequest(XMLRPC_INTERFACE_UP_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of the 'nodeDown' event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendNodeDownEvent(Event event) {
        Assert.notNull(event, "event object must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendNodeDownEvent(getLabelForEventNode(event), getEventHost(event), event.getTime());
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(getLabelForEventNode(event)));
        params.addElement(String.valueOf(getEventHost(event)));
        params.addElement(String.valueOf(event.getTime()));
        
        return sendXmlrpcRequest(XMLRPC_NODE_DOWN_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external xmlrpc server the occurance of the 'nodeUp' event.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendNodeUpEvent(Event event) {
        Assert.notNull(event, "event object must not be null");

        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendNodeUpEvent(getLabelForEventNode(event), getEventHost(event), event.getTime());
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        params.addElement(String.valueOf(getLabelForEventNode(event)));
        params.addElement(String.valueOf(getEventHost(event)));
        params.addElement(String.valueOf(event.getTime()));
        
        return sendXmlrpcRequest(XMLRPC_NODE_UP_COMMAND, params);
    }

    /**
     * <p>
     * Notify the external event xmlrpc server of the occurrence of a generic
     * event -- ie. an event that's been configured for XMLRPC forwarding, but
     * which does not correspond to one of the specific event methods of this
     * class
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public boolean sendEvent(Event event) {
        Assert.notNull(event, "event object must not be null");
        
        // FIXME: This is unused and is intended for Spring xmlrpc integration
        //Object o = m_recipient.sendNodeUpEvent(getLabelForEventNode3event), getEventHost(event), event.getTime());
        
        // Create the request parameters list
        Vector<Object> params = new Vector<Object>();
        Hashtable<String, String> table = new Hashtable<String, String>();
        params.addElement(table);

        if (event.getSource() != null) {
            table.put("source", event.getSource());
        }
        
        String label = getLabelForEventNode(event);
        if (label != null) {
            table.put("nodeLabel", label);
        }
        
        String host = getEventHost(event);
        if (host != null) {
            table.put("host", host);
        }
        
        table.put("time", String.valueOf(event.getTime()));
        table.put("uei", String.valueOf(event.getUei()));
        table.put("nodeId", Long.toString(event.getNodeid()));

        String intf = event.getInterface();
        if (intf != null) {
            table.put("interface", intf);
        }

        String service = event.getService();
        if (service != null) {
            table.put("service", service);
        }

        String descr = event.getDescr();
        if (descr != null) {
            table.put("description", descr);
        }
        
        String severity = event.getSeverity();
        if (severity != null) {
            table.put("severity", event.getSeverity());
        }

        // process event parameters (if any)
        Parms eventParams = event.getParms();
        if (eventParams != null)
        {
            int numParams = eventParams.getParmCount();
            for (int i = 0; i < numParams; i++) {
                Parm p = eventParams.getParm(i);
                Value v = p.getValue();

                table.put("param" + i + " name", p.getParmName());
                table.put("param" + i + " type", v.getType());
                table.put("param" + i + " value", v.getContent());
            }
        }

        if (event.getSnmp() == null) {
            return sendXmlrpcRequest(XMLRPC_GENERIC_COMMAND, params);
        } else {
            // get trap-specific fields
            Snmp trapInfo = event.getSnmp();

            table.put("communityString", String.valueOf(trapInfo.getCommunity()));

            table.put("genericTrapNumber", Integer.toString(trapInfo.getGeneric()));

            table.put("enterpriseId", String.valueOf(trapInfo.getId()));

            if (trapInfo.getIdtext() != null) {
                table.put("enterpriseIdText", trapInfo.getIdtext());
            }

            table.put("specificTrapNumber", Integer.toString(trapInfo.getSpecific()));

            table.put("timeStamp", Long.toString(trapInfo.getTimeStamp()));

            table.put("version", String.valueOf(trapInfo.getVersion()));

            return sendXmlrpcRequest(XMLRPC_SNMP_TRAP_COMMAND, params);
        }
    }


    private String getLabelForEventNode(Event event) {
        return getNodeLabel(event.getNodeid());
    }

    /**
     * <p>
     * This method retrieves the nodeLable from the database for a given nodeId.
     * </p>
     * 
     * @param nodeId
     *            the nodeId to retrieve the node label for.
     */
    private String getNodeLabel(long nodeId) {
        Connection dbConn = null;
        String nodeLabel = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
            d.watch(dbConn);

            if (log().isDebugEnabled()) {
                log().debug("getNodeLabel: retrieve node label for: " + nodeId);
            }

            PreparedStatement stmt = dbConn.prepareStatement("SELECT nodelabel FROM NODE WHERE nodeid = ?");
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                nodeLabel = rs.getString(1);
            }

        } catch (SQLException sqle) {
            log().warn("SQL exception while retrieving nodeLabel for: " + nodeId, sqle);
        } finally {
            d.cleanUp();
        }

        if (log().isDebugEnabled()) {
            log().debug("getNodeLabel: retrieved node label '" + nodeLabel + "' for: " + nodeId);
        }

        return nodeLabel;
    }


    private ThreadCategory log() {
        return ThreadCategory.getInstance(XmlRpcNotifier.class);
    }

    /**
     * <p>
     * This method sends an xmlrpc request to an external xmlrpc server and gets
     * the response from the xmlrpc server as a String.
     * </p>
     * 
     * @param command
     *            The server command to process the request.
     * @param params
     *            a list of parameters need for the external server command to
     *            process the request.
     */
    private boolean sendXmlrpcRequest(String command, Vector<Object> params) {
        if (m_xmlrpcClient == null) {
            return false;
        }

        boolean success = false;

        for (int i = 0; i < m_retries; i++) {
            try {
                Object reply = m_xmlrpcClient.execute(command, params);
                if (log().isDebugEnabled()) {
                    log().debug("Response from XMLRPC server: " + m_xmlrpcClient.getURL().toString() + "\n\t" + reply);
                }
                success = true;
            } catch (XmlRpcException e) {
                log().warn("Failed to send message to XMLRPC server " + m_xmlrpcClient.getURL() + ": " + e, e);
            } catch (ConnectException e) {
                log().warn("Failed to send message to XMLRPC server due to connect exception " + m_xmlrpcClient.getURL() + ": " + e);
            } catch (IOException e) {
                log().warn("Failed to send message to XMLRPC server: " + m_xmlrpcClient.getURL() + ": " + e, e);
            } catch (Throwable t) {
            	log().error("Received unknown error: ", t);
            }
            
            if (success) {
                break;
            }
        }

        if (!success) {
            log().error("Could not successfully communicate with XMLRPC server '" + m_xmlrpcClient.getURL() + "' after " + m_retries + " tries");
        }

        return success;
    }

    /**
     * <p>
     * This method try to find an external xmlrpc server which is alive and and
     * can communicate with.
     * </p>
     *
     * <p>
     * <b>Note: </b> If an xmlrpc server is found alive and could communicate
     * with, an xmlrpc client is created to communicate with this server. The
     * created xmlrpc client is kept for all the xmlrpc communications until the
     * server is no longer available.
     * </p>
     */
    public void createConnection() {
        // Create the request parameters list for the test command
        Vector<Object> params = new Vector<Object>();
        params.addElement(Long.toString(0));
        params.addElement(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI);
        params.addElement("test connection");

        boolean success = false;

        for (int i = 0; i < m_rpcServers.length; i++) {
            XmlrpcServer xServer = m_rpcServers[i];

            String url = xServer.getUrl();
            int timeout = xServer.getTimeout();

            if (log().isDebugEnabled()) {
                log().debug("Start to set up communication to XMLRPC server: " + url);
                log().debug("Setting timeout value to: " + timeout);
            }

            try {
                m_xmlrpcClient = new TimeoutSecureXmlRpcClient(url, timeout);
            } catch (MalformedURLException e) {
                log().error("Failed to send message to XMLRPC server: " + url, e);
                continue;
            }

            for (int k = 0; k < m_retries; k++) {
                try {
                    Object reply = m_xmlrpcClient.execute(XMLRPC_SERVER_RECEIVE_EVENT_COMMAND, params);

                    if (log().isDebugEnabled()) {
                        log().debug("Response from XMLRPC server: " + url + "\n\t" + reply.toString());
                    }
                    success = true;
                } catch (XmlRpcException e) {
                    log().warn("Failed to send message to XMLRPC server: " + url, e);
                } catch (ConnectException e) {
                    log().warn("Failed to send message to XMLRPC server due to connect exception " + url + ": " + e);
                } catch (IOException e) {
                    log().warn("Failed to send message to XMLRPC server: " + url, e);
                }

                // break inner loop, no more retries
                if (success) {
                    break;
                }

                try {
                    Thread.sleep(m_elapseTime);
                } catch (InterruptedException ie) {
                }
            }

            // break outer loop -- a working xmlrpc client created.
            if (success) {
                break;
            }

        }

        if (!success) {
            log().error("Can not set up communication with any XMLRPC server");
            m_xmlrpcClient = null;
        }
    }
}
