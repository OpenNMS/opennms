<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.admin.discovery.ActionDiscoveryServlet" %>
<% 
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Add Exclude Range" />
    <jsp:param name="headTitle" value="Admin" />
    <jsp:param name="quiet" value="true" />
</jsp:include>

<script type='text/javascript' src='js/ipv6/ipv6.js'></script>
<script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>
<script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>
<script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>

<script type="text/javascript">
function v4BigInteger(ip) {
    var a = ip.split('.');
    return parseInt(a[0])*Math.pow(2,24) + parseInt(a[1])*Math.pow(2,16) + parseInt(a[2])*Math.pow(2,8) + parseInt(a[3]);
};

function checkIpRange(ip1, ip2){
    if (verifyIPv4Address(ip1) && verifyIPv4Address(ip2)) {
        var a = v4BigInteger(ip1);
        var b = v4BigInteger(ip2);
        return b >= a;
    }
    if (verifyIPv6Address(ip1) && verifyIPv6Address(ip2)) {
        var a = new v6.Address(ip1).bigInteger();
        var b = new v6.Address(ip2).bigInteger();
        return b.compareTo(a) >= 0;
    }
    return false;
}

function doAddExcludeRange(){
	if(!isValidIPAddress(document.getElementById("begin").value)){
		alert("Begin Address not valid.");
		document.getElementById("begin").focus();
		return;
	}

	if(!isValidIPAddress(document.getElementById("end").value)){
		alert("End Address not valid.");
		document.getElementById("end").focus();
		return;
	}
	
	if(!checkIpRange(document.getElementById("begin").value, document.getElementById("end").value)){
		alert("Address Range not valid.");
		document.getElementById("end").focus();
		return;
	}
	
	opener.document.getElementById("erbegin").value=document.getElementById("begin").value;
	opener.document.getElementById("erend").value=document.getElementById("end").value;
	opener.document.getElementById("modifyDiscoveryConfig").action=opener.document.getElementById("modifyDiscoveryConfig").action+"?action=<%=ActionDiscoveryServlet.addExcludeRangeAction%>";
	opener.document.getElementById("modifyDiscoveryConfig").submit();
	window.close();
	opener.document.focus();
	
}

</script>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Add Range to Exclude from Discovery</h3>
      </div>
      <div class="panel-body">
        <p>Add a range of IP addresses to exclude from discovery.<br/>
        Insert <i>Begin</i> and <i>End</i> IP addresses and click on <i>Add</i> to confirm.
        </p>
        <form role="form" class="form-horizontal">
          <div class="form-group">
            <label for="begin" class="control-label col-sm-2">Being IP Address:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="begin" name="begin" value=''/>
            </div>
          </div>
          <div class="form-group">
            <label for="end" class="control-label col-sm-2">End IP Address:</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="end" name="end" value=''/>
            </div>
          </div>
          <div class="form-group">
            <div class="col-sm-12">
              <button type="button" class="btn btn-default" name="addExcludeRange" id="addExcludeRange" onclick="doAddExcludeRange();">Add</button>
              <button type="button" class="btn btn-default" name="cancel" id="cancel" onclick="window.close();opener.document.focus();">Cancel</button>
            </div>
          </div>
        </form>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>
