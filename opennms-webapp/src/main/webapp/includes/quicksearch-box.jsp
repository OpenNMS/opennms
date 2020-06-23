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
