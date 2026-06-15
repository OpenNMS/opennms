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
<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		java.util.*,
		org.opennms.web.element.*
	"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  pageContext.setAttribute("serviceNameMap", new TreeMap<String,Integer>(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap()).entrySet());
%>

<script>
function submitNodeSearch(params) {
  var parts = [];
  var keys = Object.keys(params);
  for (var i = 0; i < keys.length; i++) {
    var k = keys[i];
    var v = params[k];
    if (v !== '' && v !== null && v !== undefined) {
      parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
    }
  }
  var query = parts.join('&');
  window.location.href = 'ui/#/nodes' + (query ? '?' + query : '');
}
</script>

<div class="card">
  <div class="card-header">
    <span>Quick Search</span>
  </div>
  <div class="card-body">
    <%-- Node ID: submit directly to node detail page --%>
    <form class="form-group" action="element/node.jsp" method="get">
      <label for="nodeId" class=" col-form-label ">Node ID</label>
      <div class="input-group">
        <input class="form-control" type="text" id="nodeId" name="node" placeholder="Node ID"/>
        <div class="input-group-append">
          <button name="nodeIdSearchButton" class="btn btn-secondary" type="submit"><i class="fas fa-magnifying-glass"></i></button>
        </div>
      </div>
    </form>
    <%-- Node label --%>
    <form class="form-group" action="#" method="get" onsubmit="submitNodeSearch({nodename: this.nodename.value}); return false;">
      <label for="nodename" class=" col-form-label ">Node label</label>
      <div class="input-group">
        <input class="form-control" type="text" id="nodename" name="nodename" placeholder="localhost"/>
        <div class="input-group-append">
          <button class="btn btn-secondary" type="submit"><i class="fas fa-magnifying-glass"></i></button>
        </div>
      </div>
    </form>
    <%-- TCP/IP Address --%>
    <form class="form-group" action="#" method="get" onsubmit="submitNodeSearch({iplike: this.iplike.value}); return false;">
      <label for="iplike" class=" col-form-label ">TCP/IP Address</label>
      <div class="input-group">
        <input class="form-control" type="text" id="iplike" name="iplike" placeholder="*.*.*.* or *:*:*:*:*:*:*:*"/>
        <div class="input-group-append">
          <button class="btn btn-secondary" type="submit"><i class="fas fa-magnifying-glass"></i></button>
        </div>
      </div>
    </form>
    <%-- Service (by name) --%>
    <form class="form-group" action="#" method="get" onsubmit="submitNodeSearch({monitoredService: this.monitoredService.value}); return false;">
      <label for="monitoredService" class=" col-form-label ">Providing service</label>
      <div class="input-group">
        <select class="custom-select" id="monitoredService" name="monitoredService">
          <c:forEach var="serviceNameId" items="${serviceNameMap}">
            <option value="${serviceNameId.key}">${serviceNameId.key}</option>
          </c:forEach>
        </select>
        <div class="input-group-append">
          <button class="btn btn-secondary" type="submit"><i class="fas fa-magnifying-glass"></i></button>
        </div>
      </div>
    </form>
  </div>
</div>
