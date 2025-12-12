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
<%@page language="java" contentType="text/html" session="true"
        import="java.util.*,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation"%>

<%@ page import="com.google.common.base.Strings" %>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="element" tagdir="/WEB-INF/tags/element" %>

<c:if test="${model.nodeCount == 1 && command.snmpParm == null && command.maclike == null}">
  <jsp:forward page="/element/node.jsp?node=${model.nodes[0].node.id}"/>
</c:if>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Node List")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node List")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<!-- NMS-7099: Add custom javascripts AFTER the header was included -->
<script type="text/javascript">
    function toggleClassDisplay(clazz, displayA, displayB) {
        var targetElems = document.querySelectorAll("." + clazz);
        for (var i = 0; i < targetElems.length; i++) {
            var e = targetElems[i];
            if (e.style.display == displayA) {
                e.style.display = displayB;
            } else {
                e.style.display = displayA;
            }
        }
    }
</script>

<%
  List<OnmsMonitoringLocation> monitoringLocations = NetworkElementFactory.getInstance(getServletContext()).getMonitoringLocations();

  String selectedMonitoringLocation = "";

  if (request.getParameterMap().containsKey("monitoringLocation")) {
    selectedMonitoringLocation = request.getParameter("monitoringLocation");
  }
%>

<c:set var="anyFlows" value="false" scope="page"/>
<c:forEach var="node" items="${model.nodes}">
  <c:if test="${node.node.hasFlows}">
    <c:set var="anyFlows" value="true" scope="page"/>
  </c:if>
</c:forEach>

<div class="card">
  <div class="card-header">
<c:choose>
  <c:when test="${command.listInterfaces}">
    <span>Nodes and their interfaces</span>
  </c:when>

  <c:otherwise>
    <select style="width: 150px" class="form-control custom-select pull-right" id="monitoringLocation" onchange="javascript:location.href = location.protocol + '//' + location.host + location.pathname + '?monitoringLocation=' + this.options[this.selectedIndex].value;">
      <%
        if ("".equals(selectedMonitoringLocation)) {
      %>
      <option value="" selected>All locations</option>
      <%
      } else {
      %>
      <option value="">All locations</option>
      <%
        }

        for (OnmsMonitoringLocation monitoringLocation : monitoringLocations) {
          if (selectedMonitoringLocation.equals(monitoringLocation.getLocationName())) {
      %>
      <option value="<%=WebSecurityUtils.sanitizeString(monitoringLocation.getLocationName())%>" selected><%=WebSecurityUtils.sanitizeString(monitoringLocation.getLocationName())%></option>
      <%
      } else {
      %>
      <option value="<%=WebSecurityUtils.sanitizeString(monitoringLocation.getLocationName())%>"><%=WebSecurityUtils.sanitizeString(monitoringLocation.getLocationName())%></option>
      <%
          }
        }
      %>
    </select>
    <div class="btn-toolbar" role="toolbar">
      <span>Nodes</span>
      <span class="btn-group mr-2 ml-4"><a href="javascript:toggleClassDisplay('NLdbid', '', 'inline');"><i class="fas fa-database fa-lg" title="Toggle database IDs"></i></a>&nbsp;&nbsp;<a href="javascript:toggleClassDisplay('NLfs', '', 'inline');"><i class="fas fa-rectangle-list fa-lg" title="Toggle requisition names"></i></a>&nbsp;&nbsp;<a href="javascript:toggleClassDisplay('NLfid', '', 'inline');"><i class="fas fa-qrcode fa-lg" title="Toggle foreign IDs"></i></a>&nbsp;&nbsp;<a href="javascript:toggleClassDisplay('NLloc', '', 'inline');"><i class="fas fa-location-dot fa-lg" title="Toggle locations"></i></a> <c:if test="${anyFlows}">&nbsp;<a href="javascript:toggleClassDisplay('NLflows', '', 'inline');"><i class="fas fa-right-left fa-lg" title="Toggle flow data"></i></a></span></c:if>
    </div>
  </c:otherwise>
</c:choose>
  </div> <!-- card-header -->
  <div class="card-body">
  <c:choose>
    <c:when test="${model.nodeCount == 0}">
      <p>
        None found.
      </p>
    </c:when>

    <c:otherwise>
      <div class="row">
        <div class="col-md-6">
          <element:nodelist nodes="${model.nodesLeft}" snmpParm="${command.snmpParm}" isMaclikeSearch="${command.maclike != null}"/>
               </div>

        <div class="col-md-6">
          <element:nodelist nodes="${model.nodesRight}" snmpParm="${command.snmpParm}" isMaclikeSearch="${command.maclike != null}"/>
        </div>
      </div>
    </c:otherwise>
  </c:choose>
  </div> <!-- card-body -->
</div> <!-- panel -->

<p>
  <c:choose>
    <c:when test="${model.nodeCount == 1}">
      <c:set var="nodePluralized" value="Node"/>
    </c:when>

    <c:otherwise>
      <c:set var="nodePluralized" value="Nodes"/>
    </c:otherwise>
  </c:choose>

  <c:choose>
    <c:when test="${model.interfaceCount == 1}">
      <c:set var="interfacePluralized" value="Interface"/>
    </c:when>

    <c:otherwise>
      <c:set var="interfacePluralized" value="Interfaces"/>
    </c:otherwise>
  </c:choose>

  <c:choose>
    <c:when test="${command.listInterfaces}">
      ${model.nodeCount} ${nodePluralized}, ${model.interfaceCount} ${interfacePluralized}
    </c:when>

    <c:otherwise>
      ${model.nodeCount} ${nodePluralized}
    </c:otherwise>
  </c:choose>

  <c:url var="thisURL" value="${relativeRequestPath}">
    <c:if test="${command.nodename != null}">
      <c:param name="nodename" value="${command.nodename}"/>
    </c:if>
    <c:if test="${command.monitoringLocation != null}">
      <c:param name="monitoringLocation" value="${command.monitoringLocation}"/>
    </c:if>
    <c:if test="${command.iplike != null}">
      <c:param name="iplike" value="${command.iplike}"/>
    </c:if>
    <c:if test="${command.service != null}">
      <c:param name="service" value="${command.service}"/>
    </c:if>
    <c:if test="${command.mib2Parm != null}">
      <c:param name="mib2Parm" value="${command.mib2Parm}"/>
      <c:param name="mib2ParmValue" value="${command.mib2ParmValue}"/>
      <c:param name="mib2ParmMatchType" value="${command.mib2ParmMatchType}"/>
    </c:if>
    <c:if test="${command.snmpParm != null}">
      <c:param name="snmpParm" value="${command.snmpParm}"/>
      <c:param name="snmpParmValue" value="${command.snmpParmValue}"/>
      <c:param name="snmpParmMatchType" value="${command.snmpParmMatchType}"/>
    </c:if>
    <c:if test="${command.maclike != null}">
      <c:param name="maclike" value="${command.maclike}"/>
    </c:if>
    <c:if test="${command.foreignSource != null}">
      <c:param name="foreignSource" value="${command.foreignSource}"/>
    </c:if>
    <c:if test="${command.category1 != null}">
      <c:forEach var="category" items="${command.category1}">
        <c:param name="category1" value="${category}"/>
      </c:forEach>
    </c:if>
    <c:if test="${command.category2 != null}">
      <c:forEach var="category" items="${command.category2}">
        <c:param name="category2" value="${category}"/>
      </c:forEach>
    </c:if>
    <c:if test="${command.statusViewName != null}">
      <c:param name="statusViewName" value="${command.statusViewName}"/>
    </c:if>
    <c:if test="${command.statusSite != null}">
      <c:param name="statusSite" value="${command.statusSite}"/>
    </c:if>
    <c:if test="${command.statusRowLabel != null}">
      <c:param name="statusRowLabel" value="${command.statusRowLabel}"/>
    </c:if>
    <c:if test="${command.nodesWithOutages}">
      <c:param name="nodesWithOutages" value="${command.nodesWithOutages}"/>
    </c:if>
    <c:if test="${command.nodesWithDownAggregateStatus}">
      <c:param name="nodesWithDownAggregateStatus" value="${command.nodesWithDownAggregateStatus}"/>
    </c:if>
    <c:if test="${!command.listInterfaces}">
      <c:param name="listInterfaces" value="${!command.listInterfaces}"/>
    </c:if>
  </c:url>

  <c:choose>
    <c:when test="${!command.listInterfaces}">
    <a href="${thisURL}">Show interfaces</a>
    </c:when>
    <c:otherwise>
    <a href="${thisURL}">Hide interfaces</a>
    </c:otherwise>
  </c:choose>
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
