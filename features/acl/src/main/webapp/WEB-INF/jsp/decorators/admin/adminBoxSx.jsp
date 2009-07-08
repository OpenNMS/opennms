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
<div id="colonna-sx">
    <div class="box">
        <ul class="menu">
            <li>Menu<ul>
            <li><spring:message code="ui.groups"/></li>
            <li><a href="group.list.page" title="<spring:message code="group.list"/>"><spring:message code="ui.list"/></a></li>
            <li><spring:message code="ui.authorities"/></li>
            <li><a href="authority.list.page" title="<spring:message code="authority.list"/>"><spring:message code="ui.list"/></a></li>
            <li><spring:message code="ui.users"/></li>
            <li><a href="user.list.page" title="<spring:message code="user.list"/>"><spring:message code="ui.list"/></a></li>
        </ul></li>
        </ul>
    </div>
</div>