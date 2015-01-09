<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>

<div class="row">
    <div class="col-md-12">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Report Parameters</h3>
            </div>
            <div class="panel-body">
                <form:form modelAttribute="parameters" role="form" cssClass="form-horizontal">

                    <%-- // string parameters --%>
                    <c:forEach items="${parameters.stringParms}" var="stringParm" varStatus="stringParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="stringParms[${stringParmRow.index}].value" for="stringParms[${stringParmRow.index}].value">
                                    <c:out value="${stringParm.displayName}"/>
                                </form:label>
                                <c:choose>
                                    <c:when test="${stringParm.inputType == 'reportCategorySelector'}">
                                        <form:select cssClass="form-select" path="stringParms[${stringParmRow.index}].value">
                                            <form:options items="${categories}"/>
                                        </form:select>
                                    </c:when>
                                    <c:when test="${stringParm.inputType == 'onmsCategorySelector'}">
                                        <form:select cssClass="form-select" path="stringParms[${stringParmRow.index}].value">
                                            <form:options items="${onmsCategories}"/>
                                        </form:select>
                                    </c:when>
                                    <c:otherwise>
                                        <form:input cssClass="form-control" path="stringParms[${stringParmRow.index}].value"/>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                    </c:forEach>
                    <%-- // int parameters --%>
                    <c:forEach items="${parameters.intParms}" var="intParm" varStatus="intParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="intParms[${intParmRow.index}].value" for="intParms[${intParmRow.index}].value" >
                                    <c:out value="${intParm.displayName}"/>
                                </form:label>
                                <form:input path="intParms[${intParmRow.index}].value" cssClass="form-control"/>
                            </div>
                        </div>

                    </c:forEach>
                    <%-- // Float parameters --%>
                    <c:forEach items="${parameters.floatParms}" var="floatParm" varStatus="floatParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="floatParms[${floatParmRow.index}].value" for="floatParms[${floatParmRow.index}].value" >
                                    <c:out value="${floatParm.displayName}"/>
                                </form:label>
                                <form:input cssClass="form-control" path="floatParms[${floatParmRow.index}].value"/>
                            </div>
                        </div>

                    </c:forEach>
                    <%-- // Double parameters --%>
                    <c:forEach items="${parameters.doubleParms}" var="doubleParm" varStatus="doubleParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="doubleParms[${doubleParmRow.index}].value" for="doubleParms[${doubleParmRow.index}].value" >
                                    <c:out value="${doubleParm.displayName}"/>
                                </form:label>
                                <form:input path="doubleParms[${doubleParmRow.index}].value" cssClass="form-control"/>
                            </div>
                        </div>

                    </c:forEach>
                    <%-- // date parameters --%>
                    <c:forEach items="${parameters.dateParms}" var="date" varStatus="dateParmRow">
                        <div class="form-group">
                            <div class="col-md-6">
                                <div class="row">
                                    <div class="col-md-12">
                                        <strong>
                                            <c:out value="${date.displayName}:"/>
                                        </strong>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12">
                                        <c:choose>
                                            <c:when test="${ schedule && !date.useAbsoluteDate}">
                                                <div class="pull-left">
                                                    <form:select cssClass="form-control" path="dateParms[${dateParmRow.index}].count">
                                                        <c:forEach var="count" begin="0" end="31">
                                                            <form:option value="${count}" />
                                                        </c:forEach>
                                                    </form:select>
                                                </div>
                                                <div class="pull-left">
                                                    <form:select cssClass="form-control" path="dateParms[${dateParmRow.index}].interval">
                                                        <form:option value="day">day</form:option>
                                                        <form:option value="month">month</form:option>
                                                        <form:option value="year">year</form:option>
                                                    </form:select>
                                                </div>
                                                <div class="pull-left">
                                                    <label style="margin-top: 13%; margin-left:5px; margin-right:5px;"> ago, at </label>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <form:input cssClass="form-control" path="dateParms[${dateParmRow.index}].date" />
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="pull-left">
                                            <form:select cssClass="form-control" path="dateParms[${dateParmRow.index}].hours">
                                                <c:forEach var="hour" begin="0" end="23">
                                                    <form:option value="${hour}">
                                                        <fmt:formatNumber minIntegerDigits="2" value="${hour}" />
                                                    </form:option>
                                                </c:forEach>
                                            </form:select>
                                        </div>
                                        <div class="pull-left"><p class="lead">:</p></div>
                                        <div class="pull-left">
                                            <form:select cssClass="form-control" path="dateParms[${dateParmRow.index}].minutes">
                                                <c:forEach var="minute" begin="0" end="59">
                                                    <form:option value="${minute}">
                                                        <fmt:formatNumber minIntegerDigits="2" value="${minute}" />
                                                    </form:option>
                                                </c:forEach>
                                            </form:select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>

                    <div class="form-group">
                        <div class="col-md-2">
                            <input class="btn btn-default" type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
                            <input class="btn btn-default" type="submit" id="cancel" name="_eventId_cancel" value="Cancel"/>&#160;
                        </div>
                    </div>


                </form:form>
            </div>
        </div>


    </div>
</div>


<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />