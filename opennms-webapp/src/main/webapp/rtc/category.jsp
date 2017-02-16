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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		org.opennms.web.category.*,
		org.opennms.web.api.Util,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.servlet.MissingParameterException,
		java.util.*,
		org.opennms.netmgt.xml.rtc.Node,
		org.opennms.web.servlet.XssRequestWrapper,
		org.opennms.web.springframework.security.AclUtils,
		org.springframework.security.core.context.SecurityContextHolder
		"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<%!
    public CategoryModel model = null;
    
    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();
        }
        catch( java.io.IOException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( org.exolab.castor.xml.MarshalException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( org.exolab.castor.xml.ValidationException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }        

    }
%>

<%

   HttpServletRequest req = new XssRequestWrapper(request);
   String categoryName = req.getParameter("category");

    if (categoryName == null) {
        throw new MissingParameterException("category");
    }

    Category category = this.model.getCategory(categoryName);

    if (category == null) {
        throw new CategoryNotFoundException(categoryName);
    }
    
    AclUtils.NodeAccessChecker accessChecker = AclUtils.getNodeAccessChecker(getServletContext());

    // put the nodes in a tree map to sort by name
    TreeMap<String,Node> nodeMap = new TreeMap<String,Node>();
    for (Node node : category.getNode()) {
        int nodeId = (int)(node.getNodeid());
        String nodeLabel =
		NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId);
        // nodeMap.put( nodeLabel, node );

        if (accessChecker.isNodeAccessible(nodeId)) {
            if (nodeLabel != null && !nodeMap.containsKey(nodeLabel)) {
                nodeMap.put(nodeLabel, node);
            } else if (nodeLabel != null) {
                nodeMap.put(nodeLabel+" (nodeid="+node.getNodeid()+")", node);
            } else {
                nodeMap.put("nodeId=" + node.getNodeid(), node);
            }
        }
    }
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Category Service Level Monitoring" />
  <jsp:param name="headTitle" value="<%=category.getName()%>" />
  <jsp:param name="headTitle" value="Category" />
  <jsp:param name="headTitle" value="SLM" />
  <jsp:param name="breadcrumb" value="<a href='rtc/index.jsp'>SLM</a>" />
  <jsp:param name="breadcrumb" value="<%=category.getName()%>"/>
</jsp:include>

      <% if( category.getComment() != null ) { %>      
        <p><c:out value="<%=category.getComment()%>"/></p>
      <% } %>
      <% if( AclUtils.shouldFilter(SecurityContextHolder.getContext().getAuthentication().getAuthorities()) ) { %>
        <p style="color: red"> This list has been filtered to accessible nodes only based on your user group. </p>
      <% } %>

    <% String showoutages = req.getParameter("showoutages"); %>

        <%  
        if(showoutages == null ) {
           showoutages = "avail";
        } %>

<div class="btn-group">
  <button 
    type="button" 
    class="btn btn-default <%=(showoutages.equals("all") ? "active" : "")%>" 
    onclick="top.location = '<%= Util.calculateUrlBase( req , "rtc/category.jsp?category=" + Util.encode(category.getName()) + "&amp;showoutages=all") %>'"
  >
    All
  </button>
  <button 
    type="button" 
    class="btn btn-default <%=(showoutages.equals("outages") ? "active" : "")%>"
    onclick="top.location = '<%= Util.calculateUrlBase( req , "rtc/category.jsp?category=" + Util.encode(category.getName()) + "&amp;showoutages=outages") %>'"
  >
    With outages
  </button>
  <button 
    type="button" 
    class="btn btn-default <%=(showoutages.equals("avail") ? "active" : "")%>"
    onclick="top.location = '<%= Util.calculateUrlBase( req , "rtc/category.jsp?category=" + Util.encode(category.getName()) + "&amp;showoutages=avail") %>'"
  >
    With availability less than 100%
  </button>
</div>

<br/><br/>

    <div class="panel panel-default fix-subpixel">
      <table class="table table-condensed severity">
        <thead>
        <tr>
          <th>Nodes</th>
          <th>Outages</th>
          <th>24hr Availability</th>
        </tr>
        </thead>
      
        <%  
	    int valuecnt = 0;
	    int outagecnt = 0;
        
        if (nodeMap.size() > 0) {
            for (String nodeLabel : nodeMap.keySet()) {
                Node node = nodeMap.get(nodeLabel);
                
                double value = node.getNodevalue();
        
                if( value >= 0 ) {
                    long serviceCount = node.getNodesvccount();        
                    long serviceDownCount = node.getNodesvcdowncount();
                    double servicePercentage = 100.0;
                
                    if( serviceCount > 0 ) {
                       servicePercentage = ((double)(serviceCount-serviceDownCount))/(double)serviceCount*100.0;
                    }
                
                    String availClass = CategoryUtil.getCategoryClass( category, value );
                    String outageClass = CategoryUtil.getCategoryClass( category, servicePercentage );

                    if ( showoutages.equals("all") || (showoutages.equals("outages") && serviceDownCount > 0 ) || (showoutages.equals("avail") && value < 100 ) ) { %>
                    <tr>
                      <td><a href="element/node.jsp?node=<%=node.getNodeid()%>"><c:out value="<%=nodeLabel%>"/></a></td>
                      <td class="bright severity-<%=outageClass%>" align="right"><%=serviceDownCount%> of <%=serviceCount%></td>
                      <td class="bright severity-<%=availClass%>" align="right" width="30%"><b><%=CategoryUtil.formatValue(value)%>%</b></td>
                    </tr>
                    <%  }
                    if (value < 100 ) ++valuecnt;
                    if (serviceDownCount > 0 ) ++outagecnt;
                }
            }
            if (showoutages.equals("outages") && outagecnt == 0) { %>
			<tr>
				<td colspan="3">There are currently no outages in this Category.</td>
			</tr>
			<% } %>

			<% if (showoutages.equals("avail") && valuecnt == 0) { %>
			<tr>
				<td colspan="3">All services in this Category are at 100%.</td>
			</tr>
			<% } %>
		<% } else { %>
			<tr>
				<td colspan="3">There are no nodes in this Category.</td>
			</tr>
		<% } %>

    </table>
  </div>

  <p>Last updated: <c:out value="<%=category.getLastUpdated().toString()%>"/></p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
