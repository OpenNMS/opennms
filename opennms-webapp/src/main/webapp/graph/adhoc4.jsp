<%--

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
// 2006 Aug 15: Fix bad HTML tag from bug #1558. - dj@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

--%>

<%@page language="java"	contentType="text/html"	session="true"%>
<%@page import="org.opennms.web.*,org.opennms.web.api.Util"%>
<%@page import="java.util.*"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>

<%!
    public final static String[] REQUIRED_PARAMS =
	new String[] {
		"resourceId",
		"title",
		"style",
		"ds",
		"startMonth",
		"startDate",
		"startYear",
		"startHour",
		"startMonth",
		"startDate",
		"startYear",
		"startHour"
	};
%>

<%
	String resourceId = request.getParameter( "resourceId" );

    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    
    ResourceService resourceService = (ResourceService)webAppContext.getBean("resourceService", ResourceService.class);
    
    resourceService.promoteGraphAttributesForResource(resourceId);
    

    String title = request.getParameter( "title" );
    String style = request.getParameter( "style" );
    String ds = request.getParameter( "ds" );

    String startMonth = request.getParameter( "startMonth" );
    String startDate  = request.getParameter( "startDate" );
    String startYear  = request.getParameter( "startYear" );
    String startHour  = request.getParameter( "startHour" );

    String endMonth = request.getParameter( "endMonth" );
    String endDate  = request.getParameter( "endDate" );
    String endYear  = request.getParameter( "endYear" );
    String endHour  = request.getParameter( "endHour" );

    
    if( resourceId == null ) {
        throw new MissingParameterException( "resourceId", REQUIRED_PARAMS );
    }
    if( title == null ) {
        throw new MissingParameterException( "title", REQUIRED_PARAMS );
    }
    if( style == null ) {
        throw new MissingParameterException( "style", REQUIRED_PARAMS );
    }
    if( ds == null ) {
        throw new MissingParameterException( "ds", REQUIRED_PARAMS );
    }
    if( startMonth == null ) {
        throw new MissingParameterException( "startMonth", REQUIRED_PARAMS );
    }
    if( startDate == null ) {
        throw new MissingParameterException( "startDate", REQUIRED_PARAMS );
    }
    if( startYear == null ) {
        throw new MissingParameterException( "startYear", REQUIRED_PARAMS );
    }
    if( startHour == null ) {
        throw new MissingParameterException( "startHour", REQUIRED_PARAMS );
    }
    if( endMonth == null ) {
        throw new MissingParameterException( "endMonth", REQUIRED_PARAMS );
    }
    if( endDate == null ) {
        throw new MissingParameterException( "endDate", REQUIRED_PARAMS );
    }
    if( endYear == null ) {
        throw new MissingParameterException( "endYear", REQUIRED_PARAMS );
    }
    if( endHour == null ) {
        throw new MissingParameterException( "endHour", REQUIRED_PARAMS );
    }

    Calendar startCal = Calendar.getInstance();
    startCal.set( Calendar.MONTH, WebSecurityUtils.safeParseInt( startMonth ));
    startCal.set( Calendar.DATE, WebSecurityUtils.safeParseInt( startDate ));
    startCal.set( Calendar.YEAR, WebSecurityUtils.safeParseInt( startYear ));
    startCal.set( Calendar.HOUR_OF_DAY, WebSecurityUtils.safeParseInt( startHour ));
    startCal.set( Calendar.MINUTE, 0 );
    startCal.set( Calendar.SECOND, 0 );
    startCal.set( Calendar.MILLISECOND, 0 );

    Calendar endCal = Calendar.getInstance();
    endCal.set( Calendar.MONTH, WebSecurityUtils.safeParseInt( endMonth ));
    endCal.set( Calendar.DATE, WebSecurityUtils.safeParseInt( endDate ));
    endCal.set( Calendar.YEAR, WebSecurityUtils.safeParseInt( endYear ));
    endCal.set( Calendar.HOUR_OF_DAY, WebSecurityUtils.safeParseInt( endHour ));
    endCal.set( Calendar.MINUTE, 0 );
    endCal.set( Calendar.SECOND, 0 );
    endCal.set( Calendar.MILLISECOND, 0 );

    String start = Long.toString( startCal.getTime().getTime() );
    String end   = Long.toString( endCal.getTime().getTime() );

    //gather information for displaying around the image
    String startPretty = new Date( WebSecurityUtils.safeParseLong( start )).toString();
    String endPretty   = new Date( WebSecurityUtils.safeParseLong( end )).toString();
    
    
    String[] ignores = new String[] { "startMonth", "startYear", "startDate", "startHour","endMonth", "endYear", "endDate", "endHour" };
    Map additions = new HashMap();
    additions.put( "start", start );
    additions.put( "end", end );
    additions.put( "adhoc", "true" );
     
    String queryString = Util.makeQueryString( request, additions, ignores );
%>

<%@page import="org.opennms.web.svclayer.ResourceService"%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Resource Graphs" />
  <jsp:param name="headTitle" value="Custom" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Custom" />
</jsp:include>

<div align="center">
  <img src="graph/graph.png?<%=queryString%>" />

  <br/>

  <strong>From:</strong> <%=startPretty%> <br/>
  <strong>To:</strong> <%=endPretty%>

  <br/>
  <br/>

  <jsp:include page="/includes/bookmark.jsp" flush="false" />

</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
