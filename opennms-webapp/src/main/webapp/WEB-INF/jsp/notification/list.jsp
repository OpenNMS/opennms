<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.notification.*,
			org.opennms.web.notification.filter.*,
                org.opennms.web.api.Authentication,
		java.util.*,
		java.io.UnsupportedEncodingException,
		org.opennms.web.event.Event,
		org.opennms.web.filter.Filter,
		org.opennms.core.utils.WebSecurityUtils
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--
  This page is written to be the display (view) portion of the NotificationQueryServlet
  at the /notification/list URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) notices: the list of org.opennms.web.notification.Notification instances to display
  2) parms: an org.opennms.web.notification.NoticeQueryParms object that holds all the 
     parameters used to make this query
--%>
<%
    //required attributes
    Notification[] notices = (Notification[])request.getAttribute( "notices" );
    int noticeCount = request.getAttribute("noticeCount") == null ? -1 : (Integer)request.getAttribute("noticeCount");
    NoticeQueryParms parms = (NoticeQueryParms)request.getAttribute( "parms" );

    @SuppressWarnings("unchecked")
    Map<Integer,String[]> nodeLabels = (Map<Integer,String[]>)request.getAttribute( "nodeLabels" );
    @SuppressWarnings("unchecked")
    Map<Integer,String[]> nodeLocations = (Map<Integer,String[]>)request.getAttribute( "nodeLocations" );
    @SuppressWarnings("unchecked")
    Map<Integer,Event> events = (Map<Integer,Event>)request.getAttribute( "events" );

    if( notices == null || parms == null || nodeLabels == null ) {
        throw new ServletException( "Missing a required attribute." );
    }

    pageContext.setAttribute("addPositiveFilter", "<i class=\"fa fa-plus-square-o\"></i>");
    pageContext.setAttribute("addNegativeFilter", "<i class=\"fa fa-minus-square-o\"></i>");
    pageContext.setAttribute("addBeforeFilter", "<i class=\"fa fa-toggle-right\"></i>");
    pageContext.setAttribute("addAfterFilter", "<i class=\"fa fa-toggle-left\"></i>");
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Notice List" />
  <jsp:param name="headTitle" value="Notice List" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp'>Notifications</a>" />
  <jsp:param name="breadcrumb" value="Notice List" />
</jsp:include>

<script type="text/javascript" >
    function checkAllCheckboxes() {
       if( document.acknowledge_form.notices.length ) {  
         for( i = 0; i < document.acknowledge_form.notices.length; i++ ) {
           document.acknowledge_form.notices[i].checked = true
         }
       }
       else {
         document.acknowledge_form.notices.checked = true
       }
         
    }
    
    function submitAcknowledge()
    {
        var isChecked = false;
        var numChecked = 0;
        
        if (document.acknowledge_form.notices.length)
        {
            for( i = 0; i < document.acknowledge_form.notices.length; i++ ) 
            {
              //make sure something is checked before proceeding
              if (document.acknowledge_form.notices[i].checked)
              {
                  isChecked=true;
                  numChecked+=1;
              }
            }
            
            if (isChecked && document.acknowledge_form.multiple)
            {
              if (numChecked == parseInt(document.acknowledge_form.notices.length)) 
              { 
                var newPageNum = parseInt(document.acknowledge_form.multiple.value) - 1;
                var findVal = "multiple=" + document.acknowledge_form.multiple.value;
                var replaceWith = "multiple=" + newPageNum;
                var tmpRedirect = document.acknowledge_form.redirectParms.value;
                document.acknowledge_form.redirectParms.value = tmpRedirect.replace(findVal, replaceWith);
                document.acknowledge_form.submit();
              } 
              else 
              {
                document.acknowledge_form.submit();
              }
            }
            else if (isChecked)
            {
               document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the notices that you would like to acknowledge.");
            }
        }
        else
        {
            if (document.acknowledge_form.notices.checked)
            {

                document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the notices that you would like to acknowledge.");
            }
        }
    }
    
</script>
<!-- notification/browser.jsp -->
<% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %>
  <p>
    Currently showing only <strong>outstanding</strong> notices.
    <a href="<%=this.makeLink(parms, AcknowledgeType.ACKNOWLEDGED)%>" title="Show acknowledged notices">[Show acknowledged]</a>
  </p>
<% } else if( parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
  <p>
    Currently showing only <strong>acknowledged</strong> notices.  
    <a href="<%=this.makeLink(parms, AcknowledgeType.UNACKNOWLEDGED)%>" title="Show outstanding notices">[Show outstanding]</a>
  </p>
<% } %>
      
<!-- JS - attempt to use common pager for event and notice -->

<% if( noticeCount > 0 ) { %>
  <% String baseUrl = this.makeLink(parms); %>
  <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
    <jsp:param name="count"    value="<%=noticeCount%>" />
    <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
    <jsp:param name="limit"    value="<%=parms.limit%>"      />
    <jsp:param name="multiple" value="<%=parms.multiple%>"   />
  </jsp:include>
<% } %>

<% if( parms.filters != null && parms.filters.size() > 0 ) { %>
  <% int length = parms.filters.size(); %>
  <p>
    Applied filters:
      <% for( int i = 0; i < length; i++ ) { %>
		<span class="label label-default"><% Filter filter = parms.filters.get(i); %>
				<%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%> <a href="<%=this.makeLink( parms, filter, false)%>" title="Remove filter">[-]</a></span> &nbsp; 
      <% } %>
    &mdash; <a href="<%=this.makeLink( parms, new ArrayList<Filter>())%>" title="Remove all filters">[Remove all]</a>
  </p>
<% } %> 
        <form action="notification/acknowledge" method="post" name="acknowledge_form">
          <input type="hidden" name="curUser" value="<%=request.getRemoteUser()%>"/>
          <input type="hidden" name="redirectParms" value="<c:out value="<%=request.getQueryString()%>"/>" />
          <%=org.opennms.web.api.Util.makeHiddenTags(request)%>
      <table class="table table-condensed table-bordered severity">
			<thead>
			  <tr>
          <th nowrap><%=this.makeSortLink( parms, SortStyle.ID,SortStyle.REVERSE_ID,     "id",          "ID"           )%></th>
          <th nowrap>Event ID</th>
          <th><%=this.makeSortLink( parms, SortStyle.SEVERITY,      SortStyle.REVERSE_SEVERITY,      "severity",     "Severity"         )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.PAGETIME,      SortStyle.REVERSE_PAGETIME,      "pagetime",     "Sent&nbsp;Time"   )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.LOCATION,      SortStyle.REVERSE_LOCATION,      "location",     "Source&nbsp;Loc." )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.RESPONDER,     SortStyle.REVERSE_RESPONDER,     "answeredby",   "Responder"        )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.RESPONDTIME,   SortStyle.REVERSE_RESPONDTIME,   "respondtime",  "Respond&nbsp;Time")%></th>  
          <th><%=this.makeSortLink( parms, SortStyle.NODE,          SortStyle.REVERSE_NODE,          "node",         "Node"             )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.NODE_LOCATION, SortStyle.REVERSE_NODE_LOCATION, "nodelocation", "Node&nbsp;Loc."   )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.INTERFACE,     SortStyle.REVERSE_INTERFACE,     "interface",    "Interface"        )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.SERVICE,       SortStyle.REVERSE_SERVICE,       "service",      "Service"          )%></th>
        </tr>
      </thead>

      <% for (int i = 0; i < notices.length; i++) { 
    	final Notification notification = notices[i];
    	if (notification == null) continue;
        Event event = null;
        String eventSeverity = "Unknown";
        if (notification.getEventId() > 0) {
            event = events.get(notification.getEventId());
            if (event != null) {
              eventSeverity = event.getSeverity().getLabel();
            }
        }
        %>
        <tr class="severity-<%=eventSeverity%>">
          <td class="divider" rowspan="2" nowrap><% if((parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) && 
		(request.isUserInRole( Authentication.ROLE_ADMIN ) || !request.isUserInRole( Authentication.ROLE_READONLY ))) { %>
            <input type="checkbox" name="notices" value="<%=notification.getId()%>" />
          <% } %> 
						<a href="notification/detail.jsp?notice=<%=notification.getId()%>"><%=notification.getId()%></a></td>
          <td class="divider" rowspan="2">
            <% if ( event != null && event.getEventDisplay() != null && event.getEventDisplay() ) { %>
            <a href="event/detail.jsp?id=<%=notification.getEventId()%>"><%=notification.getEventId()%></a>
            <% } %>
          </td>
          <td class="bright divider" rowspan="2"><%=eventSeverity%></td>
          <td class="divider"><fmt:formatDate value="<%=notification.getTimeSent()%>" type="BOTH" /></td>
          <td class="divider">
            <% if ( event != null ) { %>
              <% Filter locationFilter = new LocationFilter(event.getLocation()); %>
              <a href="event/detail.jsp?id=<%=notification.getEventId()%>"><%= event.getLocation() %></a>
              <% if (parms.filters != null && !parms.filters.contains( locationFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, locationFilter, true)%>" class="filterLink" title="Show only notices from this event source location">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeLocationFilter(event.getLocation()), true)%>" class="filterLink" title="Do not show notices from this event source location">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
          <% final String responder = notification.getResponder(); %>
          <% if (responder != null) { %>
            <% Filter responderFilter = new ResponderFilter(responder); %>      
              <%= responder %>
              <% if( parms.filters != null && !parms.filters.contains( responderFilter )) { %>
                <a href="<%=this.makeLink( parms, responderFilter, true)%>" class="filterLink" title="Show only notices with this responder">${addPositiveFilter}</a>
              <% } %>
            <% } %>
            </td>
          <td class="divider">
            <%if (notification.getTimeReplied()!=null) { %>
              <fmt:formatDate value="<%=notification.getTimeReplied()%>" type="BOTH" />
            <% } %>
          </td>
          <td class="divider">
            <% if(notification.getNodeId() > 0 ) { %>
              <% Filter nodeFilter = new NodeFilter(notification.getNodeId()); %>
              <% String[] labels = nodeLabels.get(notification.getNodeId()); %>
              <a href="element/node.jsp?node=<%=notification.getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
              <% if( parms.filters != null && !parms.filters.contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only notices on this node">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeNodeFilter(notification.getNodeId(), getServletContext()), true)%>" class="filterLink" title="Do not show notices for this node">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notification.getNodeId() > 0 ) { %>
              <% String[] locations = nodeLocations.get(notification.getNodeId()); %>
              <% Filter nodeLocationFilter = new NodeLocationFilter(locations[1]); %>
              <a href="element/node.jsp?node=<%=notification.getNodeId()%>" title="<%=locations[1]%>"><%=locations[0]%></a>
              <% if( parms.filters != null && !parms.filters.contains(nodeLocationFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, nodeLocationFilter, true)%>" class="filterLink" title="Show only notices for this node location">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeNodeLocationFilter(locations[1]), true)%>" class="filterLink" title="Do not show notices for this node location">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notification.getIpAddress() != null ) { %>
              <% Filter intfFilter = new InterfaceFilter(notification.getIpAddress()); %>
              <% if( notification.getNodeId() != 0 ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(notification.getNodeId())%>"/>
                  <c:param name="intf" value="<%=notification.getIpAddress()%>"/>
                </c:url>
                <a href="<c:out value="${interfaceLink}"/>" title="More info on this interface"><%=notification.getIpAddress()%></a>
              <% } else { %>
                 <%=notification.getInterfaceId()%>
              <% } %>
              <% if( parms.filters != null && !parms.filters.contains(intfFilter) ) { %>
                <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only notices on this IP address">${addPositiveFilter}</a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notification.getServiceName() != null && !notification.getServiceName().trim().isEmpty()) { %>
              <% Filter serviceFilter = new ServiceFilter(notification.getServiceId(), getServletContext()); %>
              <% if( notification.getNodeId() != 0 && notification.getIpAddress() != null ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(notification.getNodeId())%>"/>
                  <c:param name="intf" value="<%=notification.getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(notification.getServiceId())%>"/>
                </c:url>
                <a href="<c:out value="${serviceLink}"/>" title="More info on this service"><c:out value="<%=notification.getServiceName()%>"/></a>
              <% } else { %>
                <c:out value="<%=notification.getServiceName()%>"/>
              <% } %>
              <% if( parms.filters != null && !parms.filters.contains( serviceFilter )) { %>
                <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only notices with this service type">${addPositiveFilter}</a>
              <% } %>
            <% } %>
          </td>
        </tr>
        <tr class="severity-<%=eventSeverity%>">
          <td colspan="8"><%=WebSecurityUtils.sanitizeString(notification.getTextMessage())%></td> 
        </tr>
      <% } /*end for*/%>
      </table>
      <p><%=notices.length%> notices &nbsp;

        <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED && (request.isUserInRole( Authentication.ROLE_ADMIN ) || !request.isUserInRole( Authentication.ROLE_READONLY ))) { %>
            <button type="reset" class="btn btn-default">Reset</button>
            <button type="button" onClick="checkAllCheckboxes()" class="btn btn-default">Select All</button>
            <button type="button" onClick="submitAcknowledge()" class="btn btn-default">Acknowledge Notices</button>
        <% } %>
        
	</p>
        </form>
	<!--		<% if( noticeCount > 0 ) { %>
			<p align="right"><a href="<%=this.makeLink(parms)%>&multiple=<%=parms.multiple+1%>">Next</a></p>
			<% } %> -->

        <% if( noticeCount > 0 ) { %>
          <% String baseUrl = this.makeLink(parms); %>
          <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
          <jsp:param name="count"    value="<%=noticeCount%>" />
          <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
          <jsp:param name="limit"    value="<%=parms.limit%>"      />
          <jsp:param name="multiple" value="<%=parms.multiple%>"   />
          </jsp:include>
         <% } %>
 
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


<%!
    String urlBase = "notification/browse";

    protected String makeSortLink( NoticeQueryParms parms, SortStyle style, SortStyle revStyle, String sortString, String title ) {
      StringBuffer buffer = new StringBuffer();

      buffer.append( "<nobr>" );

      if( parms.sortStyle == style ) {
          buffer.append( "<img src=\"images/arrowdown.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Ascending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink( parms, revStyle ));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else if( parms.sortStyle == revStyle ) {
          buffer.append( "<img src=\"images/arrowup.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Descending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink( parms, style )); 
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else {
          buffer.append( "<a href=\"" );
          buffer.append( this.makeLink( parms, style ));
          buffer.append( "\" title=\"Sort by " );
          buffer.append( sortString );
          buffer.append( "\">" );   
      }

      buffer.append( title );
      buffer.append( "</a>" );

      buffer.append( "</nobr>" );

      return( buffer.toString() );
    }

    public String makeLink( SortStyle sortStyle, AcknowledgeType ackType, int limit, List<Filter> filters ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( sortStyle.getShortName() );
      buffer.append( "&amp;acktype=" );
      buffer.append( ackType.getShortName() );
      if (limit > 0) {
          buffer.append( "&amp;limit=" ).append(limit);
      }

      if( filters != null ) {
        for( int i=0; i < filters.size(); i++ ) {
          buffer.append( "&amp;filter=" );
          String filterString = filters.get(i).getDescription();
          try {
            buffer.append( java.net.URLEncoder.encode(filterString, "UTF-8") );
          } catch (final UnsupportedEncodingException e) {
        	  // ignore
          }
        }
      }      

      return( buffer.toString() );
    }


    public String makeLink( NoticeQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, parms.filters) );
    }

    public String makeLink( NoticeQueryParms parms, SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.limit, parms.filters) );
    }

    public String makeLink( NoticeQueryParms parms, AcknowledgeType ackType ) {
      return( this.makeLink( parms.sortStyle, ackType, parms.limit, parms.filters) );
    }

    public String makeLink( NoticeQueryParms parms, List<Filter> filters ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, filters) );
    }


    public String makeLink( NoticeQueryParms parms, Filter filter, boolean add ) {
      List<Filter> newList = new ArrayList<>();
      if (parms.filters != null) {
          newList.addAll(parms.filters);
      }
      if( add ) {
        newList.add( filter );
      }
      else {
        newList.remove( filter );
      }

      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, newList ));
    }

%>
