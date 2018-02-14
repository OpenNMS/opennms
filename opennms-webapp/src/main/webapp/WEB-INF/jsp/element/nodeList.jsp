<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true"
        import="java.util.*,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation"%>

<%@ page import="com.google.common.base.Strings" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="element" tagdir="/WEB-INF/tags/element" %>

<c:if test="${model.nodeCount == 1 && command.snmpParm == null && command.maclike == null}">
  <jsp:forward page="/element/node.jsp?node=${model.nodes[0].node.id}"/>
</c:if>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="headTitle" value="Node List" />
  <jsp:param name="location" value="nodelist" />
  <jsp:param name="breadcrumb" value="<a href ='element/index.jsp'>Search</a>"/>
  <jsp:param name="breadcrumb" value="Node List"/>
</jsp:include>

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

<div class="panel panel-default">
  <div class="panel-heading">
<c:choose>
  <c:when test="${command.listInterfaces}">
    <h3 class="panel-title">Nodes and their interfaces</h3>
  </c:when>

  <c:otherwise>
    <select style="float:right;" class="icon-black" id="monitoringLocation" onchange="javascript:location.href = location.protocol + '//' + location.host + location.pathname + '?monitoringLocation=' + this.options[this.selectedIndex].value;">
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
      <option value="<%=monitoringLocation.getLocationName()%>" selected><%=monitoringLocation.getLocationName()%></option>
      <%
      } else {
      %>
      <option value="<%=monitoringLocation.getLocationName()%>"><%=monitoringLocation.getLocationName()%></option>
      <%
          }
        }
      %>
    </select>
    <h3 class="panel-title"><span>Nodes</span><span style="padding-left: 32px;"><a href="javascript:toggleClassDisplay('NLdbid', '', 'inline');"><i class="fa fa-database fa-lg icon-black" title="Toggle database IDs"></i></a>&nbsp;&nbsp;<a href="javascript:toggleClassDisplay('NLfs', '', 'inline');"><i class="fa fa-list-alt fa-lg icon-black" title="Toggle requisition names"></i></a>&nbsp;&nbsp;<a href="javascript:toggleClassDisplay('NLfid', '', 'inline');"><i class="fa fa-qrcode fa-lg icon-black" title="Toggle foreign IDs"></i></a>&nbsp;&nbsp;<a href="javascript:toggleClassDisplay('NLloc', '', 'inline');"><i class="fa fa-map-marker fa-lg icon-black" title="Toggle locations"></i></a></span></h3>
  </c:otherwise>
</c:choose>
  </div> <!-- panel-heading -->
  <div class="panel-body">
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
  </div> <!-- panel-body -->
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
