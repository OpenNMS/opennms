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
<%@page language="java" contentType="text/html" session="true" import="
  java.util.Map,
  java.util.TreeMap,
  org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation,
  org.opennms.netmgt.dao.api.*,
  org.springframework.web.context.WebApplicationContext,
  org.springframework.web.context.support.WebApplicationContextUtils,
  org.opennms.netmgt.config.DiscoveryConfigFactory,
  org.opennms.netmgt.config.discovery.DiscoveryConfiguration,
  org.opennms.web.admin.discovery.DiscoveryServletConstants,
  org.opennms.web.admin.discovery.ActionDiscoveryServlet,
  org.opennms.web.admin.discovery.DiscoveryScanServlet
"%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<% 
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>

<%
    WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    HttpSession sess = request.getSession(false);
    DiscoveryConfiguration currConfig;
    if (DiscoveryServletConstants.EDIT_MODE_SCAN.equals(request.getParameter("mode"))) {
    	currConfig  = (DiscoveryConfiguration) sess.getAttribute(DiscoveryScanServlet.ATTRIBUTE_DISCOVERY_CONFIGURATION);
    } else if (DiscoveryServletConstants.EDIT_MODE_CONFIG.equals(request.getParameter("mode"))) {
    	currConfig  = (DiscoveryConfiguration) sess.getAttribute(ActionDiscoveryServlet.ATTRIBUTE_DISCOVERY_CONFIGURATION);
    } else {
    	throw new ServletException("Cannot get discovery configuration from the session");
    }

    MonitoringLocationDao locationDao = context.getBean(MonitoringLocationDao.class);
    Map<String,String> locations = new TreeMap<String,String>();
    for (OnmsMonitoringLocation location : locationDao.findAll()) {
    	locations.put(location.getLocationName(), location.getLocationName());
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Admin")
          .flags("quiet")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
	opener.document.getElementById("erlocation").value=document.getElementById("location").value;
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
            <label for="location" class="col-form-label col-sm-2">Location</label>
            <div class="col-sm-10">
                <select id="location" class="form-control custom-select" name="location">
                    <% for (String key : locations.keySet()) { %>
                    <option value="<%=WebSecurityUtils.sanitizeString(key)%>" <%if(key.equals(currConfig.getLocation().orElse(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID))) out.print("selected");%>><%=WebSecurityUtils.sanitizeString(locations.get(key))%></option>
                        <% } %>
                </select>
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
