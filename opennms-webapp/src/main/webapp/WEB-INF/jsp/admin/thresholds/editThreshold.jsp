<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
 *******************************************************************************/

--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Threshold Editor" />
	<jsp:param name="headTitle" value="Edit Threshold" />
	<jsp:param name="headTitle" value="Thresholds" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
    <jsp:param name="breadcrumb" value="<a href='admin/thresholds/index.jsp'>Threshold Groups</a>" />
    <jsp:param name="breadcrumb" value="<a href='admin/thresholds/index.jsp?groupName=${groupName}&editGroup'>Edit Group</a>" />
	<jsp:param name="breadcrumb" value="Edit Threshold" />
	
</jsp:include>
<h3>Edit threshold</h3>

<form name="frm" action="admin/thresholds/index.htm" method="post">
<input type="hidden" name="finishThresholdEdit" value="1"/>
<input type="hidden" name="thresholdIndex" value="${thresholdIndex}"/>
<input type="hidden" name="groupName" value="${groupName}"/>
<input type="hidden" name="isNew" value="${isNew}"/>
  <table class="normal">
    <tr>
    	<th class="standardheader">Type</th>
    	<th class="standardheader">Datasource</th>
    	<th class="standardheader">Datasource type</th>
    	<th class="standardheader">Datasource label</th>
    	<th class="standardheader">Value</th>
    	<th class="standardheader">Re-arm</th>
    	<th class="standardheader">Trigger</th>
    </tr>
    	<tr>
    		<td class="standard">
    			<select name="type">
    				<c:forEach items="${thresholdTypes}" var="thisType">
   						<c:choose>
  							<c:when test="${threshold.type==thisType}">
    							<c:set var="selected">selected="selected"</c:set>
  							</c:when>
	 						<c:otherwise>
	    						<c:set var="selected" value=""/>
	  						</c:otherwise>
						</c:choose>
						<option ${selected} value='${thisType}'>${thisType}</option>
    				</c:forEach>
    			</select>
    		</td>
    		<td class="standard"><input type="text" name="dsName" size="30" maxlength="19" value="${threshold.dsName}"/></td>
    		<td class="standard">
    		   	<select name="dsType">
    				<c:forEach items="${dsTypes}" var="thisDsType">
   						<c:choose>
  							<c:when test="${threshold.dsType==thisDsType.key}">
    							<c:set var="selected">selected="selected"</c:set>
  							</c:when>
	 						<c:otherwise>
	    						<c:set var="selected" value=""/>
	  						</c:otherwise>
						</c:choose>
						<option ${selected} value='${thisDsType.key}'>${thisDsType.value}</option>
    				</c:forEach>
    			</select></td>
 			<td class="standard"><input type="text" name="dsLabel" size=30" value="${threshold.dsLabel}"/></td>
    		<td class="standard"><input type="text" name="value" size=10" value="${threshold.value}"/></td>
    		<td class="standard"><input type="text" name="rearm" size=10" value="${threshold.rearm}"/></td>
    		<td class="standard"><input type="text" name="trigger" size=10" value="${threshold.trigger}"/></td>
    	</tr>
    </table>
    <table class="normal">
         <tr>
                <th class="standardheader">Description</th>
                <th class="standardheader">Triggered UEI</th>
                <th class="standardheader">Re-armed UEI</th>
        </tr>
    	<tr>
			<td class="standard"><input type="text" name="description" size="60" value="${threshold.description}"/></td>
			<td class="standard"><input type="text" name="triggeredUEI" size="60" value="${threshold.triggeredUEI}"/></td>
		    <td class="standard"><input type="text" name="rearmedUEI" size="60" value="${threshold.rearmedUEI}"/></td>
    	</tr>
  </table>
  <input type="submit" name="submitAction" value="${saveButtonTitle}"/>
  <input type="submit" name="submitAction" value="${cancelButtonTitle}"/>
  
<input type="hidden" name="filterSelected" value="${filterSelected}"/>
<h3>Resource Filters</h3>
<table class="normal">
    <tr><td>Filter Operator</td>
    <td><select name="filterOperator">
        <c:forEach items="${filterOperators}" var="thisOperator">
            <c:choose>
                <c:when test="${threshold.filterOperator==thisOperator}">
                    <c:set var="selected">selected="selected"</c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="selected" value=""/>
                </c:otherwise>
            </c:choose>
            <option ${selected} value='${thisOperator}'>${thisOperator}</option>
        </c:forEach>
    </select></td></tr>
</table>
<table class="normal">
<tr><th>Field Name</th><th>Regular Expression</th><th>Actions</th></tr>
  <c:forEach items="${threshold.resourceFilter}" var="filter" varStatus="i">
    <tr>
        <c:choose>
          <c:when test="${i.count==filterSelected}">
            <td><input type="text" name="updateFilterField" size="60" value="${filter.field}"/></td>
            <td><input type="text" name="updateFilterRegexp" size="60" value="${filter.content}"/></td>          
            <td><input type="submit" name="submitAction" value="${updateButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/></td>          
          </c:when>
          <c:otherwise>
            <td class="standard"><input type="text" disabled="true" size="60" value="${filter.field}"/></td>
            <td class="standard"><input type="text" disabled="true" size="60" value="${filter.content}"/></td>
            <td><input type="submit" name="submitAction" value="${editButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                <input type="submit" name="submitAction" value="${deleteButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                <input type="submit" name="submitAction" value="${moveUpButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                <input type="submit" name="submitAction" value="${moveDownButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                </td>
          </c:otherwise>
        </c:choose>
    </tr>
  </c:forEach>
    <tr>
        <td><input type="text" name="filterField" size="60"/></td>
        <td><input type="text" name="filterRegexp" size="60"/></td>
        <td><input type="submit" name="submitAction" value="${addFilterButtonTitle}" onClick="setFilterAction('add')"/></td>
    </tr>
</table>
  
</form>
<h3>Help</h3>
<p>
<b>Description</b>: An optional description for the threshold, to help identify what is their purpose.<br/>
<b>Type</b>:<br/>
&nbsp;&nbsp;<b>high</b>: Triggers when the value of the data source exceeds the "value", and is re-armed when it drops below the "re-arm" value.<br/>
&nbsp;&nbsp;<b>low</b>: Triggers when the value of the data source drops below the "value", and is re-armed when it exceeds the "re-arm" value.<br/>
&nbsp;&nbsp;<b>relativeChange</b>: Triggers when the change in data source value from one collection to the next is greater than "value" percent.
  Re-arm and trigger are not used.<br/>
&nbsp;&nbsp;<b>absoluteChange</b>: Triggers when the value changes by more than the specified amount.  Re-arm and trigger are not used.<br/>
&nbsp;&nbsp;<b>rearmingAbsoluteChange</b>: Like absoluteChange, Triggers when the value changes by more than the specified amount.  However,
  the "trigger" is used to re-arm the event after so many iterations with an unchanged delta.  Re-arm is not used.<br/>
<b>Expression</b>: A  mathematical expression involving datasource names which will be evaluated and compared to the threshold values<br/>
<b>Data source type</b>: "node" for node-level data items, "if" for interface-level items, or any Generic Resource Type defined on datacollection-config.xml. Node-level will ignore filter configuration.<br/>
<b>Datasource label</b>: The name of the collected "string" type data item to use as a label when reporting this threshold<br/>
<b>Value</b>: Use depends on the type of threshold<br/>
<b>Re-arm</b>: Use depends on the type of threshold; it is unused/ignored for relativeChange thresholds<br/>
<b>Trigger</b>: The number of times the threshold must be "exceeded" in a row before the threshold will be triggered.  Not used for relativeChange thresholds.<br/>
<b>Triggered UEI</b>: A custom UEI to send into the events system when this threshold is triggered.  If left blank, it defaults to the standard thresholds UEIs.<br/>
<b>Rearmed UEI</b>: A custom UEI to send into the events system when this threshold is re-armed.  If left blank, it defaults to the standard thresholds UEIs.<br/>
<b>Example UEIs</b>: A typical UEI is of the format <i>"uei.opennms.org/&lt;category&gt;/&lt;name&gt;"</i>.  It is recommended that when creating custom UEIs for thresholds,<br/>
you use a one-word version of your company name as the category to avoid name conflicts.  The "name" portion is up to you.<br/>
<b>Filter Operator</b>: Define the logical function that will be applied over the thresholds filters to determinate if the threshold will be applied or not.<br />
<b>Filters</b>: Only apply for interfaces and Generic Resources. They are applied in order.<br/>
&nbsp;&nbsp;<b>operator=OR</b>: if the resource match any of them, the threshold will be processed.<br/>
&nbsp;&nbsp;<b>operator=AND</b>: the resource must match all the filters.
</p>
<jsp:include page="/includes/footer.jsp" flush="false"/>
