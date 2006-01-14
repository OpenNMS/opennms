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
	import="java.util.*,
		org.opennms.web.*,
		org.opennms.web.performance.*
	"
%>

<%!
    public PerformanceModel model = null;
  
    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel(ServletInitializer.getHomeDir());
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the PerformanceModel", t);
        }
    }
%>
<%
    String nodeIdString = request.getParameter("node");    
    if(nodeIdString == null) {
        throw new MissingParameterException("node");
    }

    int nodeId = Integer.parseInt(nodeIdString);
    
    TreeMap intfMap = new TreeMap();  
    String[] intfs = this.model.getQueryableInterfacesForNode(nodeId);
  
    // Add the readable name and the file path to the Map
    for(int i=0; i < intfs.length; i++) {
        intfMap.put(this.model.getHumanReadableNameForIfLabel(nodeId, intfs[i]), intfs[i]);
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Performance Reporting" />
  <jsp:param name="headTitle" value="Custom" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='performance/index.jsp'>Performance</a>" />
  <jsp:param name="breadcrumb" value="Custom" />
</jsp:include>

<form method="get" action="performance/adhoc2.jsp">
  <%=Util.makeHiddenTags(request)%>
  
  <h3>Step 1: Choose the Interface to Query</h3>

  <select name="intf" size="10">
    <option value="">Node-level Performance Data</option>              
    <% Iterator iterator = intfMap.keySet().iterator(); %>
    <% while(iterator.hasNext()) { %>
      <% String key = (String)iterator.next(); %>
      <option value="<%=intfMap.get(key)%>"><%=key%></option>
    <% } %>
  </select>

  <br/>
  <br/>

  <input type="submit" value="Next"/>
  <input type="reset" />
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />
