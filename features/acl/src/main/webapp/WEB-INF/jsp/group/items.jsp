<%--
/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
<div><span><spring:message code="ui.role.name"/>:</span>${group.name}</div><div><br/></div>
<form action="group.selection.page" method="post">
    <div class="lista">
        <div>
            <spring:message code="authority.user.assign"/>:<br/><br/>
            <select multiple="multiple" name="included" id="included" class="selectmultiple">
                <c:forEach var="authority" items="${groupAuthorities}" varStatus="status">
                    <option value="${authority.id}">${authority.name}</option>
                </c:forEach>
            </select>
        </div>
        <div style="vertical-align: bottom">
            <br/><br/><input type="button" onclick="javascript:moveFromList($('included'), $('available'));" value=" >> "/>
            <br/><br/><input type="button" onclick="javascript:moveFromList($('available'), $('included'));" value=" << " />
        </div>
        <div>
            <spring:message code="authority.user.available"/>:<br/><br/>
            <select multiple="multiple" id="available" class="selectmultiple">
                <c:forEach var="freeItem" items="${items}" varStatus="status">
                    <option value="${freeItem.id}">${freeItem.name}</option>
                </c:forEach>
            </select>
        </div>
    </div>
    <br/>
    <div class="pulsanti">
        <input type="hidden" name="includedHidden" id="includedHidden"/>
        <input type="hidden" name="gid" value="${param.gid}" />
        <input type="submit" onclick="javascript:setInputList($('included'),$('includedHidden'));" value="<spring:message code="authority.user.assignable"/>"/>
    </div>
</form>
<br/>