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

<%@page import="com.google.common.base.Strings"%>
<%@page import="org.opennms.web.svclayer.model.SnmpInfo"%>
<%@page import="org.opennms.netmgt.snmp.SnmpConfiguration"%>
<%@page language="java" contentType="text/html" session="true"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Configure SNMP Parameters per polled IP" />
	<jsp:param name="headTitle" value="SNMP Configuration" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Configure SNMP by IP" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/tooltip.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/ipv6.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>" />
</jsp:include>

<script type="text/javascript">
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
			alert("You must select either 'Send Event' or 'Save Locally'. It is possible to select both options.");
			return false;
		}
		
		return true;
	}
	
<%/*  checks if the given parameter is a number, so we assume it can be parsed as an integer*/%>
	function isNumber(input) {
		return !isNaN(input - 0) && input != null && input !== null
				&& input !== "" && input !== false;
	}

    function getVersion(id) {
        var element = document.getElementById(id);
        if (element == null || element.options == null || element.selectedIndex == null) {
            return "v2c";
        }

        return element.options[element.selectedIndex].value;
    }

	/*
	 * On Version change only the specificy section is shown.
	 */
	function onVersionChange() {
	    var version = getVersion("version");

	    $("select[name='version'] option[value!='" + version + "']").removeAttr('selected');
        $("select[name='version'] option[value='" + version + "']").attr('selected', true);

        var activeClass = 'snmp-' + version;

		// hide all not selected elements and show selected Element
		$("div[class*='snmp-']").each(function() {
		  if ($(this).hasClass(activeClass)) { // show
                    $(this).removeClass("hidden");
		  } else { // hide
		    $(this).addClass("hidden");
		  }
		});
		return true;
	}

	function cancel() {
		document.snmpConfigForm.action = "admin/index.jsp";
		document.snmpConfigForm.submit();
	}
</script>

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

		final String optionTemplate = "<option value=\"%s\" %s>%s</option>";
		String optionsString = "";
		for (String eachOption : options) {
			optionsString += String.format(optionTemplate, eachOption, eachOption.equals(selectedOption) ? "selected" : "", eachOption);
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


<%
if (request.getAttribute("success") != null) {
%>
  <h3>Finished configuring SNMP. OpenNMS does not need to be restarted.</h3>
<%
}
%>

<div class="row">
  <div class="col-md-6">
    <div class="panel">
      <div class="panel-heading">
        <h3 class="panel-title">SNMP Config Lookup</h3>
      </div>
      <div class="panel-body">
        <form role="form" class="form-horizontal" method="post" name="snmpConfigGetForm" action="admin/snmpConfig?action=get">
          <div class="form-group">
            <label for="lookup_ipAddress" class="control-label col-sm-3" data-toggle="tooltip" data-placement="right" title="Specify the IP Address for which you want to lookup the SNMP configuration. Either IPv4 or IPv6 format is allowed.">
            IP Address
            </label>
            <div class="col-sm-9">
              <input type="text" class="form-control" name="ipAddress" id="lookup_ipAddress"/>
            </div>
          </div>
          <div class="form-group">
            <div class="col-sm-9 col-sm-offset-2">
              <button type="submit" class="btn btn-default" name="getConfig">Look up</button>
            </div>
          </div>
        </form>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel">
      <div class="panel-heading">
        <h3 class="panel-title">Descriptions</h3>
      </div>
      <div class="panel-body">
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
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- col-md-6 -->
</div> <!--  row -->

<form role="form" class="form-horizontal" method="post" name="snmpConfigForm"
  action="admin/snmpConfig?action=add" onsubmit="return verifySnmpConfig();">

<div class="row">
  <div class="col-md-6">
    <div class="panel">
      <div class="panel-heading">
        <h3 class="panel-title">Updating SNMP Configuration</h3>
      </div>
      <div class="panel-body">
          <div class="form-group">
            <div class="col-sm-12">
              <h3>General Parameters</h3>
            </div>
          </div>

          <div class="form-group">
            <label for="version" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="Specify the SNMP version you want to use. You are not allowed to set v1/v2c and v3 parameters at the same time.">
            Version:
            </label>
            <div class="col-sm-9">
              <select id="version" name="version" class="form-control" onChange="onVersionChange()">
                <%=getOptions(version, "v2c", "v1", "v2c", "v3")%>
              </select>
              <p class="help-block"><b>Default: </b>v2c</p>
            </div>
          </div>

          <div class="form-group">
            <label for="firstIPAddress" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="Specify the IP Address you want to define as the first IP address. Even if you just want to add a specific IP address enter that one here. Either IPv4 or IPv6 format is allowed.">
              First IP Address:
            </label>
            <div class="col-sm-9">
              <input id="firstIPAddress" name="firstIPAddress" class="form-control" required="required" value="<%=firstIpAddress%>">
            </div>
          </div>

          <div class="form-group">
            <label for="lastIPAddress" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="If you want to define a range of IP addresses, specify the last IP address. If you just want to add a specific IP address to your SNMP configuration leave this field empty. Either IPv4 or IPv6 format is allowed.">
            Last IP Address:
            </label>
            <div class="col-sm-9">
              <input id="lastIPAddress" name="lastIPAddress" class="form-control">
            </div>
          </div>

          <div class="form-group">
            <label for="timeout" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The amount of time, in milliseconds, that OpenNMS will wait for a response from the agent.">
            Timeout:
            </label>
            <div class="col-sm-9">
              <input id="timeout" name="timeout" class="form-control" value="<%=timeout%>">
              <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_TIMEOUT %> ms</p>
            </div>
          </div>

          <div class="form-group">
            <label for="retryCount" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The number of retries that will be made to connect to the SNMP agent if the initial attempt fails.">
            Retries:
            </label>
            <div class="col-sm-9">
              <input id="retryCount" name="retryCount" class="form-control" value="<%=retryCount%>">
              <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_RETRIES %></p>
            </div>
          </div>

          <div class="form-group">
            <label for="port" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="This overrides the default port.">
            Port:
            </label>
            <div class="col-sm-9">
              <input id="port" name="port" class="form-control" value="<%=port%>">
              <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_PORT %></p>
            </div>
          </div>

          <div class="form-group">
            <label for="proxyHost" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="This overrides the default port.">
            Proxy Host:
            </label>
            <div class="col-sm-9">
              <input id="proxyHost" name="proxyHost=" class="form-control" value="<%=proxyHost%>">
            </div>
          </div>

          <div class="form-group">
            <label for="maxRequestSize" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The maximum size of outgoing SNMP requests. It must be at least 484.">
            Max Request Size:
            </label>
            <div class="col-sm-9">
              <input id="maxRequestSize" name="maxRequestSize" class="form-control" value="<%=maxRequestSize%>">
              <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_MAX_REQUEST_SIZE %></p>
            </div>
          </div>

          <div class="form-group">
            <label for="maxVarsPerPdu" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The maximum number of variables per SNMP request.">
            Max Vars Per Pdu:
            </label>
            <div class="col-sm-9">
              <input id="maxVarsPerPdu" name="maxVarsPerPdu" class="form-control" value="<%=maxVarsPerPdu%>">
              <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_MAX_VARS_PER_PDU %></p>
            </div>
          </div>

          <div class="form-group">
            <label for="maxRepetitions" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The maximum number of instances which the agent may return for any variables beyond those specified by the non-repeaters field. Applies only to v2c and v3.">
            Max Repetitions:
            </label>
            <div class="col-sm-9">
              <input id="maxRepetitions" name="maxRepetitions" class="form-control" value="<%=maxRepetitions%>">
              <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_MAX_REPETITIONS %></p>
            </div>
          </div>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <!-- v1/v2c parameters -->
  <div class="col-md-6 hidden snmp-v1 snmp-v2c">
    <div class="panel">
      <div class="panel-heading">
        <h3 class="panel-title">v1/v2c specific parameters</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="readCommunityString" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The default 'read' community string for SNMP queries.">
          Read Community String:
          </label>
          <div class="col-sm-9">
            <input id="readCommunityString" class="form-control" name="readCommunityString" value="<%=readCommunityString%>">
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_READ_COMMUNITY %></p>
          </div>
        </div>

        <div class="form-group">
          <label for="writeCommunityString" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The default 'write' community string for SNMP queries. Note that this is for future development - OpenNMS does not perform SNMP 'sets' at the moment.">
          Write Community String:
          </label>
          <div class="col-sm-9">
            <input id="writeCommunityString" class="form-control" name="writeCommunityString" value="<%=writeCommunityString%>">
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_WRITE_COMMUNITY %></p>
          </div>
        </div>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <!--  v3 parameters -->
  <div class="col-md-6 hidden snmp-v3">
    <div class="panel">
      <div class="panel-heading">
        <h3 class="panel-title">v3 specific parameters</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="securityName" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="A security name for SNMP v3 authentication.">
          Security Name:
          </label>
          <div class="col-sm-9">
            <input id="securityName" class="form-control" name="securityName" value="<%=securityName%>">
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_SECURITY_NAME %></p>
          </div>
        </div>

        <div class="form-group">
          <label for="securityLevel" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The security level for SNMP v3 authentication. If you leave it empty the security level is determined automatically as follows: 1) if no authentication passphrase is set noAuthNoPriv is determined 2) if authentication passphrase is set but a privacy passphrase is not authNoPriv is determined 3) if authentication and privacy passphrase is set authPriv is determined">
          Security Level:
          </label>
          <div class="col-sm-9">
            <select id="securityLevel" name="securityLevel" class="form-control">
              <option value=""></option>
              <option value="1"
                <%="1".equals(securityLevel) ? "selected" : ""%>>noAuthNoPriv</option>
              <option value="2"
                <%="2".equals(securityLevel) ? "selected" : ""%>>authNoPriv</option>
              <option value="3"
                <%="3".equals(securityLevel) ? "selected" : ""%>>authPriv</option>
            </select>
            <p class="help-block"><b>Default: </b> noAuthNoPriv|authNoPriv|authPriv</p>
          </div>
        </div>

        <div class="form-group">
          <label for="authPassPhrase" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The passphrase to use for SNMP v3 authentication.">
          Auth Passphrase:
          </label>
          <div class="col-sm-9">
            <input id="authPassPhrase" class="form-control" name="authPassPhrase" value="<%=authPassPhrase%>">
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_AUTH_PASS_PHRASE %></p>
          </div>
        </div>

        <div class="form-group">
          <label for="authProtocol" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The authentication protocol for SNMP v3.">
          Auth Protocol:
          </label>
          <div class="col-sm-9">
			<select id="authProtocol" name="authProtocol" class="form-control">
			  <%=getOptions(authProtocol, "", "", "MD5", "SHA")%>
			</select>
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_AUTH_PROTOCOL %></p>
          </div>
        </div>

        <div class="form-group">
          <label for="privPassPhrase" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="A privacy pass phrase used to encrypt the contents of SNMP v3 PDUs.">
          Privacy Passphrase:
          </label>
          <div class="col-sm-9">
            <input id="privPassPhrase" class="form-control" name="privPassPhrase" value="<%=privPassPhrase%>">
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_PRIV_PASS_PHRASE %></p>
          </div>
        </div>

        <div class="form-group">
          <label for="privProtocol" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The privacy protocol used to encrypt the contents of SNMP v3 PDUs.">
          Privacy Protocol:
          </label>
          <div class="col-sm-9">
            <select id="privProtocol" name="privProtocol" class="form-control">
              <%=getOptions(privProtocol, "", "", "DES", "AES", "AES192", "AES256")%>
            </select>
            <p class="help-block"><b>Default: </b><%=SnmpConfiguration.DEFAULT_PRIV_PROTOCOL %></p>
          </div>
        </div>

        <div class="form-group">
          <label for="engineId" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The engine id of the target agent.">
          Engine Id:
          </label>
          <div class="col-sm-9">
            <input id="engineId" class="form-control" name="engineId" value="<%=engineId%>">
          </div>
        </div>

        <div class="form-group">
          <label for="contextEngineId" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="The name of the context to obtain data from the target agent.">
          Context Engine Id:
          </label>
          <div class="col-sm-9">
            <input id="contextEngineId" class="form-control" name="contextEngineId" value="<%=contextEngineId%>">
          </div>
        </div>

        <div class="form-group">
          <label for="contextName" class="col-sm-3 control-label">
          Context Name:
          </label>
          <div class="col-sm-9">
            <input id="contextName" class="form-control" name="contextName" value="<%=contextName%>">
          </div>
        </div>

        <div class="form-group">
          <label for="enterpriseId" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="An enterprise id for SNMP v3 collection.">
          Enterprise Id:
          </label>
          <div class="col-sm-9">
            <input id="enterpriseId" class="form-control" name="enterpriseId" value="<%=enterpriseId%>">
          </div>
        </div>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!--  row -->

<div class="row">
  <div class="col-md-6">
    <div class="panel">
      <div class="panel-heading">
        <h3 class="panel-title">Save Options</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="sendEventOption" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="By default the snmp configuration is published to the system by sending an event. This is useful if you have multiple OpenNMS instances running and want to notify all of them about the changes. If you do not which to send the event, unmark the checkbox. Be aware that collectd must be activated to process the event!">
          Send Event
          </label>
          <div class="col-sm-9">
            <input type="checkbox" id="sendEventOption" name="sendEventOption" checked="checked"/>
            <p class="help-block"><b>Default: </b>enabled</p>
          </div>
        </div>

        <div class="form-group">
          <label for="saveLocallyOption" class="col-sm-3 control-label" data-toggle="tooltip" data-placement="right" title="This option saves the changes directly in snmp-config.xml and does not send an event. The difference to the 'Send Event' option is that Collectd is not needed. If Collectd is not running select this option.">
          Send Locally
          </label>
          <div class="col-sm-9">
            <input type="checkbox" id="saveLocallyOption" name="saveLocallyOption"/>
            <p class="help-block"><b>Default: </b>disabled</p>
          </div>
        </div>

        <div class="form-group">
          <div class="col-sm-9 col-sm-offset-3">
            <button type="submit" class="btn btn-default" name="saveConfig">Save Config</button>
            <button type="button" class="btn btn-default" name="cancelButton" onClick="cancel();">Cancel</button>
          </div>
        </div>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<script type="text/javascript">
$(function () {
  $('[data-toggle="tooltip"]').tooltip();
  onVersionChange();
})
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
