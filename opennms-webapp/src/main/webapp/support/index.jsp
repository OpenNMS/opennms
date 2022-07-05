<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page language="java"
        contentType="text/html"
        session="true"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="Support"/>
    <jsp:param name="headTitle" value="Get Support"/>
    <jsp:param name="location" value="help"/>
    <jsp:param name="breadcrumb" value="Support"/>
</jsp:include>

<div class="row">
    <div class="col-md-4">
        <!-- no account session found, ask for login -->
        <div class="card">
            <div class="card-header">
                <span>Commercial Support</span>
            </div>
            <div class="card-body">
                <table class="table">
                    <tr>
                        <td style="border-top: none;"><a href="https://support.opennms.com" target="_blank" class="btn btn-secondary" role="button" style="width: 100%">OpenNMS Support Portal</a></td>
                        <td style="border-top: none;">Login to the OpenNMS Support Portal to create a support ticket. Please attach a basic system report to help the support engineer who works your ticket diagnose the problem more quickly.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <div class="col-md-4">
        <jsp:include page="/includes/support-system-diagnostics.jsp" flush="false"/>
    </div>

    <div class="col-md-4">
        <jsp:include page="/includes/help-contact.jsp" flush="false"/>
    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
