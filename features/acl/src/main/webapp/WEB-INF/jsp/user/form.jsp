<%--
/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

--%>
<%@ include file="/WEB-INF/jsp/taglibs.jsp"%>
<springf:form action="user.edit.page" commandName="user" method="post">
    <table>
        <tr>
            <td><spring:message code="ui.user.username" />:</td>
            <td><springf:input path="username" maxlength="45"/></td>
            <td><springf:errors path="username" /></td>
        </tr>    
        <tr>
        <c:choose>
        <c:when test="${user.username eq null}">
            <td><spring:message code="ui.user.password"/>:</td>
        </c:when>
        <c:otherwise>
            <td><spring:message code="ui.user.password.new"/>:</td>
        </c:otherwise>
        </c:choose>
            <td><springf:password path="password"/></td>
            <td><springf:errors path="password"/></td>
        </tr>
        <tr>
            <td colspan="3">
                <input type="submit" value="<spring:message code="ui.action.save"/>"/> 
                <input type="hidden" id="id" name="id" value="${user.id}"/> 
                <input type="button" onclick="location.href = 'user.list.page'" value="<spring:message code="user.list"/>"/>
            </td>
        </tr>
    </table>
</springf:form>