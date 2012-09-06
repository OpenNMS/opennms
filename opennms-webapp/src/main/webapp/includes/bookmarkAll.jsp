<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="true"%>
<%
	String role = request.getParameter("adminrole");
%>
<script type="text/javascript">
<!--
function addbookmark()
{
	if((navigator.appName == "Microsoft Internet Explorer" && (parseInt(navigator.appVersion) >= 4)))
	{
		var x = window.location.pathname
		var hostname = window.location.hostname
		var protocol = window.location.protocol
		var port = window.location.port
		var to = x.search("/");
		var path = x
		var length = x.length
		if(to == 0)
			path = x.substring(1, length);
		to = path.search("/")
		if(to != -1)
			path = x.substring(1, to+1)
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/outage/", "Outage - OpenNMS Web Console View");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/event/", "Events - OpenNMS Web Console View");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/element/", "Element - OpenNMS Web Console View ");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/notification/", "Notification - OpenNMS Web Console View  ");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/report/", "Report - OpenNMS Web Console View ");
		if(document.forms[0].role.value == "true")
		{
			javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/admin/index.jsp", "Admin - OpenNMS Web Console View ");
		}
	}
	else if(!document.all)
	{
		var msg = "Netscape users must bookmark the pages manually by hitting"
		if(navigator.appName == "Netscape") 
		{
			msg += " <CTRL-D>";
		}
		document.write(msg);
	}
}
//-->
</script>

<form NAME="bookmark" ACTION="javascript:addbookmark()">
	<table>
	<tr><td><input type="SUBMIT" value="Bookmark Home Page Links"/></td></tr>
	<input type="hidden" value='<%= role %>' name="role"/>
	</table>
</form>
