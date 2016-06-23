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
  <jsp:param name="breadcrumb" value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="<a href='report/database/reportList.htm'>List Reports</a>" />
  <jsp:param name="breadcrumb" value="run"/>
</jsp:include>

<div class="row">
    <div class="col-md-12">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Run Online Report</h3>
            </div>
            <div class="panel-body">
                <form:form modelAttribute="parameters" cssClass="form-horizontal" role="form">
                    <%-- // string parameters --%>
                    <c:forEach items="${parameters.stringParms}" var="stringParm" varStatus="stringParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="stringParms[${stringParmRow.index}].value" for="stringParms[${stringParmRow.index}].value">
                                    <c:out value="${stringParm.displayName}:"/>
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
                                <form:label path="intParms[${intParmRow.index}].value" for="intParms[${intParmRow.index}].value">
                                    <c:out value="${intParm.displayName}:"/>
                                </form:label>
                                <form:input cssClass="form-control" path="intParms[${intParmRow.index}].value"/>
                            </div>
                        </div>
                    </c:forEach>
                    <%-- // Float parameters --%>
                    <c:forEach items="${parameters.floatParms}" var="floatParm" varStatus="floatParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="floatParms[${floatParmRow.index}].value" for="floatParms[${floatParmRow.index}].value">
                                    <c:out value="${floatParm.displayName}:"/>
                                </form:label>
                                <form:input cssClass="form-control" path="floatParms[${floatParmRow.index}].value"/>
                            </div>
                        </div>
                    </c:forEach>
                    <%-- // Double parameters --%>
                    <c:forEach items="${parameters.doubleParms}" var="doubleParm" varStatus="doubleParmRow">
                        <div class="form-group">
                            <div class="col-md-2">
                                <form:label path="doubleParms[${doubleParmRow.index}].value" for="doubleParms[${doubleParmRow.index}].value">
                                    <c:out value="${doubleParm.displayName}:"/>
                                </form:label>
                                <form:input cssClass="form-control" path="doubleParms[${doubleParmRow.index}].value"/>
                            </div>
                        </div>
                    </c:forEach>
                    <%-- // date parameters --%>
                    <c:forEach items="${parameters.dateParms}" var="date" varStatus="dateParmRow">
                        <div class="form-group">
                            <div class="col-md-8">
                                <strong >
				                    <c:out value="${date.displayName}:"/>
			                    </strong>
                                <div class="row">
                                    <div class="col-md-4">
                                        <form:input cssClass="form-control" path="dateParms[${dateParmRow.index}].date" />
                                    </div>
                                    <div class="col-md-8">

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
                            <form:label path="format" for="formatSelect">Report Format:</form:label>
                            <form:select id="formatSelect" path="format" cssClass="form-control">
                                <form:options items="${formats}"/>
                            </form:select>
                        </div>

                    </div>
                    <c:if test="${errorMessage != null}">
                        <div class="form-group">
                            <div class="col-md-4">
                                <div class="alert alert-danger" role="alert">
                                        ${errorMessage}
                                        <c:if test="${errorCause != null && errorCause.message != null}">
                                            <br/>
                                            ${errorCause.message}
                                        </c:if>
                                </div>
                            </div>
                        </div>
                    </c:if>
                    <div class="form-group">
                        <div class="col-md-2">
                            <input type="submit" class="btn btn-default" name="run" value="run report" id="run"/>
                            <c:if test="${errorMessage != null}">
                                <input type="submit" class="btn btn-default" name="cancel" value="cancel" id="cancel">
                            </c:if>
                        </div>
                    </div>

                </form:form>
            </div>
        </div>

    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
