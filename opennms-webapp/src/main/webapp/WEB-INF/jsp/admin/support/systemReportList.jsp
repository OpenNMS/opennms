<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="System Reports" />
    <jsp:param name="headTitle" value="System Reports" />
    <jsp:param name="breadcrumb" value="<a href='support/index.htm'>Support</a>"/>
    <jsp:param name="breadcrumb" value="System Reports" />
</jsp:include>

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

<form role="form" name="report" class="form-inline" action="admin/support/systemReport.htm" method="post" class="normal">

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Plugins &nbsp;&nbsp;&nbsp; <input type="checkbox" name="all" onclick="toggle(document.report.all, document.report.plugins)" checked /> All</h3>
  </div>
  <div class="panel-body">
    <p>Choose which plugins to enable:</p>
    <c:forEach items="${report.plugins}" var="plugin">
     <input type="checkbox" name="plugins" value="${plugin.name}" checked /> <c:out value="${plugin.name}" />: <c:out value="${plugin.description}" /> <br />
    </c:forEach>
  </div>
</div> <!-- panel -->

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Report Type</h3>
  </div>
  <div class="panel-body">
    <p>Choose which report to use:</p>
    <p>
    <select name="formatter" class="form-control">
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
    Output: <input type="text" name="output" class="form-control" />
    </p>
  </div> <!-- panel-body -->
  <div class="panel-footer">
    <input type="hidden" name="operation" value="run" />
    <input type="submit" class="btn btn-default" />
  </div>
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
