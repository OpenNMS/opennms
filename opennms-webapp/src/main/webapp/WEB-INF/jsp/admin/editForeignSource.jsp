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

<%@ page contentType="text/html;charset=UTF-8" language="java"
	import="
	java.util.Map,
	org.opennms.netmgt.provision.support.PluginWrapper
	" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/tree" prefix="tree" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Provisioning Requisitions" /> 
	<jsp:param name="headTitle" value="Provisioning Requisitions" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Requisitions</a>" />
	<jsp:param name="breadcrumb" value="Edit Foreign Source Definition" />
</jsp:include>

<tree:form commandName="foreignSourceEditForm"> 
  <input type="hidden" id="foreignSourceName" name="foreignSourceName" value="${fn:escapeXml(foreignSourceEditForm.foreignSourceName)}"/>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Foreign Source Name: ${fn:escapeXml(foreignSourceEditForm.foreignSourceName)}</h3>
  </div>
  <div class="panel-footer">
	<tree:actionButton label="Done" action="done" />
  </div>
</div> <!-- panel -->

	<c:set var="showDelete" value="false" scope="request" />
	<tree:nodeForm>
    	<tree:field label="Scan Interval" property="scanInterval" />
    </tree:nodeForm>
	<c:set var="showDelete" value="true" scope="request" />
    
	<h4>
		Detectors <tree:actionButton label="Add Detector" action="addDetector" />
	</h4>

	<tree:tree root="${foreignSourceEditForm.formData}" childProperty="detectors" var="detector" varStatus="detectorIter">
		<tree:nodeForm>
			<tree:field label="name" property="name" size="32" />
			<tree:select label="class" property="pluginClass" fieldSize="${classFieldWidth}" items="${detectorTypes}" />
			<c:if test="${!empty detector.availableParameterKeys}">
				<tree:action label="[Add Parameter]"  action="addParameter" />
			</c:if>
		</tree:nodeForm>

		<tree:tree root="${detector}" childProperty="parameters" var="parameter" varStatus="detectorParameterIter">
			<tree:nodeForm>
				<tree:select label="key" property="key" items="${parameter.availableParameterKeys}" fieldSize="24" />
				<tree:field label="value" property="value" size="96" />
			</tree:nodeForm>
		</tree:tree>
	</tree:tree>
	
	<h4>
		Policies <tree:actionButton label="Add Policy" action="addPolicy" />
	</h4>

	<tree:tree root="${foreignSourceEditForm.formData}" childProperty="policies" var="policy" varStatus="policyIter">
	   <c:set var="showDelete" value="true" scope="request" />
		<tree:nodeForm>
			<tree:field label="name" property="name" size="32" />
			<tree:select label="class" property="pluginClass" fieldSize="${classFieldWidth}" items="${policyTypes}" />
			<c:if test="${!empty policy.availableParameterKeys}">
				<tree:action label="[Add Parameter]"  action="addParameter" />
			</c:if>
		</tree:nodeForm>

		<tree:tree root="${policy}" childProperty="parameters" var="parameter" varStatus="policyParameterIter">

			<c:choose>
				<c:when test="${pluginInfo[policy.pluginClass].required[parameter.key]}">
					<c:set var="showDelete" value="false" scope="request" />
					<tree:nodeForm>
						<tree:readOnlyField label="key" property="key" />
						<c:choose>
							<c:when test="${empty pluginInfo[policy.pluginClass].requiredItems[parameter.key]}">
						<tree:field label="value" property="value" size="96" />
							</c:when>
							<c:otherwise>
		                		<tree:select label="value" property="value" fieldSize="${valueFieldWidth}" items="${pluginInfo[policy.pluginClass].requiredItems[parameter.key]}" />
							</c:otherwise>
						</c:choose>
					</tree:nodeForm>
				</c:when>
				<c:otherwise>
					<c:set var="showDelete" value="true" scope="request" />
					<tree:nodeForm>
						<tree:select label="key" property="key" items="${parameter.availableParameterKeys}" fieldSize="24" />
				<tree:field label="value" property="value" size="96" />
					</tree:nodeForm>
				</c:otherwise>
			</c:choose>
			<c:set var="showDelete" value="true" scope="session" />

		</tree:tree>
	</tree:tree>
	  
</tree:form> 
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
