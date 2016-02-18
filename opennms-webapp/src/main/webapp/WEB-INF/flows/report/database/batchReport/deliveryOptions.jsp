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
  <jsp:param name="breadcrumb" value="Run" />
</jsp:include>

<div class="row">
    <div class="col-md-12">
        <div class="panel panel-success">
            <div class="panel-heading">
                <h3 class="panel-title">Report Delivery Options</h3>
            </div>
            <div class="panel-body">
                <form:form commandName="deliveryOptions" cssClass="form-horizontal">
                    <div class="form-group">
                        <div class="col-md-2">
                            <form:label path="instanceId" for="instanceId">Unique name:</form:label>
                            <form:input path="instanceId" cssClass="form-control" data-toggle="tooltip" data-placement="right" title="A name to identify this report. Must be unique overall reports."/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-2">
                            <form:label path="format" for="format">Format:</form:label>
                            <form:select path="format" cssClass="form-control" data-toggle="tooltip" data-placement="right" title="The output format of the generated report.">
                                <form:options items="${formats}"/>
                            </form:select>
                        </div>

                    </div>
                    <div class="form-group">
                        <div class="col-md-2">
                            <div class="checkbox">
                                <label data-toggle="tooltip" data-placement="right" title="Indicates whether the generated report is send via email to the defined recipient.">
                                    <form:checkbox path="sendMail"/>Email report
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-2">
                            <form:label path="mailTo" for="mailTo">Recipient:</form:label>
                            <form:input path="mailTo" cssClass="form-control" data-toggle="tooltip" data-placement="right" title="The recipient of the generated report if 'Email report' is enabled."/>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-2">
                            <div class="checkbox">
                                <label data-toggle="tooltip" data-placement="right" title="Indicates whether a copy of the generated report is stored on disk.">
                                    <form:checkbox path="persist"/>Save a copy of this report
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-2">
                            <input type="submit" class="btn btn-default" id="proceed" name="_eventId_proceed" value="Proceed"/>&#160;
                            <input type="submit" class="btn btn-default" name="_eventId_revise" value="Revise"/>&#160;
                            <input type="submit" class="btn btn-default" name="_eventId_cancel" value="Cancel"/>&#160;
                        </div>
                    </div>
                </form:form>
            </div>

        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
        onVersionChange();
    })
</script>


<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
