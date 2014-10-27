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

<jsp:include page="/includes/header.jsp" flush="false" >
	<jsp:param name="title" value="Requisition Node" />
	<jsp:param name="headTitle" value="Provisioning Requisitions" />
	<jsp:param name="headTitle" value="Add Node" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Requisitions</a>" />
	<jsp:param name="breadcrumb" value="Node Quick-Add" />
</jsp:include>

<br />

<c:if test="${success}">
	<div style="border: 1px solid black; background-color: #bbffcc; margin: 2px; padding: 3px;">
		<h2>Success</h2>
		<p>Your node has been added to the ${foreignSource} requisition.</p>
	</div>
</c:if>

<div class="TwoColLeft">
<c:choose>
<c:when test="${empty requisitions}">
	<h2>Missing Requisition</h2>
	<p>You must first <a href='admin/provisioningGroups.htm'>create and import a requisition</a> before using this page.</p>
</c:when>
<c:otherwise>
<form action="admin/node/add.htm">
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
	<h3>Basic Attributes (required)</h3>
	<div class="boxWrapper">
		<table class="normal">
			<tr>
				<td>Requisition:</td>
				<td colspan="3">
					<select name="foreignSource">
						<c:forEach var="req" items="${requisitions}">
							<option><c:out value="${req.foreignSource}" /></option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>IP Address:</td>
				<td><input type="text" name="ipAddress" /></td>

				<td>Node Label:</td>
				<td><input type="text" name="nodeLabel" /></td>
			</tr>
		</table>
	</div>

	<h3>Surveillance Category Memberships (optional)</h3>
	<div class="boxWrapper">
		<table class="normal">
		<tbody id="categoryMembershipTable">
			<tr id="initialCategoryRow">
				<td>Category:</td>
				<td>
					<select name="category">
							<option value="">--</option>
						<c:forEach var="cat" items="${categories}">
							<option><c:out value="${cat}" /></option>
						</c:forEach>
					</select>
				</td>

				<td>Category:</td>
				<td>
					<select name="category">
							<option value="">--</option>
						<c:forEach var="cat" items="${categories}">
							<option><c:out value="${cat}" /></option>
						</c:forEach>
					</select>
				</td>
				<td><a href="javascript:addCategoryRow()">More...</a></td>
			</tr>
		</tbody>
		</table>
	</div>

	<h3>SNMP Parameters (optional)</h3>
	<div class="boxWrapper">
		<table class="normal">
			<tr>
				<td>Community String:</td>
				<td><input type="text" name="community" /></td>

				<td>Version</td>
				<td><select name="snmpVersion"><option>v1</option><option selected>v2c</option></select></td>

				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
			    <td><label for="noSNMP">No SNMP:</label></td>
			    <td><input id="noSNMP" type="checkbox" name="noSNMP" value="true" selected="false" /></td>
			</tr>
		</table>
	</div>

	<h3>CLI Authentication Parameters (optional)</h3>
	<div class="boxWrapper">
		<table class="normal">
			<tr>
				<td>Device Username:</td>
				<td colspan="3"><input type="text" name="deviceUsername" /></td>
			</tr>
			<tr>
				<td>Device Password:</td>
				<td><input type="text" name="devicePassword" /></td>
				
				<td>Enable Password:</td>
				<td><input type="text" name="enablePassword" /></td>
			</tr>
			<tr>
				<td>Access Method:</td>
				<td>
					<select name="accessMethod" >
					<option value="" selected="selected">--</option>
					<option value="rsh">RSH</option>
					<option value="ssh">SSH</option>
					<option value="telnet">Telnet</option>
					</select>  
				</td>
				<td><label for="autoEnableControl">Auto Enable:</label></td>
				<td>
					<input id="autoEnableControl" type="checkbox" name="autoEnable" selected="false" />
				</td>
			</tr>
		</table>
	</div>

	<input type="submit" value="Provision" />
	<input type="reset" />
</form>

</c:otherwise>
</c:choose> <!--  empty requisitions -->
</div>

<div class="TwoColRight">
	<h3>Node Quick-Add</h3>
	<div class="boxWrapper">
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
	</div>
</div>

<br />

<jsp:include page="/includes/footer.jsp" flush="false" />
