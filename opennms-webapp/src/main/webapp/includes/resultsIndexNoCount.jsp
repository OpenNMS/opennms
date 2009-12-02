<!--

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

-->

<%@page language="java" contentType="text/html" session="false" import="java.util.*,org.opennms.web.WebSecurityUtils,org.opennms.web.MissingParameterException" %>

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
    String itemCountString = request.getParameter("itemCount");
    if( itemCountString == null ) {
        throw new MissingParameterException("itemCount", new String[] {"itemCount", "baseurl"});
    }
    
    //required parameter baseurl    
    String baseUrl = request.getParameter("baseurl");
    if( baseUrl == null ) {
        throw new MissingParameterException("baseurl", new String[] {"itemCount", "baseurl"});
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
    long itemCount = WebSecurityUtils.safeParseLong(itemCountString);
    
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
    
    if ( baseUrl.indexOf(limitName) < 0) {
        baseUrl = baseUrl + "&" + limitName + "=" + limit;
    }

    //calculate the start and end numbers of the results that we are showing
    long startResult = (multiple==0) ? 1 : multiple*limit+1;
    long endResult = startResult + itemCount - 1;

%>

<p class="pager">
 <% if (limit > 0 ) { %> 
  Results: (<%=startResult%>-<%=endResult%>)
 <% } else { %>
  All Results
 <% } %> 
	
  <% if( itemCount >= limit || multiple > 0 ) { %>  
    <span>
<% if( multiple > 0 ) { %>
      <a href="<%=baseUrl%>&<%=multipleName%>=0">First</a>&nbsp;  
      <a href="<%=baseUrl%>&<%=multipleName%>=<%=multiple-1%>">Previous</a>&nbsp;  
    <% } %>
    
    <% if( itemCount >= limit  ) { %>
      <a href="<%=baseUrl%>&<%=multipleName%>=<%=multiple+1%>">Next</a>&nbsp;
    <% } %>
		</span>
   <% } %>      
</p>

