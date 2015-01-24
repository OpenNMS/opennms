<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@page language="java"	contentType="text/html"	session="true" %>

<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>

<%@page import="org.opennms.netmgt.model.OnmsNode"%>
<%@page import="org.opennms.web.element.ElementUtil"%>
<%@page import="org.opennms.web.element.NetworkElementFactory"%>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.outage.Outage"%>
<%@page import="org.opennms.web.outage.OutageQueryParms"%>
<%@page import="org.opennms.web.outage.OutageUtil"%>
<%@page import="org.opennms.web.outage.SortStyle"%>
<%@page import="org.opennms.web.outage.filter.NodeFilter"%>
<%@page import="org.opennms.web.outage.filter.NegativeNodeFilter"%>
<%@page import="org.opennms.web.outage.filter.ForeignSourceFilter"%>
<%@page import="org.opennms.web.outage.filter.NegativeForeignSourceFilter"%>
<%@page import="org.opennms.web.outage.filter.InterfaceFilter"%>
<%@page import="org.opennms.web.outage.filter.NegativeInterfaceFilter"%>
<%@page import="org.opennms.web.outage.filter.ServiceFilter"%>
<%@page import="org.opennms.web.outage.filter.NegativeServiceFilter"%>
<%@page import="org.opennms.web.outage.filter.LostServiceDateAfterFilter"%>
<%@page import="org.opennms.web.outage.filter.LostServiceDateBeforeFilter"%>
<%@page import="org.opennms.web.outage.filter.RegainedServiceDateAfterFilter"%>
<%@page import="org.opennms.web.outage.filter.RegainedServiceDateBeforeFilter"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
    public static final String ZOOM_IN_ICON = "<i class=\"fa fa-plus-square-o\"></i>";
    public static final String DISCARD_ICON = "<i class=\"fa fa-minus-square-o\"></i>";
    public static final String BEFORE_ICON  = "<i class=\"fa fa-toggle-right\"></i>";
    public static final String AFTER_ICON   = "<i class=\"fa fa-toggle-left\"></i>";
    
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


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Outage List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Outage" />
  <jsp:param name="breadcrumb" value="<a href='outage/index.jsp' title='Outages System Page'>Outages</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<link rel="stylesheet" href="css/font-awesome-4.0.3/css/font-awesome.min.css">

    <jsp:include page="/includes/search-constraints-box.jsp" />
    <br/>

    <% if( outageCount > 0 ) { %>
      <% String baseUrl = OutageUtil.makeLink(request, parms); %>
      <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
        <jsp:param name="count"    value="<%=outageCount%>" />
        <jsp:param name="baseurl"  value="<%=baseUrl%>" />
        <jsp:param name="limit"    value="<%=parms.limit%>" />
        <jsp:param name="multiple" value="<%=parms.multiple%>" />
      </jsp:include>
    <% } %>

<div class="panel panel-default">
    <table class="table table-bordered table-condensed">
      <tr>
        <th><%=this.makeSortLink(request, parms, SortStyle.ID,                SortStyle.REVERSE_ID,                "id",                        "ID" )%></th>
        <th><%=this.makeSortLink(request, parms, SortStyle.FOREIGNSOURCE,     SortStyle.REVERSE_FOREIGNSOURCE,     "foreignsource",             "Foreign Source" )%></th>
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
            <a href="outage/detail.htm?id=<%=outages[i].getId()%>"><%=outages[i].getId()%></a>
          </td>

          <!-- foreign source -->
          <td class="noWrap">
            <% if(outages[i].getNodeId() != 0 ) { %>
              <% OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(outages[i].getNodeId()); %>
              <% if(node.getForeignSource() != null) { %>
              <%=node.getForeignSource()%>
              <% Filter foreignSourceFilter = new ForeignSourceFilter(node.getForeignSource(), getServletContext()); %>
              <% if( !parms.filters.contains(foreignSourceFilter) ) { %>
                  <a href="<%=OutageUtil.makeLink( request, parms, foreignSourceFilter, true)%>" title="Show only outages for this foreign source"><%=ZOOM_IN_ICON%></a>
                  <a href="<%=OutageUtil.makeLink( request, parms, new NegativeForeignSourceFilter(node.getForeignSource(), getServletContext()), true)%>" title="Do not show outages for this foreign source"><%=DISCARD_ICON%></a>
              <% } %>
              <% } else { %>
                &nbsp;
              <% } %>
            <% } %>
          </td>

          <!-- node -->
          <td class="noWrap">
            <% if(outages[i].getNodeId() != 0 ) { %>             
              <% String longLabel  = outages[i].getNodeLabel(); %>
              <% String shortLabel = ElementUtil.truncateLabel(longLabel, 32); %>
              <a href="element/node.jsp?node=<%=outages[i].getNodeId()%>" title="<%=longLabel%>"><%=shortLabel%></a>
              <% Filter nodeFilter = new NodeFilter(outages[i].getNodeId(), getServletContext()); %>
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                  <a href="<%=OutageUtil.makeLink( request, parms, nodeFilter, true)%>" title="Show only outages on this node"><%=ZOOM_IN_ICON%></a>
                  <a href="<%=OutageUtil.makeLink( request, parms, new NegativeNodeFilter(outages[i].getNodeId(), getServletContext()), true)%>" title="Do not show outages for this node"><%=DISCARD_ICON%></a>              
              <% } %>                          
            <% } %>
          </td>
          
          <!-- interface -->
          <td class="noWrap">
            <% if(outages[i].getIpAddress() != null ) { %>
              <% if( outages[i].getNodeId() != 0 ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(outages[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=outages[i].getIpAddress()%>"/>
                </c:url>
                <a href="${interfaceLink}" title="More info on this interface"><%=outages[i].getIpAddress()%></a>
              <% } else { %>
                 <%=outages[i].getIpAddress()%>
              <% } %>
              
              <% Filter intfFilter = new InterfaceFilter(outages[i].getIpAddress()); %>
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
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(outages[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=outages[i].getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(outages[i].getServiceId())%>"/>
                </c:url>
                <a href="${serviceLink}" title="More info on this service"><c:out value="<%=outages[i].getServiceName()%>"/></a>
              <% } else { %>
                <c:out value="<%=outages[i].getServiceName()%>"/>
              <% } %>                
              
              <% Filter serviceFilter = new ServiceFilter(outages[i].getServiceId(), getServletContext()); %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                  <a href="<%=OutageUtil.makeLink( request, parms, serviceFilter, true)%>" title="Show only outages with this service type"><%=ZOOM_IN_ICON%></a>
                  <a href="<%=OutageUtil.makeLink( request, parms, new NegativeServiceFilter(outages[i].getServiceId(), getServletContext()), true)%>" title="Do not show outages for this service"><%=DISCARD_ICON%></a>
              <% } %>              
            <% } %>          
          </td>
            
          <!-- lost service time -->
          <td class="noWrap">
              <fmt:formatDate value="${outage.lostServiceTime}" type="BOTH" />
              <a href="<%=OutageUtil.makeLink( request, parms, new LostServiceDateAfterFilter(outages[i].getLostServiceTime()), true)%>" title="Only show outages beginning after this one"><%=AFTER_ICON%></a>            
              <a href="<%=OutageUtil.makeLink( request, parms, new LostServiceDateBeforeFilter(outages[i].getLostServiceTime()), true)%>" title="Only show outages beginning before this one"><%=BEFORE_ICON%></a>            
          </td>
          
          <!-- regained service time -->
          <% Date regainedTime = outages[i].getRegainedServiceTime(); %>
          <% if(regainedTime != null ) { %>
            <td class="noWrap">
                <fmt:formatDate value="${outage.regainedServiceTime}" type="BOTH" />
                <a href="<%=OutageUtil.makeLink( request, parms, new RegainedServiceDateAfterFilter(outages[i].getRegainedServiceTime()), true)%>" title="Only show outages resolving after this one"><%=AFTER_ICON%></a>            
                <a href="<%=OutageUtil.makeLink( request, parms, new RegainedServiceDateBeforeFilter(outages[i].getRegainedServiceTime()), true)%>" title="Only show outages resolving before this one"><%=BEFORE_ICON%></a>            
            </td>
          <% } else { %>
            <td class="bright"><%=OutageUtil.getStatusLabel(outages[i])%></td>
          <% } %>
        </tr>
      <% } %>
    </table>
</div>
 
     <% if( outageCount > 0 ) { %>
       <% String baseUrl = OutageUtil.makeLink(request, parms); %>
       <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
         <jsp:param name="count"    value="<%=outageCount%>" />
         <jsp:param name="baseurl"  value="<%=baseUrl%>" />
         <jsp:param name="limit"    value="<%=parms.limit%>" />
         <jsp:param name="multiple" value="<%=parms.multiple%>" />
       </jsp:include>
     <% } %>           
 

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
