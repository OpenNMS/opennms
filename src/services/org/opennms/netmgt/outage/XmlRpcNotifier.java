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
// Copyright (C) 2003 Tavve Software Company.  All rights reserved.
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

package org.opennms.netmgt.outage;

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
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>This class create an XMLRPC client and provide methods to notify the 
 * external XMLRPC server for 'nodeLostService', 'nodeRegainedService',
 * 'interfaceDown', 'interfaceUp', 'nodeDown' and 'nodeUp' events.</p>
 *
 * @author 	<A HREF="mailto:jamesz@opennms.com">James Zuo</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public final class XmlRpcNotifier
{

	/**
	 * The event from which data is to be read
	 */
	private Event			m_event;

        /**
         * The xmlrpc client to be used to notify the external xmlrpc server
         * of the occurance of an event
         */
        private XmlRpcClient            m_notifier;
        
 	/**
	 * The constructor
	 *
	 * @param url	        the xmlrpc server url 
	 * @param event 	the event to be notified to the external xmlrpc server
	 */
	public XmlRpcNotifier(String url, Event event) throws MalformedURLException
	{
		m_event = event;
                m_notifier= new XmlRpcClient(url);
                 
	}

	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeLostService' event.
	 */
	public String sendServiceDownEvent() throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(m_event.getNodeid())));
                params.addElement(new String(m_event.getInterface()));
                params.addElement(new String(m_event.getService()));
                params.addElement(new String("Not Available"));
                params.addElement(new String(m_event.getHost()));
                params.addElement(new String(m_event.getTime())); 
                return sendXmlrpcRequest("sendServiceDownEvent", params);
	}
	
        /**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeRegainedService' event.
	 */
	public String sendServiceUpEvent() throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(m_event.getNodeid())));
                params.addElement(new String(m_event.getInterface()));
                params.addElement(new String(m_event.getService()));
                params.addElement(new String("Not Available"));
                params.addElement(new String(m_event.getHost()));
                params.addElement(new String(m_event.getTime())); 
                return sendXmlrpcRequest("sendServiceUpEvent", params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'interfaceDown' event.
	 */
	public String sendInterfaceDownEvent() throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(m_event.getNodeid())));
                params.addElement(new String(m_event.getInterface()));
                params.addElement(new String(m_event.getHost()));
                params.addElement(new String(m_event.getTime())); 
                return sendXmlrpcRequest("sendInterfaceDownEvent", params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'interfaceUp' event.
	 */
	public String sendInterfaceUpEvent() throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(m_event.getNodeid())));
                params.addElement(new String(m_event.getInterface()));
                params.addElement(new String(m_event.getHost()));
                params.addElement(new String(m_event.getTime())); 
                return sendXmlrpcRequest("sendInterfaceUpEvent", params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeDown' event.
	 */
	public String sendNodeDownEvent() throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(m_event.getNodeid())));
                params.addElement(new String(m_event.getHost()));
                params.addElement(new String(m_event.getTime())); 
                return sendXmlrpcRequest("sendNodeDownEvent", params);
	}
        
	/**
	 * <p>Notify the external xmlrpc server the occurance of the 'nodeUp' event.
	 */
	public String sendNodeUpEvent() throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String(getNodeLabel(m_event.getNodeid())));
                params.addElement(new String(m_event.getHost()));
                params.addElement(new String(m_event.getTime())); 
                return sendXmlrpcRequest("sendNodeUpEvent", params);
	}

        /**
         * <p>This method sends an xmlrpc request to an external xmlrpc server and 
         *    gets the response from the xmlrpc server as a String.</p>
         * @param command       The server command to process the request.
         * @param params        a list of parameters need for the external server 
         *                      command to process the request.
         */
	private String sendXmlrpcRequest(String command, Vector params)
                throws XmlRpcException, IOException
        {
                Object reply = m_notifier.execute(command, params);
                return reply.toString();
                        
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
}
