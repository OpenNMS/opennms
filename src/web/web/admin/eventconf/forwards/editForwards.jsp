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

<%@page language="java" contentType = "text/html" session = "true" import="java.util.*,java.net.*,org.opennms.bb.common.bobjects.eventconf.*,org.opennms.bb.common.admin.eventconf.*" %>

<%
    //there should be a list of operator actions in the session that we need to 
    //display and edit
    List forwards = null;
    
    HttpSession user = request.getSession(false);
    
    if (user != null)
    {
        forwards = (List)user.getAttribute("forwards.editForwards.jsp");
        
        //lets add a row for immediate editing if none exist
        if (forwards == null)
        {
            forwards = new ArrayList();
        }
    }
%>

<html>
<head>
  <title>Edit Forwards | Event Config | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
    function validateValues()
    {
        for( i = 0; i < document.forwards.elements.length; i++ ) 
        {
           if (document.forwards.elements[i].type == "text")
           {
               if (document.forwards.elements[i].value == "")
               {
                   alert("Please assign a value to each forward or delete all blank forwards.");
                   return false;
               }
           }
        }
        
        return true;
    }
    
    function validateValuesButIgnoreComponent(component)
    {
        for( i = 0; i < document.forwards.elements.length; i++ ) 
        {
           if (document.forwards.elements[i].name != component && document.forwards.elements[i].type == "text")
           {
                if (document.forwards.elements[i].value == "")
                {
                    alert("Please assign a value to each forward or delete all blank forwards.");
                    return false;
                }
           }
        }
        
        return true;
    }
    
    function updateForwards(page) 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.forwards.redirect.value = page;
          document.forwards.action = "admin/eventconf/forwards/updateForwards";
          document.forwards.submit();
        }
    }
    
    function saveForwards(page) 
    {
        var valuesOK = validateValues();
        if (valuesOK)
        {
          document.forwards.redirect.value = page;
          document.forwards.action = "admin/eventconf/forwards/saveForwards";
          document.forwards.submit();
        }
    }
    
    function deleteForward(component, index)
    {
        var confirmed = confirm("Are you sure you want to delete this forward?");
        if (confirmed)
        {
            if (validateValuesButIgnoreComponent(component))
            {
              document.forwards.deleteIndex.value = index;
              document.forwards.redirect.value = "/admin/eventconf/forwards/deleteForward";
              document.forwards.action = "admin/eventconf/forwards/updateForwards";
              document.forwards.submit();
            }
        }
    }
    
    function cancelForwards()
    {
        document.forwards.action="admin/eventconf/modify.jsp"
        document.forwards.submit();
    }
    
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("admin/index.jsp") + "'> Admin </a>"; %>
<% String breadcrumb2 = "<a href='" + java.net.URLEncoder.encode("admin/eventconf/list.jsp") + "'> Event Configuration </a>"; %>
<% String breadcrumb3 = "Edit Forwards"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Edit Forwards" />
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
          <FORM METHOD="POST" NAME="forwards" >
          <input type="hidden" name="redirect" />
          <input type="hidden" name="deleteIndex" />
          <input type="hidden" name="rows" value="<%=forwards.size()%>">
          <input type="button" value="Add New Forward" onclick="updateForwards('/admin/eventconf/forwards/newForward')"/>
            
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
              <tr bgcolor="#999999">
                <td WITDH="15%"><b>Delete</b> </td>
                <td WIDTH="5%"><b>State</b> </td>
                <td WIDTH="5%"><b>Mechanism</b> </td>
                <td WIDTH="80%"><b>Forward</b> </td>
              </tr>
             
              <!-- forwards -->
              
              <% for (int i = 0; i < forwards.size(); i++)
                 {
                    Forward curForward = (Forward)forwards.get(i);
               %>
                    <tr bgcolor=<%=i%2==0 ? "#ffffff" : "#cccccc"%>>
                     <td width="5%" align="center"> 
                        <a href="javascript:deleteForward('<%="forward"+i%>', '<%=i%>')"> <img  src="images/trash.gif" ></a>
                     </td>
                     <td>
                        <select name="<%="state"+i%>" size="1"> <%=buildSelectOptions(Forward.FORWARD_STATES, curForward.getState())%>
                        </select>
                     </td>
                     <td>
                        <select name="<%="mechanism"+i%>" size="1"> <%=buildSelectOptions(Forward.FORWARD_MECHANISM_VALUES, curForward.getMechanism())%>
                        </select>
                     </td>
                     <td>
                        <input type="text" size="100" name="<%="forward"+i%>" value='<%=curForward.getForward()%>'>
                     </td>
                   </tr>
               <%} /*end for */%>
             
             
            </table>
          
          <input type="button" value="    OK   " onclick="saveForwards('/admin/eventconf/modify.jsp')"/> &nbsp;
          <input type="button" value="Cancel" onclick="cancelForwards()"/>
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
