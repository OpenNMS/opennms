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
<%@page language="java" contentType="text/html" session="true" import="
  org.opennms.web.admin.discovery.DiscoveryServletConstants
"%>
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

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="ipaddress-js" />
</jsp:include>

<script type="text/javascript">
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
	opener.document.getElementById("modifyDiscoveryConfig").action=opener.document.getElementById("modifyDiscoveryConfig").action+"?action=<%=DiscoveryServletConstants.addExcludeRangeAction%>";
	opener.document.getElementById("modifyDiscoveryConfig").submit();
	window.close();
	opener.document.focus();
}

</script>

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Add Range to Exclude from Discovery</span>
      </div>
      <div class="card-body">
        <form role="form" class="form">
          <div class="form-group form-row">
            <label for="begin" class="col-form-label col-sm-2">Begin IP Address</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="begin" name="begin" value=''/>
            </div>
          </div>
          <div class="form-group form-row">
            <label for="end" class="col-form-label col-sm-2">End IP Address</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" id="end" name="end" value=''/>
            </div>
          </div>
          <div class="form-group form-row">
            <div class="col-sm-12">
              <button type="button" class="btn btn-secondary" name="addExcludeRange" id="addExcludeRange" onclick="doAddExcludeRange();">Add</button>
              <button type="button" class="btn btn-secondary" name="cancel" id="cancel" onclick="window.close();opener.document.focus();">Cancel</button>
            </div>
          </div>
        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>
