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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.api.Util, org.opennms.netmgt.config.discovery.*, org.opennms.web.admin.discovery.ActionDiscoveryServlet" %>
<%
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>
<% String breadcrumb1 = "<a href='admin/index.jsp'> Admin </a>"; %>
<% String breadcrumb2 = "<a href='admin/discovery/index.jsp'> Discovery </a>"; %>
<% String breadcrumb3 = "Modify Configuration"; %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Modify Discovery Configuration" />
  <jsp:param name="headTitle" value="Discovery" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<script type="text/javascript">
function addSpecific(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-specific.jsp" )%>', 'AddSpecific', 'toolbar=0,width=700,height=350, left=0, top=0, resizable=1, scrollbars=1')
}

function addIncludeRange(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-ir.jsp" )%>', 'AddIncludeRange', 'toolbar=0,width=750 ,height=500, left=0, top=0, resizable=1, scrollbars=1')
}

function addIncludeUrl(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-url.jsp" )%>', 'AddIncludeUrl', 'toolbar=0,width=750 ,height=350, left=0, top=0, resizable=1, scrollbars=1')
}

function addExcludeRange(){
	window.open('<%=org.opennms.web.api.Util.calculateUrlBase( request, "admin/discovery/add-er.jsp" )%>', 'AddExcludeRange', 'toolbar=0,width=600 ,height=350, left=0, top=0, resizable=1, scrollbars=1')
}


function deleteSpecific(i){
      if(confirm("Are you sure you want to delete the 'Specific'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeSpecificAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteIR(i){
      if(confirm("Are you sure you want to delete the 'Include Range'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeIncludeRangeAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteIncludeUrl(i){
    if(confirm("Are you sure you want to delete the 'Include URL'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeIncludeUrlAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteER(i){
      if(confirm("Are you sure you want to delete the 'Exclude Range'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeExcludeRangeAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function restartDiscovery(){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.saveAndRestartAction%>";
	return true;
}
</script>

<%
HttpSession sess = request.getSession(false);
DiscoveryConfiguration currConfig  = (DiscoveryConfiguration) sess.getAttribute("discoveryConfiguration");
if (currConfig == null) {
	throw new ServletException("The session expired while editing the discovery configuration. Please revisit the page to resume editing the configuration.");
}
%>

<form role="form" class="form-horizontal" method="post" id="modifyDiscoveryConfig" name="modifyDiscoveryConfig" action="<%= Util.calculateUrlBase(request, "admin/discovery/actionDiscovery") %>" onsubmit="return restartDiscovery();">
<input type="hidden" id="specificipaddress" name="specificipaddress" value=""/>
<input type="hidden" id="specifictimeout" name="specifictimeout" value=""/>
<input type="hidden" id="specificretries" name="specificretries" value=""/>

<input type="hidden" id="iuurl" name="iuurl" value=""/>
<input type="hidden" id="iutimeout" name="iutimeout" value=""/>
<input type="hidden" id="iuretries" name="iuretries" value=""/>

<input type="hidden" id="irbase" name="irbase" value=""/>
<input type="hidden" id="irend" name="irend" value=""/>
<input type="hidden" id="irtimeout" name="irtimeout" value=""/>
<input type="hidden" id="irretries" name="irretries" value=""/>

<input type="hidden" id="specificipaddress" name="specificipaddress" value=""/>
<input type="hidden" id="specifictimeout" name="specifictimeout" value=""/>
<input type="hidden" id="specificretries" name="specificretries" value=""/>

<input type="hidden" id="erbegin" name="erbegin" value=""/>
<input type="hidden" id="erend" name="erend" value=""/>

<div class="row">
  <div class="col-md-4">
    <button type="submit" class="btn btn-default">Save and Restart Discovery</button>
  </div> <!-- column -->
</div> <!-- row -->

<div class="row top-buffer">
  <div class="col-md-4">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">General Settings</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="initialsleeptime" class="col-sm-4 control-label">Initial sleep time (sec.):</label>
          <div class="col-sm-8">
            <select id="initialsleeptime" class="form-control" name="initialsleeptime">
              <option value="30000" <%if(currConfig.getInitialSleepTime()==30000) out.print("selected");%>>30</option>
              <option value="60000" <%if(currConfig.getInitialSleepTime()==60000) out.print("selected");%>>60</option>
              <option value="90000" <%if(currConfig.getInitialSleepTime()==90000) out.print("selected");%>>90</option>
              <option value="120000" <%if(currConfig.getInitialSleepTime()==120000) out.print("selected");%>>120</option>
              <option value="150000" <%if(currConfig.getInitialSleepTime()==150000) out.print("selected");%>>150</option>
              <option value="300000" <%if(currConfig.getInitialSleepTime()==300000) out.print("selected");%>>300</option>
              <option value="600000" <%if(currConfig.getInitialSleepTime()==600000) out.print("selected");%>>600</option>
            </select>
          </div>
        </div> <!-- form-group -->
        <div class="form-group">
          <label for="restartsleeptime" class="col-sm-4 control-label">Restart sleep time (hours):</label>
          <div class="col-sm-8">
            <select id="restartsleeptime" class="form-control" name="restartsleeptime">
              <option value="3600000" <%if(currConfig.getRestartSleepTime()==3600000) out.print("selected");%>>1</option>
              <option value="7200000" <%if(currConfig.getRestartSleepTime()==7200000) out.print("selected");%>>2</option>
              <option value="10800000" <%if(currConfig.getRestartSleepTime()==10800000) out.print("selected");%>>3</option>
              <option value="14400000" <%if(currConfig.getRestartSleepTime()==14400000) out.print("selected");%>>4</option>
              <option value="18000000" <%if(currConfig.getRestartSleepTime()==18000000) out.print("selected");%>>5</option>
              <option value="21600000" <%if(currConfig.getRestartSleepTime()==21600000) out.print("selected");%>>6</option>
              <option value="43200000" <%if(currConfig.getRestartSleepTime()==43200000) out.print("selected");%>>12</option>
              <option value="86400000" <%if(currConfig.getRestartSleepTime()==86400000) out.print("selected");%>>24</option>
              <option value="129600000" <%if(currConfig.getRestartSleepTime()==129600000) out.print("selected");%>>36</option>
              <option value="259200000" <%if(currConfig.getRestartSleepTime()==259200000) out.print("selected");%>>72</option>
            </select>
          </div>
        </div> <!-- form-group -->
        <div class="form-group">
          <label for="threads" class="col-sm-4 control-label">Threads:</label>
          <div class="col-sm-8">
            <select id="threads" class="form-control" name="threads">
              <option value="1" <%if(currConfig.getThreads()==1) out.print("selected");%>>1</option>
              <option value="2" <%if(currConfig.getThreads()==2) out.print("selected");%>>2</option>
              <option value="3" <%if(currConfig.getThreads()==3) out.print("selected");%>>3</option>
              <option value="4" <%if(currConfig.getThreads()==4) out.print("selected");%>>4</option>
              <option value="5" <%if(currConfig.getThreads()==5) out.print("selected");%>>5</option>
              <option value="6" <%if(currConfig.getThreads()==6) out.print("selected");%>>6</option>
              <option value="7" <%if(currConfig.getThreads()==7) out.print("selected");%>>7</option>
              <option value="8" <%if(currConfig.getThreads()==8) out.print("selected");%>>8</option>
              <option value="9" <%if(currConfig.getThreads()==9) out.print("selected");%>>9</option>
              <option value="10" <%if(currConfig.getThreads()==10) out.print("selected");%>>10</option>
              <option value="15" <%if(currConfig.getThreads()==15) out.print("selected");%>>15</option>
            </select>
          </div>
        </div> <!-- form-group -->
        <div class="form-group">
          <label for="retries" class="col-sm-4 control-label">Retries:</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" id="retries" name="retries" value="<%=((currConfig.getRetries()==0)?"3":""+currConfig.getRetries())%>"/>
          </div>
        </div> <!-- form-group -->
        <div class="form-group">
          <label for="retries" class="col-sm-4 control-label">Timeout (ms.):</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" id="timeout" name="timeout" size="4" value="<%=((currConfig.getTimeout()==0)?"800":""+currConfig.getTimeout())%>"/>
          </div>
        </div> <!-- form-group -->
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row top-buffer">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Specifics</h3>
      </div>
      <%if(currConfig.getSpecificCount()>0){
            Specific[] specs = currConfig.getSpecific();
      %>
				    <table class="table table-bordered table-condensed">
				      <tr>
					<th>
					    IP Address
					</th>
					<th>
					    Timeout (ms.)
					</th>
					<th>
					    Retries
					</th>
					<th>
					    Action
					</th>
				      </tr>
				      <%for(int i=0; i<specs.length; i++){%>
					 <tr class="text-center">
					  <td><%=specs[i].getContent()%></td>
					  <td><%=(specs[i].getTimeout()!=0)?""+specs[i].getTimeout():""+currConfig.getTimeout() %></td>
					  <td><%=(specs[i].getRetries()!=0)?""+specs[i].getRetries():""+currConfig.getRetries() %></td>
					  <td width="1%"><button type="button" class="btn btn-default" onclick="deleteSpecific(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>
				     </table>
      <% } else { // end if currConfig.getSpecificsCount()>0 %>
      <div class="panel-body">
        <strong>No specifics found.</strong>
      </div>
      <% } %>
      <div class="panel-footer">
        <button type="button" class="btn btn-default" onclick="addSpecific();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row top-buffer">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Include URLs</h3>
      </div>
			    <%if(currConfig.getIncludeUrlCount()>0){
			        IncludeUrl[] urls = currConfig.getIncludeUrl();
			    %>
				    <table class="table table-bordered table-condensed">
				      <tr>
					<th>
					    URL
					</th>
					<th>
					    Timeout (ms.)
					</th>
					<th>
					    Retries
					</th>
					<th>
					    Action
					</th>
				      </tr>
				      <%for(int i=0; i<urls.length; i++){%>
					 <tr class="text-center">
					  <td><%=urls[i].getContent()%></td>
					  <td><%=(urls[i].getTimeout()!=0)?""+urls[i].getTimeout():""+currConfig.getTimeout() %></td>
					  <td><%=(urls[i].getRetries()!=0)?""+urls[i].getRetries():""+currConfig.getRetries() %></td>
					  <td width="1%"><button type="button" onclick="deleteIncludeUrl(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>
				     </table>
      <% } else { // end if currConfig.getIncludeUrlCount()>0 %>
      <div class="panel-body">
        <strong>No include URLs found.</strong>
      </div>
      <% } %>
      <div class="panel-footer">
        <button type="button" class="btn btn-default" onclick="addIncludeUrl();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row top-buffer">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Include Ranges</h3>
      </div>
				    <%if(currConfig.getIncludeRangeCount()>0){
					    IncludeRange[] irange = currConfig.getIncludeRange();
				    %>
					    <table class="table table-bordered table-condensed">
					      <tr>
						<th>
						    Begin Address
						</th>
						<th>
						    End Address
						</th>
						<th>
						    Timeout (ms.)
						</th>
						<th>
						    Retries
						</th>
						<th>
						    Action
						</th>
					      </tr>
					      <%for(int i=0; i<irange.length; i++){

					      %>
						 <tr class="text-center">
						  <td><%=irange[i].getBegin()%></td>
						  <td><%=irange[i].getEnd()%></td>
						  <td><%=(irange[i].getTimeout()!=0)?""+irange[i].getTimeout():""+currConfig.getTimeout() %></td>
						  <td><%=(irange[i].getRetries()!=0)?""+irange[i].getRetries():""+currConfig.getRetries() %></td>
						  <td width="1%"><button type="button" onclick="deleteIR(<%=i%>);">Delete</button></td>
						</tr>
					      <%} // end for%>
					     </table>
      <% } else { // end if currConfig.getIncludeRange()>0 %>
      <div class="panel-body">
        <strong>No include ranges found.</strong>
      </div>
      <% } %>
      <div class="panel-footer">
        <button type="button" class="btn btn-default" onclick="addIncludeRange();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row top-buffer">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Exclude Ranges</h3>
      </div>
			    <%if(currConfig.getExcludeRangeCount()>0){
				    ExcludeRange[] irange = currConfig.getExcludeRange();
			    %>
				    <table class="table table-bordered table-condensed">
				      <tr>
					<th>
					    Begin
					</th>
					<th>
					    End
					</th>
					<th>
					    Action
					</th>
				      </tr>
				      <%for(int i=0; i<irange.length; i++){

				      %>
					 <tr class="text-center">
					  <td><%=irange[i].getBegin()%></td>
					  <td><%=irange[i].getEnd()%></td>
					  <td width="1%"><button type="button" onclick="deleteER(<%=i%>);">Delete</button></td>
					</tr>
				      <%} // end for%>

				     </table>
      <% } else { // end if currConfig.getExcludeRangeCount()>0 %>
      <div class="panel-body">
        <strong>No exclude ranges found.</strong>
      </div>
      <% } %>
      <div class="panel-footer">
        <button type="button" class="btn btn-default" onclick="addExcludeRange();">Add New</button>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row top-buffer">
  <div class="col-md-4">
    <button type="submit" class="btn btn-default">Save and Restart Discovery</button>
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
