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
<div><span><spring:message code="ui.role.name"/>:</span>${authority.name}</div><div><br/></div>
<div><span><spring:message code="authority.description"/>:</span>${authority.description}</div><div><br/></div>
<form action="authority.selection.page" method="post">
    <div class="lista">
        <div>
            <spring:message code="authority.user.assign"/>:<br/><br/>
            <select multiple="multiple" name="included" id="included" class="selectmultiple">
                <c:forEach var="itemAuthority" items="${authorityItems}" varStatus="status">
                    <option value="${itemAuthority.categoryId}">${itemAuthority.categoryName}</option>
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
                    <option value="${freeItem.categoryId}">${freeItem.categoryName}</option>
                </c:forEach>
            </select>
        </div>
    </div>
    <br/>
    <div class="pulsanti">
        <input type="hidden" name="includedHidden" id="includedHidden"/>
        <input type="hidden" name="aid" value="${param.aid}" />
        <input type="submit" onclick="javascript:setInputList($('included'),$('includedHidden'));" value="<spring:message code="authority.user.assignable"/>"/>
    </div>
</form>
<br/>