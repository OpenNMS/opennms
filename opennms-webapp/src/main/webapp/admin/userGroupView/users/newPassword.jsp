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
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="New Password" />
  <jsp:param name="headTitle" value="New Password" />
  <jsp:param name="headTitle" value="Users" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="quiet" value="true" />
  <jsp:param name="nobreadcrumbs" value="true" />
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

<div class="card">
  <div class="card-header">
    <span>Please enter a new password and confirm</span>
  </div>
  <div class="card-body">
    <form role="form" class="form" method="post" name="goForm">
      <div class="form-group">
        <label for="pass1" class="">Password</label>
        <input type="password" class="form-control" id="pass1" name="pass1">
      </div>
  
      <div class="form-group">
        <label for="pass2" class="">Confirm Password</label>
        <input type="password" class="form-control" id="pass2" name="pass2">
      </div>

      <button type="button" class="btn btn-secondary" onclick="verifyGoForm()">OK</button>
      <button type="button" class="btn btn-secondary" onclick="window.close()">Cancel</button>
    </form>
    <p class="alert alert-warning mt-2">
      Note: Be sure to click "Finish" at the bottom of the user page to save
      changes.
    </p>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>
