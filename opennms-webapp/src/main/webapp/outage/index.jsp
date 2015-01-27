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
  <jsp:param name="title" value="Outages" />
  <jsp:param name="headTitle" value="Outages" />
  <jsp:param name="location" value="outage" />
  <jsp:param name="breadcrumb" value="Outages" />  
</jsp:include>

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
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Outage Menu</h3>
      </div>
      <div class="panel-body">
        <div class="row">
          <div class="col-md-6 col-xs-6">
            <ul class="list-unstyled">
              <li><a href="outage/list.htm?outtype=current">Current outages</a></li>
              <li><a href="outage/list.htm?outtype=both">All outages</a></li>
            </ul>
          </div> <!-- column -->
          <div class="col-md-6 col-xs-6">
            <form role="form" class="form-inline text-right" name="outageIdForm" method="get" action="outage/detail.htm" onsubmit="return validateId();">
              <div class="form-group">
                <label for="input_id">Outage ID:</label>
                <input type="text" class="form-control" id="input_id" name="id" />
              </div>
              <button type="submit" class="btn btn-default">Get details</button>
            </form>
          </div> <!-- column -->
        </div> <!-- row -->
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Outages and Service Level Availability</h3>
      </div>
      <div class="panel-body">
        <p>Outages are tracked by OpenNMS by polling services that have been provisioned. If the service does not respond to the poll, a service outage is created and service availability levels are impacted. Service outages create notifications.</p>
      </div>
    </div>
  </div>
</div>

<hr />

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
