
package org.opennms.web.notification;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * A servlet that handles querying the notifications table and and then forwards
 * the query's result to a JSP for display.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NoticeQueryServlet extends HttpServlet
{
    public static final int DEFAULT_LIMIT = 25; 
    public static final int DEFAULT_MULTIPLE = 0;


    /**
     * Parses the query string to determine what type of notice query to perform
     * (for example, what to filter on or sort by), then does the database 
     * query (through the NoticeFactory) and then forwards the results to a
     * JSP for display.
     *
     * <p>Sets the <em>notices</em> and <em>parms</em> request attributes 
     * for the forwardee JSP (or whatever gets called).</p>
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        //handle the style sort parameter
        String sortStyleString = request.getParameter( "sortby" );
        NoticeFactory.SortStyle sortStyle = NoticeFactory.SortStyle.ID;
        if( sortStyleString != null ) {
            Object temp = NoticeUtil.getSortStyle( sortStyleString );
            if( temp != null ) {
                sortStyle = (NoticeFactory.SortStyle)temp;
            }
        }
	
        //handle the acknowledgement type parameter
        String ackTypeString = request.getParameter( "acktype" );
        NoticeFactory.AcknowledgeType ackType = NoticeFactory.AcknowledgeType.UNACKNOWLEDGED;
        if( ackTypeString != null ) {
            Object temp = NoticeUtil.getAcknowledgeType( ackTypeString );
            if( temp != null ) {
                ackType = (NoticeFactory.AcknowledgeType)temp;
            }
        }
	
        //handle the filter parameters        
        String[] filterStrings = request.getParameterValues( "filter" );
        ArrayList filterArray = new ArrayList();
        if( filterStrings != null ) {
            for( int i=0; i < filterStrings.length; i++ ) {
                NoticeFactory.Filter filter = NoticeUtil.getFilter( filterStrings[i] );
                if( filter != null ) {
                    filterArray.add( filter );
                }
            }
        }
	
        //handle the optional limit parameter
        String limitString = request.getParameter( "limit" );
        int limit = DEFAULT_LIMIT;
        if( limitString != null ) {
            try {
                limit = Integer.parseInt( limitString );
            }
            catch( NumberFormatException e ) {}
        }
	
        //handle the optional multiple parameter
        String multipleString = request.getParameter( "multiple" );
        int multiple = DEFAULT_MULTIPLE;
        if( multipleString != null ) {
            try {
                multiple = Integer.parseInt( multipleString );
            }
            catch( NumberFormatException e ) {}
        }
	
        try {
            //put the parameters in a convenient struct
            NoticeQueryParms parms = new NoticeQueryParms();
            parms.sortStyle = sortStyle;
            parms.ackType = ackType;
            parms.filters = filterArray;
            parms.limit = limit;
            parms.multiple = multiple;
	    
            //query the notices with the new filters array
            Notification[] notices = NoticeFactory.getNotices( sortStyle, ackType, parms.getFilters(), limit, multiple*limit );
	    
            //add the necessary data to the request so the
            //JSP (or whatever gets called) can create the view correctly
            request.setAttribute( "notices", notices );
            request.setAttribute( "parms", parms );
	    
            //forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher( "/notification/browser.jsp" );
            dispatcher.forward( request, response );
        }
        catch( SQLException e ) {
            throw new ServletException( "", e );
        }
    }

}

