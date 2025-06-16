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

<fmt:parseDate var="morning" value="01-08-2005 03:00:00" pattern="dd-MM-yyyy HH:mm:ss"/>
<fmt:parseDate var="evening" value="01-08-2005 16:00:00" pattern="dd-MM-yyyy HH:mm:ss"/>
<c:set var="amPmList"><fmt:formatDate value="${morning}" pattern="a"/>,<fmt:formatDate value="${evening}" pattern="a"/></c:set> 

<c:set var="prefix" value="${param.prefix}" />
<fmt:parseDate var="time" value="${param.time}" pattern="HH:mm:ss" />

<div class="form-group form-row">
  <div class="col-sm-4">
					<select class="form-control custom-select" name="<c:out value='${prefix}'/>Hour">
					<fmt:formatDate var="startHour" value="${time}" pattern="h"/>
					<c:forEach var="h" begin="1" end="12">
						<c:choose>
							<c:when test="${h == startHour}">
								<option selected value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control custom-select" name="<c:out value='${prefix}'/>Minute">
					<fmt:formatDate var="startMinute" value="${time}" pattern="m"/>
					<c:forEach var="half" begin="0" end="1">
						<c:choose>
							<c:when test="${half == startMinute/30}">
								<option selected value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:when>
							<c:otherwise>
								<option value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control custom-select" name="<c:out value='${prefix}'/>AmOrPm">
					<fmt:formatDate var="startAmOrPm" value="${time}" pattern="a"/>
					<c:forEach var="a" items="${amPmList}">
						<c:choose>
						<c:when test="${a == startAmOrPm}">
							<option selected value="<c:out value='${a}'/>"><c:out value='${a}'/></option>
						</c:when>
						<c:otherwise>
							<option value="<c:out value='${a}'/>"><c:out value='${a}'/></option>
						</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
</div> <!-- form-group -->
