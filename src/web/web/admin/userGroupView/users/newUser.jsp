<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*,java.util.*,org.opennms.netmgt.config.users.*"%>

<html>
<head>
  <title>New User Info | User Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/userGroupView/index.jsp'>Users and Groups</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("User List"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="User Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<script language="JavaScript">
  function verifyGoForm() 
  {
    var id = new String(document.goForm.userID.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The user ID '" + document.goForm.userID.value + "' cannot be used. It may be confused with the administration user ID 'admin'.");
        return;
    }
    
    if (document.goForm.pass1.value == document.goForm.pass2.value) 
    {
      document.goForm.action="admin/userGroupView/users/addNewUser";
      document.goForm.submit();
    } 
    else
    {
      alert("The two password fields do not match!");
    }
    
    function close()
    {
      document.goForm.action="admin/userGroupView/users/list.jsp";
      document.goForm.submit();
    }
}
</script>

<br>

<form method="post" name="goForm">
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
    <%if (request.getParameter("action").equals("redo")) { %>
      <h3>The user <%=request.getParameter("userID")%> already exists. Please type in a different user id.</h3>
    <% } else { %>
      <h3>Please enter a user id and password below.</h3>
    <% } %>
    <table>
  <tr>
    <td width="10%">
      User ID:
    </td>
    <td width="100%">
      <input type="text" name="userID">
    </td>
  </tr>
  
  <tr>
    <td width="10%">
      Password:
    </td>
    <td width="100%">
      <input type="password" name="pass1">
    </td>
  </tr>
  
  <tr>
    <td width="10%">
      Confirm Password:
    </td>
    <td width="100%">
      <input type="password" name="pass2">
    </td>
  </tr>
  
  <tr>
    <td>
      <input type="button" value="OK" onClick="verifyGoForm()">
    </td>
    <td>
          <input type="button" value="Cancel" onClick="close()">
        </td>
      </tr>
    </table>
    </td>
    </tr>
</table>
</form>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
