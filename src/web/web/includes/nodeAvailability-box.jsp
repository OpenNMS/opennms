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
//      http://www.opennms.com/
//

-->

<%-- 
  This page is included by other JSPs to create a box containing a tree of 
  service level availability information for the interfaces and services of
  a given node.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.category.*,org.opennms.web.element.*,java.util.*" %>


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
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = Integer.parseInt( nodeIdString );

    //get the database node info
    Node node_db = NetworkElementFactory.getNode( nodeId );
    if( node_db == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }

    //get the child interfaces
    Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode( nodeId );
    if( intfs == null ) { 
        intfs = new Interface[0]; 
    }

    //get the node's overall service level availiability for the last 24 hrs
    double overallRtcValue = this.model.getNodeAvailability(nodeId);
%>

<table width="100%" cellspacing="0" cellpadding="2" border="1" bordercolor="black" bgcolor="#cccccc">
  <tr bgcolor="#999999">
    <td><b>Overall Availability</b></td>

<% if( overallRtcValue < 0 ) { %>
      <td width="30%" bgcolor="#cccccc" align="right"><b>Unmanaged</b></td>
<% } else { %>
      <td width="30%" bgcolor="<%=CategoryUtil.getCategoryColor(this.normalThreshold, this.warningThreshold, overallRtcValue)%>" align="right"><b><%=CategoryUtil.formatValue(overallRtcValue)%>%</b></td>
  </tr>

  <tr>
    <td colspan="2">
      <table width="100%" cellspacing="0" cellpadding="2" border="1">
        <% Interface[] availIntfs = this.getInterfaces(nodeId); %>
           
        <% for( int i=0; i < availIntfs.length; i++ ) { %>
          <% Interface intf = availIntfs[i]; %>
          <% String ipAddr = intf.getIpAddress(); %>
               
          <% if( intf.isManaged() ) { %>
            <%-- interface is managed --%>
            <% double intfValue = this.model.getInterfaceAvailability(nodeId, ipAddr); %>                              
            <% Service[] svcs = this.getServices(intf); %>
    
            <tr>
              <td align="left"  width="25%" rowspan="<%=svcs.length+1%>" valign="top"><a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>"><%=ipAddr%></a></td>
              <td align="right" width="75%" colspan="2" bgcolor="<%=CategoryUtil.getCategoryColor(this.normalThreshold, this.warningThreshold, intfValue)%>"><b><%=CategoryUtil.formatValue(intfValue)%>%</b></td>
            </tr>
    
            <% for( int j=0; j < svcs.length; j++ ) { %>
              <% Service service = svcs[j]; %>
                       
              <% if( service.isManaged() ) { %>
                <% double svcValue = this.model.getServiceAvailability(nodeId, ipAddr, service.getServiceId()); %>
                <tr>
                  <td align="left"  width="25%"><a href="element/service.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>&service=<%=service.getServiceId()%>"><%=service.getServiceName()%></a></td>
                  <td align="right" width="50%" bgcolor="<%=CategoryUtil.getCategoryColor(this.normalThreshold, this.warningThreshold, svcValue)%>"><b><%=CategoryUtil.formatValue(svcValue)%>%</b></td>
                </tr>
              <% } else { %>
                <tr>
                  <td align="left"  width="25%"><a href="element/service.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>&service=<%=service.getServiceId()%>"><%=service.getServiceName()%></a></td>
                  <td align="right" width="50%" bgcolor="#cccccc"><b><%=ElementUtil.getServiceStatusString(service)%></b></td>
                </tr>
              <% } %>
            <% } %>
          <% } else { %>
            <%-- interface is not managed --%>
            <% if("0.0.0.0".equals(ipAddr)) {
            }
            else { %>
            <tr>
              <td align="left" width="25%" valign="top">
              <a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>"><%=ipAddr%></a>
              </td>
              <td align="right" width="75%" colspan="2" bgcolor="#cccccc"><b><%=ElementUtil.getInterfaceStatusString(intf)%></b></td>
            </tr>
            <% } %>
          <% } %>
        <% } %>
      </table>
    </td>
  </tr>
  <tr bgcolor="#999999">
    <td colspan="2">Percentage over last 24 hours</td> <%-- next iteration, read this from same properties file that sets up for RTCVCM --%></td>
<% } %>
  </tr>   
</table>   

<%!    
    /** Convenient anonymous class for sorting Interface objects by IP address. */
    protected Comparator interfaceComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            //for brevity's sake assume they're both Interfaces
            Interface i1 = (Interface)o1;
            Interface i2 = (Interface)o2;
            
            return i1.getIpAddress().compareTo(i2.getIpAddress());
        }
        
        public boolean equals(Object o1, Object o2) {
            //for brevity's sake assume they're both Interfaces
            Interface i1 = (Interface)o1;
            Interface i2 = (Interface)o2;
            
            return i1.getIpAddress().equals(i2.getIpAddress());
        }        
    };
    
    
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

    
    public Interface[] getInterfaces(int nodeId) throws java.sql.SQLException {
        Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode(nodeId);
        
        if( intfs != null ) {
            Arrays.sort(intfs, this.interfaceComparator); 
        }

        return intfs;
    }

    
    public Service[] getServices(Interface intf) throws java.sql.SQLException {
        if( intf == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        Service[] svcs = NetworkElementFactory.getServicesOnInterface(intf.getNodeId(), intf.getIpAddress());
        
        if( svcs != null ) {
            Arrays.sort(svcs, this.serviceComparator); 
        }
        
        return svcs;
    }
%>
