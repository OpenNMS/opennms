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
