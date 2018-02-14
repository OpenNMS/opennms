<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
	import="
	    java.util.*,
		org.opennms.web.api.Util,
		org.opennms.core.utils.WebSecurityUtils,
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Group Intervals" />
  <jsp:param name="headTitle" value="Group Intervals" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>" />
  <jsp:param name="breadcrumb" value="Group Intervals" />
</jsp:include>

<h2><%=(newPath.getName()!=null ? "Editing path: " + newPath.getName() + "<br/>" : "")%></h2>

<form method="post" name="groupIntervals" action="admin/notification/destinationWizard" >
  <%=Util.makeHiddenTags(request)%>
  <input type="hidden" name="sourcePage" value="groupIntervals.jsp"/>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Choose the interval to wait between contacting each member in the groups.</h3>
  </div>
  <div class="panel-body">
    <div class="row">
      <div class="col-md-6">
        <table class="table table-condensed table-borderless">
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
              <input type="reset" class="btn btn-default"/>
            </td>
          </tr>
        </table>
      </div> <!-- column -->
    </div> <!-- row -->
  </div> <!-- panel-body -->
  <div class="panel-footer">
    <a HREF="javascript:document.groupIntervals.submit()">Next &#155;&#155;&#155;</a>
  </div> <!-- panel-footer -->
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    public String intervalTable(Path path, String[] groups, int index, String[] intervals)
    {
        StringBuffer buffer = new StringBuffer("<table class=\"table table-condensed table-borderless\">");
        
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
        StringBuffer buffer = new StringBuffer("<select class=\"form-control\" NAME=\"" + group + "Interval\">");
        
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
        List<Target> targets = null;
        
        if (index==-1)
        {
            targets = path.getTargets();
        }
        else
        {
            targets = path.getEscalates().get(index).getTargets();
        }
        
        for (final Target t : targets) {
            if (group.equals(t.getName())) {
                return t.getInterval().orElse(null);
            }
        }
        
        return null;
    }
%>
