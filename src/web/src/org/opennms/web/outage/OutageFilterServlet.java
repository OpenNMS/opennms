
package org.opennms.web.outage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.opennms.web.outage.filter.*;


/**
 * A servlet that handles querying the outages table and and then forwards
 * the query's result to a JSP for display.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class OutageFilterServlet extends HttpServlet
{
    public static final int DEFAULT_LIMIT = 25; 
    public static final int DEFAULT_MULTIPLE = 0;


    /**
     * Parses the query string to determine what type of outage query to perform
     * (for example, what to filter on or sort by), then does the database 
     * query (through the OutageFactory) and then forwards the results to a
     * JSP for display.
     *
     * <p>Sets the <em>notices</em> and <em>parms</em> request attributes 
     * for the forwardee JSP (or whatever gets called).</p>
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        //handle the style sort parameter
        String sortStyleString = request.getParameter( "sortby" );
        OutageFactory.SortStyle sortStyle = OutageFactory.DEFAULT_SORT_STYLE;
        if( sortStyleString != null ) {
            Object temp = OutageUtil.getSortStyle( sortStyleString );
            if( temp != null ) {
                sortStyle = (OutageFactory.SortStyle)temp;
            }
        }

        //handle the acknowledgement type parameter
        String outTypeString = request.getParameter( "outtype" );
        OutageFactory.OutageType outType = OutageFactory.OutageType.BOTH;
        if( outTypeString != null ) {
            Object temp = OutageUtil.getOutageType( outTypeString );
            if( temp != null ) {
                outType = (OutageFactory.OutageType)temp;
            }
        }

        //handle the filter parameters        
        String[] filterStrings = request.getParameterValues( "filter" );
        ArrayList filterArray = new ArrayList();
        if( filterStrings != null ) {
            for( int i=0; i < filterStrings.length; i++ ) {
                Filter filter = OutageUtil.getFilter( filterStrings[i] );
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
            OutageQueryParms parms = new OutageQueryParms();
            parms.sortStyle = sortStyle;
            parms.outageType = outType;
            parms.filters = filterArray;
            parms.limit = limit;
            parms.multiple = multiple;
    
            //query the notices with the new filters array
            Outage[] outages = OutageFactory.getOutages( sortStyle, outType, parms.getFilters(), limit, multiple*limit );
    
            //add the necessary data to the request so the
            //JSP (or whatever gets called) can create the view correctly
            request.setAttribute( "outages", outages );
            request.setAttribute( "parms", parms );
    
            //forward the request for proper display
            RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher( "/outage/list.jsp" );
            dispatcher.forward( request, response );
        }
        catch( SQLException e ) {
            throw new ServletException( "Error while querying database for outages", e );
        }
    }

}

