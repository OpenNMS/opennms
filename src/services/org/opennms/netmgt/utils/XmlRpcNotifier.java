//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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

package org.opennms.netmgt.utils;

import java.lang.*;
import java.io.IOException;
import java.util.Vector;
import java.net.MalformedURLException;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

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
         * The xmlrpc client to be used to notify the external xmlrpc server
         * of the occurance of an event
         */
        private XmlRpcClient            m_notifier;
        
 	/**
	 * The constructor
	 *
	 * @param url	        the xmlrpc server url
	 */
	public XmlRpcNotifier(String url) throws MalformedURLException
	{
                m_notifier= new XmlRpcClient(url);
                 
	}

	/**
	 * <p>Notify the external xmlrpc server the success of processing an event.
	 *
         * @param txNo          the external transaction number for an event.
         * @param uei           the event uei.
         * @param message       the text message to indicate the success.
         *
         */
	public String notifySuccess(long txNo, String uei, String message) throws XmlRpcException, IOException
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
	public String notifyFailure(long txNo, String uei, String reason) throws XmlRpcException, IOException
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
	public String notifyReceivedEvent(long txNo, String uei, String message) throws XmlRpcException, IOException
	{
                // Create the request parameters list
                Vector params = new Vector();
                params.addElement(new String((new Long(txNo)).toString()));
                params.addElement(new String(uei));
                params.addElement(new String(message));
                return sendXmlrpcRequest(XMLRPC_SERVER_RECEIVE_EVENT_COMMAND, params);
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
}
