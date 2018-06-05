<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Remote Poller Scan Reports" />
	<jsp:param name="headTitle" value="Remote Poller Scan Reports" />
	<jsp:param name="breadcrumb" value="Remote Poller Scan Reports" />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular/angular.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular-resource/angular-resource.js"></script>' />

	<jsp:param name="script" value='<script type="text/javascript" src="js/angular-onms-restresources.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="js/angular-onms-elementList.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="js/angular-onms-elementList-scanreport.js"></script>' />
</jsp:include>

<style>
@media (min-width: 1439px) {
	.modal-lg {
		width: 1400px !important;
	}
}
@media (min-width: 1279px) {
	.modal-lg {
		width: 1200px !important;
	}
}
</style>

<ng-include src="'scanreports/main.html'"></ng-include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
