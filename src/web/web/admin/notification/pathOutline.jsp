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
// 2002 Nov 09: Disallowed spaces in path names. Bug #657.
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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.Util,org.opennms.netmgt.config.*,org.opennms.netmgt.config.destinationPaths.*" %>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    String intervals[] = {"0m", "1m", "2m", "5m", "10m", "15m", "30m", "1h", "2h", "3h", "6h", "12h", "1d"};
    HttpSession user = request.getSession(true);
    Path newPath = (Path)user.getAttribute("newPath");
    List targetLinks = new ArrayList();
    List escalateDelays = new ArrayList();
    
    targetLinks.add( "Initial Targets" );
    String[] targets = new String[newPath.getEscalateCount()];
    for (int i = 0; i < targets.length; i++)
    {
        targetLinks.add("Escalation # " + (i+1));
        escalateDelays.add(newPath.getEscalate()[i].getDelay());
    }
%>

<html>
<head>
  <title>Path Outline | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function edit(index) 
    {
        document.outline.userAction.value="edit";
        document.outline.index.value=index;
        document.outline.submit();
    }
    
    function add(index)
    {
        document.outline.userAction.value="add";
        document.outline.index.value=index;
        document.outline.submit();
    }
    
    function remove(index)
    {
        message = "Are you sure you want to remove escalation #" + (index+1);
        if (confirm(message))
        {
            document.outline.userAction.value="remove";
            document.outline.index.value=index;
            document.outline.submit();
        }
    }
    
    function trimString(str) 
    {
        while (str.charAt(0)==" ")
        {
          str = str.substring(1);
        }
        while (str.charAt(str.length - 1)==" ")
        {
          str = str.substring(0, str.length - 1);
        }
        return str;
    }
    
    function finish()
    {
        trimmed = trimString(document.outline.name.value);
        if (trimmed=="")
        {
            alert("Please give this path a name.");
        }
        else if (trimmed.indexOf(" ") != -1)
        {
            alert("Please do not use spaces in path names.");
        }
        else if (document.outline.escalate0.options.length==0)
        {
            alert("Please give this path some initial targets.");
        }
        else
        {
            document.outline.userAction.value="finish";
            document.outline.submit();
        }
    }
    
    function cancel()
    {
        document.outline.userAction.value="cancel";
        document.outline.submit();
    }

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/notification/index.jsp'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>"; %>
<% String breadcrumb4 = "Path Outline"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Path Outline" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
    <h2><%=(newPath.getName()!=null ? "Editing path: " + newPath.getName() + "<br>" : "")%></h2>
    <h3>Choose the piece of the path that you want to edit from below. When all editing is complete click
        the <i>Finish</i> button. No changes will be permanent until the <i>Finish</i> button has been clicked.</h3>
    <br>
    <table width="100%" cellspacing="2" cellpadding="2" border="0">
    <form METHOD="POST" name="outline" action="admin/notification/destinationWizard">
      <input type="hidden" name="sourcePage" value="pathOutline.jsp"/>
      <input type="hidden" name="index"/>
      <input type="hidden" name="userAction"/>
      <input type="hidden" name="escalation" value="false"/>
      Name: 
      <% if (newPath.getName()==null) { %>
        <input type="text" name="name" value=""/>
      <% } else { %>
        <input type="text" name="name" value="<%=newPath.getName()%>"/>
      <% } %>
      <br>
      <% for (int i = 0; i < targetLinks.size(); i++) { %>
        <tr><td>
        <table width="15%" bgcolor="#999999" cellspacing="2" cellpadding="2" border="1">
           <tr>
              <td width="10%">
              <b>
              <% if (i==0) { %>
                <%="Initial Targets"%>
              <% } else { %>
                <%="Escalation #" + i%>
              <% } %>
              </b>
              <br>
              <% if (i > 0) { %>  
                Delay:
                <%=buildDelaySelect(intervals, i-1, newPath)%><br>
              <% } %>
              <%=buildTargetList(i, newPath, "escalate"+i)%>  
              </td>
              <td width="5%" valign="top">
                <input type="button" value="Edit" onclick="javascript:edit(<%=i-1%>)"/>
                <br>
                &nbsp;
                <br>
                <%if (i > 0) { %>
                  <input type="button" value="Remove" onclick="javascript:remove(<%=i-1%>)"/>
                <% } else { %>
                  &nbsp;
                <% } %>
              </td>
            </tr>
        </table>
        <tr>
          <td>
              <input type="button" value="Add Escalation" onclick="javascript:add(<%=i%>)"/>
          </td>
        </tr>
      <% } %>
      <tr>
        <td>
          <input type="button" value="Finish" onclick="javascript:finish()"/>
          <input type="button" value="Cancel" onclick="javascript:cancel()"/>
        </td>
      </tr>
    </form>
    </table>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%!
    public String buildDelaySelect(String[] intervals, int index, Path path)
    {
          StringBuffer buffer = new StringBuffer("<select NAME=\"escalate" + index  + "Delay\">");
          String selectedOption = "0m";
          
          Escalate escalate = path.getEscalate(index);
          
          for (int i = 0; i < intervals.length; i++)
          {
             if (intervals[i].equals(escalate.getDelay()))
             {
                 buffer.append("<option selected VALUE=\"" + intervals[i] + "\">").append(intervals[i]).append("</option>");
             }
             else
             {
                  buffer.append("<option VALUE=\"" + intervals[i] + "\">").append(intervals[i]).append("</option>");
             }
          }
          buffer.append("</select>");
          
          return buffer.toString();
    }
    
    public String buildTargetList(int index, Path path, String name)
    {
        StringBuffer buffer = new StringBuffer("<select  WIDTH=\"200\" STYLE=\"width: 200px\" name=\""+name+"\" size=\"4\">");
        Target[] targetList = new Target[0];
        
        if (index == 0)
        {
            targetList = path.getTarget();
        }
        else
        {
            targetList = path.getEscalate()[index-1].getTarget();
        }
        
        for (int i = 0; i < targetList.length; i++)
        {
            buffer.append("<option>").append( targetList[i].getName() ).append("</option>");
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
%>
