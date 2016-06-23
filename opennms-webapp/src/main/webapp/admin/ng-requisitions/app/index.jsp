<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
<%@ page import="org.opennms.web.api.Util" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="nobase" value="true" />
    <jsp:param name="nostyles" value="true" />
    <jsp:param name="norequirejs" value="true" />
    <jsp:param name="nobreadcrumbs" value="true" />
    <jsp:param name="ngapp" value="onms-requisitions" />
    <jsp:param name="title" value="Manage Provisioning Requisitions" />
    <jsp:param name="headTitle" value="Provisioning Requisitions" />
    <jsp:param name="headTitle" value="Admin" />
    <jsp:param name="location" value="admin" />

    <jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="../../../css/bootstrap.css" media="screen" />' />
    <jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="../../../css/opennms-theme.css" media="screen" />' />
    <jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="../../../css/font-awesome-4.4.0/css/font-awesome.min.css" />' />

    <jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="bower_components/angular-loading-bar/build/loading-bar.min.css" />' />
    <jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="bower_components/angular-growl-v2/build/angular-growl.min.css" />' />

    <jsp:param name="script" value='<script type="text/javascript" src="basehref.jsp"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/jquery/dist/jquery.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/bootstrap/dist/js/bootstrap.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular/angular.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-resource/angular-resource.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-cookies/angular-cookies.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-sanitize/angular-sanitize.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-route/angular-route.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-animate/angular-animate.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-loading-bar/build/loading-bar.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/angular-growl-v2/build/angular-growl.min.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/ip-address/dist/ip-address-globals.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="bower_components/bootbox/bootbox.js"></script>' />

    <jsp:param name="script" value='<script type="text/javascript" src="scripts/app.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/model/RequisitionInterface.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/model/RequisitionNode.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/model/Requisition.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/model/RequisitionsData.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/model/QuickNode.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/services/Requisitions.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/services/Synchronize.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/filters/startFrom.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/directives/requisitionConstraints.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Move.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/QuickAddNode.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/QuickAddNodeModal.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/CloneForeignSource.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Detector.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Policy.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/ForeignSource.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Asset.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Interface.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Node.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Requisition.js"></script>' />
    <jsp:param name="script" value='<script type="text/javascript" src="scripts/controllers/Requisitions.js"></script>' />
</jsp:include>

<div ng-view></div>
<div growl></div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
