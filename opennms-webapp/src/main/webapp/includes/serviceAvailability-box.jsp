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
		org.exolab.castor.xml.MarshalException,
		org.exolab.castor.xml.ValidationException,
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
        } catch (MarshalException e) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        } catch (ValidationException e) {
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
            m_model.getServiceAvailability(service.getNodeId(),
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
    int timelineWidth = 250;
    String emptyUrl = "/opennms/rest/timeline/empty/" + timelineStart + "/" + timelineEnd + "/" + timelineWidth;

    int nodeId = service.getNodeId();
    String ipAddr = service.getIpAddress();

    Outage[] outages = new OutageModel().getCurrentOutagesForNode(nodeId);

    String warnClass = "Normal";

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

    String timelineUrl = "/opennms/rest/timeline/html/" + String.valueOf(nodeId) + "/" + ipAddr + "/" + service.getServiceName() + "/" + timelineStart + "/" + timelineEnd + "/" + timelineWidth;
%>

<h3>Overall Availability</h3>
<table>
  <tr class="CellStatus">
    <td class="Cleared nobright" colspan="2"><%=ipAddr%></td>
    <td class="Cleared nobright"><img src="/opennms/rest/timeline/header/<%=timelineStart%>/<%=timelineEnd%>/<%=timelineWidth%>"></td>
    <td class="<%=overallStatus%> nobright"><%=overallStatusString%></td>
  </tr>
  <tr class="CellStatus"/>
    <td class="Cleared nobright"></td>
    <td class="<%=warnClass%> bright"><%=service.getServiceName()%></td>
    <%
        if (service.isManaged()) {
    %>
    <td class="Cleared nobright"><script src="<%=timelineUrl%>"></script></td>
    <%
        } else {
    %>
    <td class="Cleared nobright"><img src="<%=emptyUrl%>"></td>
    <%
        }
    %>
    <td class="<%= styleClass %> nobright"><%= statusContent %></td>
  </tr>
</table>
