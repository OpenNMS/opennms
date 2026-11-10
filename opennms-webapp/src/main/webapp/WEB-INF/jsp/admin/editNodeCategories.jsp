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
          .headTitle("Category")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Category", "admin/categories.htm")
          .breadcrumb("Show")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
function toggleFormEnablement() {
  [ "toAdd", "addButton", "removeButton", "toDelete" ].forEach( function(elemId) {
    var elem = document.getElementById(elemId);
    elem.disabled = (!elem.disabled);
  } );
}
</script>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Edit surveillance categories on ${model.node.label}</span>
      </div>
      <div class="card-body">
        <p>
        Node <a href="<c:url value='element/node.jsp?node=${model.node.id}'/>">${model.node.label}</a> (node ID: ${model.node.id}) has ${fn:length(model.node.categories)} categories 
        </p>

        <form action="admin/categories.htm" method="get">
          <input type="hidden" name="node" value="${model.node.id}"/>
          <input type="hidden" name="edit" value=""/>

        <div class="row">
          <div class="col-md-5">
            <label for="toAdd">Available categories</label>
            <select id="toAdd" class="form-control" name="toAdd" size="20" multiple="true" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if>>
              <c:forEach items="${model.categories}" var="category">
                <option value="${category.id}">${fn:escapeXml(category.name)}</option>
              </c:forEach>
            </select>
          </div>
          <div class="col-md-2 text-center">
             <div class="btn-group-vertical" role="group">
                <button id="addButton" type="submit" class="btn btn-secondary" name="action" value="Add &#155;&#155;" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if> >Add &#155;&#155;</button>
                <button id="removeButton" type="submit" class="btn btn-secondary" name="action" value="&#139;&#139; Remove" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if> >&#139;&#139; Remove</button>
             </div>
          </div>
          <div class="col-md-5">
            <label for="toDelete">Categories on node</label>
            <select id="toDelete" class="form-control" name="toDelete" size="20" multiple="true" <c:if test="${! empty model.node.foreignSource}">disabled="true"</c:if>>
              <c:forEach items="${model.sortedCategories}" var="category">
                <option value="${category.id}">${fn:escapeXml(category.name)}</option>
              </c:forEach>
            </select>
          </div>
        </div> <!-- row -->

        </form>
      </div> <!-- card-body -->
      <div class="card-footer">
        <input id="toggleCheckbox" type="checkbox" onchange="javascript:toggleFormEnablement()" />
        <label for="toggleCheckbox">Check this box to enable controls (see warning above for why)</label>
      </div> <!-- card-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->

<c:if test="${!empty model.node.foreignSource}">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Warning</span>
      </div>
      <div class="card-body">
        <p>
        You are editing category memberships for a node that was provisioned
        through a requisition. Any edits made here will be rolled back the next
        time the requisition "<em>${model.node.foreignSource}</em>" is
        synchronized (typically every 24 hours) or the node manually rescanned.
        To make permanent changes, do one of the following:
        </p>
        <p>
          <strong>Edit the requisition</strong> from the web UI, if you know that
          this is how category assignments in this requisition are managed.
        </p>
        <p>
          <strong>Edit the appropriate foreign-source definition</strong> from the
          web UI, if you know that categories for this requisition's nodes are
          automatically assigned by a <em>Set Node Category</em> foreign-source policy.
        </p>
        <p>
          <strong>Ask your OpenNMS administrator</strong> if you aren't sure, or if
          you know that the requisition "<em>${model.node.foreignSource}</em>" is created
          from some data source outside OpenNMS.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</c:if>

</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
