<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.api.Util,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.destinationPaths.*
	"
%>

<%!
    public void init() throws ServletException {
        try {
            UserFactory.init();
            GroupFactory.init();
            DestinationPathFactory.init();
            NotificationCommandFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
    
    String pathName = null;
    Path path = null;
    String times[] = {"0m", "1m", "2m", "5m", "10m", "15m", "30m", "1h", "2h", "3h", "6h", "12h", "1d"};
%>

<%
    pathName = request.getParameter("path");
    path = DestinationPathFactory.getInstance().getPath(pathName);
    if (path == null)
      path = new Path();
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Commands" />
  <jsp:param name="headTitle" value="Group Intervals" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Commands" />
</jsp:include>

<script type="text/javascript" >

    function next() 
    {
        document.targets.submit();
    }

</script>

<h3><%=(pathName!=null ? "Editing path " + pathName + "<br/>" : "")%></h3>

<h3>Step 4: Add escalations to the path.</h3>

<form method="post" name="escalations" action="" >
      <%=Util.makeHiddenTags(request)%>
      <table width="50%">
        <tr>
          <td valign="top" align="left">
            <%=displayTargets(request.getParameterValues("users"), request.getParameterValues("groups"))%>
          </td>
          <td valign="top" align="left">
            <%=displayEscalations()%>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="button" value="Add Escalation"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a href="javascript:document.commands.submit()">Next &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    public String displayTargets(String users[], String groups[])
    {
        if (users==null)
            return "";
            
        StringBuffer buffer = new StringBuffer("<h2>Initial Targets</h2>");
        buffer.append("<table width=\"100%\" cellspacing=\"2\" cellpadding=\"2\" border=\"0\">");
        
        for (int i = 0; i < users.length; i++)
        {
            buffer.append("<tr><td>").append(users[i]).append("</td>").append("</tr>");
        }
        
        for (int i = 0; i < groups.length; i++)
        {
            buffer.append("<tr><td>").append(groups[i]).append("</td>").append("</tr>");
        }
        
        buffer.append("</table>");
        return buffer.toString();
    }
    
    public String displayEscalations()
    {
        
        StringBuffer buffer = new StringBuffer("<h2>Escalation Targets</h2>");
        buffer.append("<table width=\"100%\" cellspacing=\"2\" cellpadding=\"2\" border=\"0\">");
        
        buffer.append("</table>");
        return buffer.toString();
    }
%>
