<%@page language="java" contentType="text/html" session="true"%>
<%
	String role = request.getParameter("adminrole");
%>
<script language="Javascript" type="text/javascript">
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
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/" + "outage/", "Outage - OpenNMS Web Console View");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/" + "event/", "Events - OpenNMS Web Console View");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/" + "element/", "Element - OpenNMS Web Console View ");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/" + "notification/", "Notification - OpenNMS Web Console View  ");
		javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/" + "report/", "Report - OpenNMS Web Console View ");
		if(document.forms[0].role.value == "true")
		{
			javascript:window.external.AddFavorite(protocol+"//"+hostname+":" + port + "/" + path + "/" + "admin/", "Admin - OpenNMS Web Console View ");
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
