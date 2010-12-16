<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
	import="org.opennms.netmgt.config.*,
		java.util.*,
		org.opennms.netmgt.config.users.*
	"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="New Password" />
  <jsp:param name="headTitle" value="New Password" />
  <jsp:param name="headTitle" value="Users" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="quiet" value="true" />
</jsp:include>


<script type="text/javascript">
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

<h3>Please enter a new password and confirm</h3>

<br/>

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
      <input type="button" value="OK" onclick="verifyGoForm()">
    </td>
    <td>
      <input type="button" value="Cancel" onclick="window.close()">
    </tr>
</table>
</form>

<p>
  Note: Be sure to click "Finish" at the bottom of the user page to save
  changes.
</p>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>

