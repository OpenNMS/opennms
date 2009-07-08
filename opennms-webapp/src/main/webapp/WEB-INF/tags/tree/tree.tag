<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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


<%@ attribute name="root" type="java.lang.Object" rtexprvalue="true" required="false" %>
<%@ attribute name="childProperty" rtexprvalue="false" required="true" %>
<%@ attribute name="var" rtexprvalue="false" required="true" %>
<%@ attribute name="varStatus" rtexprvalue="false" required="true" %>
<%@ variable name-from-attribute="var" alias="child" variable-class="java.lang.Object" scope="NESTED" %>
<%@ variable name-from-attribute="varStatus" alias="childStatus" variable-class="java.lang.Object" scope="NESTED" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<c:set var="node" value="${empty root ? parent : root}" /> 
<c:set var="children" value="${node[childProperty]}" />

<ul>
  <c:forEach items="${children}" var="child" varStatus="childStatus">
    <li>
	  <spring:nestedPath path="${childProperty}[${childStatus.index}]" >
	    <jsp:doBody/>
      </spring:nestedPath>
    </li>
  </c:forEach>
</ul>