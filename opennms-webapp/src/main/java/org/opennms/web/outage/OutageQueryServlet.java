//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

package org.opennms.web.outage;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that handles querying the outages table and and then forwards the
 * query's result to a JSP for display.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class OutageQueryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     *
     * Parses the query string to determine what type of outage query to perform
     * (for example, what to filter on or sort by), then does the database query
     * (through the OutageFactory) and then forwards the results to a JSP for
     * display.
     *
     * <p>
     * Sets the <em>notices</em> and <em>parms</em> request attributes for
     * the forwardee JSP (or whatever gets called).
     * </p>
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * //handle the style sort parameter String sortStyleString =
         * request.getParameter( "sortby" ); OutageFactory.SortStyle sortStyle =
         * OutageFactory.SortStyle.IFLOSTSERVICE; if( sortStyleString != null ) {
         * Object temp = OutageUtil.getSortStyle( sortStyleString ); if( temp !=
         * null ) { sortStyle = (OutageFactory.SortStyle)temp; } }
         * 
         * //handle the acknowledgement type parameter String outTypeString =
         * request.getParameter( "outtype" ); OutageFactory.OutageType outType =
         * OutageFactory.OutageType.CURRENT; if( outTypeString != null ) {
         * Object temp = OutageUtil.getOutageType( outTypeString ); if( temp !=
         * null ) { outType = (OutageFactory.OutageType)temp; } }
         * 
         * //handle the filter parameters String[] filterStrings =
         * request.getParameterValues( "filter" ); ArrayList filterArray = new
         * ArrayList(); if( filterStrings != null ) { for( int i=0; i <
         * filterStrings.length; i++ ) { OutageFactory.Filter filter =
         * OutageUtil.getFilter( filterStrings[i] ); if( filter != null ) {
         * filterArray.add( filter ); } } }
         * 
         * //handle the optional limit parameter String limitString =
         * request.getParameter( "limit" ); int limit = DEFAULT_LIMIT; if(
         * limitString != null ) { try { limit = Integer.parseInt( limitString ); }
         * catch( NumberFormatException e ) {} }
         * 
         * //handle the optional multiple parameter String multipleString =
         * request.getParameter( "multiple" ); int multiple = DEFAULT_MULTIPLE;
         * if( multipleString != null ) { try { multiple = Integer.parseInt(
         * multipleString ); } catch( NumberFormatException e ) {} }
         * 
         * try { //put the parameters in a convenient struct OutageQueryParms
         * parms = new OutageQueryParms(); parms.sortStyle = sortStyle;
         * parms.outType = outType; parms.filters = filterArray; parms.limit =
         * limit; parms.multiple = multiple;
         * 
         * //query the notices with the new filters array Outage[] outages =
         * OutageFactory.getOutages( sortStyle, outType, parms.getFilters(),
         * limit, multiple*limit );
         * 
         * //add the necessary data to the request so the //JSP (or whatever
         * gets called) can create the view correctly request.setAttribute(
         * "outages", outages ); request.setAttribute( "parms", parms );
         * 
         * //forward the request for proper display RequestDispatcher dispatcher =
         * this.getServletContext().getRequestDispatcher( "/outage/browser.jsp" );
         * dispatcher.forward( request, response ); } catch( SQLException e ) {
         * throw new ServletException( "", e ); }
         */
    }

}
