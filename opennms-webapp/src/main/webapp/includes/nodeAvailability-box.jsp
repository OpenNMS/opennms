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

    Outage[] outages = new OutageModel().getCurrentOutagesForNode(nodeId);
%>

<div id="availability-box" class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Availability</h3>
  </div>
  <table class="table table-condensed severity">
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
           
        <% for( int i=0; i < availIntfs.length; i++ ) { %>
          <% Interface intf = availIntfs[i]; %>
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
              <td class="severity-Cleared nobright" colspan="2"><a href="<c:out value="${interfaceLink}"/>"><%=ipAddr%></a></td>
              <%
                  if ("Not Monitored".equals(availValue)) {
              %>
                <td class="severity-Cleared nobright"><img src="#" data-imgsrc="<%=timelineEmptyUrl%>"></td>
              <%
                  } else {
              %>
                <td class="severity-Cleared nobright"><img src="#" data-imgsrc="<%=timelineHeaderUrl%>"></td>
              <%
                  }
              %>
              <td class="severity-<%= availClass %> nobright"><%= availValue %></td>
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

                String timelineUrl = "/opennms/rest/timeline/html/" + String.valueOf(nodeId) + "/" + ipAddr + "/" + service.getServiceName() + "/" + timelineStart + "/" + timelineEnd + "/";
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
                    <td class="severity-Cleared nobright" rowspan="<%=svcs.length%>"></td>
                    <%
                        }
                    %>
                  <td class="severity-<%= warnClass %> bright"><a href="<c:out value="${serviceLink}"/>"><%=service.getServiceName()%></a></td>
                  <td class="severity-Cleared nobright">
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
                  <td class="severity-<%= availClass %> nobright"><%= availValue %></td>
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

<script type="text/javascript" src="js/timeline-resize.js"></script>
