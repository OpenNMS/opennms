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

<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.bb.common.admin.eventconf.*, java.util.*"%>

<html>
<head>
<title>Rearrange Events | User Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
    
    function validate()
    {
        return true;
    }
    
    function selectAll()
    {
        for (i=0; i < document.rearrangeEvents.eventUEIs.length; i++) 
        {
            document.rearrangeEvents.eventUEIs.options[i].selected = true;
        }
    }
    
    function move(incr)
    {
        var i = document.rearrangeEvents.eventUEIs.selectedIndex;	// current selection
        if( i < 0 ) return;
        var j = i + incr;		// where it will move to
        if( j < 0 || j >= document.rearrangeEvents.eventUEIs.length ) return;
        var temp = document.rearrangeEvents.eventUEIs.options[i].text;	// swap them
        document.rearrangeEvents.eventUEIs.options[i].text = document.rearrangeEvents.eventUEIs.options[j].text;
        document.rearrangeEvents.eventUEIs.options[j].text = temp;
        document.rearrangeEvents.eventUEIs.selectedIndex = j;		// make new location selected
    }
    
    function saveRearrange()
    {
        var ok = validate();
        
        if(ok)
        {
            //we need to select all the users in the selectedUsers select list so the
            //request object will have all the users
            selectAll();
            
            document.rearrangeEvents.action="admin/eventconf/rearrangeEvents";
            document.rearrangeEvents.submit();
        }
    }
    
    function cancelRearrange()
    {
        document.rearrangeEvents.action="admin/eventconf/list.jsp";
        document.rearrangeEvents.submit();
    }

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'> Admin </a>"; %>
<% String breadcrumb2 = "<a href='admin/eventconf/list.jsp'> Event Configuration </a>"; %>
<% String breadcrumb3 = "Rearrange Events"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Rearrange Events" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>

<FORM METHOD="POST" NAME="rearrangeEvents">
<input type="hidden" name="redirect"/>

<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>
    
    <td>
      <table width="100%" border="0" cellspacing="0" cellpadding="2" >
        <tr>
          <td>
            <tr>
              <td>
                Rearrange the ordering of the event configurations below.
                Highlight an event UEI and click the "Move Up" and "Move Down" buttons to move that UEI one
                up or one down in the list.
                <br>
              </td>
            </tr>
          </td>
        </tr>
        
        <tr>
          <td align="left">
            <table bgcolor="white" border="1" cellpadding="5" cellspacing="2">
              <tr>
                <td align="center">
                  Event UEIs <br>
                  <%=getEventsList()%><br>
                </td>
                <td>
                  <input type="button" value="  Move Up   " onclick="move(-1)"> <br>
                  <input type="button" value="Move Down" onclick="move(1)">
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <tr>
            <td>
              <input type="button" name="finish" value="Finish" onclick="saveRearrange()">
              <input type="button" name="cancel" value="Cancel" onclick="cancelRearrange()">
            </td>
          </tr>
        
      </table>
    </td>
    
    <td>&nbsp;</td>
  
  </tr>

  </table>
  
</FORM>
  
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
</jsp:include>

</body>
</html>

<%!
    private String getEventsList()
    {
        List ueis = new ArrayList();
        
        try { 
          EventConfFactory factory = EventConfFactory.getInstance();
          ueis = factory.getEventUEIs();
        } catch (Exception e ) {
        }
        
        StringBuffer buffer = new StringBuffer("<select WIDTH=\"500\" STYLE=\"width: 500px\" multiple name=\"eventUEIs\" size=\""+(ueis.size()<28?ueis.size():28)+"\">");
        
        for (int i = 0; i < ueis.size(); i++)
        {
            buffer.append("<option>" + (String)ueis.get(i) + "</option>");
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
%>
