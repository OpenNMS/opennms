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
package org.opennms.web.response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;


public class AddInterfaceToURLServlet extends HttpServlet 
{
    protected ResponseTimeModel model;
    protected String chooseInterfaceUrl;

    
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        
        this.chooseInterfaceUrl = config.getInitParameter("rt-chooseInterfaceUrl");
        if( this.chooseInterfaceUrl == null ) {
            throw new ServletException( "chooseInterfaceUrl is a required init parameter");
        }
        
        try {
            this.model = new ResponseTimeModel( Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the ResponseTimeModel", e );
        }
    }
    

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String nodeId = request.getParameter("node");
        String endUrl = request.getParameter("endUrl");
        
        if( nodeId == null ) {
            throw new MissingParameterException( "node", new String[] {"node", "endUrl"} );
        }
        
        if( endUrl == null ) {
            throw new MissingParameterException( "endUrl", new String[] {"node", "endUrl"} );
        }

        //should return an empty array if no queryable interfaces available

	ArrayList intfs = new ArrayList();
	try
	{
       	    intfs = this.model.getQueryableInterfacesForNode(nodeId);
        }
        catch( Exception e ) {
            throw new ServletException( "Could not query interfaces", e );
	}

        if( intfs == null ) {
            //shouldn't ever happen, but just in case
            throw new ServletException( "Unexpected value: a null array" );
        }
        
        this.log( "DEBUG: Found these interfaces for node " + nodeId + ":");
        for( int i=0; i < intfs.size(); i++ ) {
            this.log( "DEBUG: " + i + "=" + intfs.get(i) );
        }            

        String[] ignores = new String[] {"endUrl"};            
        
        switch( intfs.size() ) {
            case 0:
            {
                //there are no queryable interfaces, but there must be 
                //information in the directory, otherwise the given nodeid would never have
                //been chooseable from the UI.
                throw new ServletException( "Could not find any interfaces to query");
            }
                
            case 1:
            {
                //add the interface, and redirect to end url
                HashMap additions = new HashMap();
                additions.put("intf", intfs.get(0));

                String queryString = Util.makeQueryString(request, additions, ignores);
                
                //this is a servlet context-relative URL (ie external URL), so 
                //we have to add the base URL; the base URL contains a trailing
                //slash, so I do not add one here
                response.sendRedirect( Util.calculateUrlBase(request) + endUrl + "?" + queryString );
                break;
            }

            default:
            {
                //redirect to the chooseInterfaceUrl
                String queryString = Util.makeQueryString(request);
                
                //this is a sibling URL, so no base URL is needed
                response.sendRedirect( this.chooseInterfaceUrl + "?" + queryString );
                break;
            }
        }        
    }
}
