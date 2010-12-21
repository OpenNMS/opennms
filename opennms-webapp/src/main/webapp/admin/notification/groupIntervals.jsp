<%--

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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.api.Util,
		org.opennms.web.WebSecurityUtils,
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
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    HttpSession user = request.getSession(true);
    Path newPath = (Path)user.getAttribute("newPath");
    String intervals[] = {"0m", "1m", "2m", "5m", "10m", "15m", "30m", "1h", "2h", "3h", "6h", "12h", "1d"};
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Group Intervals" />
  <jsp:param name="headTitle" value="Group Intervals" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>" />
  <jsp:param name="breadcrumb" value="Group Intervals" />
</jsp:include>

<h2><%=(newPath.getName()!=null ? "Editing path: " + newPath.getName() + "<br/>" : "")%></h2>
    <h3>Choose the interval to wait between contacting each member in the groups.</h3>
    <form method="post" name="groupIntervals" action="admin/notification/destinationWizard" >
      <%=Util.makeHiddenTags(request)%>
      <input type="hidden" name="sourcePage" value="groupIntervals.jsp"/>
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top" align="left">
          <%=intervalTable(newPath, 
                           request.getParameterValues("groups"), 
                           WebSecurityUtils.safeParseInt(request.getParameter("targetIndex")),
                           intervals)%>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="reset"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a HREF="javascript:document.groupIntervals.submit()">Next &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    public String intervalTable(Path path, String[] groups, int index, String[] intervals)
    {
        StringBuffer buffer = new StringBuffer("<table width=\"100%\" cellspacing=\"2\" cellpadding=\"2\" border=\"0\">");
        
        for (int i = 0; i < groups.length; i++)
        {
            buffer.append("<tr><td>").append(groups[i]).append("</td>");
            buffer.append("<td>").append(buildIntervalSelect(path, groups[i], index, intervals)).append("</td>");
            buffer.append("</tr>");
        }
        
        buffer.append("</table>");
        return buffer.toString();
    }
    
    public String buildIntervalSelect(Path path, String group, int index, String[] intervals)
    {
        StringBuffer buffer = new StringBuffer("<select NAME=\"" + group + "Interval\">");
        
        String selectedOption = "0m";
        
        for (int i = 0; i < intervals.length; i++)
        {
            if (path!=null && intervals[i].equals(getGroupInterval(path, group, index)) )
            {
                selectedOption = intervals[i];
                break;
            }
        }
        
        for (int i = 0; i < intervals.length; i++)
        {
            if (intervals[i].equals(selectedOption))
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
    
    public String getGroupInterval(Path path, String group, int index)
    {
        Target[] targets = null;
        
        if (index==-1)
        {
            targets = path.getTarget();
        }
        else
        {
            targets = path.getEscalate(index).getTarget();
        }
        
        for (int i = 0; i < targets.length; i++)
        {
            if (group.equals(targets[i].getName()))
                return targets[i].getInterval();
        }
        
        return null;
    }
%>
