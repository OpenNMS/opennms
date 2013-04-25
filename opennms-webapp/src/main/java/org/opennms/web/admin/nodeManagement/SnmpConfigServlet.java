/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.nodeManagement;

import static org.opennms.core.utils.InetAddressUtils.addr;

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
import org.opennms.web.api.Util;

import com.google.common.base.Strings;

/**
 * A servlet that handles configuring SNMP
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:gturner@newedgenetworks.com">Gerald Turner </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
/*
 * TODO MVR is this the servlet for handling the ui that david was talking about? especially the /admin/snmpConfigured.jsp page?
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

        
        
        EventBuilder bldr = new EventBuilder(EventConstants.CONFIGURE_SNMP_EVENT_UEI, "web ui");
        bldr.setInterface(addr(firstIPAddress));
        bldr.setService("SNMP");
        
        bldr.addParam(EventConstants.PARM_FIRST_IP_ADDRESS, firstIPAddress);
        bldr.addParam(EventConstants.PARM_LAST_IP_ADDRESS, lastIPAddress);
        bldr.addParam(EventConstants.PARM_SNMP_READ_COMMUNITY_STRING, communityString);
        
        if ( !Strings.isNullOrEmpty(timeout)) {
        	bldr.addParam(EventConstants.PARM_TIMEOUT, timeout);
        }
        if ( !Strings.isNullOrEmpty(port)) {
        	bldr.addParam(EventConstants.PARM_PORT, port);
        }
        if ( !Strings.isNullOrEmpty(retryCount)) {
        	bldr.addParam(EventConstants.PARM_RETRY_COUNT, retryCount);
        }
        if ( !Strings.isNullOrEmpty(version)) {
        	bldr.addParam(EventConstants.PARM_VERSION, version);
        }
        try {
        	EventProxy eventProxy = Util.createEventProxy();
        	if (eventProxy != null) {
        		eventProxy.send(bldr.getEvent());
        	} else {
        		throw new ServletException("Event proxy object is null, unable to send event " + bldr.getEvent().getUei());
        	}
        } catch (Throwable e) {
        	throw new ServletException("Could not send event " + bldr.getEvent().getUei(), e);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/snmpConfigured.jsp");
        dispatcher.forward(request, response);
    }

}
