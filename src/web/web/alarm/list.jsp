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
// Modifications:
//
// 2004 Feb 11: remove the extra 'limit' parameter in the base URL.
// 2003 Sep 04: Added a check to allow for deleted node alarms to display.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 10: Removed the "http://" from UEIs and references to bluebird.
// 2002 Nov 09: Removed borders around alarms.
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
//      http://www.opennms.com///

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.alarm.*,java.util.*,java.sql.SQLException,org.opennms.web.authenticate.Authentication,org.opennms.web.alarm.filter.*" %>

<%--
  This page is written to be the display (view) portion of the AlarmQueryServlet
  at the /alarm/list URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) alarms: the list of org.opennms.web.element.Alarm instances to display
  2) parms: an org.opennms.web.alarm.AlarmQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
    //required attributes
    Alarm[] alarms = (Alarm[])request.getAttribute( "alarms" );
    AlarmQueryParms parms = (AlarmQueryParms)request.getAttribute( "parms" );

    if( alarms == null || parms == null ) {
	throw new ServletException( "Missing either the alarms or parms request attribute." );
    }

    String action = null;

    if( parms.ackType == AlarmFactory.AcknowledgeType.UNACKNOWLEDGED ) {
        action = AcknowledgeAlarmServlet.ACKNOWLEDGE_ACTION;
    } 
    else if( parms.ackType == AlarmFactory.AcknowledgeType.ACKNOWLEDGED ) {
        action = AcknowledgeAlarmServlet.UNACKNOWLEDGE_ACTION;
    }

    int alarmCount = AlarmFactory.getAlarmCount( parms.ackType, parms.getFilters() );    
    
    //useful constant strings
    String addPositiveFilterString = "[+]";
    String addNegativeFilterString = "[-]";
    String addBeforeDateFilterString = "[&gt;]";
    String addAfterDateFilterString  = "[&lt;]";    
%>


<html>
<head>
  <title> List | Alarms | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
  
  <style type="text/css"> 
    a.filterLink { color:black ; text-decoration:none; };
    a.filterLink:visited { color:black ; text-decoration:none; };    
  </style>
  <script language="Javascript" type="text/javascript">
    function checkAllCheckboxes() {
       if( document.acknowledge_form.alarm.length ) {  
         for( i = 0; i < document.acknowledge_form.alarm.length; i++ ) {
           document.acknowledge_form.alarm[i].checked = true
         }
       }
       else {
         document.acknowledge_form.alarm.checked = true
       }
         
    }
    
    function submitForm(anAction)
    {
        var isChecked = false
        var numChecked = 0;
 
        if (document.acknowledge_form.alarm.length)
        {
            for( i = 0; i < document.acknowledge_form.alarm.length; i++ ) 
            {
              //make sure something is checked before proceeding
              if (document.acknowledge_form.alarm[i].checked)
              {
                isChecked=true;
                numChecked+=1;
              }
            }
            
            if (isChecked && document.acknowledge_form.multiple)
            {
              if (numChecked == parseInt(document.acknowledge_form.alarm.length)) 
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
                alert("Please check the alarms that you would like to " + anAction + ".");
            }
        }
        else
        {
            if (document.acknowledge_form.alarm.checked)
            {
                document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the alarms that you would like to " + anAction + ".");
            }
        }
    }

  </script>
</head>

<body marginwidth="0" marginheight="0" leftmargin="0" rightmargin="0" topmargin="0">

<% String breadcrumb1 = "<a href= 'alarm/index.jsp' title='Alarms System Page'>Alarms</a>"; %>
<% String breadcrumb2 = "List"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarm List" />
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
      <a href="<%=this.makeLink( parms, new ArrayList())%>" title="Remove all search constraints" >View all alarms</a>
      &nbsp;&nbsp;&nbsp;<a href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a>      
      &nbsp;&nbsp;&nbsp;<a href="javascript: void window.open('<%=org.opennms.web.Util.calculateUrlBase(request)%>/alarm/severity.jsp','', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=no,resizable=yes,directories=no,location=no,width=525,height=158')" title="Open a window explaining the alarm severities">Severity Legend</a>      
      
      <% if( parms.ackType == AlarmFactory.AcknowledgeType.UNACKNOWLEDGED ) { %> 
        &nbsp;&nbsp;&nbsp;<a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to acknowledge all alarms in the current search including those not shown on your screen?  (<%=alarmCount%> total alarms)')" title="Acknowledge all alarms that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a>
      <% } else { %>
        &nbsp;&nbsp;&nbsp;<a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to unacknowledge all alarms in the current search including those not shown on your screen)?  (<%=alarmCount%> total alarms)')" title="Unacknowledge all alarms that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a>               
      <% } %>
      <!-- end menu -->      

      <!-- hidden form for acknowledging the result set --> 
      <form action="alarm/acknowledgeByFilter" method="POST" name="acknowledge_by_filter_form">    
        <input type="hidden" name="redirectParms" value="<%=request.getQueryString()%>" />
        <input type="hidden" name="action" value="<%=action%>" />
        <%=org.opennms.web.Util.makeHiddenTags(request)%>
      </form>      

      
      <table width="100%" border="0" cellspacing="2" cellpadding="0" >
        <tr>
          <td width="50%" valign="top">
            <jsp:include page="/alarm/querypanel.jsp" flush="false" />
          
            <% if( alarmCount > 0 ) { %>
              <% String baseUrl = this.makeLink(parms); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=alarmCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.limit%>"      />
                <jsp:param name="multiple" value="<%=parms.multiple%>"   />
              </jsp:include>
            <% } %>          
          </td>
          
          <td width="50%" valign="top">          
            <% if( parms.filters.size() > 0 || parms.ackType == AlarmFactory.AcknowledgeType.UNACKNOWLEDGED || parms.ackType == AlarmFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
              <% int length = parms.filters.size(); %>

              <p>Current search constraints:
                <ol>                  
                  <% if( parms.ackType == AlarmFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
                    <li>
                      alarm is outstanding
                      &nbsp;&nbsp;
                      <a href="<%=this.makeLink(parms, AlarmFactory.AcknowledgeType.ACKNOWLEDGED)%>" title="Show acknowledged alarms"><nobr>[Show acknowledged]</nobr></a>
                    </li>
                  <% } else if( parms.ackType == AlarmFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
                    <li>
                      alarm is acknowledged
                      &nbsp;&nbsp;
                      <a href="<%=this.makeLink(parms, AlarmFactory.AcknowledgeType.UNACKNOWLEDGED)%>" title="Show outstanding alarms"><nobr>[Show outstanding]</nobr></a>
                    </li>
                  <% } %>            
                
                  <% for( int i=0; i < length; i++ ) { %>
                    <% org.opennms.web.alarm.filter.Filter filter = (org.opennms.web.alarm.filter.Filter)parms.filters.get(i); %>
                    
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

    <form action="alarm/acknowledge" method="POST" name="acknowledge_form">
      <input type="hidden" name="redirectParms" value="<%=request.getQueryString()%>" />
      <input type="hidden" name="action" value="<%=action%>" />
      <%=org.opennms.web.Util.makeHiddenTags(request)%>

      <table width="100%" cellspacing="1" cellpadding="2" border="0" bordercolor="black">
        <tr bgcolor="#999999">
          <% if ( parms.ackType == AlarmFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
          <td width="1%"><b>Ack</b></td>
          <% } else { %>
          <td width="1%"><b>UnAck</b></td>
          <% } %>
          <td width="1%"> <%=this.makeSortLink( parms, AlarmFactory.SortStyle.ID,        AlarmFactory.SortStyle.REVERSE_ID,        "id",        "ID" )%></td>
          <td width="10%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.SEVERITY,  AlarmFactory.SortStyle.REVERSE_SEVERITY,  "severity",  "Severity"  )%></td>
          <td width="25%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.NODE,      AlarmFactory.SortStyle.REVERSE_NODE,      "node",      "Node"      )%></td>
          <td width="16%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.INTERFACE, AlarmFactory.SortStyle.REVERSE_INTERFACE, "interface", "Interface" )%></td>
          <td width="15%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.SERVICE,   AlarmFactory.SortStyle.REVERSE_SERVICE,   "service",   "Service"   )%></td>
          <td width="10%"><b>Ackd</b></td>
        </tr>      
        <tr bgcolor="#999999">
          <td width="1%">&nbsp;</td>
          <td width="1%">&nbsp;</td>
          <td width="1%">&nbsp;</td>
          <td width="19%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.COUNT,  AlarmFactory.SortStyle.REVERSE_COUNT,  "count",  "Count"  )%></td>
          <td width="30%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.LASTEVENTTIME,  AlarmFactory.SortStyle.REVERSE_LASTEVENTTIME,  "lasteventtime",  "Last Event Time"  )%></td>
          <td width="30%"><%=this.makeSortLink( parms, AlarmFactory.SortStyle.FIRSTEVENTTIME,  AlarmFactory.SortStyle.REVERSE_FIRSTEVENTTIME,  "firsteventtime",  "First Event Time"  )%></td>
          <td width="1%">&nbsp;</td>
        </tr>
      <% for( int i=0; i < alarms.length; i++ ) { %>        
        <tr valign="top" bgcolor="<%=(i%2 == 0) ? "white" : "#cccccc"%>">
          <td valign="top" rowspan="3" bgcolor="<%=AlarmUtil.getSeverityColor(alarms[i].getSeverity())%>">
            <nobr>
              <input type="checkbox" name="alarm" value="<%=alarms[i].getId()%>" /> 
            </nobr>
          </td>
          <td valign="top" rowspan="3" bgcolor="<%=AlarmUtil.getSeverityColor(alarms[i].getSeverity())%>">
            <a href="alarm/detail.jsp?id=<%=alarms[i].getId()%>"><%=alarms[i].getId()%></a>
          </td>
          
          <td valign="top" rowspan="3" bgcolor="<%=AlarmUtil.getSeverityColor(alarms[i].getSeverity())%>">
            <%=AlarmUtil.getSeverityLabel(alarms[i].getSeverity())%>
            
            <% org.opennms.web.alarm.filter.Filter severityFilter = new SeverityFilter(alarms[i].getSeverity()); %>      
            <% if( !parms.filters.contains( severityFilter )) { %>
              <nobr>
                <a href="<%=this.makeLink( parms, severityFilter, true)%>" class="filterLink" title="Show only alarms with this severity"><%=addPositiveFilterString%></a>
                <a href="<%=this.makeLink( parms, new NegativeSeverityFilter(alarms[i].getSeverity()), true)%>" class="filterLink" title="Do not show alarms with this severity"><%=addNegativeFilterString%></a>
              </nobr>
            <% } %>
          </td>
          <td>
	    <% if(alarms[i].getNodeId() != 0 && alarms[i].getNodeLabel()!= null ) { %>
              <% org.opennms.web.alarm.filter.Filter nodeFilter = new NodeFilter(alarms[i].getNodeId()); %>             
              <% String[] labels = this.getNodeLabels( alarms[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=alarms[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only alarms on this node"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeNodeFilter(alarms[i].getNodeId()), true)%>" class="filterLink" title="Do not show alarms for this node"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td>
            <% if(alarms[i].getIpAddress() != null ) { %>
              <% org.opennms.web.alarm.filter.Filter intfFilter = new InterfaceFilter(alarms[i].getIpAddress()); %>
              <% if( alarms[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=alarms[i].getNodeId()%>&intf=<%=alarms[i].getIpAddress()%>" title="More info on this interface"><%=alarms[i].getIpAddress()%></a>
              <% } else { %>
                 <%=alarms[i].getIpAddress()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only alarms on this IP address"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeInterfaceFilter(alarms[i].getIpAddress()), true)%>" class="filterLink" title="Do not show alarms for this interface"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td>
            <% if(alarms[i].getServiceName() != null) { %>
              <% org.opennms.web.alarm.filter.Filter serviceFilter = new ServiceFilter(alarms[i].getServiceId()); %>
              <% if( alarms[i].getNodeId() != 0 && alarms[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=alarms[i].getNodeId()%>&intf=<%=alarms[i].getIpAddress()%>&service=<%=alarms[i].getServiceId()%>" title="More info on this service"><%=alarms[i].getServiceName()%></a>
              <% } else { %>
                <%=alarms[i].getServiceName()%>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only alarms with this service type"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeServiceFilter(alarms[i].getServiceId()), true)%>" class="filterLink" title="Do not show alarms for this service"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>                            
            <% } else { %>
              &nbsp;
            <% } %>
          </td>          
          <td>
            <% if (alarms[i].isAcknowledged()) { %>
              <% org.opennms.web.alarm.filter.Filter acknByFilter = new AcknowledgedByFilter(alarms[i].getAcknowledgeUser()); %>      
              <%=alarms[i].getAcknowledgeUser()%>
              <% if( !parms.filters.contains( acknByFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, acknByFilter, true)%>" class="filterLink" title="Show only alarms with this acknowledged by user"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeAcknowledgedByFilter(alarms[i].getAcknowledgeUser()), true)%>" class="filterLink" title="Do not show alarms acknowledgd by this user"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>              
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
        </tr>
	<tr valign="top" bgcolor="<%=(i%2 == 0) ? "white" : "#cccccc"%>">
          <td valign="top" rowspan="1" >
            <%=alarms[i].getCount()%>
          </td>
          <td>
            <nobr><%=org.opennms.netmgt.EventConstants.formatToUIString(alarms[i].getLastEventTime())%></nobr>
            <nobr>
              <a href="<%=this.makeLink( parms, new AfterLastEventTimeFilter(alarms[i].getLastEventTime()), true)%>"  class="filterLink" title="Only show alarms occurring after this one"><%=addAfterDateFilterString%></a>            
              <a href="<%=this.makeLink( parms, new BeforeLastEventTimeFilter(alarms[i].getLastEventTime()), true)%>" class="filterLink" title="Only show alarms occurring before this one"><%=addBeforeDateFilterString%></a>
            </nobr>
          </td>
          <td>
            <nobr><%=org.opennms.netmgt.EventConstants.formatToUIString(alarms[i].getFirstEventTime())%></nobr>
            <nobr>
              <a href="<%=this.makeLink( parms, new AfterFirstEventTimeFilter(alarms[i].getFirstEventTime()), true)%>"  class="filterLink" title="Only show alarms occurring after this one"><%=addAfterDateFilterString%></a>            
              <a href="<%=this.makeLink( parms, new BeforeFirstEventTimeFilter(alarms[i].getFirstEventTime()), true)%>" class="filterLink" title="Only show alarms occurring before this one"><%=addBeforeDateFilterString%></a>
            </nobr>
          </td>
	  <td>&nbsp;</td>
	</tr>
        
        <tr valign="top" bgcolor="<%=(i%2 == 0) ? "white" : "#cccccc"%>">
          <td valign="top" colspan="3"><%=alarms[i].getLogMessage()%></td>
          <td valign="top">
            <%=alarms[i].isAcknowledged() ? org.opennms.netmgt.EventConstants.formatToUIString(alarms[i].getAcknowledgeTime()) : "&nbsp;"%>
          </td>
        </tr>
       
      <% } /*end for*/%>
      
        <tr>
          <td colspan="3"><%=alarms.length%> alarms</td>
          <td colspan="6">
          <% if( parms.ackType == AlarmFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
            <input type="button" value="Acknowledge Alarms" onClick="submitForm('acknowledge')"/>
            <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
            <input TYPE="reset" />
          <% } else if( parms.ackType == AlarmFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
            <input type="button" value="Unacknowledge Alarms" onClick="submitForm('unacknowledge')"/>
            <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
            <input TYPE="reset" />
          <% } %>
          </td>
        </tr>
      </table>
      </form>

      <%--<br>
      <% if(request.isUserInRole(Authentication.ADMIN_ROLE)) { %>
        <a HREF="admin/alarms.jsp" title="Acknowledge or Unacknowledge All Alarms">[Acknowledge or Unacknowledge All Alarms]</a>
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
    String urlBase = "alarm/list";

    protected String makeSortLink( AlarmQueryParms parms, AlarmFactory.SortStyle style, AlarmFactory.SortStyle revStyle, String sortString, String title ) {
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
                String filterString = AlarmUtil.getFilterString((org.opennms.web.alarm.filter.Filter)filters.get(i));
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }      
    
        return( buffer.toString() );
    }

    public String makeLink( AlarmFactory.SortStyle sortStyle, AlarmFactory.AcknowledgeType ackType, ArrayList filters, int limit ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( AlarmUtil.getSortStyleString(sortStyle) );
      buffer.append( "&acktype=" );
      buffer.append( AlarmUtil.getAcknowledgeTypeString(ackType) );
      buffer.append( this.getFiltersAsString(filters) );

      return( buffer.toString() );
    }


    public String makeLink( AlarmQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( AlarmQueryParms parms, AlarmFactory.SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( AlarmQueryParms parms, AlarmFactory.AcknowledgeType ackType ) {
      return( this.makeLink( parms.sortStyle, ackType, parms.filters, parms.limit) );
    }


    public String makeLink( AlarmQueryParms parms, ArrayList filters ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, filters, parms.limit) );
    }


    public String makeLink( AlarmQueryParms parms, org.opennms.web.alarm.filter.Filter filter, boolean add ) {
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


