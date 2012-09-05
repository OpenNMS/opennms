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