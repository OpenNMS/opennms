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
    String itemCountString = request.getParameter("itemCount");
    if( itemCountString == null ) {
        throw new MissingParameterException("itemCount", new String[] {"itemCount", "baseurl"});
    }
    
    //required parameter baseurl    
    String baseUrl = request.getParameter("baseurl");
    if( baseUrl == null ) {
        throw new MissingParameterException("baseurl", new String[] {"itemCount", "baseurl"});
    }

    //optional parameter, limitname
    String limitName = request.getParameter("limitname");
    if(limitName == null) {
        limitName = DEFAULT_LIMIT_PARAM_NAME;
    }

    //optional parameter limit
    String limitString = request.getParameter(limitName);

    // Remove any limit parameters in the baseUrl
    if (limitString != null) {
        baseUrl = baseUrl.replace("&"+limitName+"="+limitString, "");
        baseUrl = baseUrl.replace("&amp;"+limitName+"="+limitString, "");
        if (baseUrl.endsWith("&")) {
            baseUrl = baseUrl.substring(0,baseUrl.length()-1);
        }
        if (baseUrl.endsWith("&amp;")) {
            baseUrl = baseUrl.substring(0,baseUrl.length()-5);
        }
    }

    //optional parameter, multiplename
    String multipleName = request.getParameter("multiplename");
    if(multipleName == null) {
        multipleName = DEFAULT_MULTIPLE_PARAM_NAME;
    }

    //optional parameter multiple
    String multipleString = request.getParameter(multipleName);

    String footerStr = request.getParameter("footer");
    if (footerStr == null || "".equals(footerStr)) {
        footerStr = "false";
    }
    boolean inFooter = "true".equals(footerStr);

    //get the count
    long itemCount = WebSecurityUtils.safeParseLong(itemCountString);

    //get the limit, use the default if not set in the request
    int limit    = (limitString != null) ? WebSecurityUtils.safeParseInt(limitString) : DEFAULT_LIMIT;
    if (limit < 1) {
    	limit = DEFAULT_LIMIT;
    }

    // get the multiple, use the default if not set in the request
    int multiple = (multipleString != null) ? Math.max(DEFAULT_MULTIPLE, WebSecurityUtils.safeParseInt(multipleString)) : DEFAULT_MULTIPLE;

    //calculate the start and end numbers of the results that we are showing
    long startResult = (multiple==0) ? 1 : multiple*limit+1;
    long endResult = startResult + itemCount - 1;

    Integer limitList[] = { 10, 25, 50, 100, 250, 500, 1000, 2000 };
%>

 <% if ( itemCount > 0 && limit > 0 ) { %>
  <div class="text-center">
  <strong>Results <%=startResult%>-<%=endResult%></strong>
  </div>
 <% } else { %>
  <div class="text-center">
  <strong>All Results</strong>
  </div>
 <% } %>

<c:url var="baseUrl" value="<%=baseUrl%>"></c:url>
<c:url var="firstUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="0"/>
</c:url>
<c:url var="previousUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="<%=multiple==0?"0":Integer.toString(multiple-1)%>"/>
</c:url>
<c:url var="nextUrl" value="<%=baseUrl%>">
  <c:param name="<%=limitName%>" value="<%=Integer.toString(limit)%>"/>
  <c:param name="<%=multipleName%>" value="<%=Integer.toString(multiple+1)%>"/>
</c:url>

  <nav>
  <ul class="pagination pagination-sm nav navbar-nav navbar-right<%= inFooter ? " dropup" : "" %>" style="margin: -20px 0px;">
  <% if( itemCount >= limit || multiple > 0 ) { %>
    <li class="<%=multiple > 0 ? "" : "disabled"%>"><a href="${firstUrl}">First</a></li>
    <li class="<%=multiple > 0 ? "" : "disabled"%>"><a href="${previousUrl}">Previous</a></li>
    <li class="<%=itemCount >= limit ? "" : "disabled"%>"><a href="${nextUrl}">Next</a></li>
  <% } %>
    <li class="dropdown">
      <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Limit <span class="caret"></span></a>
      <ul class="dropdown-menu" role="menu">
        <% for ( int i : limitList ) { %>
        <li class="<%=i==limit ? "disabled":""%>"><a href='${baseUrl}&<%=limitName%>=<%=i%>'><%=i%></a></li>
        <% } %>
      </ul>
    </li>
  </ul>
  </nav>
  <br/>
