<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

    //optional parameter limit    
    String limitString = request.getParameter("limit");

    //optional parameter multiple    
    String multipleString = request.getParameter("multiple");

    //optional parameter, limitname
    String limitName = request.getParameter("limitname");
    if(limitName == null) {
        limitName = DEFAULT_LIMIT_PARAM_NAME;
    }

    //optional parameter, multiplename
    String multipleName = request.getParameter("multiplename");
    if(multipleName == null) {
        multipleName = DEFAULT_MULTIPLE_PARAM_NAME;
    }

    //get the count    
    long count = WebSecurityUtils.safeParseLong(countString);
    
    //get the limit, use the default if not set in the request
    int limit    = (limitString != null) ? WebSecurityUtils.safeParseInt(limitString) : DEFAULT_LIMIT;
    if (limit < 1) {
    	limit = DEFAULT_LIMIT;
    }

    // get the multiple, use the default if not set in the request
    int multiple = (multipleString != null) ? Math.max(DEFAULT_MULTIPLE, WebSecurityUtils.safeParseInt(multipleString)) : DEFAULT_MULTIPLE;

    //format the base url to accept limit and multiple parameters
    if( baseUrl.indexOf("?") < 0 ) {
        //does not contain a "?", so append one
        baseUrl = baseUrl + "?";
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
%>

 <% if (limit > 0 ) { %> 
  <strong>Results <%=startResult%>-<%=endResult%> of <%=count%></strong>
 <% } else { %>
  <strong>All Results</strong>
 <% } %> 

<% if( count > limit ) { %>  
  <nav>
    <ul class="pagination pagination-sm">
    <% if( multiple > 0 ) { %>
      <li><a href="<%=baseUrl%>&amp;<%=multipleName%>=0&amp;<%=limitName%>=<%=limit%>">First</a></li>
      <li><a href="<%=baseUrl%>&amp;<%=multipleName%>=<%=multiple-1%>&amp;<%=limitName%>=<%=limit%>">Previous</a></li>
    <% } %>
    
    <% for( int i=startIndex; i <= endIndex; i++ ) { %>
      <% if( multiple == i ) { %>
         <li class="active"><span><%=i+1%></span></li>
      <% } else { %>
        <li><a href="<%=baseUrl%>&amp;<%=multipleName%>=<%=i%>&amp;<%=limitName%>=<%=limit%>"><%=i+1%></a></li>
      <% } %>
    <% } %>
    <% if( multiple < highestPossibleIndex ) { %>
      <li><a href="<%=baseUrl%>&amp;<%=multipleName%>=<%=multiple+1%>&amp;<%=limitName%>=<%=limit%>">Next</a></li>
      <li><a href="<%=baseUrl%>&amp;<%=multipleName%>=<%=highestPossibleIndex%>&amp;<%=limitName%>=<%=limit%>">Last</a></li>
    <% } %>
    </ul>
  </nav>
<% } %>
