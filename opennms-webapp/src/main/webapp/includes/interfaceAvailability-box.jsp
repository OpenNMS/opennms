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
<%-- 
  This page is included by other JSPs to create a box containing a tree of 
  service level availability information for the services of a given interface.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.category.*,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.*,
		java.util.Date
	"
%>
<%@ page import="org.opennms.web.outage.Outage" %>
<%@ page import="org.opennms.web.outage.OutageModel" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    protected CategoryModel model;
    
    protected double normalThreshold;
    protected double warningThreshold; 
    
    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();
            
            this.normalThreshold = this.model.getCategoryNormalThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
            this.warningThreshold = this.model.getCategoryWarningThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
        }
        catch( java.io.IOException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
    }
%>

<%
    String requestNode = request.getParameter("node");
    String ipAddr = request.getParameter("ipAddr");
    String overallStatusString = request.getParameter("interfaceStatus");
	String overallStatus = "Indeterminate";

	int nodeId = -1;
	
	if ( requestNode != null ) {
		nodeId = WebSecurityUtils.safeParseInt(requestNode);
	}

    //get the child services (in alphabetical order)
    Service[] services = ElementUtil.getServicesOnInterface(nodeId, ipAddr,getServletContext());

    //get the interface's overall service level availiability for the last 24 hrs
    double overallRtcValue = this.model.getInterfaceAvailability(nodeId, ipAddr);

    if (services.length < 1) {
    	overallStatusString = "Not Monitored";
 	} else {
     	overallStatus = CategoryUtil.getCategoryClass(this.normalThreshold, this.warningThreshold, overallRtcValue);
     	overallStatusString = CategoryUtil.formatValue(overallRtcValue) + "%";
 	}

    long timelineEnd = new Date().getTime() / 1000;
    long timelineStart = timelineEnd - 3600 * 24;
    String timelineHeaderUrl = "/opennms/rest/timeline/header/" + timelineStart + "/" + timelineEnd + "/";
    String timelineEmptyUrl = "/opennms/rest/timeline/empty/" + timelineStart + "/" + timelineEnd + "/";

    Outage[] outages = OutageModel.getCurrentOutagesForNode(nodeId);
%>

<div id="availability-box" class="card">
<div class="card-header">
<span>Availability</span>
</div>
<table class="table table-sm severity">
  <tr class="CellStatus">
    <td class="severity-Cleared nobright interface address" colspan="2"><%=ipAddr%></td>
    <td class="severity-Cleared nobright interface header"><img src="#" data-imgsrc="<%=timelineHeaderUrl%>"></td>
    <td class="severity-<%= overallStatus %> nobright interface percent"><%= overallStatusString %></td>
  </tr>

  <% for( int i=0; i < services.length; i++ ) { %>
    <tr class="CellStatus">

    <% Service service = services[i]; %>
    <%
        if (i==0) {
    %>
    <td class="severity-Cleared nobright spacer" rowspan="<%=services.length%>"></td>
    <%
        }
        double svcValue = 0;

        String warnClass = service.isManaged() ? "Normal" : "Indeterminate";

        for(int o=0;o<outages.length;o++) {
            if (outages[o].getIpAddress().equals(ipAddr) && outages[o].getServiceName().equals(service.getServiceName())) {
                warnClass = "Critical";
                break;
            }
        }

        String timelineUrl = "/opennms/rest/timeline/html/" + String.valueOf(nodeId) + "/" + java.net.URLEncoder.encode(ipAddr, "UTF-8") + "/" + service.getServiceId() + "/" + timelineStart + "/" + timelineEnd + "/";
    %>
    <%
      String serviceClass;

      if( service.isManaged() ) {
        svcValue = CategoryModel.getServiceAvailability(nodeId, ipAddr, service.getServiceId());
        serviceClass = CategoryUtil.getCategoryClass(this.normalThreshold, this.warningThreshold, svcValue);
      } else {
        serviceClass = "Indeterminate";
      }
    %>
    <c:url var="serviceLink" value="element/service.jsp">
      <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
      <c:param name="intf" value="<%=ipAddr%>"/>
      <c:param name="service" value="<%=String.valueOf(service.getServiceId())%>"/>
    </c:url>
    <td class="severity-<%=warnClass%> bright service name"><a href="<c:out value="${serviceLink}"/>"><c:out value="<%=service.getServiceName()%>"/></a></td>
    <% if( service.isManaged() ) { %>
      <td class="severity-Cleared nobright service timeline">
        <span data-src="<%=timelineUrl%>"></span>
      </td>
      <td class="severity-<%=serviceClass%> nobright"><%=CategoryUtil.formatValue(svcValue)%>%</td>
    <% } else { %>
      <td class="severity-Cleared nobright"><img src="#" data-imgsrc="<%=timelineEmptyUrl%>"></td>
      <td class="severity-<%=serviceClass%> nobright service percent"><%=ElementUtil.getServiceStatusString(service)%></td>
    <% } %>
    </tr>
  <% } %>
</table>
</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="timeline-resize" />
</jsp:include>