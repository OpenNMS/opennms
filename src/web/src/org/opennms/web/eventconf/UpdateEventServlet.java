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

package org.opennms.web.eventconf;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.bobject.*;
import org.opennms.web.eventconf.*;


/**
 * A servlet that handles updating an event
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class UpdateEventServlet extends HttpServlet
{
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
       	HttpSession user = request.getSession(false);
	
    	if (user != null)
	{
		Event event = (Event)user.getAttribute("event.modify.jsp");
		
		//fill in all the values from the modify.jsp page
		event.setDescription(request.getParameter("descr"));
		event.setLogMessage(request.getParameter("logmsg"));
		event.setLogMessageDest(request.getParameter("logDest"));
		event.setSeverity(request.getParameter("severity"));
		
		//mask information set by dialog page
		
		if ( (request.getParameter("snmp_id") != null && !request.getParameter("snmp_id").trim().equals("")) &&
	             (request.getParameter("snmp_version") != null && !request.getParameter("snmp_version").trim().equals(""))
		   )
		{
			Snmp newSnmp = new Snmp();
			newSnmp.setId(request.getParameter("snmp_id"));
			newSnmp.setIdText(request.getParameter("snmp_text"));
			newSnmp.setVersion(request.getParameter("snmp_version"));
			newSnmp.setSpecific(request.getParameter("snmp_specific"));
			newSnmp.setGeneric(request.getParameter("snmp_generic"));
			newSnmp.setCommunity(request.getParameter("snmp_community"));
			event.setSnmp(newSnmp);
		}
		else
		{
			event.setSnmp(null);
		}
		
		if ( (request.getParameter("correlationMin") != null && !request.getParameter("correlationMin").trim().equals("")) ||
		     (request.getParameter("correlationMax") != null && !request.getParameter("correlationMax").trim().equals("")) ||
		     (request.getParameter("correlationTime") != null && !request.getParameter("correlationTime").trim().equals("")) ||
		     (request.getParameter("correlationUEIs") != null && !request.getParameter("correlationUEIs").trim().equals(""))
		   )
		{
			Correlation newCorrelation = new Correlation();
			parseCorrelationUEIs(request.getParameter("correlationUEIs"), newCorrelation);
			newCorrelation.setCorrelationPath(request.getParameter("correlationPath"));
			newCorrelation.setCorrelationMin(request.getParameter("correlationMin"));
			newCorrelation.setCorrelationMax(request.getParameter("correlationMax"));
			newCorrelation.setCorrelationTime(request.getParameter("correlationTime"));
			newCorrelation.setState(request.getParameter("correlationState"));
			event.setCorrelation(newCorrelation);
		}
		else
		{
			event.setCorrelation(null);
		}
		
		event.setOperInstruct(request.getParameter("operatorInstruction"));
		
		//auto action information set by dialog screen
		//operator action information set by dialog screen
		
		event.setAutoAcknowledge(request.getParameter("autoAcknowledge"));
		event.setAutoAcknowledgeState(request.getParameter("autoAcknowledgeState"));
		
		parseLogGroups(request.getParameter("logGroups"), event);
		parseNotifications(request.getParameter("notifications"), event);
		
		//forwards set by dialog screen
		event.setTticket(request.getParameter("troubleTicket"));
		event.setTticketState(request.getParameter("troubleTicketState"));
		
		event.setMouseOverText(request.getParameter("mouseOver"));
	}
	
	//forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(request.getParameter("redirect"));
        dispatcher.forward( request, response );
    }
    
    /**
    */
    private void parseCorrelationUEIs(String valuesBuffer, Correlation correlation)
    {
	    if (valuesBuffer != null && !valuesBuffer.trim().equals(""))
	    {
		    StringTokenizer tokenizer = new StringTokenizer(valuesBuffer, "\n");
		    
		    while(tokenizer.hasMoreTokens())
		    {
			    String value = tokenizer.nextToken();
			    correlation.addCorrelationUEI(value.trim());
		    }
	    }
    }
    
    /**
    */
    private void parseLogGroups(String valuesBuffer, Event event)
    {
	    event.clearLogGroups();
	    
	    if (valuesBuffer != null && !valuesBuffer.trim().equals(""))
	    {
		    StringTokenizer tokenizer = new StringTokenizer(valuesBuffer, "\n");
		    
		    while(tokenizer.hasMoreTokens())
		    {
			    String value = tokenizer.nextToken();
			    event.addLogGroup(value.trim());
		    }
	    }
    }
    
    /**
    */
    private void parseNotifications(String valuesBuffer, Event event)
    {
	    event.clearNotifications();
	    
	    if (valuesBuffer != null && !valuesBuffer.trim().equals(""))
	    {
		    StringTokenizer tokenizer = new StringTokenizer(valuesBuffer, "\n");
		    
		    while(tokenizer.hasMoreTokens())
		    {
			    String value = tokenizer.nextToken();
			    event.addNotification(value.trim());
		    }
	    }
    }
}
