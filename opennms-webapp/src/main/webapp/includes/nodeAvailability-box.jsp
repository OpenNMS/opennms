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
  service level availability information for the interfaces and services of
  a given node.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.category.*,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.OnmsNode,
		java.util.*,
        org.springframework.util.Assert,
        org.opennms.web.servlet.MissingParameterException,
        org.opennms.core.utils.WebSecurityUtils,org.opennms.web.outage.*,java.util.*"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    private CategoryModel m_model;
    
    private double m_normalThreshold;
	private double m_warningThreshold;
      
    public void init() throws ServletException {
        try {
            m_model = CategoryModel.getInstance();
        } catch (Throwable e) {
            throw new ServletException("Could not instantiate the CategoryModel: " + e, e);
        }
        
        m_normalThreshold = m_model.getCategoryNormalThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
        m_warningThreshold = m_model.getCategoryWarningThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
    }
%>

<%
    String nodeIdString = request.getParameter("node");

    if (nodeIdString == null) {
        throw new MissingParameterException("node");
    }

    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

    //get the node's overall service level availiability for the last 24 hrs
    double overallRtcValue = m_model.getNodeAvailability(nodeId);

    String availClass;
    String availValue;

    long timelineEnd = new Date().getTime() / 1000;
    long timelineStart = timelineEnd - 3600 * 24;

    String timelineHeaderUrl = "/opennms/rest/timeline/header/" + timelineStart + "/" + timelineEnd + "/";
    String timelineEmptyUrl = "/opennms/rest/timeline/empty/" + timelineStart + "/" + timelineEnd + "/" ;

    Outage[] outages = OutageModel.getCurrentOutagesForNode(nodeId);
%>

<div id="availability-box" class="card">
  <div class="card-header">
    <span>Availability</span>
  </div>
  <table class="table table-sm severity">
    <tr>
<%
  if (overallRtcValue < 0) {
    availClass = "Indeterminate";
    availValue = "Unmanaged";
  } else {
    availClass = CategoryUtil.getCategoryClass(m_normalThreshold, m_warningThreshold, overallRtcValue);
    availValue = CategoryUtil.formatValue(overallRtcValue) + "%";
  }
%>
    <td class="severity-<%= availClass %> nobright" colspan="3">Availability (last 24 hours)</td>
    <td colspan="1" class="severity-<%= availClass %> nobright"><%= availValue %></td>

  </tr>

    <%  if (overallRtcValue >= 0) { %>
       <% Interface[] availIntfs = NetworkElementFactory.getInstance(getServletContext()).getActiveInterfacesOnNode(nodeId); %>
       <% boolean oversized = availIntfs.length > 10; %>
           
        <% for( int i=0; i < availIntfs.length; i++ ) { %>
          <% Interface intf = availIntfs[i]; %>
          <% if (oversized && ! "P".equals(intf.getIsSnmpPrimary())) { continue; } %>
          <% String ipAddr = intf.getIpAddress(); %>
          
          <c:url var="interfaceLink" value="element/interface.jsp">
            <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
            <c:param name="intf" value="<%=ipAddr%>"/>
          </c:url>
          <% if( intf.isManaged() ) { %>
            <%-- interface is managed --%>
            <% double intfValue = m_model.getInterfaceAvailability(nodeId, ipAddr); %>                              
            <% Service[] svcs = ElementUtil.getServicesOnInterface(nodeId,ipAddr,getServletContext()); %>

            <tr>
              <%
                if (svcs.length < 1) {
                    availClass = "Indeterminate";
                    availValue = "Not Monitored";
                } else if (!ElementUtil.hasLocallyMonitoredServices(svcs)) {
                    availClass = "Indeterminate";
                    availValue = "Remotely Monitored";
                } else {
                  availClass = CategoryUtil.getCategoryClass(m_normalThreshold, m_warningThreshold, intfValue);
                  availValue = CategoryUtil.formatValue(intfValue) + "%";
                }
              %>
              <td class="severity-Cleared nobright interface address" colspan="2"><a href="<c:out value="${interfaceLink}"/>"><%=ipAddr%></a></td>
              <%
                  if ("Not Monitored".equals(availValue)) {
              %>
                <td class="severity-Cleared nobright interface header empty"><img src="#" data-imgsrc="<%=timelineEmptyUrl%>"></td>
              <%
                  } else {
              %>
                <td class="severity-Cleared nobright interface header monitored"><img src="#" data-imgsrc="<%=timelineHeaderUrl%>"></td>
              <%
                  }
              %>
              <td class="severity-<%= availClass %> nobright interface percent"><%= availValue %></td>
            </tr>
    
            <% for( int j=0; j < svcs.length; j++ ) { %>
              <%
                Service service = svcs[j];

                String warnClass = "Indeterminate";

                if (service.isManaged()) {
                  double svcValue = CategoryModel.getServiceAvailability(nodeId, ipAddr, service.getServiceId());
                  availClass = CategoryUtil.getCategoryClass(m_normalThreshold, m_warningThreshold, svcValue);
                  availValue = CategoryUtil.formatValue(svcValue) + "%";

                  warnClass = "Normal";

                  for(int o=0;o<outages.length;o++) {
                    if (outages[o].getIpAddress().equals(ipAddr) && outages[o].getServiceName().equals(service.getServiceName())) {
                      warnClass = "Critical";
                      break;
                    }
                  }

                } else {
                  availClass = "Indeterminate";
                  availValue = ElementUtil.getServiceStatusString(service);
                }

                String timelineUrl = "/opennms/rest/timeline/html/" + String.valueOf(nodeId) + "/" + java.net.URLEncoder.encode(ipAddr, "UTF-8") + "/" + service.getServiceId() + "/" + timelineStart + "/" + timelineEnd + "/";
              %>
                       
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                  <c:param name="intf" value="<%=ipAddr%>"/>
                  <c:param name="service" value="<%=String.valueOf(service.getServiceId())%>"/>
                </c:url>
                <tr>
                    <%
                        if (j==0) {
                    %>
                    <td class="severity-Cleared nobright spacer" rowspan="<%=svcs.length%>"></td>
                    <%
                        }
                    %>
                  <td class="severity-<%= warnClass %> bright service name"><a href="<c:out value="${serviceLink}"/>"><%=service.getServiceName()%></a></td>
                  <td class="severity-Cleared nobright service timeline">
                    <%
                         if (service.isManaged()) {
                    %>
                    <span data-src="<%=timelineUrl%>"></span>
                    <%
                        } else {
                    %>
                    <img src="#" data-imgsrc="<%=timelineEmptyUrl%>">
                    <%
                        }
                    %>
                  </td>
                  <td class="severity-<%= availClass %> nobright service percent"><%= availValue %></td>
                </tr>
            <% } %>
          <% } else { %>
      <%-- interface is not managed --%>
      <% if("0.0.0.0".equals(ipAddr)) {
      }
      else { %>
      <tr>
          <td class="severity-Cleared nobright" colspan=2>
              <a href="<c:out value="${interfaceLink}"/>"><%=ipAddr%></a>
          </td>
          <!--<td class="severity-Cleared nobright"></td>-->
          <td class="severity-Cleared nobright"><img src="#" data-imgsrc="<%=timelineEmptyUrl%>"></td>
          <td class="severity-Indeterminate" colspan="2"><%=ElementUtil.getInterfaceStatusString(intf)%></td>
      </tr>
      <% } %>
      <% } %>
      <% } %>
      <% } %>
  </table>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="timeline-resize" />
</jsp:include>
