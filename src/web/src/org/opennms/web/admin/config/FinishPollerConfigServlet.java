//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

package org.opennms.web.admin.config;

import java.io.IOException;
import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.netmgt.config.*;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.netmgt.xml.event.*;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.netmgt.config.EventconfFactory;

import org.opennms.netmgt.EventConstants;

/**
 * A servlet that handles signaling that the poller config has been updated
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class FinishPollerConfigServlet extends HttpServlet
{
	public void init() 
		throws ServletException
	{
	}
	
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
	        Event newEvent = new Event();
                newEvent.setUei("uei.opennms.org/internal/reloadPollerConfig");
                newEvent.setSource("web ui");
		newEvent.setTime(EventConstants.formatToString(new java.util.Date()));
                
                try
		{
			EventProxy eventProxy = new TcpEventProxy();
                        if (eventProxy != null)
			{
				eventProxy.send(newEvent);
			}
			else
			{
				throw new ServletException("Event proxy object is null, unable to send event " + newEvent.getUei());
			}
		}
		catch(Exception e)
		{
			throw new ServletException("Could not send event " + newEvent.getUei(), e);
		}
		
		//forward the request for proper display
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/index.jsp");
		dispatcher.forward( request, response );
	}
}
