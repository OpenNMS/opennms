<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Expression Threshold Editor" />
	<jsp:param name="headTitle" value="Edit Expression Threshold" />
	<jsp:param name="headTitle" value="Thresholds" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/thresholds/index.jsp'>Threshold Groups</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/thresholds/index.jsp?groupName=${groupName}&editGroup'>Edit Group</a>" />
	<jsp:param name="breadcrumb" value="Edit Threshold" />
</jsp:include>

<form name="frm" action="admin/thresholds/index.htm" method="post">
<input type="hidden" name="finishExpressionEdit" value="1"/>
<input type="hidden" name="expressionIndex" value="${expressionIndex}"/>
<input type="hidden" name="groupName" value="${groupName}"/>
<input type="hidden" name="isNew" value="${isNew}"/>
<input type="hidden" name="filterSelected" value="${filterSelected}"/>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Edit expression threshold</h3>
      </div>
      <table class="table table-condensed">
        <tr>
        	<th>Type</th>
        	<th>Expression</th>
        	<th>Datasource type</th>
        	<th>Datasource label</th>
        	<th>Value</th>
        	<th>Re-arm</th>
        	<th>Trigger</th>
        </tr>
        	<tr>
                <td>
                    <select name="type" class="form-control">
                        <c:forEach items="${thresholdTypes}" var="thisType">
                            <c:choose>
                                <c:when test="${expression.type.enumName==thisType}">
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
        		<td><input type="text" name="expression" class="form-control" size="30" value="${expression.expression}"/></td>
        		<td>
        		   	<select name="dsType" class="form-control">
        				<c:forEach items="${dsTypes}" var="thisDsType">
       						<c:choose>
      							<c:when test="${expression.dsType==thisDsType.key}">
        							<c:set var="selected">selected="selected"</c:set>
      							</c:when>
    	 						<c:otherwise>
    	    						<c:set var="selected" value=""/>
    	  						</c:otherwise>
    						</c:choose>
    						<option ${selected} value='${thisDsType.key}'>${thisDsType.value}</option>
        				</c:forEach>
        			</select></td>
                <td><input type="text" name="dsLabel" class="form-control" size="30" value="${expression.dsLabel.orElse(null)}"/></td>
                <td><input type="text" name="value" class="form-control" size="10" value="${expression.value}"/></td>
                <td><input type="text" name="rearm" class="form-control" size="10" value="${expression.rearm}"/></td>
                <td><input type="text" name="trigger" class="form-control" size="10" value="${expression.trigger}"/></td>
        	</tr>
        </table>
        <table class="table table-condensed">
             <tr>
                    <th>Description</th>
                    <th>Triggered UEI</th>
                    <th>Re-armed UEI</th>
            </tr>
        	<tr>
                <td><input type="text" name="description" class="form-control" size="60" value="${expression.description.orElse(null)}"/></td>
                <td><input type="text" name="triggeredUEI" class="form-control" size="60" value="${expression.triggeredUEI.orElse(null)}"/></td>
                <td><input type="text" name="rearmedUEI" class="form-control" size="60" value="${expression.rearmedUEI.orElse(null)}"/></td>
        	</tr>
      </table>
      <div class="panel-footer">
        <input type="submit" name="submitAction" class="btn btn-default" value="${saveButtonTitle}"/>
        <input type="submit" name="submitAction" class="btn btn-default" value="${cancelButtonTitle}"/>
      </div> <!-- panel-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->
  
<div class="row">
  <div class="col-md-8">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Resource Filters</h3>
      </div>
      <div class="panel-body">
        <div class="row">
          <div class="col-sm-4">
            <table class="table table-condensed">
              <tr>
                <th>Filter Operator</th>
              </tr>
              <tr>
                <td>
                  <select name="filterOperator" class="form-control">
                      <c:forEach items="${filterOperators}" var="thisOperator">
                          <c:choose>
                              <c:when test="${expression.filterOperator.enumName==thisOperator}">
                                  <c:set var="selected">selected="selected"</c:set>
                              </c:when>
                              <c:otherwise>
                                  <c:set var="selected" value=""/>
                              </c:otherwise>
                          </c:choose>
                          <option ${selected} value='${thisOperator}'>${thisOperator}</option>
                      </c:forEach>
                  </select>
                </td>
              </tr>
            </table>
          </div> <!-- column -->
        </div> <!-- row -->
        <div class="row">
          <div class="col-md-12">
            <table class="table table-condensed">
            <tr><th>Field Name</th><th>Regular Expression</th><th>Actions</th></tr>
              <c:forEach items="${expression.resourceFilters}" var="filter" varStatus="i">
                <tr name="filter.${i.count}">
                    <c:choose>
                      <c:when test="${i.count==filterSelected}">
                        <td><input type="text" name="updateFilterField" class="form-control" size="60" value="${fn:escapeXml(filter.field)}"/></td>
                        <td><input type="text" name="updateFilterRegexp" class="form-control" size="60" value="${fn:escapeXml(filter.content.orElse(null))}"/></td>
                        <td><input type="submit" name="submitAction" class="form-control" value="${updateButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/></td>          
                      </c:when>
                      <c:otherwise>
                        <td class="standard"><input type="text" class="form-control" disabled="disabled" size="60" value="${fn:escapeXml(filter.field)}"/></td>
                        <td class="standard"><input type="text" class="form-control" disabled="disabled" size="60" value="${fn:escapeXml(filter.content.orElse(null))}"/></td>
                        <td><input type="submit" name="submitAction" class="btn btn-default" value="${editButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                            <input type="submit" name="submitAction" class="btn btn-default" value="${deleteButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                            <input type="submit" name="submitAction" class="btn btn-default" value="${moveUpButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                            <input type="submit" name="submitAction" class="btn btn-default" value="${moveDownButtonTitle}" onClick="document.frm.filterSelected.value='${i.count}'"/>
                            </td>
                      </c:otherwise>
                    </c:choose>
                </tr>
              </c:forEach>
                <tr>
                    <td><input type="text" name="filterField" class="form-control" size="60"/></td>
                    <td><input type="text" name="filterRegexp" class="form-control" size="60"/></td>
                    <td><input type="submit" name="submitAction" class="btn btn-default" value="${addFilterButtonTitle}" onClick="setFilterAction('add')"/></td>
                </tr>
            </table>
          </div> <!-- column -->
        </div> <!-- row -->
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->
  
</form>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Help</h3>
      </div>
      <div class="panel-body">
        <p>
        <b>Description</b>: An optional description for the threshold expression, to help identify what is their purpose.<br/>
        <b>Type</b>:<br/>
        &nbsp;&nbsp;<b>high</b>: Triggers when the value of the data source equals or exceeds the "value", and is re-armed when it drops below the "re-arm" value.<br/>
        &nbsp;&nbsp;<b>low</b>: Triggers when the value of the data source drops to or below the "value", and is re-armed when it equals or exceeds the "re-arm" value.<br/>
        &nbsp;&nbsp;<b>relativeChange</b>: Triggers when the change in data source value from one collection to the next is greater than or equal to "value" percent.
          Re-arm and trigger are not used.<br/>
        &nbsp;&nbsp;<b>absoluteChange</b>: Triggers when the value changes by the specified amount or greater.  Re-arm and trigger are not used.<br/>
        &nbsp;&nbsp;<b>rearmingAbsoluteChange</b>: Like absoluteChange, Triggers when the value changes by the specified amount or greater.  However,
          the "trigger" is used to re-arm the event after so many iterations with an unchanged delta.  Re-arm is not used.<br/>
        <b>Expression</b>: A  mathematical expression involving datasource names which will be evaluated and compared to the threshold values<br/>
        <b>Data source type</b>: Node for "node-level" data items, and "interface" for interface-level items.  <br/>
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
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
