<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.*,org.opennms.web.response.*,java.util.*" %>

<%!
    public final static String[] REQUIRED_PARAMS = new String[] {"rrddir", "title", "style", "ds", "startMonth", "startDate", "startYear", "startHour", "startMonth", "startDate", "startYear", "startHour" };
%>

<%
    String rrdDir = request.getParameter( "rrddir" );
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

    if( rrdDir == null ) {
        throw new MissingParameterException( "rrdDir", REQUIRED_PARAMS );
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
    startCal.set( Calendar.MONTH, Integer.parseInt( startMonth ));
    startCal.set( Calendar.DATE, Integer.parseInt( startDate ));
    startCal.set( Calendar.YEAR, Integer.parseInt( startYear ));
    startCal.set( Calendar.HOUR_OF_DAY, Integer.parseInt( startHour ));
    startCal.set( Calendar.MINUTE, 0 );
    startCal.set( Calendar.SECOND, 0 );
    startCal.set( Calendar.MILLISECOND, 0 );

    Calendar endCal = Calendar.getInstance();
    endCal.set( Calendar.MONTH, Integer.parseInt( endMonth ));
    endCal.set( Calendar.DATE, Integer.parseInt( endDate ));
    endCal.set( Calendar.YEAR, Integer.parseInt( endYear ));
    endCal.set( Calendar.HOUR_OF_DAY, Integer.parseInt( endHour ));
    endCal.set( Calendar.MINUTE, 0 );
    endCal.set( Calendar.SECOND, 0 );
    endCal.set( Calendar.MILLISECOND, 0 );

    String start = Long.toString( startCal.getTime().getTime() );
    String end   = Long.toString( endCal.getTime().getTime() );

    //gather information for displaying around the image
    String startPretty = new Date( Long.parseLong( start )).toString();
    String endPretty   = new Date( Long.parseLong( end )).toString();

    String[] ignores = new String[] { "startMonth", "startYear", "startDate", "startHour","endMonth", "endYear", "endDate", "endHour" };
    Map additions = new HashMap();
    additions.put( "start", start );
    additions.put( "end", end );    
     
    String queryString = Util.makeQueryString( request, additions, ignores ); 
%>

<html>
<head>
  <title>Custom | Response Time | Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='report/index.jsp'>Reports</a>"; %>
<% String breadcrumb2 = "<a href='response/index.jsp'>Response Time</a>"; %>
<% String breadcrumb3 = "Custom"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Response Time Reporting" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellpadding="2" cellspacing="2" border="0">
  <tr>
    <td align="center">
      <img src="response/adhocGraph.png?<%=queryString%>" />
    </td>
  </tr>

  <tr>
    <td align="center">
       <b>From</b> <%=startPretty%> <br>
       <b>To</b> <%=endPretty%>
    </td>
  </tr>
  <tr>
    <td align="center">
	<jsp:include page="/includes/bookmark.jsp" flush="false" />
    </td>
  </tr>

</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
