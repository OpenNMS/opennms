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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,org.opennms.web.*,org.opennms.web.api.Util"
%>

<%  
    Calendar now = Calendar.getInstance();

    Calendar yesterday = Calendar.getInstance();
    yesterday.add( Calendar.DATE, -1 );
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Resource Graphs" />
  <jsp:param name="headTitle" value="Custom" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Custom" />
</jsp:include>

<form method="get" action="graph/adhoc4.jsp">
  <%=Util.makeHiddenTags( request )%>

  <h3>Step 3: Choose the Title for the Graph</h3>
 
  Title: <input type="text" name="title" value="Graph Title" />

  <h3>Step 4: Choose the Time Span of the Graph</h3>

  Query from date<br/>

  <select name="startMonth" size="1">
    <% for( int i = 0; i < 12; i++ ) { %>
      <option value="<%=i%>" <% if( yesterday.get( Calendar.MONTH ) == i ) out.print("selected=\"selected\" ");%>><%=MONTHS[i]%></option>
    <% } %>
  </select>

  <input type="text" name="startDate" size="4" maxlength="2" value="<%=yesterday.get( Calendar.DATE )%>" />
  <input type="text" name="startYear" size="6" maxlength="4" value="<%=yesterday.get( Calendar.YEAR )%>" />

  <select name="startHour" size="1">
    <% int yesterdayHour = yesterday.get( Calendar.HOUR_OF_DAY ); %>
    <% for( int i = 1; i < 25; i++ ) { %>
      <option value="<%=i%>" <% if( yesterdayHour == i ) out.print( "selected " ); %>>
        <%=(i<13) ? i : i-12%>&nbsp;<%=(i<13) ? "AM" : "PM"%>
      </option>
    <% } %>
  </select>

  <br/>

  Query to date<br/>

  <select name="endMonth" size="1">
    <% for( int i = 0; i < 12; i++ ) { %>
      <option value="<%=i%>" <% if( now.get( Calendar.MONTH ) == i ) out.print("selected=\"selected\" ");%>><%=MONTHS[i]%></option>
    <% } %>
  </select>

  <input type="text" name="endDate" size="4" maxlength="2" value="<%=now.get( Calendar.DATE )%>" />
  <input type="text" name="endYear" size="6" maxlength="4" value="<%=now.get( Calendar.YEAR )%>" />

  <select name="endHour" size="1">
    <% int nowHour = now.get( Calendar.HOUR_OF_DAY ); %>
    <% for( int i = 1; i < 25; i++ ) { %>
      <option value="<%=i%>" <% if( nowHour == i ) out.print( "selected " ); %>>
        <%=(i<13) ? i : i-12%>&nbsp;<%=(i<13) ? "AM" : "PM"%>
      </option>
    <% } %>
  </select>

  <br/>
  <br/>

  <input type="submit" value="Next"/>
  <input type="reset" />
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    //note these run from 0-11, this is because of java.util.Calendar!
    public static final String[] MONTHS = new String[] {
       "January",
       "February",
       "March",
       "April",
       "May",
       "June",
       "July",
       "August",
       "September",
       "October",
       "November",
       "December"
    };
%>
