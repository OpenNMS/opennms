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

<%@page language="java" contentType = "text/html" session = "true" import="java.util.*,java.net.*,org.opennms.web.eventconf.bobject.*,org.opennms.web.eventconf.*" %>

<%
  
  HttpSession user = request.getSession(false);
  
  Event event = null;
  if (user != null)
  {
    event = (Event)user.getAttribute("event.modify.jsp");
  }
  
%>

<html>
<head>
  <title>Modify Event | Event Config | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function validateValues()
    {
        for( i = 0; i < document.modify.elements.length; i++ ) 
        {
           if (document.modify.elements[i].name == "uei" && document.modify.elements[i].value == "")
           {
              alert("Please assign a UEI to this event before continuing.");
              return false;
           }
           
           if (document.modify.elements[i].name == "descr" && document.modify.elements[i].value == "")
           {
              alert("Please fill in a description for this event before continuing.");
              return false;
           }
           
           if (document.modify.elements[i].name == "logmsg" && document.modify.elements[i].value == "")
           {
              alert("Please fill in a log message for this event before continuing.");
              return false;
           }
        }
        
        return true;
    }
    
    function updateEvent(page) 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.redirect.value = page;
          document.modify.action = "admin/eventconf/updateEvent";
          document.modify.submit();
        }
    }
    
    function saveEvent(page) 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.redirect.value = page;
          document.modify.action = "admin/eventconf/saveEvent";
          document.modify.submit();
        }
    }
    
    function editMasks() 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.action = "admin/eventconf/masks/maskEditing";
          document.modify.submit();
        }
    }
    
    function editAutoActions() 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.action = "admin/eventconf/autoActions/autoActionEditing";
          document.modify.submit();
        }
    }
    
    function editOperActions() 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.action = "admin/eventconf/operActions/operActionEditing";
          document.modify.submit();
        }
    }
    
    function editNotifications() 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.action = "admin/eventconf/notifications/notificationsEditing";
          document.modify.submit();
        }
    }
    
    function editForwards() 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.modify.action = "admin/eventconf/forwards/forwardsEditing";
          document.modify.submit();
        }
    }
    
    function cancelEvent()
    {
      document.modify.action = "admin/eventconf/list.jsp";
      document.modify.submit();
    }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'> Admin </a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/eventconf/list.jsp'> Event Configuration </a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Modify Event"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Event" />
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
          
          <FORM METHOD="POST" NAME="modify">
          <input type="hidden" name="redirect">
          <input type="hidden" name="eventUEI">
          
          <table>
            <tr>
              <td> <input type="button" name="ok" value="Ok" onclick="saveEvent('/admin/eventconf/list.jsp')"> </td> &nbsp;
              <td> <input type="button" name="cancel" value="Cancel" onclick="cancelEvent()"> </td>
            </tr>
          </table>
          
          <br>
          
          <!-- uei -->
          <h3>UEI </h3>
          <table>
            <tr>
              <td> <%=event.getUei()%></td>
            </tr>
          </table>
          
          <br>
          
          <!-- description -->
          <h3> Description </h3>
          <table>
            <tr>
              <td><textarea rows=3 cols=100 name="descr"><%=event.getDescription()%></textarea> </td>
            </tr>
          </table>
          
          <br>
          
          <!-- log message and destination -->
          <h3> Log Message </h3>
          <table>
            <tr>
              <td valign="top"><h4>Message:</h4></td>
              <td><textarea rows=2 cols=100 name="logmsg"><%=event.getLogMessage()%></textarea></td>
            </tr>
            <tr>
              <td valign="top"><h4> Destination:</h4></td>
              <td><select name="logDest" size='1'>
                    <%=buildSelectOptions(Event.LOGMSG_DEST_VALUES, event.getLogMessageDestination())%>
                  </select>
              </td>
            </tr>
          </table>
          
          <br>
          
          <!-- severity -->
          <table>
            <tr>
              <td witdh="10%"> <h3> Severity </h3> </td>
              <%  String severities[] = {"Indeterminate","Cleared","Normal","Warning","Minor","Major","Critical"};
              %>
              <td><select name="severity" size='1'>
                    <%=buildSelectOptions(severities, event.getSeverity())%>
                  </select>
              </td>
            </tr>
          </table>
          
          <br>
          
          <!-- mask information -->
          <h3> Mask </h3>
          <input type="button" name="modifyMasks" value="Edit Elements" onclick="editMasks('admin/eventconf/masks/maskEditing')">
          <% List maskElements = event.getMask();
             if (maskElements.size() > 0)
             {
          %>
              <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
                <tr bgcolor="#999999">
                    <td width="10%"><b>Name</b></td>
                    <td><b>Values</b></td>
                </tr>
                
                <tr>
                 <% for (int i = 0; i < maskElements.size(); i++)
                    {
                       MaskElement element = (MaskElement)maskElements.get(i);
                       List values = element.getElementValues();
                       String name = element.getElementName();
                  %>
                        <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                         
                         <td width="10%" valign="top" rowspan="<%=values.size()%>"><%=name%> </td>
                         <input type="hidden" name="mask" value=<%=name%>> 
                         
                         <td> <%=(String)values.get(0)%></td></tr>
                         <input type="hidden" name=<%="mask"+i%> value=<%=(String)values.get(0)%>>
                         
                         <%
                           for (int j = 1; j < values.size(); j++)
                           {
                           %>
                             <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                               <td><%=(String)values.get(j)%></td>
                               <input type="hidden" name=<%="mask"+i%> value=<%=(String)values.get(j)%>>
                             </tr>
                         <%} /*end for loop*/ %>
                        </tr>
                  <%} /* end for loop*/ %>
                </tr>
              </table>
            <br>
         <%} /* end if */ %>
          
          <!-- snmp information -->
          <%   Snmp snmp = event.getSnmp();
           %>
            <h3> SNMP </h3>
            <table>
                <tr>
                    <td width="10%" valign="top"> ID: </td>
                    <%if (snmp != null && !snmp.getId().trim().equals("")) { %>
                      <td><input type=text size=20 name="snmp_id" value='<%=snmp.getId()%>'> </td>
                    <% } else { %>
                      <td><input type=text size=20 name="snmp_id" value=""></td>
                    <%}%>
                </tr>
                <tr>
                    <td width="10%" valign="top"> ID Text:</td>
                    <%if (snmp != null && !snmp.getIdText().trim().equals("")) { %>
                      <td><input type=text size=20 name="snmp_text" value='<%=snmp.getIdText()%>'> </td>
                    <% } else { %>
                      <td><input type=text size=20 name="snmp_text" value=""></td>
                    <%}%>
                </tr>
                <tr>
                    <td width="10%" valign="top"> Version: </td>
                    <%if (snmp != null && !snmp.getVersion().trim().equals("")) { %>
                      <td><input type=text size=20 name="snmp_version" value='<%=snmp.getVersion()%>'> </td>
                    <% } else { %>
                      <td><input type=text size=20 name="snmp_version" value=""></td>
                    <%}%>
                </tr>
                <tr>
                    <td width="10%" valign="top"> Specific: </td>
                    <%if (snmp != null && !snmp.getSpecific().trim().equals("")) { %>
                      <td><input type=text size=20 name="snmp_specific" value='<%=snmp.getSpecific()%>'> </td>
                    <% } else { %>
                      <td><input type=text size=20 name="snmp_specific" value=""></td>
                    <%}%>
                </tr>
                <tr>
                    <td width="10%" valign="top"> Generic: </td>
                    <%if (snmp != null && !snmp.getGeneric().trim().equals("")) { %>
                      <td><input type=text size=20 name="snmp_generic" value='<%=snmp.getGeneric()%>'> </td>
                    <% } else { %>
                      <td><input type=text size=20 name="snmp_generic" value=""></td>
                    <%}%>
                </tr>
                <tr>
                    <td width="10%" valign="top"> Community: </td>
                    <%if (snmp != null && !snmp.getCommunity().trim().equals("")) { %>
                      <td><input type=text size=20 name="snmp_community" value='<%=snmp.getCommunity()%>'> </td>
                    <% } else { %>
                      <td><input type=text size=20 name="snmp_community" value=""></td>
                    <%}%>
                </tr>
            </table>
            
            <br>
          
          <!-- correlation -->
          <h3> Correlation </h3>
          <%
            Correlation correlation = event.getCorrelation(); 
           %>
            <table>
              <tr>
                <td width="10%" valign="top"> Path: </td>
                <td>
                <% String path = null;
                   String state = null;
                   if (correlation == null)
                   {
                      path = Correlation.CORRELATION_PATH_VALUES[Correlation.CORRELATION_PATH_DEFAULT_INDEX];
                      state = Correlation.CORRELATION_STATE_VALUES[Correlation.CORRELATION_STATE_DEFAULT_INDEX];
                   }
                   else
                   {
                      path = correlation.getCorrelationPath();
                      state = correlation.getState();
                   }
                %>
                  <select name="correlationPath" size='1'>
                      <%=buildSelectOptions(Correlation.CORRELATION_PATH_VALUES, path)%>
                  </select>
                </td>
              </tr>
              <tr>
                <td width="10%" valign="top"> State:</td>
                <td>
                  <select name="correlationState" size='1'>
                    <%=buildSelectOptions(Correlation.CORRELATION_STATE_VALUES, state)%>
                  </select>
                </td>
              </tr>
              
              <% 
                 StringBuffer ueiBuffer = new StringBuffer();
                 
                 if (correlation != null)
                 {
                    List ueiList = correlation.getCorrelationUEIs();
                    for (int i = 0; i < ueiList.size(); i++) 
                    {
                       ueiBuffer.append(ueiList.get(i)).append("\n");
                    }
                 }
              %>
                <tr>
                  <td width="10%"  valign="top"> UEIs (one per line): </td>
                  <td>
                    <textarea rows=5 cols=100 name="correlationUEIs"><%=ueiBuffer.toString()%></textarea>
                  </td>
                </tr>
              
              <tr>
                <td width="10%" valign="top"> Minimum: </td>
                <%if (correlation != null && correlation.getCorrelationMin() != null)
                  {
                %>
                    <td><input type="text" size="10" name="correlationMin" value='<%=correlation.getCorrelationMin()%>'></td>
                <%} else
                  {
                %>
                    <td><input type="text" size="10" name="correlationMin" value=""></td>
                <%}%>
              </tr>
              
              <tr>
                <td width="10%" valign="top"> Maximum: </td>
                <%if (correlation != null && correlation.getCorrelationMax() != null)
                  {
                %> 
                    <td><input type="text" size="10" name="correlationMax" value='<%=correlation.getCorrelationMax()%>'></td>
                <%} else 
                  {
                %>
                    <td><input type="text" size="10" name="correlationMax" value="">
                <%}%>
              </tr>
              
              <tr>
                <td width="10%" valign="top"> Time: </td>
                <%if (correlation != null && correlation.getCorrelationTime() != null)
                  {
                %> 
                    <td><input type="text" size="10" name="correlationTime" value='<%=correlation.getCorrelationTime()%>'></td>
                <%} else 
                  {
                %>
                    <td><input type="text" size="10" name="correlationTime" value=""></td>
                <%}%>
              </tr>
            </table>
          
          <!-- operator instruction -->
          <h3> Operator Instruction </h3>
            <table>
              <tr>
                <%if (event.getOperInstruct() == null) { %> 
                  <td><input type="text" size="100" name="operatorInstruction" value=""></td>
                <%} else { %>
                  <td><input type="text" size="100" name="operatorInstruction" value='<%=event.getOperInstruct()%>'></td>
                <%}%>
              </tr>
            </table>
          <br>
          
          <!-- auto action information -->
          <h3> Automatic Actions </h3>
          <input type="button" name="modifyAutoActions" value="Edit Auto Actions" onclick="editAutoActions()">
          <% List autoActions = event.getAutoActions();
             if (autoActions.size() > 0)
             {
          %>
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
             <tr bgcolor="#999999">
              <td WIDTH="5%"><b>State</b> </td>
              <td WIDTH="95%"><b>Command</b> </td>
             </tr>
            
            <%
              for (int i = 0; i < autoActions.size(); i++) 
              {
                AutoAction curAction = (AutoAction)autoActions.get(i);
             %>
                <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                  <td><%=curAction.getState()%></td>
                  <td><%=curAction.getAutoAction()%></td>
                </tr>
            <%} /*end for loop*/ 
             %>
          </table>
          
          <br>
          <%} /* end if */%>
          
          <!-- operator action information -->
          <h3> Operator Actions </h3>
            <input type="button" name="modifyOperActions" value="Edit Operator Actions" onclick="editOperActions()">
            
          <% List operActions = event.getOperatorActions();
             if (operActions.size() > 0)
             {
           %>
              <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
                <tr bgcolor="#999999">
                  <td WIDTH="5%"><b>State</b> </td>
                  <td WITDH="15%"><b>Menu Text</b> </td>
                  <td WIDTH="80%"><b>Command</b> </td>
                </tr>
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
               <%} /*end for loop*/ 
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
              <td><input type="text" size="100" name="autoAcknowledge" value='<%=event.getAutoAcknowledge()%>'></td>
            </tr>
            <tr>
              <td width="15%" valign="top"> State:</td>
              <td>
                  <select name="autoAcknowledgeState" size='1'>
                    <%=buildSelectOptions(Event.CONFIGURATION_STATES, event.getAutoAcknowledgeState())%>
                  </select>
              </td>
            </tr>
          </table>
          
          <br>
          <%} /* end if */%>
          
          <!-- log groups -->
          <h3> Log Groups (one per line)</h3>
          <% 
             StringBuffer logGroupsBuffer = new StringBuffer();
             List logGroups = event.getLogGroups();
             if (logGroups.size() > 0)
             {
                for (int i = 0; i < logGroups.size(); i++) 
                {
                   logGroupsBuffer.append(logGroups.get(i)+"\n");
                }
             }
            %>
            <textarea rows=5 cols=100 name="logGroups"><%=logGroupsBuffer.toString()%></textarea>
          <br>
          
          <!-- notifications -->
          <h3> Notifications (one per line)</h3>
          <% 
             List notifications = event.getNotifications();
             StringBuffer notifBuffer = new StringBuffer();
             if (notifications.size() > 0)
             {
                 for (int i = 0; i < notifications.size(); i++) 
                 {
                    notifBuffer.append(notifications.get(i)).append("\n");
                 }
              }
          %>
          <textarea rows=5 cols=100 name="notifications"><%=notifBuffer.toString()%></textarea>
          
          <br>
          
          <!-- trouble ticket -->
          <h3> Trouble Ticket </h3>
          <table>
            <tr>
              <td width="10%"> Ticket: </td>
              <td>
                <%if (event.getTTicket() == null) { %>
                  <input type="text" size="100" name="troubleTicket" value="">
                <%} else { %>
                  <input type="text" size="100" name="troubleTicket" value='<%=event.getTTicket()%>'>
                <%}%>
              </td>
            </tr>
            <tr>
              <td witdh="10%"> State: </td>
              <td>
                  <select name="troubleTicketState" size='1'>
                    <%=buildSelectOptions(Event.CONFIGURATION_STATES, event.getTTicketState())%>
                  </select>
              </td>
          </table>
          
          <br>
          
           <!-- forwards -->
           <h3> Forwards </h3>
            <input type="button" name="modifyForwards" value="Edit Forwards" onclick="editForwards()">
           <% List forwards = event.getForwards();
              if (forwards.size() > 0)
              {
           %>
           
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <tr>
                  <tr bgcolor="#999999">
                    <td WIDTH="10%"><b>State</b> </td>
                    <td WIDTH="10%"><b>Mechanism</b> </td>
                    <td WITDH="100%"><b>Forward</b> </td>
                  </tr>
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
             </tr>
            </table>
          <br>
          <%} /* end if */ %>
          
          <!-- mouse over text -->
          <table>
              <tr>
                <td width="20%"> <h3> MouseOver Text </h3> </td>
              </tr>
              
              <tr>
                <td>
                  <%if (event.getMouseOverText() == null) { %>
                    <input type="text" size="100" name="mouseOver" value="">
                  <%} else { %>
                    <input type="text" size="100" name="mouseOver" value='<%=event.getMouseOverText()%>'>
                  <%}%>
                </td>
              </tr>
          </table>
          
          <br>
          
          <table>
            <tr>
              <td> <input type="button" name="ok" value="Ok" onclick="saveEvent('/admin/eventconf/list.jsp')"> </td> &nbsp;
              <td> <input type="button" name="cancel" value="Cancel" onclick="cancelEvent()"> </td>
            </tr>
          </table>
          
          </FORM>
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


<%!
   public String buildSelectOptions(String values[], String selected)
   {
      StringBuffer buffer = new StringBuffer();
      
      for (int i = 0; i < values.length; i++)
      {
          if (selected.equals(values[i])) 
          {
              buffer.append("<option value=\"").append(values[i]+"\"").append(" selected>").append(values[i]).append("</options>");
          }
          else 
          {
             buffer.append("<option value=\"").append(values[i]+"\">").append(values[i]).append("</options>");
          }
      }
      
      return buffer.toString();
   }
%>
