<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 14: Sanitize input
// 2006 Aug 17: fix some HTML. - dj@opennms.org
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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.Util,
		org.opennms.web.outage.*,
		org.opennms.web.outage.filter.Filter,
		org.opennms.web.XssRequestWrapper,
		org.opennms.web.WebSecurityUtils
	"
%>

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
<form action="outage/list.htm" method="GET" name="outage_search_constraints_box_outtype_form">
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

