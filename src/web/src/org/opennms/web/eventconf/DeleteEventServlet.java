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
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.eventconf.*;
import org.opennms.web.parsers.*;
import org.opennms.web.MissingParameterException;


/**
 * This servlet deletes a given event from the eventconf.xml file
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DeleteEventServlet extends HttpServlet
{

    /** The URL to redirect the client to in case of success. */
    protected String redirectSuccess;


    /** 
     * Looks up the <code>dispath.success</code> parameter in the servlet's
     * config.  If not present, this servlet will throw an exception so it
     * will be marked unavailable. 
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter( "redirect.success" );

        if( this.redirectSuccess == null ) {
            throw new UnavailableException( "Require a redirect.success init parameter." );
        }
    }


    /**
     * Delete the event specified in the POST and then redirect the 
     * client to an appropriate URL for display.
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        //required parameter
        String eventUEI = request.getParameter( "event" );
        if( eventUEI == null ) {
            throw new MissingParameterException( "event" );
        }
	
	
        try {
            
	    EventConfFactory eventFactory = EventConfFactory.getInstance();
            eventFactory.removeEvent(eventUEI);
            
            response.sendRedirect( "list.jsp");
        }
        catch( XMLWriteException e) {
            throw new ServletException( "Error writing the eventconf.xml file", e );
        }
	catch( Exception e) {
	    throw new ServletException( "Error deleting event " + eventUEI, e);
	}
    }


    protected String getRedirectString( HttpServletRequest request ) {
        String redirectValue = this.redirectSuccess;
        String redirectParms = request.getParameter( "redirectParms" );

        if( redirectParms != null ) {
            StringBuffer buffer = new StringBuffer( this.redirectSuccess );
            buffer.append( "?" );
            buffer.append( redirectParms );
            redirectValue = buffer.toString();                
        }

        return( redirectValue );
    }

}

