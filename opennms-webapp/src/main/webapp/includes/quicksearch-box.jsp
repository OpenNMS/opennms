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

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Quick Search</h3>
  </div>
  <div class="panel-body">
    <form class="form-inline" action="element/nodeList.htm" method="get">
      <label for="nodeId">Node ID:</label><br/>
      <input type="hidden" name="listInterfaces" value="false"/>
      <input class="form-control" type="text" name="nodeId" />
      <input name="nodeIdSearchButton" class="form-control btn btn-default" type="submit" value="Search"/>
    </form>
    <br/>
    <form class="form-inline" action="element/nodeList.htm" method="get">
      <label for="nodename">Node label like:</label><br/>
      <input type="hidden" name="listInterfaces" value="true"/>
      <input class="form-control" type="text" name="nodename" />
      <input class="form-control btn btn-default" type="submit" value="Search"/>
    </form>
    <br/>
    <form class="form-inline" action="element/nodeList.htm" method="get">
      <label for="iplike">TCP/IP Address like:</label><br/>
      <input type="hidden" name="listInterfaces" value="false"/>
      <input class="form-control" type="text" name="iplike" value="" placeholder="*.*.*.* or *:*:*:*:*:*:*:*" />
      <input class="form-control btn btn-default" type="submit" value="Search"/>
    </form>
    <br/>
    <form class="form-inline" action="element/nodeList.htm" method="get">
      <label for="service">Providing service:</label><br/>
      <input type="hidden" name="listInterfaces" value="false"/>
      <select class="form-control" name="service">
      <c:forEach var="serviceNameId" items="${serviceNameMap}">
        <option value="${serviceNameId.value}">${serviceNameId.key}</option>
      </c:forEach>
      </select>
      <input class="form-control btn btn-default" type="submit" value="Search"/>
    </form>
  </div>
</div>
