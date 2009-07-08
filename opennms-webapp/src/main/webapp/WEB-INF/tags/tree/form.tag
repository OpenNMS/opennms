<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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


<%@ attribute name="commandName" type="java.lang.Object" rtexprvalue="true" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<script language="Javascript" type="text/javascript" >
	function submitTreeForm(formName, target, action) {
	    document[formName].formPath.value = target;
	    document[formName].action.value = action;
		document[formName].submit();
	}
</script>


<form:form commandName="${commandName}" name="${commandName}"> 
  
  <c:set var="treeFormName" value="${commandName}" scope="request" />
  <c:set var="treeFormModel" value="${requestScope[commandName]}" scope="request" />
  
  <form:hidden path="formPath"/>
  <form:hidden path="action" />

  <spring:nestedPath path="formData">

    <jsp:doBody />
  
  </spring:nestedPath>
</form:form>