package org.opennms.web.performance;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.web.graph.*;


public class AddReportsToUrlServlet extends HttpServlet 
{
    protected PerformanceModel model;

    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel( Vault.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
        }
    }


    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        //required parameter node
        String nodeIdString = request.getParameter("node");        
        if(nodeIdString == null) {
            throw new MissingParameterException("node", new String[] {"node"});
        }

        //optional parameter intf
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
