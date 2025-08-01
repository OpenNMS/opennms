<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

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
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>


<%!
    public CategoryModel model = null;
    
    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();
        }
        catch( java.io.IOException e ) {
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


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle(category.getName())
          .headTitle("Category")
          .headTitle("SLM")
          .breadcrumb("SLM", "rtc/index.jsp")
          .breadcrumb(category.getName())
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
    class="btn btn-secondary <%=(showoutages.equals("all") ? "active" : "")%>"
    onclick="top.location = '<%= Util.calculateUrlBase( req , "rtc/category.jsp?category=" + Util.encode(category.getName()) + "&amp;showoutages=all") %>'"
  >
    All
  </button>
  <button 
    type="button" 
    class="btn btn-secondary <%=(showoutages.equals("outages") ? "active" : "")%>"
    onclick="top.location = '<%= Util.calculateUrlBase( req , "rtc/category.jsp?category=" + Util.encode(category.getName()) + "&amp;showoutages=outages") %>'"
  >
    With outages
  </button>
  <button 
    type="button" 
    class="btn btn-secondary <%=(showoutages.equals("avail") ? "active" : "")%>"
    onclick="top.location = '<%= Util.calculateUrlBase( req , "rtc/category.jsp?category=" + Util.encode(category.getName()) + "&amp;showoutages=avail") %>'"
  >
    With availability less than 100%
  </button>
</div>

<br/><br/>

    <div class="card fix-subpixel">
      <table class="table table-sm severity">
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

  <p>Last updated: <onms:datetime date="<%=category.getLastUpdated()%>"/></p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
