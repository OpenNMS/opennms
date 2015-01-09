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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Surveillance View" />
	<jsp:param name="headTitle" value="Surveillance" />
	<jsp:param name="breadcrumb" value="Surveillance" />
</jsp:include>

<jsp:include page="/includes/surveillance-key.jsp" flush="false" />

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Surveillance View: ${webTable.title}</h3>
  </div>

  <table class="table table-condensed table-bordered severity">
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
