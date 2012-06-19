<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"	contentType="text/html"	session="true" %>

<%@page import="org.opennms.web.api.Util"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@page import="org.opennms.web.servlet.XssRequestWrapper"%>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.outage.OutageQueryParms"%>
<%@page import="org.opennms.web.outage.OutageType"%>
<%@page import="org.opennms.web.outage.OutageUtil"%>

<%
	XssRequestWrapper req = new XssRequestWrapper(request);

    //required attribute parms
    OutageQueryParms parms = (OutageQueryParms)req.getAttribute( "parms" );

    if( parms == null ) {
        throw new ServletException( "Missing the outage parms request attribute." );
    }

    int length = parms.filters.size();    
%>

<!-- acknowledged/outstanding row -->

<form action="outage/list.htm" method="get" name="outage_search_constraints_box_outtype_form">
  <%=Util.makeHiddenTags(req, new String[] {"outtype"})%>
    
  <p>
    Outage type:
    <select name="outtype" size="1" onChange="javascript: document.outage_search_constraints_box_outtype_form.submit()">
      <option value="<%=OutageType.CURRENT.getShortName() %>" <%=(parms.outageType == OutageType.CURRENT) ? "selected=\"1\"" : ""%>>
        Current
      </option>
      
      <option value="<%=OutageType.RESOLVED.getShortName()%>" <%=(parms.outageType == OutageType.RESOLVED) ? "selected=\"1\"" : ""%>>
        Resolved
      </option>
      
      <option value="<%=OutageType.BOTH.getShortName()%>" <%=(parms.outageType == OutageType.BOTH) ? "selected=\"1\"" : ""%>>
        Both Current &amp; Resolved
      </option>
    </select>        
  </p> 
</form>    

<% if( length > 0 ) { %>
  <p>Search constraints: 
      <% for(int i=0; i < length; i++) { %>
        <% Filter filter = (Filter)parms.filters.get(i); %> 
        &nbsp; <span class="filter"><%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%> <a href="<%=OutageUtil.makeLink(req, parms, filter, false)%>">[-]</a></span>
      <% } %>   
  </p>    
<% } %>  

