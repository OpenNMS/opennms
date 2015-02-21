<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="false" import="org.opennms.core.utils.WebSecurityUtils,org.opennms.web.servlet.MissingParameterException" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%!
    protected static final String DEFAULT_LIMIT_PARAM_NAME    = "limit";
    protected static final String DEFAULT_MULTIPLE_PARAM_NAME = "multiple";

    protected static final int DEFAULT_LIMIT    = 25;
    protected static final int DEFAULT_MULTIPLE = 0;

    protected static final int LOWER_OFFSET = 5;
    protected static final int UPPER_OFFSET = 4;    
%>

<%
    //required parameter count
    String countString = request.getParameter("count");
    if( countString == null ) {
        throw new MissingParameterException("count", new String[] {"count", "baseurl"});
    }
    
    //required parameter baseurl    
    String baseUrl = request.getParameter("baseurl");
    if( baseUrl == null ) {
        throw new MissingParameterException("baseurl", new String[] {"count", "baseurl"});
    }

    //optional parameter, limitname
    String limitName = request.getParameter("limitname");
    if(limitName == null || "".equals(limitName)) {
        limitName = DEFAULT_LIMIT_PARAM_NAME;
    }

    //optional parameter limit
    String limitString = request.getParameter(limitName);

    //optional parameter, multiplename
    String multipleName = request.getParameter("multiplename");
    if(multipleName == null || "".equals(limitName)) {
        multipleName = DEFAULT_MULTIPLE_PARAM_NAME;
    }

    //optional parameter multiple
    String multipleString = request.getParameter(multipleName);

    //get the count
    long count = WebSecurityUtils.safeParseLong(countString);

    //get the limit, use the default if not set in the request
    int limit    = (limitString != null) ? WebSecurityUtils.safeParseInt(limitString) : DEFAULT_LIMIT;
    if (limit < 1) {
    	limit = DEFAULT_LIMIT;
    }

    // get the multiple, use the default if not set in the request
    int multiple = (multipleString != null) ? Math.max(DEFAULT_MULTIPLE, WebSecurityUtils.safeParseInt(multipleString)) : DEFAULT_MULTIPLE;

    // Remove any limit parameters in the baseUrl
    if (limitString != null) {
        baseUrl = baseUrl.replace("?"+limitName+"="+limitString+"&", "?");
        baseUrl = baseUrl.replace("&"+limitName+"="+limitString, "");
        baseUrl = baseUrl.replace("&amp;"+limitName+"="+limitString, "");
        if (baseUrl.endsWith("&")) {
            baseUrl = baseUrl.substring(0,baseUrl.length()-1);
        }
        if (baseUrl.endsWith("&amp;")) {
            baseUrl = baseUrl.substring(0,baseUrl.length()-5);
        }
    }

    //calculate the start and end numbers of the results that we are showing
    long startResult = (multiple==0) ? 1 : multiple*limit;
    long endResult = (multiple+1)*limit;
    endResult = (endResult < count) ? endResult : count;

    //this is the total number of pages, each showing <limit> number of results,
    //that it would take to display the entire result set with <count> results
    int highestPossibleIndex = (int)Math.ceil(count/(float)limit)-1;

    //calculate the start and end number of the page indices that we are showing
    int startIndex = multiple-LOWER_OFFSET;
    startIndex = (startIndex < 0) ? 0 : startIndex;

    int endIndex = multiple+UPPER_OFFSET;
    endIndex = (endIndex > highestPossibleIndex) ? highestPossibleIndex : endIndex;

    Integer[] limitList = { 10, 25, 50, 100, 250, 500, 1000, 2000 };
%>
<c:url var="firstUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="0"/>
</c:url>
<c:url var="previousUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="<%=Integer.toString(multiple-1)%>"/>
</c:url>
<c:url var="nextUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="<%=Integer.toString(multiple+1)%>"/>
</c:url>
<c:url var="lastUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="<%=Integer.toString(highestPossibleIndex)%>"/>
</c:url>

 <nav class="navbar" style="min-height: 25px;">
   <div class="row" style="margin-right: 0px">
     <div class="navbar-text navbar-left" style="margin-top: 5px; margin-bottom: 5px;">
 <% if ( limit > 0 ) { %>
  <strong>Results <%=startResult%>-<%=endResult%> of <%=count%></strong>
 <% } else { %>
  <strong>All Results</strong>
 <% } %>
     </div>

<% if( count > limit ) { %>
    <ul class="pagination pagination-sm nav navbar-nav navbar-right" style="margin: 5px 0px;">
    <% if( multiple > 0 ) { %>
      <li><a href="${firstUrl}">First</a></li>
      <li><a href="${previousUrl}">Previous</a></li>
    <% } %>

    <% for( int i=startIndex; i <= endIndex; i++ ) { %>
      <% if( multiple == i ) { %>
      <li class="active"><span><%=i+1%></span></li>
      <% } else { %>
        <c:url var="pagedUrl" value="<%=baseUrl%>">
          <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
          <c:param name="<%=multipleName%>" value="<%=Integer.toString(i)%>"/>
        </c:url>
      <li><a href="${pagedUrl}"><%=i+1%></a></li>
      <% } %>
    <% } %>
    <% if( multiple < highestPossibleIndex ) { %>
      <li><a href="${nextUrl}">Next</a></li>
      <li><a href="${lastUrl}">Last</a></li>
    <% } %>
      <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Limit <span class="caret"></span></a>
        <ul class="dropdown-menu" role="menu">
          <% for ( int i : limitList ) { %>
          <li class="<%=i==limit?"disabled":""%>"><a href="<%=baseUrl%>&<%=limitName%>=<%=i%>"><%=i%></a></li>
          <% } %>
        </ul>
      </li>
    </ul>
<% } %>
    </div>
  </nav>
