
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

