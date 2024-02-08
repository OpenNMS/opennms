<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Add Interface")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Add Interface")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
    <div class="card">
      <div class="card-header">
        <span>Enter IP Address</span>
      </div>
      <div class="card-body">
        <form method="post" name="newIpForm" onsubmit="return verifyIpAddress();" action="admin/addNewInterface">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${param.action == 'redo'}">
              <p class="text-danger">
                  The IP address ${param.ipAddress} already exists.
                  Please enter a different IP address.
              </p>
            </c:if>

            <div class="form-group">
              <label for="input_ipAddress">IP Address</label>
              <input size="15" name="ipAddress" id="input_ipAddress" class="form-control">
            </div>

            <div class="form-group">
              <input type="submit" class="btn btn-secondary" value="Add">
              <input type="button" class="btn btn-secondary" value="Cancel" onclick="cancel()">
            </div>
        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-8">
    <div class="card">
      <div class="card-header">
        <span>Add Interface</span>
      </div>
      <div class="card-body">
        <p>
        Enter in a valid IP address to generate a newSuspectEvent. This will add a node to the OpenNMS
        database for this device. Note: if the IP address already exists in OpenNMS, use "Rescan" from
        the node page to update it. Also, if no services exist for this IP, it will still be added.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
