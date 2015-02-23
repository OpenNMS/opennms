<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
	<jsp:param name="title" value="Requisition Node" />
	<jsp:param name="headTitle" value="Provisioning Requisitions" />
	<jsp:param name="headTitle" value="Add Node" />
	<jsp:param name="location" value="addNode" />		
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Requisitions</a>" />
	<jsp:param name="breadcrumb" value="Node Quick-Add" />
</jsp:include>

<br />

<c:if test="${success}">
	<div style="border: 1px solid black; background-color: #bbffcc; margin: 2px; padding: 3px;">
		<h3>Success</h3>
		<p>Your node has been added to the ${foreignSource} requisition.</p>
	</div>
</c:if>

<div class="row">
  <div class="col-md-5">
<c:choose>
<c:when test="${empty requisitions}">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Missing Requisition</h3>
      </div>
      <div class="panel-body">
        <p>You must first <a href='admin/provisioningGroups.htm'>create and import a requisition</a> before using this page.</p>
      </div>
    </div> <!-- panel -->
</c:when>
<c:otherwise>
<form role="form" class="form-horizontal" action="admin/node/add.htm">
	<script type="text/javascript">
	function addCategoryRow() {
		var categoryMembershipTable = document.getElementById("categoryMembershipTable");
		var initialCategoryRow = document.getElementById("initialCategoryRow");
		var newCategoryRow = initialCategoryRow.cloneNode(true);
		newCategoryRow.id = "";
		categoryMembershipTable.appendChild(newCategoryRow);
	}
	</script>
	<input type="hidden" name="actionCode" value="add" />

        <div class="panel panel-default">
          <div class="panel-heading">
	    <h3 class="panel-title">Basic Attributes (required)</h3>
          </div>
          <div class="panel-body">
            <div class="form-group">
              <label for="input_foreignSource" class="col-sm-2 control-label">Requisition:</label>
              <div class="col-sm-10">
                <select name="foreignSource" class="form-control">
                  <c:forEach var="req" items="${requisitions}">
                  <option><c:out value="${req.foreignSource}" /></option>
                  </c:forEach>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label for="input_ipAddress" class="col-sm-2 control-label">IP Address:</label>
              <div class="col-sm-10">
                <input type="text" class="form-control" name="ipAddress" id="input_ipAddress" />
              </div>
            </div>

            <div class="form-group">
              <label for="input_nodeLabel" class="col-sm-2 control-label">Node Label:</label>
              <div class="col-sm-10">
                <input type="text" class="form-control" name="nodeLabel" id="input_nodeLabel" />
              </div>
            </div>
          </div> <!-- panel-body -->
        </div> <!-- panel -->

        <div class="panel panel-default">
          <div class="panel-heading">
	    <h3 class="panel-title">Surveillance Category Memberships (optional)</h3>
          </div>
          <div class="panel-body" id="categoryMembershipTable">
            <div class="form-group" id="initialCategoryRow">
              <label class="control-label col-sm-2">Category:</label>
              <div class="col-sm-3">
                <select name="category" class="form-control">
                  <option value="">--</option>
                  <c:forEach var="cat" items="${categories}">
                    <option><c:out value="${cat}" /></option>
                  </c:forEach>
                </select>
              </div>
              <label class="control-label col-sm-2">Category:</label>
              <div class="col-sm-3">
                <select name="category" class="form-control">
                  <option value="">--</option>
                  <c:forEach var="cat" items="${categories}">
                    <option><c:out value="${cat}" /></option>
                  </c:forEach>
                </select>
              </div>
              <div class="col-sm-2">
                <a href="javascript:addCategoryRow()" class="btn btn-default">More...</a>
              </div>
            </div>
          </div> <!-- panel-body -->
        </div> <!-- panel -->

        <div class="panel panel-default">
          <div class="panel-heading">
	    <h3 class="panel-title">SNMP Parameters (optional)</h3>
          </div>
          <div class="panel-body">
            <div class="form-group">
              <label class="control-label col-sm-2">Version:</label>
              <div class="col-sm-10">
                <select name="snmpVersion" class="form-control"><option>v1</option><option selected>v2c</option></select>
              </div>
            </div>

            <div class="form-group">
              <label class="control-label col-sm-2">Community String:</label>
              <div class="col-sm-10">
                <input type="text" name="community" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label class="control-label col-sm-2">No SNMP:</label>
              <div class="col-sm-10">
                <input id="noSNMP" type="checkbox" name="noSNMP" value="true" selected="false" />
              </div>
            </div>
          </div> <!-- panel-body -->
        </div> <!-- panel -->

        <div class="panel panel-default">
          <div class="panel-heading">
	    <h3 class="panel-title">CLI Authentication Parameters (optional)</h3>
          </div>
          <div class="panel-body">
            <div class="form-group">
              <label class="control-label col-sm-2">Device Username:</label>
              <div class="col-sm-10">
                <input type="text" name="deviceUsername" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label class="control-label col-sm-2">Device Password:</label>
              <div class="col-sm-10">
                <input type="text" name="devicePassword" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label class="control-label col-sm-2">Enable Password:</label>
              <div class="col-sm-10">
                <input type="text" name="enablePassword" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label class="control-label col-sm-2">Access Password:</label>
              <div class="col-sm-10">
                <select name="accessMethod" class="form-control" >
                  <option value="" selected="selected">--</option>
                  <option value="rsh">RSH</option>
                  <option value="ssh">SSH</option>
                  <option value="telnet">Telnet</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label class="control-label col-sm-2">Auto Enable:</label>
              <div class="col-sm-10">
                <input id="autoEnableControl" type="checkbox" name="autoEnable" selected="false" />
              </div>
            </div>
          </div> <!-- panel-body -->
          <div class="panel-footer">
	    <input type="submit" value="Provision" class="btn btn-default" />
	    <input type="reset" class="btn btn-default" />
          </div>
        </div> <!-- panel -->
</form>

</c:otherwise>
</c:choose> <!--  empty requisitions -->
  </div> <!-- column -->

  <div class="col-md-7">
    <div class="panel panel-default">
      <div class="panel-heading">
	<h3 class="panel-title">Node Quick-Add</h3>
      </div>
      <div class="panel-body">
		<p>
		This workflow provides a quick way to add a node to an existing
		provisioning requisition in this OpenNMS system.
		</p>

		<p>
		<strong>Note: This operation <em>will</em> override any un-synchronized
		modifications made to the selected requisition.</strong>
		</p>

		<p>
		<em>Basic Attributes</em> are common to all nodes. Select the requisition
		into which this node should be added, provide an IP address on which OpenNMS
		will communicate with the node, and enter a node label. The node label will
		serve as the display name for the node throughout OpenNMS.
		</p>

		<p>
		<em>Surveillance Category Memberships</em> are optional and work like tags.
		A node can be a member of any number of surveillance categories, and the
		names of those categories can be used in a variety of powerful ways throughout
		the OpenNMS system.
		</p>

		<p>
		<em>SNMP Parameters</em> are optional and apply only to the node being
		requisitioned. If no values are specified here, OpenNMS' system-wide SNMP
		configuration will be used to determine the appropriate values for the IP
		address entered in the <em>Basic Attributes</em> section. If the node does not
		support SNMP, the "No SNMP" box should be checked. Configuring SNMPv3
		parameters via the web UI is not supported; contact your OpenNMS administrator
		if this node requires SNMPv3 parameters that differ from those in the
		system-wide configuration.
		</p>

		<p>
		<em>CLI Authentication Parameters</em> are optional and will be used only if one
		or more provisioning adapters are configured to use them. Typically this is the
		case if OpenNMS is integrated with an external configuration management system.
		</p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
