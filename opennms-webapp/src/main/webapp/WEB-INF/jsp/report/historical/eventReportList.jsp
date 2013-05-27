<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="List Reports" />
	<jsp:param name="headTitle" value="List Reports" />
	<jsp:param name="breadcrumb"
		value="<a href='report/index.jsp'>Reports</a>" />
	<jsp:param name="breadcrumb"
		value="<a href='report/historical/index.htm'>Historical</a>" />
	<jsp:param name="breadcrumb" value="List Reports" />
</jsp:include>
<div class="spacer" style="height: 15px">
	<!--  -->
</div>
<style type="text/css">
a:hover {
	color: green;
}
</style>
<c:choose>
	<c:when test="${not empty errorMessage}">
		<p>${errorMessage}</p>
	</c:when>
	<c:otherwise>
		<c:if test="${noOfPages >3}">
			<c:choose>
				<c:when test="${not empty eventList}">

					<c:choose>
						<c:when test="${currentPage eq 1 }">
							<span style="background-color: green"> 1 </span>
						</c:when>
						<c:otherwise>
							<c:if test="${currentPage gt 2 }">
								<a href="report/historical/eventReportList.htm?page=1">
									First </a>
							</c:if>
						</c:otherwise>
					</c:choose>

					<c:if test="${currentPage eq 1}">

						<c:if test="${noOfPages >3}">
							<c:forEach begin="${currentPage+1}" end="${currentPage+2}"
								var="i">

								<a href="report/historical/eventReportList.htm?page=${i}">${i}</a>

							</c:forEach>
						</c:if>

					</c:if>
					<c:if test="${currentPage eq noOfPages}">

						<c:choose>
							<c:when test="${noOfPages >3}">

								<a
									href="report/historical/eventReportList.htm?page=${noOfPages-2}">${noOfPages-2}</a>
								<a
									href="report/historical/eventReportList.htm?page=${noOfPages-1}">${noOfPages-1}</a>
							</c:when>

							<c:otherwise>
							</c:otherwise>

						</c:choose>

					</c:if>


					<c:if test="${(currentPage ne noOfPages)}">
						<c:if test="${(currentPage > 1)}">

							<c:forEach begin="${currentPage-1}" end="${currentPage+1}"
								var="i">


								<c:choose>

									<c:when test="${currentPage eq i}">
										<c:if test="${(i le noOfPages) }">
											<c:if test="${(currentPage > 1)  }">
												<c:if test="${(i gt 1) }">

													<span style="background-color: green">${i}</span>
												</c:if>

											</c:if>

											<!--  we can try this also looks good <span style="background-color: green">${i}</span>  -->
										</c:if>
									</c:when>
									<c:otherwise>

										<c:if test="${(i le noOfPages) }">
											<c:if test="${(i gt 0) }">


												<a href="report/historical/eventReportList.htm?page=${i}">${i}</a>
											</c:if>
										</c:if>


									</c:otherwise>

								</c:choose>

							</c:forEach>

						</c:if>
					</c:if>
				</c:when>
				<c:otherwise>
				</c:otherwise>
			</c:choose>

			<%--For displaying Last  page --%>
			<c:choose>

				<c:when test="${currentPage eq noOfPages }">
					<span style="background-color: green">${noOfPages}</span>
				</c:when>
				<c:otherwise>
					<c:if test="${currentPage lt noOfPages-1 }">
						<a href="report/historical/eventReportList.htm?page=${noOfPages}">Last</a>
					</c:if>
				</c:otherwise>
			</c:choose>
		</c:if>

		<c:if test="${noOfPages eq 3}">

			<c:choose>
				<c:when test="${currentPage eq 1 }">
					<span style="background-color: green"> 1 </span>
				</c:when>
				<c:otherwise>
					<a href="report/historical/eventReportList.htm?page=1"> First </a>
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${currentPage eq noOfPages-1}">
					<span style="background-color: green">${noOfPages-1}</span>
				</c:when>
				<c:otherwise>
					<a href="report/historical/eventReportList.htm?page=${noOfPages-1}">${noOfPages-1}</a>
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${currentPage eq noOfPages }">
					<span style="background-color: green">${noOfPages}</span>
				</c:when>
				<c:otherwise>
					<a href="report/historical/eventReportList.htm?page=${noOfPages}">
						Last </a>
				</c:otherwise>
			</c:choose>
		</c:if>

		<c:if test="${noOfPages eq 2}">

			<c:choose>
				<c:when test="${currentPage eq 1 }">
					<span style="background-color: green"> 1 </span>
				</c:when>
				<c:otherwise>
					<a href="report/historical/eventReportList.htm?page=1"> First </a>
				</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${currentPage eq noOfPages }">
					<span style="background-color: green">${noOfPages}</span>
				</c:when>
				<c:otherwise>
					<a href="report/historical/eventReportList.htm?page=${noOfPages}">
						Last </a>
				</c:otherwise>
			</c:choose>
		</c:if>

		<%--For displaying The Table --%>
		<c:choose>
			<c:when test="${empty eventList}">
				<b><p>No Event Report found.</p></b>
			</c:when>

			<c:otherwise>
				<table border="1">
					<thead>
						<tr>
							<td colspan="2" id="o-repository-title">Event Historical
								Reports</td>
						</tr>

						<tr>
							<th>File Name</th>
							<th>Action</th>
						</tr>

						<c:forEach var="mapEntry" items="${eventList}">
							<tr>
								<td><c:out value="${mapEntry.key}">
									</c:out></td>
								<td><a
									href="report/historical/eventReportDownload.htm?filepath=${mapEntry.value}&filename=${mapEntry.key}">Save</a>&nbsp;</td>


							</tr>
						</c:forEach>
				</table>
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>
<jsp:include page="/includes/footer.jsp" flush="false" />
