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
        <h3 class="panel-title">Edit application ${fn:escapeXml(model.application.name)}</h3>
      </div>
      <div class="panel-body">
        <p>
        Application '${fn:escapeXml(model.application.name)}' has ${fn:length(model.sortedMemberServices)} services
        </p>

        <div class="row">
        <form role="form" action="admin/applications.htm" method="get">
          <input type="hidden" name="applicationid" value="${model.application.id}"/>
          <input type="hidden" name="edit" value="edit"/>

          <div class="form-group col-md-5">
            <label for="input_toAdd">Available services</label>
            <select name="toAdd" id="input_toAdd" class="form-control" size="20" multiple>
              <c:forEach items="${model.monitoredServices}" var="service">
                <option value="${service.id}">${fn:escapeXml(service.ipInterface.node.label)} / ${service.ipAddressAsString} / ${service.serviceName}</option>
              </c:forEach>
            </select>
          </div>

          <div class="form-group col-md-2 text-center">
            <input type="submit" class="btn btn-default" name="action" value="Add &#155;&#155;"/>
            <input type="submit" class="btn btn-default" name="action" value="&#139;&#139; Remove"/>
          </div>

          <div class="form-group col-md-5">
            <label for="input_toDelete">Services on application</label>
            <select name="toDelete" id="input_toDelete" class="form-control" size="20" multiple>
              <c:forEach items="${model.sortedMemberServices}" var="service">
                <option value="${service.id}">${fn:escapeXml(service.ipInterface.node.label)} / ${service.ipAddressAsString} / ${service.serviceName}</option>
              </c:forEach>
            </select>
          </div>
        </form>
        </div> <!-- row -->
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
