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

<div class="card">
  <div class="card-header">
    <span>Quick Search</span>
  </div>
  <div class="card-body">
    <form class="form-group" action="element/nodeList.htm" method="get">
      <label for="nodeId" class=" col-form-label ">Node ID</label>
      <div class="input-group">
        <input class="form-control" type="text" id="nodeId" name="nodeId" placeholder="Node ID"/>
        <input type="hidden" name="listInterfaces" value="false"/>
        <div class="input-group-append">
          <button name="nodeIdSearchButton" class="btn btn-secondary" type="submit"><i class="fa fa-search"></i></button>
        </div>
      </div>
    </form>
    <form class="form-group" action="element/nodeList.htm" method="get">
      <label for="nodename" class=" col-form-label ">Node label</label>
      <div class="input-group">
        <input class="form-control" type="text" id="nodename" name="nodename" placeholder="localhost"/>
        <input type="hidden" name="listInterfaces" value="true"/>
        <div class="input-group-append">
          <button class="btn btn-secondary" type="submit"><i class="fa fa-search"></i></button>
        </div>
      </div>
    </form>
    <form class="form-group" action="element/nodeList.htm" method="get">
      <label for="iplike" class=" col-form-label ">TCP/IP Address</label>
      <div class="input-group">
        <input class="form-control" type="text" id="iplike" name="iplike" placeholder="*.*.*.* or *:*:*:*:*:*:*:*"/>
        <input type="hidden" name="listInterfaces" value="false"/>
        <div class="input-group-append">
          <button class="btn btn-secondary" type="submit"><i class="fa fa-search"></i></button>
        </div>
      </div>
    </form>
    <form class="form-group" action="element/nodeList.htm" method="get">
      <label for="service" class=" col-form-label ">Providing service</label>
      <div class="input-group">
        <select class="custom-select" id="service" name="service">
          <c:forEach var="serviceNameId" items="${serviceNameMap}">
            <option value="${serviceNameId.value}">${serviceNameId.key}</option>
          </c:forEach>
        </select>
        <input type="hidden" name="listInterfaces" value="false"/>
        <div class="input-group-append">
          <button class="btn btn-secondary" type="submit"><i class="fa fa-search"></i></button>
        </div>
      </div>
    </form>
  </div>
</div>
