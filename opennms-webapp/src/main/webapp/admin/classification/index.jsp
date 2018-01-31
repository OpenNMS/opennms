<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="norequirejs" value="true" />

    <jsp:param name="title" value="Flow Classification" />
    <jsp:param name="headTitle" value="Flow Classification" />
    <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
    <jsp:param name="breadcrumb" value="Flow Classification" />

    <jsp:param name="script" value='<script type="text/javascript" src="lib/angular/angular.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="lib/angular-resource/angular-resource.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="lib/angular-bootstrap/ui-bootstrap-tpls.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="lib/angular-bootstrap-checkbox/angular-bootstrap-checkbox.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="lib/angular-loading-bar/build/loading-bar.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/onms-pagination/module.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/angular-onms-elementList.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/onms-classifications/lib/angular-sanitize.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/onms-classifications/lib/angular-ui-router.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/onms-classifications/lib/angular-bootstrap-toggle.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/onms-classifications/lib/angular-bootstrap-confirm.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="js/onms-classifications/app.js"></script>' />
</jsp:include>

<link rel='stylesheet' type='text/css' href='lib/angular-loading-bar/build/loading-bar.css' />
<link rel='stylesheet' type='text/css' href='js/onms-classifications/lib/angular-bootstrap-toggle.css' />

<div ng-app="onms.classifications" ui-view>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
