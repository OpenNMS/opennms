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

// 06/19/2007 Added support for more Parameters
//
// 03/08/2005 Created.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.Util;

/**
 * A servlet that handles configuring SNMP
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class SnmpConfigServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 2824294300141467193L;
    /** Log4j. */
    private final static Logger log =
        Logger.getLogger(SnmpConfigServlet.class);

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstIPAddress = request.getParameter("firstIPAddress");
        String lastIPAddress = request.getParameter("lastIPAddress");
        String communityString = request.getParameter("communityString");
        String timeout = request.getParameter("timeout");
        String version = request.getParameter("version");
        String retryCount = request.getParameter("retryCount");
        String port = request.getParameter("port");
        if (log.isDebugEnabled())
            log.debug("doPost: firstIPAddress=" + firstIPAddress + ", "
                      + "lastIPAddress=" + lastIPAddress + ", and "
                      + "communityString=" + communityString);

        
        
        Event newEvent = new Event();
        newEvent.setUei(EventConstants.CONFIGURE_SNMP_EVENT_UEI);
        newEvent.setSource("web ui");
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));
        newEvent.setService("SNMP");
        newEvent.setInterface(firstIPAddress);
        
        EventBuilder builder = new EventBuilder(newEvent);
        builder.addParam(EventConstants.PARM_FIRST_IP_ADDRESS, firstIPAddress);
        builder.addParam(EventConstants.PARM_LAST_IP_ADDRESS, lastIPAddress);
        builder.addParam(EventConstants.PARM_COMMUNITY_STRING, communityString);
        
        if ( timeout.length() > 0) {
        	builder.addParam(EventConstants.PARM_TIMEOUT, timeout);
        }
        if ( port.length() > 0 ) {
        	builder.addParam(EventConstants.PARM_PORT, port);
        }
        if ( retryCount.length() > 0 ) {
        	builder.addParam(EventConstants.PARM_RETRY_COUNT, retryCount);
        }
        if ( version.length() > 0 ) {
        	builder.addParam(EventConstants.PARM_VERSION, version);
        }
        try {
        	EventProxy eventProxy = Util.createEventProxy();
        	if (eventProxy != null) {
        		eventProxy.send(builder.getEvent());
        	} else {
        		throw new ServletException("Event proxy object is null, unable to send event " + newEvent.getUei());
        	}
        } catch (Exception e) {
        	throw new ServletException("Could not send event " + newEvent.getUei(), e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/snmpConfigured.jsp");
        dispatcher.forward(request, response);
    }

}
