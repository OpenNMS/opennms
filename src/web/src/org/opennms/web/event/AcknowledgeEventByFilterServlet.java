package org.opennms.web.event;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.*;
import org.opennms.web.MissingParameterException;
import org.opennms.web.event.filter.*;


/**
 * This servlet receives an HTTP POST with a list of events
 * to acknowledge or unacknowledge, and then it redirects the client to a 
 * URL for display.  The target URL is configurable in the servlet
 * config (web.xml file).
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AcknowledgeEventByFilterServlet extends HttpServlet
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
     * Acknowledge the events specified in the POST and then redirect the 
     * client to an appropriate URL for display.
     */
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        //required parameter
        String[] filterStrings = request.getParameterValues( "filter" );
        String action = request.getParameter( "action" );

        if( filterStrings == null ) {
            filterStrings = new String[0];
        }

        if( action == null ) {
            throw new MissingParameterException( "action", new String[] { "filter", "action" } );
        }

        //handle the filter parameters        
        ArrayList filterArray = new ArrayList();
        for( int i=0; i < filterStrings.length; i++ ) {
            Filter filter = EventUtil.getFilter( filterStrings[i] );
            if( filter != null ) {
                filterArray.add( filter );
            }
        }
        
        Filter[] filters = new Filter[filterArray.size()];                        
        filters = (Filter[])filterArray.toArray( filters );
                
        try {
            if( action.equals( AcknowledgeEventServlet.ACKNOWLEDGE_ACTION )) {
                EventFactory.acknowledge( filters, request.getRemoteUser() );
            }
            else if( action.equals( AcknowledgeEventServlet.UNACKNOWLEDGE_ACTION )) {
                EventFactory.unacknowledge( filters );
            }
            else {
                throw new ServletException( "Unknown acknowledge action: " + action );
            }

            response.sendRedirect( this.getRedirectString(request) );
        }
        catch( SQLException e ) {
            throw new ServletException( "Database exception", e );
        }
    }

    
    /** 
     * Convenience method for dynamically creating the redirect URL if 
     * necessary.
     */
    protected String getRedirectString( HttpServletRequest request ) {
        String redirectValue = request.getParameter("redirect");

        if( redirectValue != null ) {
            return( redirectValue );
        }
        
        redirectValue = this.redirectSuccess;
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


