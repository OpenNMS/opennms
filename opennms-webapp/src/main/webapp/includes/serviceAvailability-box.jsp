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
  This page is included by other JSPs to create a table containing
  the service level availability for a particular service.  
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		java.io.IOException,
		org.opennms.web.category.*,
		org.opennms.web.element.*,
		java.util.Date
	"
%>
<%@ page import="org.opennms.netmgt.model.OnmsMonitoredService" %>
<%@ page import="org.opennms.web.outage.Outage" %>
<%@ page import="org.opennms.web.outage.OutageModel" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    private CategoryModel m_model;
    
    private double m_normalThreshold;
    private double m_warningThreshold;
    

    public void init() throws ServletException {
        try {
            m_model = CategoryModel.getInstance();
            
            m_normalThreshold  = m_model.getCategoryNormalThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
            m_warningThreshold = m_model.getCategoryWarningThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);            
        } catch (IOException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
    }
%>

<%
    Service service = ElementUtil.getServiceByParams(request, getServletContext());
    
    String styleClass;
    String statusContent;

    if (service.isManaged()) {
        //find the availability value for this node
        double rtcValue =
            CategoryModel.getServiceAvailability(service.getNodeId(),
	                                       service.getIpAddress(),
                                           service.getServiceId());
        
        styleClass = CategoryUtil.getCategoryClass(m_normalThreshold,
                                                   m_warningThreshold,
                                                   rtcValue);
    	statusContent = CategoryUtil.formatValue(rtcValue) + "%";
    } else {
        styleClass = "Indeterminate";
		statusContent = ElementUtil.getServiceStatusString(service);
    }

    long timelineEnd = new Date().getTime() / 1000;
    long timelineStart = timelineEnd - 3600 * 24;
    String timelineHeaderUrl = "/opennms/rest/timeline/header/" + timelineStart + "/" + timelineEnd + "/";
    String timelineEmptyUrl = "/opennms/rest/timeline/empty/" + timelineStart + "/" + timelineEnd + "/";

    int nodeId = service.getNodeId();
    String ipAddr = service.getIpAddress();

    Outage[] outages = OutageModel.getCurrentOutagesForNode(nodeId);

    String warnClass = service.isManaged() ? "Normal" : "Indeterminate";

    for(int o=0;o<outages.length;o++) {
        if (outages[o].getIpAddress().equals(ipAddr) && outages[o].getServiceName().equals(service.getServiceName())) {
            warnClass = "Critical";
            break;
        }
    }

    String overallStatusString = request.getParameter("interfaceStatus");
    String overallStatus = "Indeterminate";

    double overallRtcValue = this.m_model.getInterfaceAvailability(nodeId, ipAddr);

    int serviceCount = ElementUtil.getServicesOnInterface(nodeId, ipAddr,getServletContext()).length;

    if (serviceCount < 1) {
        overallStatusString = "Not Monitored";
    } else {
        overallStatus = CategoryUtil.getCategoryClass(this.m_normalThreshold, this.m_warningThreshold, overallRtcValue);
        overallStatusString = CategoryUtil.formatValue(overallRtcValue) + "%";
    }

    String timelineUrl = "/opennms/rest/timeline/html/" + String.valueOf(nodeId) + "/" + java.net.URLEncoder.encode(ipAddr, "UTF-8") + "/" + service.getServiceId() + "/" + timelineStart + "/" + timelineEnd + "/";
%>

<div id="availability-box" class="card">
<div class="card-header">
    <span>Overall Availability</span>
</div>
<table class="table table-sm severity">
  <tr class="CellStatus">
    <td class="severity-Cleared nobright interface address" colspan="2"><%=ipAddr%></td>
    <td class="severity-Cleared nobright interface header"><img src="#" data-imgsrc="<%=timelineHeaderUrl%>"></td>
    <td class="severity-<%=overallStatus%> nobright interface percent"><%=overallStatusString%></td>
  </tr>
  <tr class="CellStatus"/>
    <td class="severity-Cleared nobright spacer"></td>
    <td class="severity-<%=warnClass%> bright service name"><%=service.getServiceName()%></td>
    <%
        if (service.isManaged()) {
    %>
    <td class="severity-Cleared nobright service timeline"><span data-src="<%=timelineUrl%>"></span></td>
    <%
        } else {
    %>
    <td class="severity-Cleared nobright"><img src="#" data-imgsrc="<%=timelineEmptyUrl%>"></td>
    <%
        }
    %>
    <td class="severity-<%= styleClass %> nobright service percent"><%= statusContent %></td>
  </tr>
</table>
</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="timeline-resize" />
</jsp:include>
