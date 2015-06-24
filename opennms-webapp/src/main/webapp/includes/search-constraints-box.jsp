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
  <input type="hidden" name="outtype"/>
</form>

<div class="btn-group">
  <button 
    type="button" 
    class="btn btn-default <%=(parms.outageType == OutageType.CURRENT) ? "active" : ""%>" 
    onclick="document.outage_search_constraints_box_outtype_form.outtype.value = '<%=OutageType.CURRENT.getShortName() %>'; document.outage_search_constraints_box_outtype_form.submit();"
  >
    Current
  </button>
  <button 
    type="button" 
    class="btn btn-default <%=(parms.outageType == OutageType.RESOLVED) ? "active" : ""%>"
    onclick="document.outage_search_constraints_box_outtype_form.outtype.value = '<%=OutageType.RESOLVED.getShortName() %>'; document.outage_search_constraints_box_outtype_form.submit();"
  >
    Resolved
  </button>
  <button 
    type="button" 
    class="btn btn-default <%=(parms.outageType == OutageType.BOTH) ? "active" : ""%>"
    onclick="document.outage_search_constraints_box_outtype_form.outtype.value = '<%=OutageType.BOTH.getShortName() %>'; document.outage_search_constraints_box_outtype_form.submit();"
  >
    Both Current &amp; Resolved
  </button>
</div>

<br/>

<% if( length > 0 ) { %>
  <br/>
  <strong>Search constraints: 
      <% for(int i=0; i < length; i++) { %>
        <% Filter filter = (Filter)parms.filters.get(i); %> 
        &nbsp; <span class="label label-default"><%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%></span> <a href="<%=OutageUtil.makeLink(req, parms, filter, false)%>"> <i class="fa fa-minus-square-o"></i></a>
      <% } %>
  </strong>
  <br/>
<% } %>
