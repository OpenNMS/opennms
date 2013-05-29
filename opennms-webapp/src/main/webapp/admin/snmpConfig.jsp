<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page import="org.opennms.netmgt.snmp.SnmpConfiguration"%>
<%@page import="org.opennms.netmgt.config.snmp.SnmpConfig"%>
<%@page import="com.google.common.base.Strings"%>
<%@page import="org.opennms.web.snmpinfo.SnmpInfo"%>
<%@page import="com.google.common.base.Charsets"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="com.google.common.io.Files"%>
<%@page import="org.opennms.netmgt.config.SnmpPeerFactory"%>
<%@page language="java" contentType="text/html" session="true"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Configure SNMP Parameters per polled IP" />
	<jsp:param name="headTitle" value="SNMP Configuration" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Configure SNMP by IP" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/ipv6.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>" />
</jsp:include>

<script type="text/javascript">
	<!-- Tooltip  Stuff -->
	ttContent = null;
	document.onmousemove = updateTT;
	
	<!-- shows the tool tip -->
	function showTT(id) {
		ttContent = document.getElementById(id);
		if (ttContent != null) ttContent.style.display = "block"
	}
	 
	<!-- hides the tool tip -->
	function hideTT() {
		if (ttContent != null) ttContent.style.display = "none";
	}
	
	<!-- ensures that the tool tip moves with the mouse, but only if the tool tip is visible -->
	function updateTT(event) {
		if (ttContent != null && ttContent.style.display == 'block') {
			x = (event.pageX ? event.pageX : window.event.x) + ttContent.offsetParent.scrollLeft - ttContent.offsetParent.offsetLeft;
			y = (event.pageY ? event.pageY : window.event.y) + ttContent.offsetParent.scrollTop - ttContent.offsetParent.offsetTop;
			ttContent.style.left = (x + 20) + "px";
			ttContent.style.top  = (y + 20) + "px";
		}
	}
	
	<!-- Other Stuff -->
	function verifySnmpConfig() {
		var ipValue = new String("");

		// validate Ip-Address
		ipValue = new String(document.snmpConfigForm.firstIPAddress.value);
		if (ipValue == "") {
			alert("Please enter a valid first IP address!");
			return false;
		}
		if (!isValidIPAddress(ipValue)) {
			alert(ipValue + " is not a valid IP address!");
			return false;
		}
		ipValue = new String(document.snmpConfigForm.lastIPAddress.value);
		if (ipValue != "" && !isValidIPAddress(ipValue)) {
			alert(ipValue + " is not a valid IP address!");
			return false;
		}

		//validate timeout
		var timeout = new String(document.snmpConfigForm.timeout.value);
		if (timeout != "" && (!isNumber(timeout) || parseInt(timeout) <= 0)) {
			alert(timeout
					+ " is not a valid timeout. Please enter a number greater than 0 or leave it empty.");
			return false;
		}

		//validate retryCount
		var retryCount = new String(document.snmpConfigForm.retryCount.value);
		if (retryCount != ""
				&& (!isNumber(retryCount) || parseInt(retryCount) <= 0)) {
			alert(retryCount
					+ " is not a valid Retry Count. Please enter a number greater than 0 or leave it empty.");
			return false;
		}

		// validate port
		var port = new String(document.snmpConfigForm.port.value);
		if (port != "" && (!isNumber(port) || parseInt(port) <= 0)) {
			alert(port
					+ " is not a valid Port. Please enter a number greater than 0 or leave it empty.");
			return false;
		}

		// validate maxRequestSize
		var maxRequestSize = new String(
				document.snmpConfigForm.maxRequestSize.value);
		if (maxRequestSize != ""
				&& (!isNumber(maxRequestSize) || parseInt(maxRequestSize) < 484)) {
			alert(maxRequestSize
					+ " is not a valid Max Request Size. Please enter a number greater or equal than 484 or leave it empty.");
			return false;
		}

		// validate maxVarsPerPdu
		var maxVarsPerPdu = new String(
				document.snmpConfigForm.maxVarsPerPdu.value);
		if (maxVarsPerPdu != ""
				&& (!isNumber(maxVarsPerPdu) || parseInt(maxVarsPerPdu) <= 0)) {
			alert(maxVarsPerPdu
					+ " is not a valid Max Vars Per Pdu. Please enter a number greater than 0 or leave it empty.");
			return false;
		}

		// validate maxRepetitions
		var maxRepetitions = new String(
				document.snmpConfigForm.maxRepetitions.value);
		if (maxRepetitions != ""
				&& (!isNumber(maxRepetitions) || parseInt(maxRepetitions) <= 0)) {
			alert(maxRepetitions
					+ " is not a valid Max Repetitions. Please enter a number greater than 0 or leave it empty.");
			return false;
		}		
		
		// validate save options (at least one must be selected)
		var sendEventOption = document.snmpConfigForm.sendEventOption.checked;
		var sendLocallyOption = document.snmpConfigForm.saveLocallyOption.checked;
		if (!sendEventOption && !sendLocallyOption) {
			alert("You must select either 'send Event' or 'save locally'. It is possible to select both options.");
			return false;
		}
		
		return true;
	}
	
<%/*  checks if the given parameter is a number, so we assume it can be parsed as an integer*/%>
	function isNumber(input) {
		return !isNaN(input - 0) && input != null && input !== null
				&& input !== "" && input !== false;
	}

	/*
	 * On Version change only the specificy section is shown.
	 */
	function onVersionChange() {
		var versionElements = new Array(document.getElementById("v1v2"), document.getElementById("v3"));
		var selectedElement = null;
		//  determine selected element
		if (document.getElementById("version").value == "v1" || document.getElementById("version").value == "v2c")
			selectedElement = document.getElementById("v1v2");
		if (document.getElementById("version").value == "v3")
			selectedElement = document.getElementById("v3");

		// hide all not selected elements and show selected Element
		for ( var elementIndex in versionElements) {
			var element = versionElements[elementIndex];
			if (element == selectedElement) { // show
				element.style.visibility = null;
				element.style.display = "block";
			} else { // hide
				element.style.visibility = "hidden";
				element.style.display = "none";
			}
		}
	}

	function cancel() {
		document.snmpConfigForm.action = "admin/index.jsp";
		document.snmpConfigForm.submit();
	}
</script>

<style type="text/css">
	<!--
	img.info {
		background:url(css/images/ui-icons_454545_256x240.png);
		width: 16px;
		height: 16px;
		background-position: -16px -144px;
	}
	
	.tooltip {
		position: absolute;
		display: none;
		padding: 10px;
		background-color: #EEE;
		z-index: 1000;
		max-width: 250px;
	}
	
	.ipAddress {
		width: 200px;
	}
	
	.required {
		vertical-align:top; 
		font-size:0.8em; 
		line-height:100%;
		color: red;
	}
	-->
</style>
<div class="tooltip" id="versionTT"><p><b>Default: </b>v<%=SnmpConfiguration.DEFAULT_VERSION %><br/>Specify the SNMP version you want to use. You are not allowed to set v1/v2c and v3 parameters at the time.</p></div>
<div class="tooltip" id="firstIpAddressTT"><p><b>Default: </b>-<br/>Specify the IP Address you want to define as the first IP address. Even if you just want to add a specific IP address enter that one here. Either IPv4 or IPv6 format is allowed.</p></div>
<div class="tooltip" id="lastIpAddressTT"><p><b>Default: </b>-<br/>If you want to define a range of IP addresses, specify the last IP address. If you just want to add a specific IP address to your SNMP configuration leave this field empty. Either IPv4 or IPv6 format is allowed.</p></div>
<div class="tooltip" id="timeoutTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_TIMEOUT %> ms<br/>The amount of time, in milliseconds, that OpenNMS will wait for a response from the agent.</p></div>
<div class="tooltip" id="retryCountTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_RETRIES %><br/>The number of attempts that will be made to connect to the SNMP agent.</p></div>
<div class="tooltip" id="portTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_PORT %><br/>This overrides the default port.</p></div>
<div class="tooltip" id="maxRequestSizeTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_MAX_REQUEST_SIZE %><br/>The maximum size of outgoing SNMP requests. It must be at least 484.</p></div>
<div class="tooltip" id="maxVarsPerPduTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_MAX_VARS_PER_PDU %><br/>The maximum number of variables per SNMP request.</p></div>
<div class="tooltip" id="maxRepetitionsTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_MAX_REPETITIONS %><br/>The maximum number of attempts which are made to get the variables beyond those specified by the non repeaters field.</p></div>
<div class="tooltip" id="readCommunityStringTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_READ_COMMUNITY %><br/>The default "read" community string for SNMP queries.</p></div>
<div class="tooltip" id="writeCommunityStringTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_WRITE_COMMUNITY %><br/>The default "write" community string for SNMP queries. Note that this is for future development - OpenNMS does not perform SNMP "sets" at the moment.</p></div>
<div class="tooltip" id="securityNameTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_SECURITY_NAME %><br/>A security name for SNMP v3 authentication.</p></div>
<div class="tooltip" id="securityLevelTT"><p><b>Default: </b>noAuthNoPriv|authNoPriv|authPriv<br/>The security level for SNMP v3 authentication. If you leave it empty the security level is determined automatically as follows:<ul><li>if no authentication passphrase is set <u>noAuthNoPriv</u> is determined</li><li>if authentication passphrase is set but a privacy passphrase is not <u>authNoPriv</u> is determined</li><li>if authentication and privacy passphrase is set <u>authPriv</u> is determined</ul></p></div>
<div class="tooltip" id="authPassPhraseTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_AUTH_PASS_PHRASE %><br/>The passphrase to use for SNMP v3 authentication.</p></div>
<div class="tooltip" id="authProtocolTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_AUTH_PROTOCOL %><br/>The authentication protocol for SNMP v3.</p></div>
<div class="tooltip" id="privPassPhraseTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_PRIV_PASS_PHRASE %><br/>A privacy pass phrase used to encrypt the contents of SNMP v3 packages.</p></div>
<div class="tooltip" id="privProtocolTT"><p><b>Default: </b><%=SnmpConfiguration.DEFAULT_PRIV_PROTOCOL %><br/>The privacy protocol used to encrypt the contents of SNMP v3 packages.</p></div>
<div class="tooltip" id="engineIdTT"><p><b>Default: </b>-<br/>The engine id of the target agent.</p></div>
<div class="tooltip" id="contextEngineIdTT"><p><b>Default: </b>-<br/>The name of the context to obtain data from the target agent.</p></div>
<div class="tooltip" id="contextNameTT"><p><b>Default: </b>-<br/>The context engine id of the target entity on the agent.</p></div>
<div class="tooltip" id="enterpriseIdTT"><p><b>Default: </b>-<br/>An enterprise id for SNMP v3 collection</p></div>
<div class="tooltip" id="ipAddressLookupTT"><p><b>Default: </b>-<br/>Specify the IP Address for which you want to lookup the SNMP configuration. Either IPv4 or IPv6 format is allowed.</div>
<div class="tooltip" id="proxyHostTT"><p><b>Default: </b>-<br/>A proxy host to use to communicate with the SNMP agent.</p></div>
<div class="tooltip" id="sendEventOptionTT"><p><b>Default: </b>enabled<br/>By default the snmp configuration is published to the system by sending an event. This is useful if you have multiple OpenNMS instances running and want to notify all of them about the changes. If you do not which to send the event, unmark the checkbox. <b>Be aware that collectd must be activated to process the event!</b></p></div>
<div class="tooltip" id="saveLocallyOptionTT"><p><b>Default: </b>disabled<br/>This option saves the changes directly in snmp-config.xml and does not send an event. The difference between the "send Event" option is that collectd is not needed. If collectd is not running select this option.</p></div>

<%!// does Null Pointer handling
	public String getValue(Object input) {
		if (input == null) return "";
		return input.toString();
	}

	public String getOptions(String selectedOption, String defaultOption, String... options) {
		// prevent Nullpointer
		if (defaultOption == null) defaultOption = "";
		// ensure that there is a default :)
		if (Strings.isNullOrEmpty(selectedOption)) selectedOption = defaultOption;

		final String optionTemplate = "<option %s>%s</option>";
		String optionsString = "";
		for (String eachOption : options) {
			optionsString += String.format(optionTemplate, eachOption.equals(selectedOption) ? "selected" : "",
					eachOption);
			optionsString += "\n";
		}
		return optionsString.trim();
	}%>

<%
	Object obj = request.getAttribute("snmpConfigForIp");
	SnmpInfo snmpInfo = obj == null ? new SnmpInfo() : (SnmpInfo) obj;

	String firstIpAddress = getValue(request.getAttribute("firstIPAddress"));
	String version = getValue(snmpInfo.getVersion());
	String timeout = getValue(snmpInfo.getTimeout());
	String retryCount = getValue(snmpInfo.getRetries());
	String port = getValue(snmpInfo.getPort());
	String proxyHost = getValue(snmpInfo.getProxyHost()); 
	String maxRequestSize = getValue(snmpInfo.getMaxRequestSize());
	String maxVarsPerPdu = getValue(snmpInfo.getMaxVarsPerPdu());
	String maxRepetitions = getValue(snmpInfo.getMaxRepetitions());
	String readCommunityString = getValue(snmpInfo.getReadCommunity());
	String writeCommunityString = getValue(snmpInfo.getWriteCommunity());
	String securityName = getValue(snmpInfo.getSecurityName());
	String securityLevel = getValue(snmpInfo.getSecurityLevel());
	String authPassPhrase = getValue(snmpInfo.getAuthPassPhrase());
	String authProtocol = getValue(snmpInfo.getAuthProtocol());
	String privPassPhrase = getValue(snmpInfo.getPrivPassPhrase());
	String privProtocol = getValue(snmpInfo.getPrivProtocol());
	String engineId = getValue(snmpInfo.getEngineId());
	String contextEngineId = getValue(snmpInfo.getContextEngineId());
	String contextName = getValue(snmpInfo.getContextName());
	String enterpriseId = getValue(snmpInfo.getEnterpriseId());
%>

<body onload="onVersionChange()">
	<div class="TwoColLAdmin">
		<form method="post" name="snmpConfigGetForm"
			action="admin/snmpConfig?action=get">
			<h3>SNMP Config Lookup</h3>
			<div>
				<table>
					<tr>
						<td width="25%">IP Address:</td>
						<td width="50%">
							<input type="text" name="ipAddress" class="ipAddress"/>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('ipAddressLookupTT')" onMouseOut="hideTT()"/> 
						</td>
					</tr>
					<tr>
						<td align="right">
							<input type="submit" name="getConfig" value="Look up">
						</td>
						<td>&nbsp;</td>
					<tr>
				</table>
			</div>
		</form>
		<form method="post" name="snmpConfigForm"
			action="admin/snmpConfig?action=add"
			onsubmit="return verifySnmpConfig();">
			<!--  General parameters -->
			<h3>Updating SNMP Configuration</h3>
			<div id="general">
				<table>
					<tr>
						<th colspan="2">General parameters</th>
					</tr>
					<tr>
						<td width="25%">Version:</td>
						<td width="50%">
							<select id="version" name="version" onChange="onVersionChange()">
								<%=getOptions(version, "v2c", "v1", "v2c", "v3")%>
							</select>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('versionTT')" onMouseOut="hideTT()"/>
						</td>
					</tr>
					<tr>
						<td width="25%">First IP Address: <span class="required" title="This field is required">*</span></td>
						<td width="50%">
							<input name="firstIPAddress" value="<%=firstIpAddress%>" class="ipAddress">
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('firstIpAddressTT')" onMouseOut="hideTT()"/>
						</td>
					</tr>
					<tr>
						<td width="25%">Last IP Address:</td>
						<td width="50%">
							<input name="lastIPAddress" class="ipAddress">
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('lastIpAddressTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
				</table>
				<table>
					<tr>
						<td width="25%">Timeout:</td>
						<td width="50%">
							<input style="width:120px;" name="timeout" value="<%=timeout%>"> 
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('timeoutTT')" onMouseOut="hideTT()"/> 
							</td>
					</tr>
					<tr>
						<td width="25%">Retries:</td>
						<td width="50%">
							<input style="width:120px;" name="retryCount" value="<%=retryCount%>"> 
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('retryCountTT')" onMouseOut="hideTT()"/> 
						</td>
					</tr>
					<tr>
						<td width="25%">Port:</td>
						<td width="50%">
							<input style="width:120px;" name="port" value="<%=port%>">
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('portTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
					<tr>
						<td width="25%">Proxy Host:</td>
						<td width="50%">
							<input style="width:120px;" name="proxyHost" value="<%=proxyHost %>">
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('proxyHostTT')" onMouseOut="hideTT()"/>
						</td>
					</tr>
				</table>
				<table>
					<tr>
						<td width="25%">Max Request Size:</td>
						<td width="50%">
							<input style="width:120px;" name="maxRequestSize" value="<%=maxRequestSize%>" /> 
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('maxRequestSizeTT')" onMouseOut="hideTT()"/> 
							
						</td>
					</tr>
					<tr>
						<td width="25%">Max Vars Per Pdu:</td>
						<td width="50%">
							<input style="width:120px;" name="maxVarsPerPdu" value="<%=maxVarsPerPdu%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('maxVarsPerPduTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
					<tr>
						<td width="25%">Max Repetitions:</td>
						<td width="50%">
							<input style="width:120px;" name="maxRepetitions"value="<%=maxRepetitions%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('maxRepetitionsTT')" onMouseOut="hideTT()"/>
							</td>
					</tr>

				</table>
			</div>
			<!-- v1/v2c parameters -->
			<div id="v1v2" style="visibility: hidden; display: none;">
				<table>
					<tr>
						<th colspan="2">v1/v2c specific parameters</th>
					</tr>
					<tr>
						<td width="25%">Read Community String:</td>
						<td width="50%">
							<input class="ipAddress" name="readCommunityString" value="<%=readCommunityString%>" /> 
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('readCommunityStringTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
					<tr>
						<td width="25%">Write Community String:</td>
						<td width="50%">
							<input class="ipAddress" name="writeCommunityString" value="<%=writeCommunityString%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('writeCommunityStringTT')" onMouseOut="hideTT()"/>
							</td>
					</tr>
				</table>
			</div>

			<!--  v3 parameters -->
			<div id="v3" style="visibility: hidden; display: none;">
				<table>
					<tr>
						<th colspan="2">v3 specific parameters</th>
					</tr>
					<tr>
						<td width="25%">Security Name:</td>
						<td width="50%">
							<input style="width:120px;" name="securityName" value="<%=securityName%>" /> 
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('securityNameTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
					<tr>
						<td width="25%">Security Level:</td>
						<td width="50%">
							<select name="securityLevel" style="width: 120px">
								<option value=""></option>
								<option value="1"
									<%="1".equals(securityLevel) ? "selected" : ""%>>noAuthNoPriv</option>
								<option value="2"
									<%="2".equals(securityLevel) ? "selected" : ""%>>authNoPriv</option>
								<option value="3"
									<%="3".equals(securityLevel) ? "selected" : ""%>>authPriv</option>
							</select>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('securityLevelTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
				</table>
				<table>
					<tr>
						<td width="25%">Auth Passphrase:</td>
						<td width="50%">
							<input style="width:120px;" name="authPassPhrase" value="<%=authPassPhrase%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('authPassPhraseTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
					<tr>
						<td width="25%">Auth Protocol:</td>
						<td width="50%">
							<select name="authProtocol" style="width: 120px">
								<%=getOptions(authProtocol, "", "", "MD5", "SHA")%>
							</select>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('authProtocolTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
				</table>
				<table>
					<tr>
						<td width="25%">Privacy Passphrase:</td>
						<td width="50%">
							<input style="width:120px;" name="privPassPhrase" value="<%=privPassPhrase%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('privPassPhraseTT')" onMouseOut="hideTT()"/>
							</td>
					</tr>
					<tr>
						<td width="25%">Privacy Protocol:</td>
						<td width="50%">
							<select name="privProtocol" style="width: 120px">
								<%=getOptions(privProtocol, "", "", "DES", "AES", "AES192", "AES256")%>
							</select>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('privProtocolTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
				</table>
				<table>
					<tr>
						<td width="25%">Engine Id:</td>
						<td width="50%">
							<input style="width:120px;" name="engineId" value="<%=engineId%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('engineIdTT')" onMouseOut="hideTT()"/>
							</td>
					</tr>
					<tr>
						<td width="25%">Context Engine Id:</td>
						<td width="50%">
							<input style="width:120px;" name="contextEngineId" value="<%=contextEngineId%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('contextEngineIdTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
					<tr>
						<td width="25%">Context Name:</td>
						<td width="50%">
							<input style="width:120px;" name="contextName" value="<%=contextName%>" />
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('contextNameTT')" onMouseOut="hideTT()"/>
						</td>
					</tr>
					<tr>
						<td width="25%">Enterprise Id:</td>
						<td width="50%">
							<input style="width:120px;" name="enterpriseId" value="<%=enterpriseId%>" /> 
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('enterpriseIdTT')" onMouseOut="hideTT()"/>
							
						</td>
					</tr>
				</table>
			</div>
			<!--  submit area -->
			<div>
				<%
					if (request.getAttribute("success") != null) {
				%>
				<div>
					<p><b>Finished configuring SNMP.</b> OpenNMS does not need to be restarted.</p>
				</div>
				<%
					}
				%>
				<table>
					<tr>
						<td width="25%" align="right">
							<label for="sendEventOption">send Event</label>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('sendEventOptionTT')" onMouseOut="hideTT()"/>
							<input type="checkbox" id="sendEventOption" name="sendEventOption" checked="checked"/>
						</td>
						<td width="50%"></td>
					</tr>
					<tr>
						<td width="25%" align="right">
							<label for="saveLocallyOption">save locally</label>
							<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('saveLocallyOptionTT')" onMouseOut="hideTT()"/>
							<input type="checkbox" id="saveLocallyOption" name="saveLocallyOption"/>
						</td>
						<td width="50%"></td>
					</tr>
					<tr>
						<td width="25%" align="right">
							<input type="submit" name="saveConfig" value="Save config">
						</td>
						<td width="50%">
							<input type="button" name="cancelButton" value="Cancel" onClick="cancel();">
						</td>
					</tr>
				</table>
			</div>
		</form>
	</div>

	<div class="TwoColRAdmin">
		<h3>Descriptions</h3>
		<p>
			<b>SNMP Config Lookup:</b> You can look up the actual SNMP
			configuration for a specific IP. To do so enter the IP Address in the
			SNMP Config Lookup box and press "Look up". The configuration will
			then be shown in the "Updateing SNMP Community Names" area.
		</p>

		<p>
			<b>Updating SNMP Configuration:</b> In the boxes on the left, enter
			in a specific IP address and community string, or a range of IP
			addresses and a community string, and other SNMP parameters.
		</p>
		<p>OpenNMS will optimize this list, so enter the most generic
			first (i.e. the largest range) and the specific IP addresses last,
			because if a range is added that includes a specific IP address, the
			community name for the specific address will be changed to be that of
			the range.</p>
		<p>For devices that have already been provisioned and that have an
			event stating that data collection has failed because the community
			name changed, it may be necessary to update the SNMP information on
			the interface page for that device (by selecting the "Update SNMP"
			link) for these changes to take effect.</p>
	</div>
	<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
