<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Application" />
	<jsp:param name="headTitle" value="Application" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/applications.htm'>Applications</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<div class="row">
  <div class="col-md-8">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Application: ${fn:escapeXml(model.application.name)}</h3>
      </div>
      <div class="panel-body">
        <p>
        Application '${fn:escapeXml(model.application.name)}' has ${fn:length(model.memberServices)} services.
        </p>

        <p>
        <a href="admin/applications.htm?edit=edit&applicationid=${model.application.id}">Edit application</a>
        </p>

        <table class="table table-condensed">
          <tr>
            <th>Node</th>
            <th>Interface</th>
            <th>Service</th>
          </tr>
          <c:forEach items="${model.memberServices}" var="service">
            <tr>
              <td><a href="element/node.jsp?node=${service.ipInterface.node.id}">${fn:escapeXml(service.ipInterface.node.label)}</a></td>
              <td><a href="element/interface.jsp?ipinterfaceid=${service.ipInterface.id}">${service.ipInterface.ipAddress.hostAddress}</a></td>
              <td><a href="element/service.jsp?ifserviceid=${service.id}">${fn:escapeXml(service.serviceName)}</a></td>
            </tr>
          </c:forEach>
        </table>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
