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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Add Interface" />
  <jsp:param name="headTitle" value="Add Interface" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Add Interface" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="ipaddress-js" />
</jsp:include>

<%--
 XXX Can't do this because body is in the header:
	onLoad="document.newIpForm.ipAddress.focus()"
--%>


<script type="text/javascript">
        function verifyIpAddress () {
                var prompt = new String("IP Address");
                var errorMsg = new String("");
                var ipValue = new String(document.newIpForm.ipAddress.value);

                if (!isValidIPAddress(ipValue)) {
                        alert (ipValue + " is not a valid IP address!");
						return false;
                }
                else{
                        document.newIpForm.action="admin/addNewInterface";
                        return true;
                }
        }
    
        function cancel()
        {
                document.newIpForm.action="admin/index.jsp";
                document.newIpForm.submit();
        }
</script>

<div class="row">
  <div class="col-md-4">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Enter IP Address</h3>
      </div>
      <div class="panel-body">
        <form method="post" name="newIpForm" onsubmit="return verifyIpAddress();">
            <c:if test="${param.action == 'redo'}">
              <p class="text-danger">
                  The IP address ${param.ipAddress} already exists.
                  Please enter a different IP address.
              </p>
            </c:if>

            <div class="form-group">
              <label for="input_ipAddress">IP address:</label>
              <input size="15" name="ipAddress" class="form-control">
            </div>

            <div class="form-group">
              <input type="submit" class="btn btn-default" value="Add">
              <input type="button" class="btn btn-default" value="Cancel" onclick="cancel()">
            </div>
        </form>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-8">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Add Interface</h3>
      </div>
      <div class="panel-body">
        <p>
        Enter in a valid IP address to generate a newSuspectEvent. This will add a node to the OpenNMS
        database for this device. Note: if the IP address already exists in OpenNMS, use "Rescan" from
        the node page to update it. Also, if no services exist for this IP, it will still be added.
        </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
