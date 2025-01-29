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
<!-- Begin
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
//  End -->
</script>

<form role="form" name="report" class="form" action="admin/support/systemReport.htm" method="post" class="normal">

<div class="card">
  <div class="card-header">
    <span>Plugins &nbsp;&nbsp;&nbsp; <input type="checkbox" name="all" onclick="toggle(document.report.all, document.report.plugins)" checked /> All</span>
  </div>
  <div class="card-body">
    <p>Choose which plugins to enable:</p>
    <c:forEach items="${report.plugins}" var="plugin">
     <input type="checkbox" name="plugins" value="${plugin.name}" checked /> <c:out value="${plugin.name}" />: <c:out value="${plugin.description}" /> <br />
    </c:forEach>
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
