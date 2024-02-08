<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="prefix" value="${param.prefix}" />
<fmt:parseDate var="date" value="${param.date}" pattern="dd-MM-yyyy" />

<div class="form-group form-row">
  <div class="col-sm-4">
					<select class="form-control custom-select" name="<c:out value='${prefix}'/>Date">
					<fmt:formatDate var="startDate" value="${date}" pattern="d"/>
					<c:forEach var="d" begin="1" end="31">
						<c:choose>
							<c:when test="${d == startDate}">
								<option selected value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control custom-select" name="<c:out value='${prefix}'/>Month">
					<fmt:formatDate var="startMonth" value="${date}" pattern="M"/>
					<c:forEach var="m" begin="1" end="12">
						<fmt:parseDate var="mo" value="${m}" pattern="M" />
						<fmt:formatDate var="monthName" value="${mo}" pattern="MMMM" />
						<c:choose>
							<c:when test="${m == startMonth}">
								<option selected value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control custom-select" name="<c:out value='${prefix}'/>Year">
					<fmt:formatDate var="yearStr" value="${date}" pattern="yyyy" />
					<fmt:parseNumber var="startYear" value="${yearStr}"/>
					<c:forEach var="y" begin="0" end="6">
						<c:set var="year" value="${startYear+y-3}"/>
						<c:choose>
							<c:when test="${year == startYear}">
								<option selected value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
</div> <!-- form-group -->
