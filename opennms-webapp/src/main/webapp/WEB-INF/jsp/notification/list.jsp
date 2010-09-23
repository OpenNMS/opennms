<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr 16: Convert to a controller
// 2008 Aug 14: Sanitize input
// 2004 Nov 18: Added "Acknowledge Notices" and "Select All" buttons at the top of the table
//              So it isn't necessary to scroll all the way to the bottom. Bill Ayres.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 10: Removed "http://" from UEIs and references to bluebird.
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
	import="org.opennms.web.notification.*,
			org.opennms.web.notification.filter.*,
                org.opennms.web.springframework.security.Authentication,
		java.util.*,
		java.sql.SQLException,
		org.opennms.web.event.Event,
		org.opennms.web.filter.Filter,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.WebSecurityUtils,
		org.opennms.netmgt.model.OnmsSeverity
	"
%>

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

    Map<Integer,String[]> nodeLabels = (Map<Integer,String[]>)request.getAttribute( "nodeLabels" );
    Map<Integer,Event> events = (Map<Integer,Event>)request.getAttribute( "events" );

    if( notices == null || parms == null || nodeLabels == null ) {
        throw new ServletException( "Missing a required attribute." );
    }

    // Make 'action' the opposite of the current acknowledgement state
    String action = AcknowledgeType.ACKNOWLEDGED.getShortName();
    if (parms.ackType != null && parms.ackType == AcknowledgeType.ACKNOWLEDGED) {
        action = AcknowledgeType.UNACKNOWLEDGED.getShortName();
    }

    pageContext.setAttribute("addPositiveFilter", "[+]");
    pageContext.setAttribute("addNegativeFilter", "[-]");
    pageContext.setAttribute("addBeforeFilter", "[&gt;]");
    pageContext.setAttribute("addAfterFilter", "[&lt;]");
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notice List" />
  <jsp:param name="headTitle" value="Browse" />
  <jsp:param name="headTitle" value="Notices" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp' title='Notice System Page'>Notices</a>" />
  <jsp:param name="breadcrumb" value="List" />
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

<% if( parms.filters.size() > 0 ) { %>
  <% int length = parms.filters.size(); %>
  <p>
    Applied filters:
      <% for( int i = 0; i < length; i++ ) { %>
		<span class="filter"><% Filter filter = (Filter)parms.filters.get(i); %>
				<%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%> <a href="<%=this.makeLink( parms, filter, false)%>" title="Remove filter">[-]</a></span> &nbsp; 
      <% } %>
    &mdash; <a href="<%=this.makeLink( parms, new ArrayList<Filter>())%>" title="Remove all filters">[Remove all]</a>
  </p>
<% } %>
	<jsp:include page="/includes/key.jsp" flush="false" />
        <form action="notification/acknowledge" method="post" name="acknowledge_form">
          <input type="hidden" name="curUser" value="<%=request.getRemoteUser()%>">
          <input type="hidden" name="redirectParms" value="<%=org.opennms.web.Util.htmlify(request.getQueryString())%>" />
          <%=org.opennms.web.Util.makeHiddenTags(request)%>
      <table>
			<thead>
			  <tr>
          <th class="noWrap"><%=this.makeSortLink( parms, SortStyle.ID,SortStyle.REVERSE_ID,     "id",          "ID"           )%></th>
          <th class="noWrap">Event ID</th>
          <th>Severity</th>
          <th><%=this.makeSortLink( parms, SortStyle.PAGETIME,    SortStyle.REVERSE_PAGETIME,    "pagetime",    "Sent Time"    )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.RESPONDER,   SortStyle.REVERSE_RESPONDER,   "answeredby",  "Responder"    )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.RESPONDTIME, SortStyle.REVERSE_RESPONDTIME, "respondtime", "Respond Time" )%></th>  
          <th><%=this.makeSortLink( parms, SortStyle.NODE,        SortStyle.REVERSE_NODE,        "node",        "Node"         )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.INTERFACE,   SortStyle.REVERSE_INTERFACE,   "interface",   "Interface"    )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.SERVICE,     SortStyle.REVERSE_SERVICE,     "service",     "Service"      )%></th>
        </tr>
      </thead>

      <% for( int i=0; i < notices.length; i++ ) { 
        Event event = events.get(notices[i].getEventId());
        String eventSeverity = event.getSeverity().getLabel();
        %>
        <tr class="<%=eventSeverity%>">
          <td class="divider noWrap" rowspan="2"><% if((parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) && 
		(request.isUserInRole( Authentication.ADMIN_ROLE ) || !request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
            <input type="checkbox" name="notices" value="<%=notices[i].getId()%>" />
          <% } %> 
						<a href="notification/detail.jsp?notice=<%=notices[i].getId()%>"><%=notices[i].getId()%></a></td>
          <td class="divider" rowspan="2">
            <% if ( event.getEventDisplay() != null && event.getEventDisplay() ) { %>
            <a href="event/detail.jsp?id=<%=notices[i].getEventId()%>"><%=notices[i].getEventId()%></a>
            <% } %>
          </td>
          <td class="bright divider" rowspan="2"><%=eventSeverity%></td>
          <td class="divider"><%=org.opennms.web.Util.formatDateToUIString(notices[i].getTimeSent())%></td>
          <td class="divider"><% Filter responderFilter = new ResponderFilter(notices[i].getResponder()); %>      
            <% if(notices[i].getResponder()!=null) {%>
              <%=notices[i].getResponder()%>
              <% if( !parms.filters.contains( responderFilter )) { %>
                <a href="<%=this.makeLink( parms, responderFilter, true)%>" class="filterLink" title="Show only notices with this responder">${addPositiveFilter}</a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <%if (notices[i].getTimeReplied()!=null) { %>
              <%=org.opennms.web.Util.formatDateToUIString(notices[i].getTimeReplied())%>
            <% } %>
					</td>
          <td class="divider">
            <% if(notices[i].getNodeId() != 0 ) { %>
              <% Filter nodeFilter = new NodeFilter(notices[i].getNodeId()); %>
              <% String[] labels = nodeLabels.get(notices[i].getNodeId()); %>
              <a href="element/node.jsp?node=<%=notices[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only notices on this node">${addPositiveFilter}</a>
                <a href="<%=this.makeLink( parms, new NegativeNodeFilter(notices[i].getNodeId()), true)%>" class="filterLink" title="Do not show events for this node">${addNegativeFilter}</a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notices[i].getIpAddress() != null ) { %>
              <% Filter intfFilter = new InterfaceFilter(notices[i].getIpAddress()); %>
              <% if( notices[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>" title="More info on this interface"><%=notices[i].getIpAddress()%></a>
              <% } else { %>
                 <%=notices[i].getInterfaceId()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only notices on this IP address">${addPositiveFilter}</a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notices[i].getServiceName() != null) { %>
              <% Filter serviceFilter = new ServiceFilter(notices[i].getServiceId()); %>
              <% if( notices[i].getNodeId() != 0 && notices[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>&service=<%=notices[i].getServiceId()%>" title="More info on this service"><%=notices[i].getServiceName()%></a>
              <% } else { %>
                <%=notices[i].getServiceName()%>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only notices with this service type">${addPositiveFilter}</a>
              <% } %>
            <% } %>
          </td>
        </tr>
        <tr class="<%=eventSeverity%>">
          <td colspan="6"><%=notices[i].getTextMessage()%></td> 
        </tr>
      <% } /*end for*/%>
      </table>
      <p><%=notices.length%> notices &nbsp;

        <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED && (request.isUserInRole( Authentication.ADMIN_ROLE ) || !request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
            <input TYPE="reset" />
            <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
            <input type="button" value="Acknowledge Notices" onClick="submitAcknowledge()"/>
        <% } %>
        
	</p>
        </form>
	<!--		<% if( noticeCount > 0 ) { %>
			<p align="right"><a href="<%=this.makeLink(parms)%>&multiple=<%=parms.multiple+1%>">Next</a></p>
			<% } %> -->
		<jsp:include page="/includes/bookmark.jsp" flush="false" />
        <% if( noticeCount > 0 ) { %>
          <% String baseUrl = this.makeLink(parms); %>
          <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
          <jsp:param name="count"    value="<%=noticeCount%>" />
          <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
          <jsp:param name="limit"    value="<%=parms.limit%>"      />
          <jsp:param name="multiple" value="<%=parms.multiple%>"   />
          </jsp:include>
         <% } %>
 
<jsp:include page="/includes/footer.jsp" flush="false" />


<%!
    String urlBase = "notification/browse";

    protected String makeSortLink( NoticeQueryParms parms, SortStyle style, SortStyle revStyle, String sortString, String title ) {
      StringBuffer buffer = new StringBuffer();

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

      return( buffer.toString() );
    }

    public String makeLink( SortStyle sortStyle, AcknowledgeType ackType, int limit, List<Filter> filters ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( sortStyle.getShortName() );
      buffer.append( "&acktype=" );
      buffer.append( ackType.getShortName() );
      if (limit > 0) {
          buffer.append( "&limit=" ).append(limit);
      }

      if( filters != null ) {
        for( int i=0; i < filters.size(); i++ ) {
          buffer.append( "&filter=" );
          String filterString = filters.get(i).getDescription();
          buffer.append( java.net.URLEncoder.encode(filterString) );
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
      ArrayList<Filter> newList = new ArrayList<Filter>( parms.filters );
      if( add ) {
        newList.add( filter );
      }
      else {
        newList.remove( filter );
      }

      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, newList ));
    }

%>

