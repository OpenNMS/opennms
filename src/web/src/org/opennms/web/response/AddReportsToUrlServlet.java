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
// Modifications:
//
// 12 Nov 2002: Added response time reports to webUI.
//
// Original coda base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.web.graph.PrefabGraph;


public class AddReportsToUrlServlet extends HttpServlet 
{
    protected ResponseTimeModel model;

    public void init() throws ServletException {
        try {
            this.model = new ResponseTimeModel( Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the ResponseTimeModel", e );
        }
    }


    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        //required parameter node
        String nodeIdString = request.getParameter("node");        
        if(nodeIdString == null) {
            throw new MissingParameterException("node", new String[] {"node"});
        }

        String intf = request.getParameter( "intf" );
        
        // In this block of code, it is possible to end up with an empty
        // list of queries. This will result in a somewhat cryptic
        // "Missing parameter" message on the results.jsp page and will
        // probably be changed soon to a nicer error message.

        PrefabGraph[] queries = null;
        
        if(intf == null ) {
            queries = this.model.getQueries(nodeIdString);            
        }
        else {            
            boolean showNodeQueries = true;
            queries = this.model.getQueries(nodeIdString, intf, showNodeQueries);            
        }
        
        String[] queryNames = new String[queries.length];

        for( int i=0; i < queries.length; i++ ) {
            queryNames[i] = queries[i].getName();
        }

        Map additions = new HashMap();
        additions.put( "reports", queryNames );
        String queryString = Util.makeQueryString( request, additions );

        response.sendRedirect( "results.jsp?" + queryString );
    }
}
