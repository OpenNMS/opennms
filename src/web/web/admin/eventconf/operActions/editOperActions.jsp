<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType = "text/html" session = "true" import="java.util.*,java.net.*,org.opennms.bb.common.bobjects.eventconf.*,org.opennms.bb.common.admin.eventconf.*" %>

<%
    //there should be a list of operator actions in the session that we need to 
    //display and edit
    List operActions = null;
    
    HttpSession user = request.getSession(false);
    
    if (user != null)
    {
        operActions = (List)user.getAttribute("operActions.editOperActions.jsp");
        
        if (operActions == null)
        {
            operActions = new ArrayList();
        }
    }
%>

<html>
<head>
  <title>Edit Operator Actions | Event Config | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
    function validateValues()
    {
        for( i = 0; i < document.operActions.elements.length; i++ ) 
        {
           if (document.operActions.elements[i].type == "text")
           {
               if (document.operActions.elements[i].value == "")
               {
                   alert("Please assign a value to each operator action and menu text field or delete all blank actions.");
                   return false;
               }
           }
        }
        
        return true;
    }
    
    function validateValuesButIgnoreComponent(components)
    {
        for( i = 0; i < document.operActions.elements.length; i++ ) 
        {
           var inThisAction = false;
           for (j = 0; j < components.length; j++)
           {  
              if (document.operActions.elements[i].name != components[j])
              {
                  inThisAction = true;
              }
           }
           
           if (!inThisAction && document.operActions.elements[i].type == "text")
           {
                if (document.operActions.elements[i].value == "")
                {
                    alert("Please assign a value to each operator action and menu text field or delete all blank actions.");
                    return false;
                }
           }
        }
        
        return true;
    }
    
    function updateActions(page) 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.operActions.redirect.value = page;
          document.operActions.action = "admin/eventconf/operActions/updateOperActions";
          document.operActions.submit();
        }
    }
    
    function saveActions(page) 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.operActions.redirect.value = page;
          document.operActions.action = "admin/eventconf/operActions/saveOperActions";
          document.operActions.submit();
        }
    }
    
    function deleteAction(component, index)
    {
        var confirmed = confirm("Are you sure you want to delete this action?");
        if (confirmed)
        {
            if (validateValuesButIgnoreComponent(component))
            {
              document.operActions.deleteIndex.value = index;
              document.operActions.redirect.value = "/admin/eventconf/operActions/deleteOperAction";
              document.operActions.action = "admin/eventconf/operActions/updateOperActions";
              document.operActions.submit();
            }
        }
    }
    
    function cancelActions()
    {
        document.operActions.action="admin/eventconf/modify.jsp"
        document.operActions.submit();
    }
    
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'> Admin </a>"; %>
<% String breadcrumb2 = "<a href='admin/eventconf/list.jsp'> Event Configuration </a>"; %>
<% String breadcrumb3 = "Edit Event Operator Actions"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Edit Event Operator Actions" />
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
          
          <!-- mask information -->
          <FORM METHOD="POST" NAME="operActions" >
          <input type="hidden" name="redirect" />
          <input type="hidden" name="deleteIndex" />
          <input type="hidden" name="rows" value="<%=operActions.size()%>">
          <input type="button" value="Add New Action" onclick="updateActions('/admin/eventconf/operActions/newOperAction')"/>
            
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <tr bgcolor="#999999">
                <td WITDH="15%"><b>Delete</b> </td>
                <td WIDTH="5%"><b>State</b> </td>
                <td WIDTH="95%"><b>Command / Menu Text</b> </td>
              </tr>
             
              <!-- operator action information -->
              <%
                for (int i = 0; i < operActions.size(); i++) 
                {
                    OperatorAction curAction = (OperatorAction)operActions.get(i);
               %>
                  <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                    <td width="5%" align="center" rowspan="2"> 
                            <a href="javascript:deleteAction(['<%="action"+i%>','<%="menu"+i%>'], '<%=i%>')"> <img  src="images/trash.gif" ></a>
                    </td>
                    <td rowspan="2">
                        <select name="<%="state"+i%>" size="1"> <%=buildSelectOptions(OperatorAction.OPERATOR_ACTION_STATES, curAction.getState())%>
                        </select>
                    </td>
                    
                    <td>
                      <input type="text" size="100" name="<%="action"+i%>" value='<%=curAction.getOperatorAction()%>'>
                    </td> 
                    </tr>
                    
                    <td bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                      <input type="text" size="100" name="<%="menu"+i%>" value='<%=curAction.getMenuText()%>'>
                    </td> 
                  </tr>
             <% } /*end for loop*/ 
              %>
             
            </table>
            
            <input type="button" value="    OK   " onclick="saveActions('/admin/eventconf/modify.jsp')">
            <input type="button" value="Cancel" onclick="cancelActions()">
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
