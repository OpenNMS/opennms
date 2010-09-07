<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
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
                org.opennms.web.acegisecurity.Authentication,
		org.opennms.web.element.*,
		java.util.*,
		java.sql.SQLException,
		org.opennms.web.event.*,
		org.opennms.web.event.filter.*,
		org.opennms.web.WebSecurityUtils
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
<%!
   public String getBgColor(Notification n) {
	   String bgcolor="#cccccc";
	   try {
		return EventUtil.getSeverityColor(EventFactory.getEvent(n.getEventId()).getSeverity());
	   } catch (Exception e) {
	   	return bgcolor;
	   }
   }
%>

<%
    //required attributes
    Notification[] notices = (Notification[])request.getAttribute( "notices" );
    NoticeQueryParms parms = (NoticeQueryParms)request.getAttribute( "parms" );

    if( notices == null || parms == null ) {
        throw new ServletException( "Missing either the notices or parms request attribute." );
    }

    String action = null;

    if( parms.ackType == NoticeFactory.AcknowledgeType.UNACKNOWLEDGED ) {
        action = "1";
    } 
    else if( parms.ackType == NoticeFactory.AcknowledgeType.ACKNOWLEDGED ) {
        action = "2";
    }

    int noticeCount = NoticeFactory.getNoticeCount( parms.ackType, parms.getFilters() );
    HashMap nodeLabelMap = new HashMap();
    
    //useful constant strings
    String addPositiveFilterString = "[+]";    
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notice List" />
  <jsp:param name="headTitle" value="Browse" />
  <jsp:param name="headTitle" value="Notices" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp' title='Notice System Page'>Notices</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<script language="Javascript" type="text/javascript" >
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
<% if( parms.ackType == NoticeFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
  <p>
    Currently showing only <strong>outstanding</strong> notices.
    <a href="<%=this.makeLink(parms, NoticeFactory.AcknowledgeType.ACKNOWLEDGED)%>" title="Show acknowledged notices">[Show acknowledged]</a>
  </p>
<% } else if( parms.ackType == NoticeFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
  <p>
    Currently showing only <strong>acknowledged</strong> notices.  
    <a href="<%=this.makeLink(parms, NoticeFactory.AcknowledgeType.UNACKNOWLEDGED)%>" title="Show outstanding notices">[Show outstanding]</a>
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
		<span class="filter"><% NoticeFactory.Filter filter = (NoticeFactory.Filter)parms.filters.get(i); %>
				<%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%> <a href="<%=this.makeLink( parms, filter, false)%>" title="Remove filter">[-]</a></span> &nbsp; 
      <% } %>
    &mdash; <a href="<%=this.makeLink( parms, new ArrayList())%>" title="Remove all filters">[Remove all]</a>
  </p>
<% } %>
	<jsp:include page="/includes/key.jsp" flush="false" />
        <form action="notification/acknowledge" method="post" name="acknowledge_form">
          <input type="hidden" name="redirectParms" value="<%=org.opennms.web.Util.htmlify(request.getQueryString())%>" />
          <%=org.opennms.web.Util.makeHiddenTags(request)%>        
	<!--			<% if( parms.ackType == NoticeFactory.AcknowledgeType.UNACKNOWLEDGED &&  !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
          <p><input TYPE="reset" />
						<input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
						<input type="button" value="Acknowledge Notices" onClick="submitAcknowledge()"/>
          </p>
        <% } %> -->
      <table>
			<thead>
			  <tr>
          <th class="noWrap"><%=this.makeSortLink( parms, NoticeFactory.SortStyle.ID,NoticeFactory.SortStyle.REVERSE_ID,     "id",          "ID"           )%></th>
          <th class="noWrap">Event ID</th>
          <th>Severity</th>
          <th><%=this.makeSortLink( parms, NoticeFactory.SortStyle.PAGETIME,    NoticeFactory.SortStyle.REVERSE_PAGETIME,    "pagetime",    "Sent Time"    )%></th>
          <th><%=this.makeSortLink( parms, NoticeFactory.SortStyle.RESPONDER,   NoticeFactory.SortStyle.REVERSE_RESPONDER,   "asweredby",   "Responder"    )%></th>
          <th><%=this.makeSortLink( parms, NoticeFactory.SortStyle.RESPONDTIME, NoticeFactory.SortStyle.REVERSE_RESPONDTIME, "respondtime", "Respond Time" )%></th>  
          <th><%=this.makeSortLink( parms, NoticeFactory.SortStyle.NODE,        NoticeFactory.SortStyle.REVERSE_NODE,        "node",        "Node"         )%></th>
          <th><%=this.makeSortLink( parms, NoticeFactory.SortStyle.INTERFACE,   NoticeFactory.SortStyle.REVERSE_INTERFACE,   "interface",   "Interface"    )%></th>
          <th><%=this.makeSortLink( parms, NoticeFactory.SortStyle.SERVICE,     NoticeFactory.SortStyle.REVERSE_SERVICE,     "service",     "Service"      )%></th>
        </tr>
      </thead>

      <% for( int i=0; i < notices.length; i++ ) { 
        Event event = EventFactory.getEvent( notices[i].getEventId() );
        int severity = (event == null? 0 : event.getSeverity());
        String eventSeverity = EventUtil.getSeverityLabel(severity);
        %>
        <tr class="<%=eventSeverity%>">
          <td class="divider noWrap" rowspan="2"><% if((parms.ackType == NoticeFactory.AcknowledgeType.UNACKNOWLEDGED ) && 
		!(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
            <input type="checkbox" name="notices" value="<%=notices[i].getId()%>" />
          <% } %> 
						<a href="notification/detail.jsp?notice=<%=notices[i].getId()%>"><%=notices[i].getId()%></a></td>
          <td class="divider" rowspan="2">
            <% if ( NoticeFactory.canDisplayEvent(notices[i].getEventId()) ) { %>
            <a href="event/detail.jsp?id=<%=notices[i].getEventId()%>"><%=notices[i].getEventId()%></a>
            <% } %>
          </td>
          <td class="bright divider" rowspan="2"><%=eventSeverity%></td>
          <td class="divider"><%=org.opennms.netmgt.EventConstants.formatToUIString(notices[i].getTimeSent())%></td>
          <td class="divider"><% NoticeFactory.Filter responderFilter = new NoticeFactory.ResponderFilter(notices[i].getResponder()); %>      
            <% if(notices[i].getResponder()!=null) {%>
              <%=notices[i].getResponder()%>
              <% if( !parms.filters.contains( responderFilter )) { %>
                <a href="<%=this.makeLink( parms, responderFilter, true)%>" class="filterLink" title="Show only notices with this responder"><%=addPositiveFilterString%></a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <%if (notices[i].getTimeReplied()!=null) { %>
              <%=org.opennms.netmgt.EventConstants.formatToUIString(notices[i].getTimeReplied())%>
            <% } %>
					</td>
          <td class="divider">
            <% if(notices[i].getNodeId() != 0 ) { %>
              <% NoticeFactory.Filter nodeFilter = new NoticeFactory.NodeFilter(notices[i].getNodeId()); %>
              <% String[] labels = this.getNodeLabels( notices[i].getNodeId(), nodeLabelMap ); %>
              <a href="element/node.jsp?node=<%=notices[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only notices on this node"><%=addPositiveFilterString%></a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notices[i].getIpAddress() != null ) { %>
              <% NoticeFactory.Filter intfFilter = new NoticeFactory.InterfaceFilter(notices[i].getIpAddress()); %>
              <% if( notices[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>" title="More info on this interface"><%=notices[i].getIpAddress()%></a>
              <% } else { %>
                 <%=notices[i].getInterfaceId()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only notices on this IP address"><%=addPositiveFilterString%></a>
              <% } %>
            <% } %>
          </td>
          <td class="divider">
            <% if(notices[i].getServiceName() != null) { %>
              <% NoticeFactory.Filter serviceFilter = new NoticeFactory.ServiceFilter(notices[i].getServiceId()); %>
              <% if( notices[i].getNodeId() != 0 && notices[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>&service=<%=notices[i].getServiceId()%>" title="More info on this service"><%=notices[i].getServiceName()%></a>
              <% } else { %>
                <%=notices[i].getServiceName()%>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only notices with this service type"><%=addPositiveFilterString%></a>
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

        <% if( parms.ackType == NoticeFactory.AcknowledgeType.UNACKNOWLEDGED &&  !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
            <input TYPE="reset" />
            <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
            <input type="button" value="Acknowledge Notices" onClick="submitAcknowledge()"/>
        <% } %>
        
	</p>
	<!--		<% if( noticeCount > 0 ) { %>
			<p align="right"><a href="<%=this.makeLink(parms)%>&multiple=<%=parms.multiple+1%>">Next</a></p>
			<% } %> -->
		</form>
<jsp:include page="/includes/footer.jsp" flush="false" />


<%!
    String urlBase = "notification/browse";

    protected String makeSortLink( NoticeQueryParms parms, NoticeFactory.SortStyle style, NoticeFactory.SortStyle revStyle, String sortString, String title ) {
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

    public String makeLink( NoticeFactory.SortStyle sortStyle, NoticeFactory.AcknowledgeType ackType, int limit, List filters ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( NoticeUtil.getSortStyleString(sortStyle) );
      buffer.append( "&acktype=" );
      buffer.append( NoticeUtil.getAcknowledgeTypeString(ackType) );
      if (limit > 0) {
          buffer.append( "&limit=" ).append(limit);
      }

      if( filters != null ) {
        for( int i=0; i < filters.size(); i++ ) {
          buffer.append( "&filter=" );
          String filterString = NoticeUtil.getFilterString((NoticeFactory.Filter)filters.get(i));
          buffer.append( java.net.URLEncoder.encode(filterString) );
        }
      }      

      return( buffer.toString() );
    }


    public String makeLink( NoticeQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, parms.filters) );
    }


    public String makeLink( NoticeQueryParms parms, NoticeFactory.SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.limit, parms.filters) );
    }


    public String makeLink( NoticeQueryParms parms, NoticeFactory.AcknowledgeType ackType ) {
      return( this.makeLink( parms.sortStyle, ackType, parms.limit, parms.filters) );
    }


    public String makeLink( NoticeQueryParms parms, List filters ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, filters) );
    }


    public String makeLink( NoticeQueryParms parms, NoticeFactory.Filter filter, boolean add ) {
      ArrayList newList = new ArrayList( parms.filters );
      if( add ) {
        newList.add( filter );
      }
      else {
        newList.remove( filter );
      }

      return( this.makeLink( parms.sortStyle, parms.ackType, parms.limit, newList ));
    }


    public String[] getNodeLabels( int nodeId, Map labelMap ) throws SQLException {
        Integer nodeInt = new Integer( nodeId ); 
        String[] labels = (String[])labelMap.get( nodeInt );

        if( labels == null ) {
            String longLabel = NetworkElementFactory.getNodeLabel( nodeId );

            if( longLabel == null ) {
                //when they finally get the "not null" added to the nodelabel column,
                //this should never happen, but until then...
                labels = new String[] { "&lt;No Node Label&gt;", "&lt;No Node Label&gt;" };
            }
            else {
                if( longLabel.length() > 32 ) {
                    String shortLabel = longLabel.substring( 0, 31 ) + "...";                        
                    labels = new String[] { shortLabel, longLabel };
                }
                else {
                    labels = new String[] { longLabel, longLabel };
                }

                labelMap.put( nodeInt, labels );
            }
        }

        return( labels );
    }
%>
