<!--

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
//      http://www.blast.com/
//

-->

<%-- 
  This page is included by other JSPs to create a table containing
  the service level availability for a particular service.  
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.category.*,org.opennms.web.element.*" %>

<%!
    protected CategoryModel model;
    
    protected double normalThreshold;
    protected double warningThreshold;
    

    public void init() throws ServletException {
        try {
            this.model = CategoryModel.getInstance();
            
            this.normalThreshold  = this.model.getCategoryNormalThreshold(CategoryModel.OVERALL_AVAILABILITY_CATEGORY);
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
    //required parameter node
    String nodeIdString = request.getParameter("node");
    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException("node", new String[] {"node", "intf", "service"});
    }
    
    //required parameter intf
    String ipAddr = request.getParameter("intf");
    if( ipAddr == null ) {
        throw new org.opennms.web.MissingParameterException("intf", new String[] {"node", "intf", "service"});
    }
    
    //required parameter service
    String serviceIdString = request.getParameter("service");
    if( serviceIdString == null ) {
        throw new org.opennms.web.MissingParameterException("service", new String[] {"node", "intf", "service"});
    }
    
    int nodeId = Integer.parseInt(nodeIdString);
    int serviceId = Integer.parseInt(serviceIdString);

    //get the service's database info
    Service service = NetworkElementFactory.getService(nodeId, ipAddr, serviceId);
    
    //find the availability value for this node
    double rtcValue = this.model.getServiceAvailability(nodeId, ipAddr, serviceId);
%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  <tr bgcolor="#999999">
    <td width="30%"><b>Overall&nbsp;Availability</b></td>
    <% if( service.isManaged() ) { %>
      <td bgcolor="<%=CategoryUtil.getCategoryColor(this.normalThreshold, this.warningThreshold, rtcValue)%>" align="right"><b><%=CategoryUtil.formatValue(rtcValue)%>%</b></td>    
    <% } else { %>
      <td bgcolor="#cccccc" align="right"><b><%=ElementUtil.getServiceStatusString(service)%></b></td>
    <% } %>
  </tr>
</table>
