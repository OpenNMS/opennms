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
// 2004 Jan 13: Added this new code for the XML RPC Daemon
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.outage.OutageConstants;
import org.opennms.netmgt.xml.event.Event;


/**
 * <p>This class create an XMLRPC client and provide methods to notify the 
 * external XMLRPC server if a failure occurs during processing an event.
 *
 * @author 	<A HREF="mailto:jamesz@blast.com">James Zuo</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public final class XmlRpcNotifier
{

	/**
	 * The external xmlrpc server procedure to process an event success.
	 */
	private static final String XMLRPC_SERVER_SUCCESS_COMMAND = "notifySuccess";

	/**
	 * The external xmlrpc server procedure to process an event failure.
	 */
	private static final String XMLRPC_SERVER_FAILURE_COMMAND = "notifyFailure";

        /**
         * The external xmlrpc server procedure to listen to the receiving event notice.
         */
        private static final String XMLRPC_SERVER_RECEIVE_EVENT_COMMAND = "notifyReceivedEvent";
        
	/**
	 * The external xmlrpc server procedure to process a nodeRegainedService event.
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
         * A boolean flag configurated to indicate to how to set the NMS server Name:
         * From user opennms server configuration or simply take from InetAddress.getLocalHost().
         */
        private boolean                 m_verifyServer;          
        
        /**
         * The host NMS server name
         */
        private String                  m_localServer;
 	
        /**
	 * The constructor
	 */
	public XmlRpcNotifier(XmlrpcServer[] rpcServers, int retries, int elapseTime, 
                        boolean verifyServer, String localServer) 
	{
                m_rpcServers = rpcServers;
                m_retries = retries;
                m_elapseTime = elapseTime;
                createConnection();
                 
                m_verifyServer = verifyServer;
                if (m_verifyServer)
                        m_localServer = localServer;
                
	}

	/**
	 * <p>Notify the external xmlrpc server the success of processing an event.
	 *
         * @param txNo          the external transaction number for an event.
         * @param uei           the event uei.
         * @param message       the text message to indicate the success.
         *
         */
	public boolean notifySuccess(long txNo, String uei, String message) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String((new Long(txNo)).toString()));
                params.addElement(new String(uei));
                params.addElement(new String(message));
                return sendXmlrpcRequest(XMLRPC_SERVER_SUCCESS_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of failure during processing an event.
	 *
         * @param txNo          the external transaction number for an event.
         * @param uei           the event uei.
         * @param reason        the text message to explain the reason of the failure to 
         *                      the external xmlrpc server.
         */
	public boolean notifyFailure(long txNo, String uei, String reason) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String((new Long(txNo)).toString()));
                params.addElement(new String(uei));
                params.addElement(new String(reason));
                return sendXmlrpcRequest(XMLRPC_SERVER_FAILURE_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the request has been received.
         *
         * @param txNo          the external transaction number for an event.
         * @param uei           the event uei.
         * @param message       text message to notify the external xmlrpc server.
	 */
	public boolean notifyReceivedEvent(long txNo, String uei, String message) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String((new Long(txNo)).toString()));
                params.addElement(new String(uei));
                params.addElement(new String(message));
                return sendXmlrpcRequest(XMLRPC_SERVER_RECEIVE_EVENT_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeLostService' event.
	 */
	public boolean sendServiceDownEvent(Event event) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(event.getNodeid())));
                params.addElement(new String(event.getInterface()));
                params.addElement(new String(event.getService()));
                params.addElement(new String("Not Available"));

                if (m_verifyServer)
                        params.addElement(new String(m_localServer));
                else
                        params.addElement(new String(event.getHost()));
                params.addElement(new String(event.getTime())); 
                return sendXmlrpcRequest(XMLRPC_SERVICE_DOWN_COMMAND, params);
	}
	
        /**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeRegainedService' event.
	 */
	public boolean sendServiceUpEvent(Event event) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(event.getNodeid())));
                params.addElement(new String(event.getInterface()));
                params.addElement(new String(event.getService()));
                params.addElement(new String("Not Available"));
                
                if (m_verifyServer)
                        params.addElement(new String(m_localServer));
                else
                        params.addElement(new String(event.getHost()));
                params.addElement(new String(event.getTime())); 
                return sendXmlrpcRequest(XMLRPC_SERVICE_UP_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'interfaceDown' event.
	 */
	public boolean sendInterfaceDownEvent(Event event) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(event.getNodeid())));
                params.addElement(new String(event.getInterface()));
                
                if (m_verifyServer)
                        params.addElement(new String(m_localServer));
                else
                        params.addElement(new String(event.getHost()));
                params.addElement(new String(event.getTime())); 
                return sendXmlrpcRequest(XMLRPC_INTERFACE_DOWN_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'interfaceUp' event.
	 */
	public boolean sendInterfaceUpEvent(Event event) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(event.getNodeid())));
                params.addElement(new String(event.getInterface()));
                params.addElement(new String(event.getHost()));
                
                if (m_verifyServer)
                        params.addElement(new String(m_localServer));
                else
                        params.addElement(new String(event.getTime())); 
                return sendXmlrpcRequest(XMLRPC_INTERFACE_UP_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeDown' event.
	 */
	public boolean sendNodeDownEvent(Event event) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(event.getNodeid())));
                
                if (m_verifyServer)
                        params.addElement(new String(m_localServer));
                else
                        params.addElement(new String(event.getHost()));
                params.addElement(new String(event.getTime())); 
                return sendXmlrpcRequest(XMLRPC_NODE_DOWN_COMMAND, params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeUp' event.
	 */
	public boolean sendNodeUpEvent(Event event) 
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(event.getNodeid())));
                
                if (m_verifyServer)
                        params.addElement(new String(m_localServer));
                else
                        params.addElement(new String(event.getHost()));
                params.addElement(new String(event.getTime())); 
                return sendXmlrpcRequest(XMLRPC_NODE_UP_COMMAND, params);
	}

        /**
         * <p> This method retrieves the nodeLable from the database for a given nodeId. </p>
         *
         * @param nodeId        the nodeId to retrieve the node label for.
         */
         private String getNodeLabel(long nodeId) 
         {
                Category log = ThreadCategory.getInstance(XmlRpcNotifier.class);
                
         
                Connection dbConn = null;
                String nodeLabel = null;
                
                try
                {
                        dbConn = DatabaseConnectionFactory.getInstance().getConnection();
                        
                        if (log.isDebugEnabled())
                                log.debug("getNodeLabel: retrieve node label for: " + nodeId);
                                
                        PreparedStatement stmt = dbConn.prepareStatement(OutageConstants.DB_GET_NODE_LABEL);
                        stmt.setLong(1, nodeId);
                        ResultSet rs = stmt.executeQuery();

                        while(rs.next())
                        {
                                nodeLabel = rs.getString(1);
                        }
                        stmt.close();
                                
                } catch (SQLException sqle)
                {
                        log.warn("SQL exception while retrieving nodeLabel for: " + nodeId, sqle);
                } finally
                {
                        try
                        {
                                if (dbConn != null)
                                        dbConn.close();
                        } catch (SQLException e)
                        {
                                log.warn("Exception closing JDBC connection", e);
                        }
                }
                return nodeLabel;
         }

        /**
         * <p>This method sends an xmlrpc request to an external xmlrpc server and 
         *    gets the response from the xmlrpc server as a String.</p>
         * @param command       The server command to process the request.
         * @param params        a list of parameters need for the external server 
         *                      command to process the request.
         */
	private boolean sendXmlrpcRequest(String command, Vector params)
        {
                Category log = ThreadCategory.getInstance(getClass());
                
                if (m_xmlrpcClient == null)
                        return false;
                        
                boolean success = false;
                
                for(int i = 0; i < m_retries; i++)
                {
                        try
                        {
                                Object reply = m_xmlrpcClient.execute(command, params);
                                if (log.isDebugEnabled())
                                        log.debug("Response from XMLRPC server: " + m_xmlrpcClient.getURL().toString()
                                                + "\n\t" + reply.toString());
                                success = true;
                        }
                        catch (XmlRpcException e)
                        {
                                //log.error("Failed to send message to XMLRPC server: ", e); 
                        }
                        catch (IOException e)
                        {
                                //log.error("XmlrpcNotifier: Failed to send message to XMLRPC server: ", e); 
                        }
                        if (success)
                                break;
                }
                
                if (!success)
                        log.error("Can not communicate with XMLRPC server: " + m_xmlrpcClient.getURL().toString());
                
                return success;
        }
        
        /**
         * <p>This method try to find an external xmlrpc server which is alive and 
         * and can communicate with.</p>
         *
         * <p><b>Note:</b> If an xmlrpc server is found alive and could communicate 
         * with, an xmlrpc client is created to communicate with this server. The 
         * created xmlrpc client is kept for all the xmlrpc communications until 
         * the server is no longer available. </p>
         */
	public void createConnection()
        {
                Category log = ThreadCategory.getInstance(getClass());

                // Create the request parameters list for the test command
                Vector params = new Vector();
                params.addElement(new String((new Long(0L)).toString()));
                params.addElement(new String(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI));
                params.addElement(new String("test connection"));
                
                boolean success = false;
                
                for (int i=0; i < m_rpcServers.length; i++)
                {
                        XmlrpcServer xServer = m_rpcServers[i];
                        String hostName = xServer.getHostname();
                        int port = xServer.getPort();
                                
                        if (log.isDebugEnabled())
                                log.debug("Start to set up communication to XMLRPC server: " 
                                        + hostName + "/" + port );

                        try
                        {
                                m_xmlrpcClient  = new XmlRpcClient(hostName, port);
                        }
                        catch (MalformedURLException e)
                        {
                                log.error("Failed to send message to XMLRPC server: " 
                                        + hostName + "/" + port, e);
                                continue;
                        }
                        
                        for ( int k = 0; k < m_retries; k++)
                        {
                                try 
                                {
                                        Object reply = m_xmlrpcClient.execute(
                                                XMLRPC_SERVER_RECEIVE_EVENT_COMMAND, params);
                                                
                                        if (log.isDebugEnabled())
                                                log.debug("Response from XMLRPC server: " + hostName
                                                        + "/" + port + "\n\t" + reply.toString());
                                        success = true;
                                }
                                catch (XmlRpcException e)
                                {
                                        log.error("Failed to send message to XMLRPC server: " 
                                                + hostName + "/" + port, e);
                                }
                                catch (IOException e)
                                {
                                        log.error("Failed to send message to XMLRPC server: " 
                                                + hostName + "/" + port, e);
                                }
                                
                                // break inner loop, no more retries
                                if (success)
                                        break;

                                try
                                {
                                        Thread.sleep(m_elapseTime);
                                }
                                catch (InterruptedException ie) {}
                                
                        }

                        // break outer loop, a working xmlrpc client created.
                        if (success)
                                break;
                                
                }
                
                if (!success)
                {
                        log.error("Can not set up communication with any XMLRPC server");
                        m_xmlrpcClient = null;
                }
        }
}
