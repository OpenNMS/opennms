<%@page language="java" contentType="text/html" session="true" import="java.util.Date" %>
<html>
<head>
  <title>Users and Groups | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Users and Groups"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Users and Groups" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td valign="top">
      <h3>Users and Groups</h3>

      <p>
        <a HREF="admin/userGroupView/users/list.jsp">Configure Users</a>
      </p>
      <p>
        <a HREF="admin/userGroupView/groups/list.jsp">Configure Groups</a>
      </p>
      <!--
      <p>
        <a HREF="admin/userGroupView/views/list.jsp">Configure Views</a>
      </p>
      -->
    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
      <h3>Users</h3>
      <p>
        Add new <em>Users</em>, change user names and passwords, and edit notification information.
      </p>

      <h3>Groups</h3>
      <p>
        Assign and unassign <em>Users</em> to <em>Groups</em>.
      </p>

      <!--
      <h3>Views</h3>
      <p>
        Assign and unassign <em>Users</em> and <em>Groups</em> to <em>Views</em>.
      </p>
      -->

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
