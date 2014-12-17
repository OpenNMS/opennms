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
	import="java.util.*,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.admin.nodeManagement.*
	"
%>

<%!
	int interfaceIndex;
%>

<%
	String nodeIdString = request.getParameter( "node" );

	if( nodeIdString == null ) {
		throw new org.opennms.web.servlet.MissingParameterException( "node" );
	}

	int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

	String nodeLabel = request.getParameter( "nodelabel" );

	if( nodeLabel == null ) {
		throw new org.opennms.web.servlet.MissingParameterException( "nodelabel" );
	}

	HttpSession userSession = request.getSession(false);
	List<SnmpManagedInterface> interfaces = null;

	interfaceIndex = 0;

	if (userSession != null) {
		interfaces = (List<SnmpManagedInterface>)userSession.getAttribute("listInterfacesForNode.snmpselect.jsp");
	}
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
	<jsp:param name="title" value="Select SNMP Interfaces" />
	<jsp:param name="headTitle" value="Select SNMP Interfaces" />
	<jsp:param name="headTitle" value="Admin"/>
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Select SNMP Interfaces" />
</jsp:include>

<script type="text/javascript" >
	
	function applyChanges() {
		if (confirm("Are you sure you want to proceed? This action can be undone by returning to this page.")) {
			document.chooseSnmpNodes.submit();
		}
	}

	function cancel() {
		document.chooseSnmpNodes.action="admin/index.jsp";
		document.chooseSnmpNodes.submit();
	}

	function collectAll() {
		for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++) {
			var elementType = document.chooseSnmpNodes.elements[c].type;
			if (elementType == "select" || elementType == "select-one") {
				document.chooseSnmpNodes.elements[c].options[0].selected = true;
				document.chooseSnmpNodes.elements[c].options[1].selected = false;
				document.chooseSnmpNodes.elements[c].options[2].selected = false;
			}
		}
	}

	function collectNone() {
		for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++) {
			var elementType = document.chooseSnmpNodes.elements[c].type;
			if (elementType == "select" || elementType == "select-one") {
				document.chooseSnmpNodes.elements[c].options[0].selected = false;
				document.chooseSnmpNodes.elements[c].options[1].selected = true;
				document.chooseSnmpNodes.elements[c].options[2].selected = false;
			}
		}
	}
	function collectDefault() {
		for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++) {
			var elementType = document.chooseSnmpNodes.elements[c].type;
			if (elementType == "select" || elementType == "select-one") {
				document.chooseSnmpNodes.elements[c].options[0].selected = false;
				document.chooseSnmpNodes.elements[c].options[1].selected = false;
				document.chooseSnmpNodes.elements[c].options[2].selected = true;
			}
		}
	}
</script>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Choose SNMP Interfaces for Data Collection</h3>
      </div>
      <div class="panel-body">
	<p>
		Listed below are all the known interfaces for the selected node. If
		snmpStorageFlag is set to "select" for a collection scheme that includes
		the interface marked as "Primary", only the interfaces checked below will have
		their collected SNMP data stored. This has no effect if snmpStorageFlag is
		set to "primary" or "all".
	</p>

	<p>
		In order to change what interfaces are scheduled for collection, simply click
		the collect column and a combobox will appear. Then select what type of collection
		and it will update immediately.
	</p>

	<p>
		<strong>Note:</strong>
		Interfaces marked as Primary or Secondary will always be selected
		for data collection.  To remove them, edit the IP address range in the
		collectd configuration file.
	</p>

	<br/>
	<%=listNodeName(nodeId, nodeLabel)%>
	<br/>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <form method="post" name="chooseSnmpNodes" action="admin/changeCollectStatus">
	<input type="hidden" name="node" value="<%=nodeId%>" />
	<opennms:snmpSelectList id="selectList"></opennms:snmpSelectList>
	<!-- For IE -->
	<div name="opennms-snmpSelectList" id="selectList-ie"></div>
      </form>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>
<%!
	public String listNodeName(int intnodeid, String nodelabel) {
		StringBuffer nodename = new StringBuffer();

		nodename.append("<strong style='font-weight:bold;'>Node ID</strong>: ");
		nodename.append(intnodeid);
		nodename.append("<br/>");
		nodename.append("<strong style='font-weight:bold;'>Node Label</strong>: ");
		nodename.append(nodelabel);
		nodename.append("<br/>\n");
		return nodename.toString();
	}
%>

<%!

public String buildTableRows(List<SnmpManagedInterface> interfaces, int intnodeid, int stop) throws java.sql.SQLException {
	StringBuffer row = new StringBuffer();
	String collStatus = "Not Collected";

	for (SnmpManagedInterface curInterface : interfaces) {
		String statusTest = curInterface.getStatus();
		if (statusTest == null) {
			statusTest = "N";
		}

		String collFlag = curInterface.getCollectFlag();
		if (collFlag == null) {
			collFlag = "N";
		}

		Map<String,String> collOptions = new LinkedHashMap<String,String>();
		collOptions.put("UC", "Collect");
		collOptions.put("UN", "Don't Collect");

		if (statusTest.equals("P")) {
			collStatus = "Primary";
			collOptions.put("C", "Default (Collect)");
		}
		else if (statusTest.equals("S")) {
			collStatus = "Secondary";
			collOptions.put("N", "Default (Don't Collect)");
		}
		else {
			collStatus = "Not Eligible";
			collOptions.put("N", "Default (Don't Collect)");
		}

		if (curInterface.getNodeid() == intnodeid) {
			String ipHostname = curInterface.getIpHostname();
			if (ipHostname == null) {
				ipHostname = "";
			}

			row.append("<tr>\n");

			row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
			if ( curInterface.getIfIndex() > 0 ) {
				row.append(curInterface.getIfIndex());
			} else {
				row.append("&nbsp;");
			}
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"20%\" align=\"left\">");
		  	row.append(ipHostname);
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
			row.append(curInterface.getIfType());
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
			row.append(curInterface.getIfDescr());
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
			row.append(curInterface.getIfName());
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
			row.append(curInterface.getIfAlias());
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
			row.append(collStatus);
			row.append("</td>\n");

			row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
			row.append("<select name=\"collect-").append(curInterface.getIfIndex()).append("\">");
			for (Map.Entry<String,String> option : collOptions.entrySet()) {
				row.append("<option value=\"").append(option.getKey()).append("\"");
				if (collFlag.equals(option.getKey())) {
					row.append(" selected");
				}
				row.append(">").append(option.getValue()).append("</option>");
			}
			row.append("</select>");

			row.append("</td>\n");

			row.append("</tr>\n");
		}
	}

	return row.toString();
}
      
%>
