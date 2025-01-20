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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("System Reports")
          .breadcrumb("Support", "support/index.htm")
          .breadcrumb("System Reports")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
// <!-- Begin
function checkAll(field)
{
for (i = 0; i < field.length; i++)
	field[i].checked = true ;
}

function uncheckAll(field)
{
for (i = 0; i < field.length; i++)
	field[i].checked = false ;
}

function toggle(box, field)
{
	if (box.checked == true) {
		checkAll(field);
	} else {
		uncheckAll(field);
	}
}

function toggleGroup(activeGroup) {
  // Select group elements
  const group1Checkboxes = document.querySelectorAll("#group1 input[type='checkbox']");
  const group2Radios = document.querySelectorAll("#group2 input[type='radio']");

  if (activeGroup === 1) {
    // Enable and check all checkboxes, disable and uncheck all radios
    group1Checkboxes.forEach(checkbox => {
      checkbox.disabled = false;
      checkbox.checked = true;
    });
    group2Radios.forEach(radio => {
      radio.disabled = true;
      radio.checked = false;
    });
  } else if (activeGroup === 2) {
    // Disable and uncheck all checkboxes, enable radios, and check the first one
    group1Checkboxes.forEach(checkbox => {
      checkbox.disabled = true;
      checkbox.checked = false;
    });
    group2Radios.forEach(radio => {
      radio.disabled = false;
    });
    if (group2Radios.length > 0) {
      group2Radios[0].checked = true;
    }
  }
}

//  End -->
</script>


<form role="form" name="report" class="form" action="admin/support/systemReport.htm" method="post" class="normal">

<div class="card">
  <div class="card-header">
    <span>Plugins</span>
  </div>
  <div class="card-body">
    <p>Choose which plugins to enable:</p>
    <div class="d-flex ml-2">
      <!-- Radio Group -->
      <div class="mr-5">
        <!-- First Radio Group -->
        <input type="radio" id="radio1" name="group" onclick="toggleGroup(1)" checked />
        <label for="radio1" class="font-weight-bold">Text File Report</label>
        <div id="group1" class="ml-3">
          <c:forEach items="${report.plugins}" var="plugin" varStatus="status">
            <c:if test="${plugin.fullOutputOnly == false}">
              <input type="checkbox" id="checkbox_${status.index}" name="plugins" value="${plugin.name}" checked />
              <label for="checkbox_${status.index}"><c:out value="${plugin.name}" />: <c:out value="${plugin.description}" /></label>
              <br />
            </c:if>
          </c:forEach>
        </div>
      </div>

      <div>
        <!-- Second Radio Group -->
        <input type="radio" id="radio2" name="group" onclick="toggleGroup(2)" />
        <label for="radio2" class="font-weight-bold">Log Files</label>
        <div id="group2" class="ml-3">
          <c:forEach items="${report.plugins}" var="plugin" varStatus="status">
            <c:if test="${plugin.fullOutputOnly == true}">
              <input type="radio" id="checkbox_${status.index}" name="plugins" value="${plugin.name}" disabled />
              <label for="checkbox_${status.index}"><c:out value="${plugin.name}" />: <c:out value="${plugin.description}" /></label>
              <br />
            </c:if>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>
</div> <!-- panel -->

<div class="card">
  <div class="card-header">
    <span>Report Type</span>
  </div>
  <div class="card-body">
      <div class="form-group row">
          <label class="col-2" id="formatter" for="formatter">Choose which report to use</label>
          <select name="formatter" class="col-10 form-control custom-select">
              <c:forEach items="${report.formatters}" var="formatter">
                  <c:choose>
                      <c:when test="${formatter.name == 'text'}">
                          <c:set var="formatterSelected" value="selected" />
                      </c:when>
                      <c:otherwise>
                          <c:set var="formatterSelected" value="" />
                      </c:otherwise>
                  </c:choose>
                  <option value="<c:out value="${formatter.name}" />" <c:out value="${formatterSelected}" />><c:out value="${formatter.name}: ${formatter.description}" /></option>
              </c:forEach>
          </select>
      </div>
      <div class="form-group row">
          <label for="filename" class="col-2">File name <small>(optional)</small></label>
          <input type="text" id="filename" name="output" class="form-control col-10" />
      </div>
      <div class="form-group">
          <input type="hidden" name="operation" value="run" />
          <input type="submit" class="btn btn-secondary" value="Generate System Report" />
      </div>
  </div> <!-- card-body -->
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
