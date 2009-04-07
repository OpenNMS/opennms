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
// 2004 Jan 13: Added this new code for the XML RPC Daemon
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.outage.*,
		java.util.*,
		java.sql.SQLException,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.acegisecurity.Authentication,
		org.opennms.web.outage.filter.*,
		org.opennms.web.element.ElementUtil,
		java.text.DateFormat
	"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--
  This page is written to be the display (view) portion of the OutageFilterServlet
  at the /outage/list.htm URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) outages: the list of org.opennms.web.outage.Outage instances to display
  2) parms: an org.opennms.web.outage.OutageQueryParms object that holds all the 
     parameters used to make this query
--%>

<%!
    //useful constant strings
    public static final String ZOOM_IN_ICON = "[+]";
    public static final String DISCARD_ICON = "[-]";
    public static final String BEFORE_ICON  = "[&gt;]";
    public static final String AFTER_ICON   = "[&lt;]";
    
    public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
%>

<%
    //required attributes
    Outage[] outages = (Outage[])request.getAttribute( "outages" );
    OutageQueryParms parms = (OutageQueryParms)request.getAttribute( "parms" );
    int outageCount = (Integer)request.getAttribute( "outageCount" );

    if( outages == null || parms == null ) {
        throw new ServletException( "Missing either the outages or parms request attribute." );
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Outage List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Outage" />
  <jsp:param name="breadcrumb" value="<a href='outage/index.jsp' title='Outages System Page'>Outages</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

    <% if( outageCount > 0 ) { %>
      <% String baseUrl = OutageUtil.makeLink(request, parms); %>
      <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
        <jsp:param name="count"    value="<%=outageCount%>" />
        <jsp:param name="baseurl"  value="<%=baseUrl%>" />
        <jsp:param name="limit"    value="<%=parms.limit%>" />
        <jsp:param name="multiple" value="<%=parms.multiple%>" />
      </jsp:include>
    <% } %>           
    <jsp:include page="/includes/search-constraints-box.jsp" />
    <table>
      <tr>
        <th><%=this.makeSortLink(request, parms, SortStyle.ID,                SortStyle.REVERSE_ID,                "id",                        "ID" )%></th>
        <th><%=this.makeSortLink(request, parms, SortStyle.NODE,              SortStyle.REVERSE_NODE,              "node",                      "Node")%></th>
        <th><%=this.makeSortLink(request, parms, SortStyle.INTERFACE,         SortStyle.REVERSE_INTERFACE,         "interface",                 "Interface")%></th>
        <th><%=this.makeSortLink(request, parms, SortStyle.SERVICE,           SortStyle.REVERSE_SERVICE,           "service",                   "Service")%></th>
        <th><%=this.makeSortLink(request, parms, SortStyle.IFLOSTSERVICE,     SortStyle.REVERSE_IFLOSTSERVICE,     "time service was lost",     "Down")%></th>
        <th><%=this.makeSortLink(request, parms, SortStyle.IFREGAINEDSERVICE, SortStyle.REVERSE_IFREGAINEDSERVICE, "time service was regained", "Up")%></th>
      </tr>      
      
      <%
        for( int i=0; i < outages.length; i++ ) {
        Outage outage = outages[i];
      	pageContext.setAttribute("outage", outage);
      %>
        <tr class="<%=OutageUtil.getStatusColor(outages[i])%>">
        
          <!-- outage id -->
          <td>
            <a href="outage/detail.jsp?id=<%=outages[i].getId()%>"><%=outages[i].getId()%></a>
          </td>
          
          <!-- node -->
          <td class="noWrap">
            <% if(outages[i].getNodeId() != 0 ) { %>             
              <% String longLabel  = outages[i].getNodeLabel(); %>
              <% String shortLabel = ElementUtil.truncateLabel(longLabel, 32); %>
              <a href="element/node.jsp?node=<%=outages[i].getNodeId()%>" title="<%=longLabel%>"><%=shortLabel%></a>
              <% org.opennms.web.outage.filter.Filter nodeFilter = new NodeFilter(outages[i].getNodeId()); %>
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                  <a href="<%=OutageUtil.makeLink( request, parms, nodeFilter, true)%>" title="Show only outages on this node"><%=ZOOM_IN_ICON%></a>
                  <a href="<%=OutageUtil.makeLink( request, parms, new NegativeNodeFilter(outages[i].getNodeId()), true)%>" title="Do not show outages for this node"><%=DISCARD_ICON%></a>              
              <% } %>                          
            <% } %>
          </td>
          
          <!-- interface -->
          <td class="noWrap">
            <% if(outages[i].getIpAddress() != null ) { %>
              <% if( outages[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=outages[i].getNodeId()%>&intf=<%=outages[i].getIpAddress()%>" title="More info on this interface"><%=outages[i].getIpAddress()%></a>
              <% } else { %>
                 <%=outages[i].getIpAddress()%>
              <% } %>
              
              <% org.opennms.web.outage.filter.Filter intfFilter = new InterfaceFilter(outages[i].getIpAddress()); %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                  <a href="<%=OutageUtil.makeLink( request, parms, intfFilter, true)%>" title="Show only outages on this IP address"><%=ZOOM_IN_ICON%></a>
                  <a href="<%=OutageUtil.makeLink( request, parms, new NegativeInterfaceFilter(outages[i].getIpAddress()), true)%>" title="Do not show outages for this interface"><%=DISCARD_ICON%></a>                                            
              <% } %>                          
            <% } %>
          </td>
          
          <!-- service -->
          <td class="noWrap">
            <% if(outages[i].getServiceName() != null) { %>
              <% if( outages[i].getNodeId() != 0 && outages[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=outages[i].getNodeId()%>&intf=<%=outages[i].getIpAddress()%>&service=<%=outages[i].getServiceId()%>" title="More info on this service"><%=outages[i].getServiceName()%></a>
              <% } else { %>
                <%=outages[i].getServiceName()%>
              <% } %>                
              
              <% org.opennms.web.outage.filter.Filter serviceFilter = new ServiceFilter(outages[i].getServiceId()); %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                  <a href="<%=OutageUtil.makeLink( request, parms, serviceFilter, true)%>" title="Show only outages with this service type"><%=ZOOM_IN_ICON%></a>
                  <a href="<%=OutageUtil.makeLink( request, parms, new NegativeServiceFilter(outages[i].getServiceId()), true)%>" title="Do not show outages for this service"><%=DISCARD_ICON%></a>
              <% } %>              
            <% } %>          
          </td>
            
          <!-- lost service time -->
          <td class="noWrap">
	    <fmt:formatDate value="${outage.lostServiceTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${outage.lostServiceTime}" type="time" pattern="HH:mm:ss"/>
              <a href="<%=OutageUtil.makeLink( request, parms, new LostServiceDateAfterFilter(outages[i].getLostServiceTime()), true)%>" title="Only show outages beginning after this one"><%=AFTER_ICON%></a>            
              <a href="<%=OutageUtil.makeLink( request, parms, new LostServiceDateBeforeFilter(outages[i].getLostServiceTime()), true)%>" title="Only show outages beginning before this one"><%=BEFORE_ICON%></a>            
          </td>
          
          <!-- regained service time -->
          <% Date regainedTime = outages[i].getRegainedServiceTime(); %>
          <% if(regainedTime != null ) { %>
            <td class="noWrap">
	    <fmt:formatDate value="${outage.regainedServiceTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${outage.regainedServiceTime}" type="time" pattern="HH:mm:ss"/>
                <a href="<%=OutageUtil.makeLink( request, parms, new RegainedServiceDateAfterFilter(outages[i].getRegainedServiceTime()), true)%>" title="Only show outages resolving after this one"><%=AFTER_ICON%></a>            
                <a href="<%=OutageUtil.makeLink( request, parms, new RegainedServiceDateBeforeFilter(outages[i].getRegainedServiceTime()), true)%>" title="Only show outages resolving before this one"><%=BEFORE_ICON%></a>            
            </td>
          <% } else { %>
            <td class="bright"><%=OutageUtil.getStatusLabel(outages[i])%></td>
          <% } %>
        </tr>
      <% } %>
    </table>

<jsp:include page="/includes/bookmark.jsp" flush="false" />
<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    protected String makeSortLink(HttpServletRequest request, OutageQueryParms parms, SortStyle style, SortStyle revStyle, String sortString, String title ) {
      StringBuffer buffer = new StringBuffer();


      if( parms.sortStyle == style ) {
          buffer.append( "<img src=\"images/arrowdown.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Ascending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( OutageUtil.makeLink(request, parms, revStyle ));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else if( parms.sortStyle == revStyle ) {
          buffer.append( "<img src=\"images/arrowup.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Descending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( OutageUtil.makeLink(request, parms, style )); 
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else {
          buffer.append( "<a href=\"" );
          buffer.append( OutageUtil.makeLink(request, parms, style ));
          buffer.append( "\" title=\"Sort by " );
          buffer.append( sortString );
          buffer.append( "\">" );   
      }

      buffer.append( title );
      buffer.append( "</a>" );

      return( buffer.toString() );
    }  
%>

