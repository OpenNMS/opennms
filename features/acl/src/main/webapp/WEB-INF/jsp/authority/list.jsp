<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

--%>
<%@ include file="/WEB-INF/jsp/taglibs.jsp"%>
<tag:pager href="authority.list.page"/>
<div>
 	<div align="center"><span><spring:message code="ui.authorities"/></span></div>
 	<c:if test="${msg ne ''}">${msg}</c:if>
	<table class="list">
		<thead>
			<tr>
				<th align="left"><spring:message code="ui.authority.name"/></th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="authority" items="${authorities}" varStatus="status">
			<c:choose>
			<c:when test="${status.count % 2 == 0}"><tr class="table-row-dispari"></c:when>
			<c:otherwise><tr class="table-row-pari"></c:otherwise>
			</c:choose>
				<td align="left"><a href="authority.detail.page?aid=${authority.id}" title="<spring:message code="ui.action.view"/>">${authority.name}</a></td>
				<td align="center"><a href="authority.detail.page?aid=${authority.id}"><img border="0" title="<spring:message code="ui.action.view"/>" alt="<spring:message code="ui.action.view"/>" src="img/view.png"/></a></td>
				<td align="center"><a href="authority.edit.page?aid=${authority.id}"><img border="0" title="<spring:message code="ui.action.edit"/>" alt="<spring:message code="ui.action.edit"/>" src="img/edit.png"/></a></td>
				<td align="center"><a href="authority.confirm.page?aid=${authority.id}"><img border="0" alt="<spring:message code="ui.action.delete"/>" title="<spring:message code="ui.action.delete"/>" src="img/del.png"/></a></td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</div>
<br/>
<tag:pager href="authority.list.page"/>
<div align="center">
<input type="image" src="img/add.png" title="<spring:message code="ui.action.role.new"/>" alt="<spring:message code="ui.action.role.new"/>" onclick="location.href = 'authority.edit.page'" value="<spring:message code="ui.action.authority.new"/>"/>
</div>