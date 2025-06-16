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
  org.opennms.web.api.Util,
  org.opennms.netmgt.config.DiscoveryConfigFactory,
  org.opennms.netmgt.config.discovery.*,
  org.opennms.netmgt.provision.persist.requisition.Requisition,
  org.opennms.netmgt.dao.api.*,
  org.opennms.netmgt.model.monitoringLocations.*,
  org.springframework.web.context.WebApplicationContext,
  org.springframework.web.context.support.WebApplicationContextUtils,
  org.opennms.web.svclayer.api.RequisitionAccessService,
  org.opennms.web.admin.discovery.DiscoveryServletConstants,
  org.opennms.web.admin.discovery.DiscoveryScanServlet"
%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Discovery")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Discovery", "admin/discovery/index.jsp")
          .breadcrumb("Create Discovery Scan")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
function addSpecific(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-specific.jsp?mode=scan&nobreadcrumbs=true" )%>', 'AddSpecific', 'toolbar=0,width=700,height=500, left=0, top=0, resizable=1, scrollbars=1')
}

function addIncludeRange(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-ir.jsp?mode=scan&nobreadcrumbs=true" )%>', 'AddIncludeRange', 'toolbar=0,width=750 ,height=670, left=0, top=0, resizable=1, scrollbars=1')
}

function addIncludeUrl(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-url.jsp?mode=scan&nobreadcrumbs=true" )%>', 'AddIncludeUrl', 'toolbar=0,width=750 ,height=500, left=0, top=0, resizable=1, scrollbars=1')
}

function addExcludeUrl(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-ex-url.jsp?mode=scan&nobreadcrumbs=true" )%>', 'AddExcludeUrl', 'toolbar=0,width=750 ,height=500, left=0, top=0, resizable=1, scrollbars=1')
}

function addExcludeRange(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-er.jsp?mode=scan&nobreadcrumbs=true" )%>', 'AddExcludeRange', 'toolbar=0,width=600 ,height=350, left=0, top=0, resizable=1, scrollbars=1')
}


function deleteSpecific(i){
	if(confirm("Are you sure you want to delete this specific address?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=DiscoveryServletConstants.removeSpecificAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteIR(i){
	if(confirm("Are you sure you want to delete this include range?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=DiscoveryServletConstants.removeIncludeRangeAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteIncludeUrl(i){
	if(confirm("Are you sure you want to delete this include URL?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=DiscoveryServletConstants.removeIncludeUrlAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteExcludeUrl(i){
	if(confirm("Are you sure you want to delete this include URL?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=DiscoveryServletConstants.removeExcludeUrlAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteER(i){
	if(confirm("Are you sure you want to delete this exclude range?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=DiscoveryServletConstants.removeExcludeRangeAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function restartDiscovery(){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=DiscoveryServletConstants.saveAndRestartAction%>";
	return true;
}
</script>

<%!

/**
 * TODO: Use a better constant for this value
 */
private static final String DEFAULT_FOREIGN_SOURCE = "default";

%>

<%

WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

HttpSession sess = request.getSession(false);
DiscoveryConfiguration currConfig  = (DiscoveryConfiguration) sess.getAttribute(DiscoveryScanServlet.ATTRIBUTE_DISCOVERY_CONFIGURATION);
// If there's no config in the session yet, create a new blank config
if (currConfig == null) {
  currConfig = new DiscoveryConfiguration();
  // Set the timeout and retries to the default values
  currConfig.setTimeout(DiscoveryConfigFactory.DEFAULT_TIMEOUT);
  currConfig.setRetries(DiscoveryConfigFactory.DEFAULT_RETRIES);
  sess.setAttribute(DiscoveryScanServlet.ATTRIBUTE_DISCOVERY_CONFIGURATION, currConfig);
}

// Map of primary key to label (which in this case are the same)
MonitoringLocationDao locationDao = context.getBean(MonitoringLocationDao.class);
Map<String,String> locations = new TreeMap<String,String>();
for (OnmsMonitoringLocation location : locationDao.findAll()) {
  locations.put(location.getLocationName(), location.getLocationName());
}

// Map of primary key to label (which in this case are the same too)
RequisitionAccessService reqAccessService = context.getBean(RequisitionAccessService.class);
Map<String,String> foreignsources = new TreeMap<String,String>();
for (Requisition requisition : reqAccessService.getRequisitions()) {
  if (! "Minions".equals(requisition.getForeignSource())) {
    foreignsources.put(requisition.getForeignSource(), requisition.getForeignSource());
  }
}

%>

<form role="form" class="form" method="post" id="modifyDiscoveryConfig" name="modifyDiscoveryConfig" action="<%= Util.calculateUrlBase(request, "admin/discovery/scanConfig") %>" onsubmit="return restartDiscovery();">

<input type="hidden" id="specificipaddress" name="specificipaddress" value=""/>
<input type="hidden" id="specifictimeout" name="specifictimeout" value=""/>
<input type="hidden" id="specificretries" name="specificretries" value=""/>
<input type="hidden" id="specificforeignsource" name="specificforeignsource" value=""/>
<input type="hidden" id="specificlocation" name="specificlocation" value=""/>

<input type="hidden" id="iuurl" name="iuurl" value=""/>
<input type="hidden" id="iutimeout" name="iutimeout" value=""/>
<input type="hidden" id="iuretries" name="iuretries" value=""/>
<input type="hidden" id="iuforeignsource" name="iuforeignsource" value=""/>
<input type="hidden" id="iulocation" name="iulocation" value=""/>

<input type="hidden" id="euurl" name="euurl" value=""/>
<input type="hidden" id="euforeignsource" name="euforeignsource" value=""/>
<input type="hidden" id="eulocation" name="eulocation" value=""/>

<input type="hidden" id="irbase" name="irbase" value=""/>
<input type="hidden" id="irend" name="irend" value=""/>
<input type="hidden" id="irtimeout" name="irtimeout" value=""/>
<input type="hidden" id="irretries" name="irretries" value=""/>
<input type="hidden" id="irforeignsource" name="irforeignsource" value=""/>
<input type="hidden" id="irlocation" name="irlocation" value=""/>

<input type="hidden" id="erbegin" name="erbegin" value=""/>
<input type="hidden" id="erend" name="erend" value=""/>
<input type="hidden" id="erlocation" name="erlocation" value=""/>

<button type="submit" class="btn btn-secondary mt-2 mb-4">Start Discovery Scan</button>

<div class="row">
  <div class="col-sm-12 col-md-10 col-lg-8">
    <div class="card">

        <div class="card-header">
            <span>General Settings</span>
        </div>
        <div class="card-body">
            <div class="form-group form-row">
                <label for="retries" class="col-form-label col-md-4">Timeout (milliseconds)</label>
                <input type="text" class="form-control col-md-8" id="timeout" name="timeout" value="<%=currConfig.getTimeout().orElse(DiscoveryConfigFactory.DEFAULT_TIMEOUT)%>"/>
            </div> <!-- form-group -->
            <div class="form-group form-row">
                <label for="retries" class="col-form-label col-md-4">Retries</label>
                <input type="text" class="form-control col-md-8" id="retries" name="retries" value="<%=currConfig.getRetries().orElse(DiscoveryConfigFactory.DEFAULT_RETRIES)%>"/>
            </div> <!-- form-group -->
            <div class="form-group form-row">
                <label for="foreignsource" class="col-form-label col-md-4">Foreign Source</label>
                <select id="foreignsource" class="form-control custom-select col-md-8" name="foreignsource">
                    <option value="" <%if (!currConfig.getForeignSource().isPresent()) out.print("selected");%>>None selected</option>
                    <% for (String key : foreignsources.keySet()) { %>
                    <option value="<%=WebSecurityUtils.sanitizeString(key)%>" <%if(key.equals(currConfig.getForeignSource().orElse(null))) out.print("selected");%>><%=WebSecurityUtils.sanitizeString(foreignsources.get(key))%></option>
                    <% } %>
                </select>
            </div> <!-- form-group -->
            <div class="form-group form-row">
                <label for="location" class="col-form-label col-md-4">Location</label>
                <select id="location" class="form-control custom-select col-md-8" name="location">
                    <% for (String key : locations.keySet()) { %>
                    <option value="<%=WebSecurityUtils.sanitizeString(key)%>" <%if(key.equals(currConfig.getLocation().orElse(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID))) out.print("selected");%>><%=WebSecurityUtils.sanitizeString(locations.get(key))%></option>
                    <% } %>
                </select>
            </div> <!-- form-group -->

            <h4 class="">Advanced configuration</h4>
            <div class="form-group form-row">
                <label for="chunksize" class="col-form-label col-md-4">Task chunk size</label>
                <input type="text" class="form-control col-md-8" id="chunksize" name="chunksize" value="<%=currConfig.getChunkSize().orElse(DiscoveryConfigFactory.DEFAULT_CHUNK_SIZE)%>"/>
            </div> <!-- form-group -->
        </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-sm-12 col-md-10 col-lg-8">
    <div class="card">
      <div class="card-header">
        <span>Specific Addresses</span>
      </div>
      <%if(currConfig.getSpecifics().size()>0){
            Specific[] specs = currConfig.getSpecifics().toArray(new Specific[0]);
      %>
				    <table class="table table-sm">
				      <tr>
					<th>IP&nbsp;Address</th>
					<th>Timeout&nbsp;(milliseconds)</th>
					<th>Retries</th>
					<th>Foreign&nbsp;Source</th>
					<th>Location</th>
					<th>Action</th>
				      </tr>
				      <%for(int i=0; i<specs.length; i++){%>
					 <tr>
					  <td><%=specs[i].getAddress()%></td>
					  <td><%=specs[i].getTimeout().isPresent() ? "" + specs[i].getTimeout().get() : "<i>Use Default</i>" %></td>
					  <td><%=specs[i].getRetries().isPresent() ? "" + specs[i].getRetries().get() : "<i>Use Default</i>" %></td>
					  <td><%=specs[i].getForeignSource().isPresent() ? specs[i].getForeignSource().get() : "<i>Use Default</i>" %></td>
					  <td><%=specs[i].getLocation().isPresent() ? specs[i].getLocation().get() : "<i>Use Default</i>" %></td>
					  <td width="1%"><button type="button" class="btn btn-sm btn-secondary" onclick="deleteSpecific(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>
				     </table>
      <% } else { // end if currConfig.getSpecificsCount()>0 %>
      <div class="card-body">
        <strong>No specifics found.</strong>
      </div>
      <% } %>
      <div class="card-footer">
        <button type="button" class="btn btn-secondary pull-right" onclick="addSpecific();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-sm-12 col-md-10 col-lg-8">
    <div class="card">
      <div class="card-header">
        <span>Include URLs</span>
      </div>
			    <%if(currConfig.getIncludeUrls().size()>0){
			        IncludeUrl[] urls = currConfig.getIncludeUrls().toArray(new IncludeUrl[0]);
			    %>
				    <table class="table table-sm">
				      <tr>
					<th>URL</th>
					<th>Timeout&nbsp;(milliseconds)</th>
					<th>Retries</th> 
					<th>Foreign&nbsp;Source</th>
					<th>Location</th>
					<th>Action</th>
				      </tr>
				      <%for(int i=0; i<urls.length; i++){%>
					 <tr class="text-center">
					  <td><%=urls[i].getUrl().get()%></td>
					  <td><%=urls[i].getTimeout().isPresent() ? "" + urls[i].getTimeout().get() : "<i>Use Default</i>" %></td>
					  <td><%=urls[i].getRetries().isPresent() ? "" + urls[i].getRetries().get() : "<i>Use Default</i>" %></td>
					  <td><%=urls[i].getForeignSource().isPresent() ? urls[i].getForeignSource().get() : "<i>Use Default</i>" %></td>
					  <td><%=urls[i].getLocation().isPresent() ? urls[i].getLocation().get() : "<i>Use Default</i>" %></td>
					  <td width="1%"><button type="button" class="btn btn-sm btn-secondary" onclick="deleteIncludeUrl(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>
				     </table>
      <% } else { // end if currConfig.getIncludeUrlCount()>0 %>
      <div class="card-body">
        <strong>No include URLs found.</strong>
      </div>
      <% } %>
      <div class="card-footer">
        <button type="button" class="btn btn-secondary pull-right" onclick="addIncludeUrl();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-sm-12 col-md-10 col-lg-8">
    <div class="card">
      <div class="card-header">
        <span>Exclude URLs</span>
      </div>
			    <%if(currConfig.getExcludeUrls().size()>0){
			        ExcludeUrl[] urls = currConfig.getExcludeUrls().toArray(new ExcludeUrl[0]);
			    %>
				    <table class="table table-sm">
				      <tr>
					<th>URL</th>
					<th>Foreign&nbsp;Source</th>
					<th>Location</th>
					<th>Action</th>
				      </tr>
				      <%for(int i=0; i<urls.length; i++){%>
					 <tr class="text-center">
					  <td><%=urls[i].getUrl()%></td>
					  <td><%=urls[i].getForeignSource().isPresent() ? urls[i].getForeignSource().get() : "<i>Use Default</i>" %></td>
					  <td><%=urls[i].getLocation().isPresent() ? urls[i].getLocation().get() : "<i>Use Default</i>" %></td>
					  <td width="1%"><button type="button" class="btn btn-sm btn-secondary" onclick="deleteExcludeUrl(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>
				     </table>
      <% } else { // end if currConfig.getExcludeUrlCount()>0 %>
      <div class="card-body">
        <strong>No exclude URLs found.</strong>
      </div>
      <% } %>
      <div class="card-footer">
        <button type="button" class="btn btn-secondary pull-right" onclick="addExcludeUrl();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-sm-12 col-md-10 col-lg-8">
    <div class="card">
      <div class="card-header">
        <span>Include Ranges</span>
      </div>
				    <%if(currConfig.getIncludeRanges().size()>0){
					    IncludeRange[] irange = currConfig.getIncludeRanges().toArray(new IncludeRange[0]);
				    %>
					    <table class="table table-sm">
					      <tr>
						<th>Begin&nbsp;Address</th>
						<th>End&nbsp;Address</th>
						<th>Timeout&nbsp;(milliseconds)</th>
						<th>Retries</th>
						<th>Foreign&nbsp;Source</th>
						<th>Location</th>
						<th>Action</th>
					      </tr>
					      <%for(int i=0; i<irange.length; i++){

					      %>
						 <tr>
						  <td><%=irange[i].getBegin()%></td>
						  <td><%=irange[i].getEnd()%></td>
						  <td><%=irange[i].getTimeout().isPresent() ? "" + irange[i].getTimeout().get() : "<i>Use Default</i>" %></td>
						  <td><%=irange[i].getRetries().isPresent() ? "" + irange[i].getRetries().get() : "<i>Use Default</i>" %></td>
						  <td><%=irange[i].getForeignSource().isPresent() ? irange[i].getForeignSource().get() : "<i>Use Default</i>" %></td>
						  <td><%=irange[i].getLocation().isPresent() ? irange[i].getLocation().get() : "<i>Use Default</i>" %></td>
						  <td width="1%"><button type="button" class="btn btn-sm btn-secondary" onclick="deleteIR(<%=i%>);">Delete</button></td>
						</tr>
					      <%} // end for%>
					     </table>
      <% } else { // end if currConfig.getIncludeRange()>0 %>
      <div class="card-body">
        <strong>No include ranges found.</strong>
      </div>
      <% } %>
      <div class="card-footer">
        <button type="button" class="btn btn-secondary pull-right" onclick="addIncludeRange();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-sm-12 col-md-10 col-lg-8">
    <div class="card">
      <div class="card-header">
        <span>Exclude Ranges</span>
      </div>
			    <%if(currConfig.getExcludeRanges().size()>0){
				    ExcludeRange[] irange = currConfig.getExcludeRanges().toArray(new ExcludeRange[0]);
			    %>
				    <table class="table table-sm">
				      <tr>
					<th>Begin</th>
					<th>End</th>
					<th>Location</th>
					<th>Action</th>
				      </tr>
				      <%for(int i=0; i<irange.length; i++){

				      %>
					 <tr>
					  <td><%=irange[i].getBegin()%></td>
					  <td><%=irange[i].getEnd()%></td>
				      <td><%=irange[i].getLocation().isPresent() ? irange[i].getLocation().get() : "<i>Use Default</i>" %></td>
					  <td width="1%"><button type="button" class="btn btn-sm btn-secondary" onclick="deleteER(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>

				     </table>
      <% } else { // end if currConfig.getExcludeRangeCount()>0 %>
      <div class="card-body">
        <strong>No exclude ranges found.</strong>
      </div>
      <% } %>
      <div class="card-footer">
        <button type="button" class="btn btn-secondary pull-right" onclick="addExcludeRange();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<button type="submit" class="btn btn-secondary mt-2 mb-4">Start Discovery Scan</button>

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
