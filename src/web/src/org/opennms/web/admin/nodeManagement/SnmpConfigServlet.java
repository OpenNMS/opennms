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

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * A servlet that handles configuring SNMP
 *
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpConfigServlet extends HttpServlet {

    /** Log4j. */
    private final static Logger log =
        Logger.getLogger(SnmpConfigServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstIPAddress = request.getParameter("firstIPAddress");
        String lastIPAddress = request.getParameter("lastIPAddress");
        String communityString = request.getParameter("communityString");
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
        Parms eventParms = new Parms();
        newEvent.setParms(eventParms);
        Parm eventParm = null;
        Value parmValue = null;
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_FIRST_IP_ADDRESS);
        parmValue = new Value();
        parmValue.setContent(firstIPAddress);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_LAST_IP_ADDRESS);
        parmValue = new Value();
        parmValue.setContent(lastIPAddress);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_COMMUNITY_STRING);
        parmValue = new Value();
        parmValue.setContent(communityString);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        try {
            EventProxy eventProxy = new TcpEventProxy();
            if (eventProxy != null) {
                eventProxy.send(newEvent);
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
