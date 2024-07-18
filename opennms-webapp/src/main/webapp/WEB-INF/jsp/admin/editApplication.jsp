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
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Application")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Applications", "admin/applications.htm")
          .breadcrumb("Show")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

    <div class="card">
      <div class="card-header">
        <span>Edit application ${fn:escapeXml(model.application.name)}</span>
      </div>
      <div class="card-body">
        <p>
        Application '${fn:escapeXml(model.application.name)}' has ${fn:length(model.sortedMemberServices)} services
        </p>

        <h5>Defined service(s):</h5>

        <form class="form-row" role="form" action="admin/applications.htm" method="get">
          <input type="hidden" name="applicationid" value="${model.application.id}"/>
          <input type="hidden" name="edit" value="services"/>

          <div class="form-group col-md-5">
            <label for="input_toAdd">Available services</label>
            <select name="serviceAdds" id="input_toAdd" class="form-control" size="15" multiple>
              <c:forEach items="${model.monitoredServices}" var="service">
                <option value="${service.id}">${fn:escapeXml(service.ipInterface.node.label)} / ${service.ipAddressAsString} / ${service.serviceName}</option>
              </c:forEach>
            </select>
          </div>

          <div class="form-group col-md-2 text-center mb-auto mt-auto">
            <input type="submit" id="input_addService" class="btn btn-secondary" name="action" value="Add &#155;&#155;"/>
            <input type="submit" id="input_removeService" class="btn btn-secondary" name="action" value="&#139;&#139; Remove"/>
          </div>

          <div class="form-group col-md-5">
            <label for="input_toDelete">Services on application</label>
            <select name="serviceDeletes" id="input_toDelete" class="form-control" size="15" multiple>
              <c:forEach items="${model.sortedMemberServices}" var="service">
                <option value="${service.id}">${fn:escapeXml(service.ipInterface.node.label)} / ${service.ipAddressAsString} / ${service.serviceName}</option>
              </c:forEach>
            </select>
          </div>
        </form>

        <h5>Associated location(s):</h5>

        <form class="form-row" role="form" action="admin/applications.htm" method="get">
          <input type="hidden" name="applicationid" value="${model.application.id}"/>
          <input type="hidden" name="edit" value="locations"/>

          <div class="form-group col-md-5">
            <label for="input_locationAdd">Available locations</label>
            <select name="locationAdds" id="input_locationAdd" class="form-control" size="10" multiple>
              <c:forEach items="${model.monitoringLocations}" var="location">
                <option value="${fn:escapeXml(location.locationName)}">${fn:escapeXml(location.locationName)}</option>
              </c:forEach>
            </select>
          </div>

          <div class="form-group col-md-2 text-center mb-auto mt-auto">
            <input type="submit" id="input_addLocation" class="btn btn-secondary" name="action" value="Add &#155;&#155;"/>
            <input type="submit" id="input_removeLocation" class="btn btn-secondary" name="action" value="&#139;&#139; Remove"/>
          </div>

          <div class="form-group col-md-5">
            <label for="input_locationDelete">Locations on application</label>
            <select name="locationDeletes" id="input_locationDelete" class="form-control" size="10" multiple>
              <c:forEach items="${model.sortedMemberLocations}" var="location">
                <option value="${fn:escapeXml(location.locationName)}">${fn:escapeXml(location.locationName)}</option>
              </c:forEach>
            </select>
          </div>
        </form>

      </div> <!-- card-body -->
    </div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
