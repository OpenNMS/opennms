<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,java.text.*,org.opennms.netmgt.config.groups.*"%>
<%
	Group group = null;
  	String groupName = request.getParameter("groupName");
	try
  	{
		GroupFactory.init();
		GroupFactory groupFactory = GroupFactory.getInstance();
      		group = groupFactory.getGroup(groupName);
  	}
	catch (Exception e)
  	{
      		throw new ServletException("Could not find group " + groupName + " in group factory.", e);
  	}

%>
<html>
<head>
<title>Group Detail | User Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/userGroupView/index.jsp'>Users and Groups</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("<a href='admin/userGroupView/groups/list.jsp'>Group List</a>"); %>
<% String breadcrumb4 = java.net.URLEncoder.encode("Group Detail"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Group Detail" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>

<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
    <table width="100%" border="0" cellspacing="0" cellpadding="2" >
      <tr>
        <td>
          <h2>Details for Group: <%=group.getName()%></h2>
          <table width="100%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td width="10%" valign="top">
                <b>Comments:</b>
              </td>
              <td width="90%" valign="top">
                <%=group.getComments()%>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <br>
      <tr>
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="2" >
            <tr>
              <td>
                <b>Assigned Users:</b>
                <% Collection users = group.getUserCollection();
                if (users.size() < 1)
                { %>
                  <table width="50%" border="0" cellspacing="0" cellpadding="2" >
                    <tr>
                      <td>
                        No users belong to this group.
                      </td>
                    </tr>
                  </table>
                <% }
                else { %>
                  <table width="50%" border="1" cellspacing="0" cellpadding="2" >
                    <% 	Iterator usersIter = (Iterator)users.iterator(); 
			while (usersIter != null && usersIter.hasNext()) { %>
                      <tr>
                        <td>
                          <%=(String)usersIter.next()%>
                        </td>
                      </tr>
                    <% } %>
                  </table>
                <% } %>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
</jsp:include>
</body>
</html>
