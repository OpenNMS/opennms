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
// 2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor
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
	import="org.opennms.web.WebSecurityUtils,
		org.opennms.web.category.*,
		org.opennms.web.element.*,
		java.util.*,
        org.springframework.util.Assert,
        org.opennms.web.MissingParameterException
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    private CategoryModel m_model;
    
    private double m_normalThreshold;
	private double m_warningThreshold;
  
    private ServiceNameComparator m_serviceComparator = new ServiceNameComparator();
    
    public Service[] getServices(Interface intf) throws java.sql.SQLException {
        Assert.notNull(intf, "intf argument cannot be null");
        
        Service[] svcs = NetworkElementFactory.getInstance(getServletContext()).getServicesOnInterface(intf.getNodeId(), intf.getIpAddress());
        
        if (svcs != null) {
            Arrays.sort(svcs, m_serviceComparator); 
        }
        
        return svcs;
    }
    
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

    //get the database node info
    Node node_db = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
    if (node_db == null) {
        //handle this WAY better, very awful
        throw new ServletException("No such node in database");
    }

    //get the child interfaces
    Interface[] intfs = NetworkElementFactory.getInstance(getServletContext()).getActiveInterfacesOnNode( nodeId );
    if (intfs == null) { 
        intfs = new Interface[0]; 
    }

    //get the node's overall service level availiability for the last 24 hrs
    double overallRtcValue = m_model.getNodeAvailability(nodeId);

    String availClass;
    String availValue;
%>

<div id="availability-box">

<h3 class="o-box">Availability</h3>
<table class="o-box">
  <tr class="CellStatus">

<%
  if (overallRtcValue < 0) {
    availClass = "Indeterminate";
    availValue = "Unmanaged";
  } else {
    availClass = CategoryUtil.getCategoryClass(m_normalThreshold, m_warningThreshold, overallRtcValue);
    availValue = CategoryUtil.formatValue(overallRtcValue) + "%";
  }
%>

    <td class="<%= availClass %> nobright">Availability (last 24 hours)</td>
    <td colspan="2" class="<%= availClass %> bright"><%= availValue %></td>

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
            <% Service[] svcs = getServices(intf); %>

            <tr class="CellStatus">
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
              <td class="<%= availClass %> nobright" rowspan="<%=svcs.length+1%>"><a href="${interfaceLink}"><%=ipAddr%></a></td>
              <td class="<%= availClass %> nobright">Overall</td>
              <td class="<%= availClass %> bright"><%= availValue %></td>
            </tr>
    
            <% for( int j=0; j < svcs.length; j++ ) { %>
              <%
                Service service = svcs[j];

                if (service.isManaged()) {
                  double svcValue = m_model.getServiceAvailability(nodeId, ipAddr, service.getServiceId());
                  availClass = CategoryUtil.getCategoryClass(m_normalThreshold, m_warningThreshold, svcValue);
                  availValue = CategoryUtil.formatValue(svcValue) + "%";
                } else {
                  availClass = "Indeterminate";
                  availValue = ElementUtil.getServiceStatusString(service);
                }
              %>
                       
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                  <c:param name="intf" value="<%=ipAddr%>"/>
                  <c:param name="service" value="<%=String.valueOf(service.getServiceId())%>"/>
                </c:url>
                <tr class="CellStatus">
                  <td class="<%= availClass %> nobright"><a href="${serviceLink}"><%=service.getServiceName()%></a></td>
                  <td class="<%= availClass %> bright"><%= availValue %></td>
                </tr>
            <% } %>
          <% } else { %>
            <%-- interface is not managed --%>
            <% if("0.0.0.0".equals(ipAddr)) {
            }
            else { %>
            <tr class="CellStatus">
              <td>
              <a href="${interfaceLink}"><%=ipAddr%></a>
              </td>
              <td class="Indeterminate" colspan="2"><%=ElementUtil.getInterfaceStatusString(intf)%></td>
            </tr>
            <% } %>
          <% } %>
        <% } %>
<% } %>
</table>   

</div>

