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
  service level availability information for the services of a given interface.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.category.*,
		org.opennms.web.element.*,
		java.util.*
	"
%>
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
        catch( org.exolab.castor.xml.MarshalException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }
        catch( org.exolab.castor.xml.ValidationException e ) {
            throw new ServletException("Could not instantiate the CategoryModel", e);
        }        
    }
%>

<%
    Interface intf = ElementUtil.getInterfaceByParams(request, getServletContext());
    int nodeId = intf.getNodeId();
    String ipAddr = intf.getIpAddress();

    //get the child services (in alphabetical order)
    Service[] services = this.getServices(intf);
    if( services == null ) { 
        services = new Service[0]; 
    }

    //get the interface's overall service level availiability for the last 24 hrs
    double overallRtcValue = this.model.getInterfaceAvailability(nodeId, ipAddr);
%>

<%
String overallStatus;
String overallStatusString;

if (overallRtcValue < 0) {
    overallStatus = "Indeterminate";
    overallStatusString = ElementUtil.getInterfaceStatusString(intf);
} else {
    if (services.length < 1) {
        overallStatus = "Indeterminate";
        overallStatusString = "Not Monitored";
    } else {
        overallStatus = CategoryUtil.getCategoryClass(this.normalThreshold, this.warningThreshold, overallRtcValue);
        overallStatusString = CategoryUtil.formatValue(overallRtcValue) + "%";
    }
}
%>

<h3>Availability</h3>
<table>
  <tr class="<%= overallStatus %>">
    <td class="divider">Overall Availability</td>
    <td class="divider bright" colspan="2"><%= overallStatusString %></td>
  </tr>

  <% for( int i=0; i < services.length; i++ ) { %>
    <% Service service = services[i]; %>
    <% double svcValue = 0; %>
    <% if( service.isManaged() ) { %>
      <% svcValue = this.model.getServiceAvailability(nodeId, ipAddr, service.getServiceId()); %>     
      <tr class="<%=CategoryUtil.getCategoryClass(this.normalThreshold, this.warningThreshold, svcValue)%>">
    <% } else { %>
      <tr class="Indeterminate">
    <% } %>
    <c:url var="serviceLink" value="element/service.jsp">
      <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
      <c:param name="intf" value="<%=ipAddr%>"/>
      <c:param name="service" value="<%=String.valueOf(service.getServiceId())%>"/>
    </c:url>
    <td class="divider"><a href="${serviceLink}"><c:out value="<%=service.getServiceName()%>"/></a></td>
    <% if( service.isManaged() ) { %>
      <td class="divider bright"><%=CategoryUtil.formatValue(svcValue)%>%</td>
    <% } else { %>
      <td class="divider bright"><%=ElementUtil.getServiceStatusString(service)%></td>
    <% } %>
    </tr>
  <% } %>
  <tr>
    <td colspan="2" style="text-align: right;">Percentage over last 24 hours</td> <%-- next iteration, read this from same properties file that sets up for RTCVCM --%>
  </tr>   
</table>   

<%!    
    /** Convenient anonymous class for sorting Service objects by service name. */
    protected Comparator serviceComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            //for brevity's sake assume they're both Services
            Service s1 = (Service)o1;
            Service s2 = (Service)o2;
            
            return s1.getServiceName().compareTo(s2.getServiceName());
        }
        
        public boolean equals(Object o1, Object o2) {
            //for brevity's sake assume they're both Services
            Service s1 = (Service)o1;
            Service s2 = (Service)o2;
            
            return s1.getServiceName().equals(s2.getServiceName());
        }        
    };

    
    public Service[] getServices(Interface intf) throws java.sql.SQLException {
        if( intf == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        Service[] svcs = NetworkElementFactory.getInstance(getServletContext()).getServicesOnInterface(intf.getNodeId(), intf.getIpAddress());
        
        if( svcs != null ) {
            Arrays.sort(svcs, this.serviceComparator); 
        }
        
        return svcs;
    }
%>
