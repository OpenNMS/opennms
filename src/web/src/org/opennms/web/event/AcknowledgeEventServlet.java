
package org.opennms.web.event;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.event.EventFactory;
import org.opennms.web.MissingParameterException;


/**
 * This servlet receives an HTTP POST with a list of events
 * to acknowledge or unacknowledge, and then it redirects the client to a 
 * URL for display.  The target URL is configurable in the servlet
 * config (web.xml file).
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AcknowledgeEventServlet extends HttpServlet
{
    public final static String ACKNOWLEDGE_ACTION   = "1";
    public final static String UNACKNOWLEDGE_ACTION = "2";
    

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
        String[] eventIdStrings = request.getParameterValues( "event" );
        String action = request.getParameter( "action" );

        if( eventIdStrings == null ) {
            throw new MissingParameterException( "event", new String[] { "event", "action" } );
        }

        if( action == null ) {
            throw new MissingParameterException( "action", new String[] { "event", "action" } );
        }

        //convert the event id strings to ints
        int[] eventIds = new int[eventIdStrings.length];
        for( int i=0; i < eventIds.length; i++ ) {
            eventIds[i] = Integer.parseInt( eventIdStrings[i] );
        }
    
        try {
            if( action.equals( ACKNOWLEDGE_ACTION )) {
                EventFactory.acknowledge( eventIds, request.getRemoteUser() );
            }
            else if( action.equals( UNACKNOWLEDGE_ACTION )) {
                EventFactory.unacknowledge( eventIds );
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

