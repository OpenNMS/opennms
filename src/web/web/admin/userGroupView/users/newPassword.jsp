<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,org.opennms.netmgt.config.users.*"%>

<html>
<head>
  <title>New User Password | User Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<script language="JavaScript">
  function verifyGoForm() 
  {
    if (document.goForm.pass1.value == document.goForm.pass2.value) 
    {
      window.opener.document.modifyUser.password.value=document.goForm.pass1.value;
      
      window.close();
    } 
    else
    {
      alert("The two password fields do not match!");
    }
}
</script>

<br>
<h3>Please enter the new Password and confirm.</h3><br>
<form method="post" name="goForm">

<table>
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
      <input type="button" value="Cancel" onClick="window.close()">
    </tr>
</table>
</form>

</body>
</html>
