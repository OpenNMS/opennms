<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.event.*,java.util.*,java.sql.SQLException,org.opennms.web.authenticate.Authentication,org.opennms.web.event.filter.*" %>

<%--
  This page is written to be the display (view) portion of the EventQueryServlet
  at the /event/list URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) events: the list of org.opennms.web.element.Event instances to display
  2) parms: an org.opennms.web.event.EventQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
    //required attributes
    Event[] events = (Event[])request.getAttribute( "events" );
    EventQueryParms parms = (EventQueryParms)request.getAttribute( "parms" );

    if( events == null || parms == null ) {
        throw new ServletException( "Missing either the events or parms request attribute." );
    }

    String action = null;

    if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) {
        action = AcknowledgeEventServlet.ACKNOWLEDGE_ACTION;
    } 
    else if( parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) {
        action = AcknowledgeEventServlet.UNACKNOWLEDGE_ACTION;
    }

    int eventCount = EventFactory.getEventCount( parms.ackType, parms.getFilters() );    
    
    //useful constant strings
    String addPositiveFilterString = "[+]";
    String addNegativeFilterString = "[-]";
    String addBeforeDateFilterString = "[&gt;]";
    String addAfterDateFilterString  = "[&lt;]";    
%>


<html>
<head>
  <title> List | Events | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
  
  <style type="text/css"> 
    a.filterLink { color:black ; text-decoration:none; };
    a.filterLink:visited { color:black ; text-decoration:none; };    
  </style>
  <script language="Javascript" type="text/javascript">
    function checkAllCheckboxes() {
       if( document.acknowledge_form.event.length ) {  
         for( i = 0; i < document.acknowledge_form.event.length; i++ ) {
           document.acknowledge_form.event[i].checked = true
         }
       }
       else {
         document.acknowledge_form.event.checked = true
       }
         
    }
    
    function submitForm(anAction)
    {
        var isChecked = false
        
        if (document.acknowledge_form.event.length)
        {
            for( i = 0; i < document.acknowledge_form.event.length; i++ ) 
            {
              //make sure something is checked before proceeding
              if (document.acknowledge_form.event[i].checked)
              {
                isChecked=true;
              }
            }
            
            if (isChecked)
            {
                document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the events that you would like to " + anAction + ".");
            }
        }
        else
        {
            if (document.acknowledge_form.event.checked)
            {
                document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the events that you would like to " + anAction + ".");
            }
        }
    }

  </script>
</head>

<body marginwidth="0" marginheight="0" leftmargin="0" rightmargin="0" topmargin="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='event/index.jsp' title='Events System Page'>Events</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("List"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event List" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br/>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
      <!-- menu -->
      <a href="<%=this.makeLink( parms, new ArrayList())%>" title="Remove all search constraints" >View all events</a>
      &nbsp;&nbsp;&nbsp;<a href="event/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a>      
      &nbsp;&nbsp;&nbsp;<a href="javascript: void window.open('<%=org.opennms.web.Util.calculateUrlBase(request)%>/event/severity.jsp','', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=no,resizable=yes,directories=no,location=no,width=525,height=158')" title="Open a window explaining the event severities">Severity Legend</a>      
      
      <% if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %> 
        &nbsp;&nbsp;&nbsp;<a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to acknowledge all events in the current search including those not shown on your screen?  (<%=eventCount%> total events)')" title="Acknowledge all events that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a>
      <% } else { %>
        &nbsp;&nbsp;&nbsp;<a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to unacknowledge all events in the current search including those not shown on your screen)?  (<%=eventCount%> total events)')" title="Unacknowledge all events that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a>               
      <% } %>
      <!-- end menu -->      

      <!-- hidden form for acknowledging the result set --> 
      <form action="event/acknowledgeByFilter" method="POST" name="acknowledge_by_filter_form">    
        <input type="hidden" name="redirectParms" value="<%=request.getQueryString()%>" />
        <input type="hidden" name="action" value="<%=action%>" />
        <%=org.opennms.web.Util.makeHiddenTags(request)%>
      </form>      

      
      <table width="100%" border="0" cellspacing="2" cellpadding="0" >
        <tr>
          <td width="50%" valign="top">
            <jsp:include page="/event/querypanel.jsp" flush="false" />
          
            <% if( eventCount > 0 ) { %>
              <% String baseUrl = java.net.URLEncoder.encode(this.makeLink(parms)); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=eventCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.limit%>"      />
                <jsp:param name="multiple" value="<%=parms.multiple%>"   />
              </jsp:include>
            <% } %>          
          </td>
          
          <td width="50%" valign="top">          
            <% if( parms.filters.size() > 0 || parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED || parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
              <% int length = parms.filters.size(); %>

              <p>Current search constraints:
                <ol>                  
                  <% if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
                    <li>
                      event is outstanding
                      &nbsp;&nbsp;
                      <a href="<%=this.makeLink(parms, EventFactory.AcknowledgeType.ACKNOWLEDGED)%>" title="Show acknowledged events"><nobr>[Show acknowledged]</nobr></a>
                    </li>
                  <% } else if( parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
                    <li>
                      event is acknowledged
                      &nbsp;&nbsp;
                      <a href="<%=this.makeLink(parms, EventFactory.AcknowledgeType.UNACKNOWLEDGED)%>" title="Show outstanding events"><nobr>[Show outstanding]</nobr></a>
                    </li>
                  <% } %>            
                
                  <% for( int i=0; i < length; i++ ) { %>
                    <% org.opennms.web.event.filter.Filter filter = (org.opennms.web.event.filter.Filter)parms.filters.get(i); %>
                    
                    <li>
                      <%=filter.getTextDescription()%>
                      &nbsp;&nbsp;
                      <a href="<%=this.makeLink( parms, filter, false)%>" title="Remove filter">[Remove]</a>
                    </li>                    
                  <% } %>
                </ol>
              </p>           
            <% } %>
          </td>
        </tr>
    </table>

    <form action="event/acknowledge" method="POST" name="acknowledge_form">
      <input type="hidden" name="redirectParms" value="<%=request.getQueryString()%>" />
      <input type="hidden" name="action" value="<%=action%>" />
      
      <table width="100%" cellspacing="1" cellpadding="2" border="0" bordercolor="black">
        <tr bgcolor="#999999">
          <td width="1%"><b>Ack</b></td>
          <td width="1%"> <%=this.makeSortLink( parms, EventFactory.SortStyle.ID,        EventFactory.SortStyle.REVERSE_ID,        "id",        "ID" )%></td>
          <td width="10%"><%=this.makeSortLink( parms, EventFactory.SortStyle.SEVERITY,  EventFactory.SortStyle.REVERSE_SEVERITY,  "severity",  "Severity"  )%></td>
          <td width="19%"><%=this.makeSortLink( parms, EventFactory.SortStyle.TIME,      EventFactory.SortStyle.REVERSE_TIME,      "time",      "Time"      )%></td>
          <td width="25%"><%=this.makeSortLink( parms, EventFactory.SortStyle.NODE,      EventFactory.SortStyle.REVERSE_NODE,      "node",      "Node"      )%></td>
          <td width="16%"><%=this.makeSortLink( parms, EventFactory.SortStyle.INTERFACE, EventFactory.SortStyle.REVERSE_INTERFACE, "interface", "Interface" )%></td>
          <td width="15%"><%=this.makeSortLink( parms, EventFactory.SortStyle.SERVICE,   EventFactory.SortStyle.REVERSE_SERVICE,   "service",   "Service"   )%></td>
          <td width="10%"><b>Ackd</b></td>
        </tr>      
      <% for( int i=0; i < events.length; i++ ) { %>        
        <tr valign="top" bgcolor="<%=(i%2 == 0) ? "white" : "#cccccc"%>">
          <td valign="top" rowspan="2" bgcolor="<%=EventUtil.getSeverityColor(events[i].getSeverity())%>">
            <nobr>
              <input type="checkbox" name="event" value="<%=events[i].getId()%>" /> 
            </nobr>
          </td>
          <td valign="top" rowspan="2" bgcolor="<%=EventUtil.getSeverityColor(events[i].getSeverity())%>">
            <a href="event/detail.jsp?id=<%=events[i].getId()%>"><%=events[i].getId()%></a>
          </td>
          
          <td valign="top" rowspan="2" bgcolor="<%=EventUtil.getSeverityColor(events[i].getSeverity())%>">
            <%=EventUtil.getSeverityLabel(events[i].getSeverity())%>
            
            <% org.opennms.web.event.filter.Filter severityFilter = new SeverityFilter(events[i].getSeverity()); %>      
            <% if( !parms.filters.contains( severityFilter )) { %>
              <nobr>
                <a href="<%=this.makeLink( parms, severityFilter, true)%>" class="filterLink" title="Show only events with this severity"><%=addPositiveFilterString%></a>
                <a href="<%=this.makeLink( parms, new NegativeSeverityFilter(events[i].getSeverity()), true)%>" class="filterLink" title="Do not show events with this severity"><%=addNegativeFilterString%></a>
              </nobr>
            <% } %>
          </td>
          <td>
            <nobr><%=org.opennms.netmgt.EventConstants.formatToUIString(events[i].getTime())%></nobr>
            <nobr>
              <a href="<%=this.makeLink( parms, new AfterDateFilter(events[i].getTime()), true)%>"  class="filterLink" title="Only show events occurring after this one"><%=addAfterDateFilterString%></a>            
              <a href="<%=this.makeLink( parms, new BeforeDateFilter(events[i].getTime()), true)%>" class="filterLink" title="Only show events occurring before this one"><%=addBeforeDateFilterString%></a>
            </nobr>
          </td>
          <td>
            <% if(events[i].getNodeId() != 0 ) { %>
              <% org.opennms.web.event.filter.Filter nodeFilter = new NodeFilter(events[i].getNodeId()); %>             
              <% String[] labels = this.getNodeLabels( events[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=events[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only events on this node"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeNodeFilter(events[i].getNodeId()), true)%>" class="filterLink" title="Do not show events for this node"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td>
            <% if(events[i].getIpAddress() != null ) { %>
              <% org.opennms.web.event.filter.Filter intfFilter = new InterfaceFilter(events[i].getIpAddress()); %>
              <% if( events[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=events[i].getNodeId()%>&intf=<%=events[i].getIpAddress()%>" title="More info on this interface"><%=events[i].getIpAddress()%></a>
              <% } else { %>
                 <%=events[i].getIpAddress()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only events on this IP address"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeInterfaceFilter(events[i].getIpAddress()), true)%>" class="filterLink" title="Do not show events for this interface"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td>
            <% if(events[i].getServiceName() != null) { %>
              <% org.opennms.web.event.filter.Filter serviceFilter = new ServiceFilter(events[i].getServiceId()); %>
              <% if( events[i].getNodeId() != 0 && events[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=events[i].getNodeId()%>&intf=<%=events[i].getIpAddress()%>&service=<%=events[i].getServiceId()%>" title="More info on this service"><%=events[i].getServiceName()%></a>
              <% } else { %>
                <%=events[i].getServiceName()%>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only events with this service type"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeServiceFilter(events[i].getServiceId()), true)%>" class="filterLink" title="Do not show events for this service"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>                            
            <% } else { %>
              &nbsp;
            <% } %>
          </td>          
          <td>
            <% if (events[i].isAcknowledged()) { %>
              <% org.opennms.web.event.filter.Filter acknByFilter = new AcknowledgedByFilter(events[i].getAcknowledgeUser()); %>      
              <%=events[i].getAcknowledgeUser()%>
              <% if( !parms.filters.contains( acknByFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, acknByFilter, true)%>" class="filterLink" title="Show only events with this acknowledged by user"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeAcknowledgedByFilter(events[i].getAcknowledgeUser()), true)%>" class="filterLink" title="Do not show events acknowledgd by this user"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>              
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
        </tr>
        
        <tr valign="top" bgcolor="<%=(i%2 == 0) ? "white" : "#cccccc"%>">
          <td colspan="4"><%=events[i].getLogMessage()%></td>
          <td valign="top">
            <%=events[i].isAcknowledged() ? org.opennms.netmgt.EventConstants.formatToUIString(events[i].getAcknowledgeTime()) : "&nbsp;"%>
          </td>
        </tr>
       
      <% } /*end for*/%>
      
        <tr>
          <td colspan="2"><%=events.length%> events</td>
          <td colspan="6">
          <% if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
            <input type="button" value="Acknowledge Events" onClick="submitForm('acknowledge')"/>
            <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
            <input TYPE="reset" />
          <% } else if( parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
            <input type="button" value="Unacknowledge Events" onClick="submitForm('unacknowledge')"/>
            <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
            <input TYPE="reset" />
          <% } %>
          </td>
        </tr>
      </table>
      </form>

      <%--<br>
      <% if(request.isUserInRole(Authentication.ADMIN_ROLE)) { %>
        <a HREF="admin/events.jsp" title="Acknowledge or Unacknowledge All Events">[Acknowledge or Unacknowledge All Events]</a>
      <% } %>--%>

    </td>
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/bookmark.jsp" flush="false" />
<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>


<%!
    String urlBase = "event/list";

    protected String makeSortLink( EventQueryParms parms, EventFactory.SortStyle style, EventFactory.SortStyle revStyle, String sortString, String title ) {
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

      buffer.append( "<font color=\"black\"><b>" );
      buffer.append( title );
      buffer.append( "</b></font></a>" );
      buffer.append( "</nobr>" );

      return( buffer.toString() );
    }

    
    public String getFiltersAsString(ArrayList filters ) {
        StringBuffer buffer = new StringBuffer();
    
        if( filters != null ) {
            for( int i=0; i < filters.size(); i++ ) {
                buffer.append( "&filter=" );
                String filterString = EventUtil.getFilterString((org.opennms.web.event.filter.Filter)filters.get(i));
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }      
    
        return( buffer.toString() );
    }

    public String makeLink( EventFactory.SortStyle sortStyle, EventFactory.AcknowledgeType ackType, ArrayList filters, int limit ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( EventUtil.getSortStyleString(sortStyle) );
      buffer.append( "&acktype=" );
      buffer.append( EventUtil.getAcknowledgeTypeString(ackType) );
      buffer.append( "&limit=" );
      buffer.append( limit );
      buffer.append( this.getFiltersAsString(filters) );

      return( buffer.toString() );
    }


    public String makeLink( EventQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, EventFactory.SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, EventFactory.AcknowledgeType ackType ) {
      return( this.makeLink( parms.sortStyle, ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, ArrayList filters ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, org.opennms.web.event.filter.Filter filter, boolean add ) {
      ArrayList newList = new ArrayList( parms.filters );
      if( add ) {
        newList.add( filter );
      }
      else {
        newList.remove( filter );
      }

      return( this.makeLink( parms.sortStyle, parms.ackType, newList, parms.limit ));
    }


    public String[] getNodeLabels( String nodeLabel ) {
        String[] labels = null;

        if( nodeLabel.length() > 32 ) {
            String shortLabel = nodeLabel.substring( 0, 31 ) + "...";                        
            labels = new String[] { shortLabel, nodeLabel };
        }
        else {
            labels = new String[] { nodeLabel, nodeLabel };
        }

        return( labels );
    }

%>


