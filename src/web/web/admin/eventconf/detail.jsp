<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType = "text/html" session = "true" import="java.util.*,java.net.*,org.opennms.netmgt.config.EventconfFactory,org.opennms.netmgt.xml.eventconf.Event" %>

<%
  //init method
  EventconfFactory eventFactory = EventconfFactory.getInstance();
  List event = eventFactory.getEvents((String)request.getParameter( "uei" ));
%>

<html>
<head>
  <title>Event Details | Event Config | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("admin/index.jsp") + "'> Admin </a>"; %>
<% String breadcrumb2 = "<a href='" + java.net.URLEncoder.encode("admin/eventconf/index.jsp") + "'> Event Configuration </a>"; %>
<% String breadcrumb3 = "Details"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event Details" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>

<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>
    
      <td width="100%" valign="top" >
          
          <br>
          
          <!-- uei -->
          <table>
            <tr>
              <td width="10%"> <h3>UEI </h3> </td>
              <td><%=event.getUei()%></td>
            </tr>
          </table>
          
          <br>
          
          <!-- description -->
          <h3> Description </h3>
          <table>
            <tr>
              <td><%=event.getDescription()%></td>
            </tr>
          </table>
          
          <br>
          
          <!-- log message and destination -->
          <h3> Log Message </h3>
          <table>
            <tr>
              <td valign="top"><h4>Message:</h4></td>
              <td><%=event.getLogMessage()%></td>
            </tr>
            <tr>
              <td valign="top"><h4> Destination:</h4></td>
              <td><%=event.getLogMessageDestination()%> </td>
            </tr>
          </table>
          
          <br>
          
          <!-- severity -->
          <table>
            <tr>
              <td witdh="10%"> <h3> Severity </h3> </td>
              <td><%=event.getSeverity()%></td>
            </tr>
          </table>
          
          <br>
          
          <!-- mask information -->
          <% List maskElements = event.getMask();
             if (maskElements.size() > 0)
             {
          %>
          <h3> Mask </h3>
             <h4> Mask Elements </h4>
             <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <th width="10%"> Name </th>
              <th> Value </th>
              <tr>
                <% for (int i = 0; i < maskElements.size(); i++)
                   {
                      MaskElement element = (MaskElement)maskElements.get(i);
                      List values = element.getElementValues();
                %>
                      <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                        <td width="10%"  valign="top" rowspan="<%=values.size()%>"><%=element.getElementName()%>: </td>
                        <td> <%=(String)values.get(0)%></td></tr>
                        <%
                          for (int j = 1; j < values.size(); j++) 
                          {
                        %>
                            <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                              <td><%=(String)values.get(j)%></td>
                            </tr>
                        <%} /*end for loop*/ %>
                      </tr>
                 <%} /* end for loop*/ %>
             </tr>
            </table>
         <br>
           <%} /* end if */ %>
          
          <!-- snmp information -->
          <%if (event.getSnmp() != null)
              {
                Snmp snmp = event.getSnmp();
            %>
            <h3> SNMP </h3>
            <table>
              <tr>
                <td width="10%" valign="top"> ID: </td>
                <td><%=snmp.getId()%></td>
              </tr>
                <%if (snmp.getIdText() != null && !snmp.getIdText().trim().equals("")) {
                %>
                <tr>
                  <td width="10%" valign="top"> ID Text:</td>
                  <td><%=snmp.getIdText()%></td>
                </tr>
                <%} /* end if */ %>
              <tr>
                <td width="10%" valign="top"> Version: </td>
                <td><%=snmp.getVersion()%></td>
              </tr>
              <tr>
                <%if (snmp.getSpecific() != null && !snmp.getSpecific().trim().equals("")) {
                %>
                <tr>
                  <td width="10%" valign="top"> Specific: </td>
                  <td><%=snmp.getSpecific()%></td>
                </tr>
                <%} /* end if */ %>
                <%if (snmp.getGeneric() != null && !snmp.getGeneric().trim().equals("")) {
                %>
                <tr>
                  <td width="10%" valign="top"> Generic: </td>
                  <td><%=snmp.getGeneric()%></td>
                </tr>
                <%} /* end if */ %>
                <%if (snmp.getCommunity() != null && !snmp.getCommunity().trim().equals("")) {
                %>
                <tr>
                  <td width="10%" valign="top"> Community: </td>
                  <td><%=snmp.getCommunity()%></td>
                </tr>
                <%} /* end if */ %>
            
            </table>
            
            <br>
          <%} /*end if */%>
          
          <!-- correlation -->
          <%if (event.getCorrelation() != null)
              {
                Correlation correlation = event.getCorrelation(); 
            %>
            <h3> Correlation </h3>
            <table>
              <tr>
                <td width="10%" valign="top"> Path: </td>
                <td><%=correlation.getCorrelationPath()%></td>
              </tr>
              <tr>
                <td width="10%" valign="top"> State:</td>
                <td><%=correlation.getState()%></td>
              </tr>
              
              <% List ueiList = correlation.getCorrelationUEIs();
                 if (ueiList.size() > 0)
                 {
                %>
                <tr>
                  <td width="10%"  valign="top" rowspan="<%=ueiList.size()%>"> UEIs: </td>
                  <td> <%=(String)ueiList.get(0)%></td></tr>
                  <%
                    for (int i = 1; i < ueiList.size(); i++) 
                    {
                  %>
                    <td><%=(String)ueiList.get(i)%></td></tr>
                  <%} /*end for loop*/ %>
                </tr>
                <%} /* end if */ %>
              
              <% if (correlation.getCorrelationMin() != null && !correlation.getCorrelationMin().trim().equals(""))
                 {
              %>
              <tr>
                <td width="10%" valign="top"> Minimum: </td>
                <td><%=correlation.getCorrelationMin()%></td>
              </tr>
              <%} /* end if */ %>
              
              <% if (correlation.getCorrelationMax() != null && !correlation.getCorrelationMax().trim().equals(""))
                 {
              %>
              <tr>
                <td width="10%" valign="top"> Maximum: </td>
                <td><%=correlation.getCorrelationMax()%></td>
              </tr>
              <%} /* end if */ %>
              
              <% if (correlation.getCorrelationTime() != null && !correlation.getCorrelationTime().trim().equals(""))
                 {
              %>
              <tr>
                <td width="10%" valign="top"> Time: </td>
                <td><%=correlation.getCorrelationTime()%></td>
              </tr>
              <%} /* end if */ %>
            
            </table>
            <%} /* end if */ %>
          
          <!-- operator instruction -->
          <% if (event.getOperInstruct() != null && !event.getOperInstruct().trim().equals(""))
             {
          %>
          <h3> Operator Instruction </h3>
            <table>
              <tr>
                <td><%=event.getOperInstruct()%></td>
                </tr>
            </table>
          <br>
          <%} /* end if */ %>
          
          <!-- auto action information -->
          <% List autoActions = event.getAutoActions();
             if (autoActions.size() > 0)
             {
          %>
          <h3> Automatic Actions </h3>
          <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
             <th WIDTH="5%">State </th>
             <th WIDTH="95%">Command </th>
            <%
              for (int i = 0; i < autoActions.size(); i++) 
              {
                AutoAction curAction = (AutoAction)autoActions.get(i);
            %>
              <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                <td><%=curAction.getState()%></td>
                <td><%=curAction.getAutoAction()%></td>
              </tr>
              <% } /*end for loop*/ 
              %>
          </table>
          
          <br>
          <%} /* end if */%>
          
          <!-- operator action information -->
          <% List operActions = event.getOperatorActions();
             if (operActions.size() > 0)
             {
           %>
          <h3> Operator Actions </h3>
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <th WIDTH="5%">State </th>
              <th WITDH="15%">Menu Text </th>
              <th WIDTH="80%">Command </th>
            <%
              for (int i = 0; i < operActions.size(); i++) 
              {
                OperatorAction curAction = (OperatorAction)operActions.get(i);
             %>
                <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                  <td><%=curAction.getState()%></td>
                  <td><%=curAction.getMenuText()%></td>
                  <td><%=curAction.getOperatorAction()%></td>
                </tr>
              <% } /*end for loop*/ 
              %>
          </table>
          
          <br>
          <%} /*end if */%>
          
          <!-- auto acknowlege -->
          <% if (event.getAutoAcknowledge() != null)
             {
          %>
          <h3> Automatic Acknowledge </h3>
          <table>
            <tr>
              <td width="15%" valign="top"> Acknowledgement: </td>
              <td><%=event.getAutoAcknowledge()%></td>
            </tr>
            <tr>
              <td width="15%" valign="top"> State:</td>
              <td><%=event.getAutoAcknowledgeState()%></td>
            </tr>
          </table>
          
          <br>
          <%} /* end if */%>
          
          <!-- log groups -->
          <% List logGroups = event.getLogGroups();
             if (logGroups.size() > 0)
             {
          %>
          <h3> Log Groups </h3>
          <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <%
              for (int i = 0; i < logGroups.size(); i++) {
              %>
                  <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                    <td><%=(String)logGroups.get(i)%></td>
                  </tr>
              <% } /*end for loop*/ 
              %>
          </table>
          <br>
          <%} /* end if */%>
          
          <!-- notifications -->
          <% List notifications = event.getNotifications();
             if (notifications.size() > 0)
             {
          %>
          <h3> Notifications </h3>
          <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <%
              for (int i = 0; i < notifications.size(); i++) {
              %>
                  <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                    <td><%=(String)notifications.get(i)%></td>
                  </tr>
              <% } /*end for loop*/ 
              %>
          </table>
          <%} /* end if */%>
          
          <br>
          
          <!-- trouble ticket -->
          <% if (event.getTTicket() != null && !event.getTTicket().trim().equals(""))
             {
          %>
          <h3> Trouble Ticket </h3>
          <table>
            <tr>
              <td width="10%"> Ticket: </td>
              <td><%=event.getTTicket()%></td>
            </tr>
            <tr>
              <td witdh="10%"> State: </td>
              <td><%=event.getTTicketState()%> </td>
          </table>
          <%} /* end if */%>
          
          <br>
          
           <!-- forwards -->
           <% List forwards = event.getForwards();
              if (forwards.size() > 0)
              {
           %>
           <h3> Forwards </h3>
           <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
             <th WIDTH="10%">State </th>
             <th WIDTH="10%">Mechanism </th>
             <th WITDH="100%">Forward </th>
             <% for (int i = 0; i < forwards.size(); i++)
                {
                  Forward curForward = (Forward)forwards.get(i);
             %>
                <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                  <td><%=curForward.getState()%></td>
                  <td><%=curForward.getMechanism()%></td>
                  <td><%=curForward.getForward()%></td>
                </tr>
             <%} /*end for */%>
          </table>
          <br>
          <%} /* end if */ %>
          
          <!-- mouse over text -->
          <% if (event.getMouseOverText() != null && !event.getMouseOverText().trim().equals(""))
             {
          %>
          <table>
            <tr>
              <td width="20%"> <h3> MouseOver Text </h3> </td>
              <td><%=event.getMouseOverText()%></td>
            </tr>
          </table>
          <%} /* end if */ %>
          
          <br>
      </td>
    
    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="admin" />
</jsp:include>
</body>
</html>
