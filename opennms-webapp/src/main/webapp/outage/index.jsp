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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Outages")
          .breadcrumb("Outages")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
function validateId() {
  var outageId = document.outageIdForm.id.value;
  if (outageId.length > 1 && parseInt(outageId).toString() == outageId) {
    return true;
  } else {
    alert("Please enter a valid outage ID.");
    return false;
  }
}
</script>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Outage Menu</span>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-6 col-xs-6">
            <ul class="list-unstyled">
              <li><a href="outage/list.htm?outtype=current">Current outages</a></li>
              <li><a href="outage/list.htm?outtype=both">All outages</a></li>
            </ul>
          </div> <!-- column -->
          <div class="col-md-6 col-xs-6">
            <form role="form" class="form pull-right" name="outageIdForm" method="get" action="outage/detail.htm" onsubmit="return validateId();">
              <div class="form-group">
                <label for="input_id">Outage ID</label>
                <div class="input-group">
                  <input type="text" class="form-control" id="input_id" name="id" />
                  <div class="input-group-append">
                    <button type="submit" class="btn btn-secondary"><i class="fas fa-magnifying-glass"></i></button>
                  </div>
                </div>
              </div>
            </form>
          </div> <!-- column -->
        </div> <!-- row -->
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Outages and Service Level Availability</span>
      </div>
      <div class="card-body">
        <p>Outages are tracked by OpenNMS by polling services that have been provisioned. If the service does not respond to the poll, a service outage is created and service availability levels are impacted. Service outages create notifications.</p>
      </div>
    </div>
  </div>
</div>

<hr />

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
