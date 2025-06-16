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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Surveillance")
          .breadcrumb("Surveillance")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<jsp:include page="/includes/surveillance-key.jsp" flush="false" />

<div class="card">
  <div class="card-header">
    <span>Surveillance View: ${webTable.title}</span>
  </div>

  <table class="table table-sm table-bordered severity">
    <tr>
    <c:forEach items="${webTable.columnHeaders}" var="headerCell">
      <th class="${headerCell.styleClass}">
        <c:choose>
          <c:when test="${! empty headerCell.link}">
            <a href="${headerCell.link}">${headerCell.content}</a>
          </c:when>
          <c:otherwise>
            ${headerCell.content}
          </c:otherwise>
        </c:choose>
      </th>
    </c:forEach>
    </tr>

    <c:forEach items="${webTable.rows}" var="row">
      <tr class="CellStatus">
        <c:forEach items="${row}" var="cell">
          <td class="severity-${cell.styleClass} bright">
            <c:choose>
              <c:when test="${! empty cell.link}">
                <a href="${cell.link}">${cell.content}</a>
              </c:when>
              <c:otherwise>
                ${cell.content}
              </c:otherwise>
            </c:choose>
          </td>
        </c:forEach>
      </tr>
    </c:forEach>
  </table>
</div> <!-- panel -->

<c:if test="${fn:length(viewNames) > 1}">
  <script type="text/javascript">
    function validateChooseViewNameChosen() {
      var selectedViewName = false
      
      for (i = 0; i < document.chooseViewNameList.viewName.length; i++) {
        // make sure something is checked before proceeding
        if (document.chooseViewNameList.viewName[i].selected) {
          selectedViewName = document.chooseViewNameList.viewName[i].text;
          break;
        }
      }
      
      return selectedViewName;
    }
    
    function goChooseViewNameChange() {
      var viewNameChosen = validateChooseViewNameChosen();
      if (viewNameChosen != false) {
        document.chooseViewNameForm.viewName.value = viewNameChosen;
        document.chooseViewNameForm.submit();
      }
    }
  </script>

  <form method="get" name="chooseViewNameForm" action="${relativeRequestPath}" >
    <input type="hidden" name="viewName" value="node" />
  </form>
        
  <form role="form" class="form-inline" name="chooseViewNameList">
    <div class="form-group">
      <label for="input_viewName">Choose another view:</label>
      <select class="form-control" id="input_viewName" name="viewName" onchange="goChooseViewNameChange();">
        <c:forEach var="viewName" items="${viewNames}">
          <c:choose>
            <c:when test="${viewName == webTable.title}">
              <c:set var="selected">selected="selected"</c:set>
            </c:when>
            
            <c:otherwise>
              <c:set var="selected" value=""/>
            </c:otherwise> 
          </c:choose>
          <option ${selected}>${viewName}</option>
        </c:forEach>
      </select>
    </div>
  </form>
</c:if>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
