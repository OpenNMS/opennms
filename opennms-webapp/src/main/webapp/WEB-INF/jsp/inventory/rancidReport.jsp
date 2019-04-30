<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Inventory Reports" />
  <jsp:param name="headTitle" value="Inventory Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="Inventory(Rancid) Reports" />
</jsp:include>

<div class="row">
    <div class="col-md-6">

        <div class="card">
            <div class="card-header">
                <span>Generate reports</span>
            </div>

            <table class="table table-sm mb-0">
            </table>

        </div>

        <div class="card">
            <div class="card-header">
                <span>Inventory</span>
            </div>
            <div class="card-body">
                <form id="inventoryReport" class="form" method="post" name="inventoryReport">
                    <div class="form-group">
                        <label id="dateLabel" for="date">Date</label>
                        <input class="form-control" id="date" type="text" name="date" value="YYYY/MM/DD">
                    </div>

                    <div class="form-group" >
                        <label id="fieldhasLabel" for="fieldhas">Matching</label>
                        <input class="form-control" id="fieldhas" type="text" name="fieldhas" value="">
                    </div>
                    <div class="form-group">
                        <label id="reporttypeLabel" for="reporttype">Report type</label>
                        <select class="form-control custom-select" id="reporttype">
                            <option value="rancidlist">Rancid</option>
                            <option value="inventory">Inventory</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Report format</label>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" id="reportfileTypeXml" name="reportfiletype" value="pdftype">
                            <label class="form-check-label" for="reportfileTypeXml">XML</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" id="reportFileTypeHtml" name="reportfiletype" value="htmltype" checked>
                            <label class="form-check-label" for="reportFileTypeHtml">HTML</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <label id="reportemailLabel" for="reportemail">Email to</label>
                        <input class="form-control" id="reportemail" type="text" name="reportemail" value="">
                    </div>

                    <div class="form-group">
                        <input class="btn btn-secondary" id="run" type="button" value="run" onClick="runInventory()">
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="col-md-6">
        <div class="card">
            <div class="card-header" >
                <span>Descriptions</span>
            </div>
            <div class="card-body">
                <p><b>Date</b> provide a date at which you want to get report.
                    If you leave it blank the report data is today.
                </p>

                <p><b>Matching</b> provide a regular expression to match on. The report will only contain
                    the items that match the specified string.
                    If you leave it blank the report data contains all items. This matching applies
                    only to inventory report, it will be ignored in case of rancid list report.
                </p>

                <p><b>Report Type</b> Selecting <em>Rancid</em> you get a list of all the rancid
                    devices in router.db (for every rancid group) at a specified date.
                    Selecting <em>Inventory</em> you get a list of all inventory items for each device
                    that had inventory data at the specified data for matching string
                </p>

                <p><b>Report Format</b> Selecting <em>HTML</em> you get an HTML report.
                    Selecting <em>XML</em> you get an XML report.
                </p>

                <p><b>Email</b> Sets the email address of the user that will receive the report.
                </p>

            </div>
        </div>

    </div>
</div>



<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<script type="text/javascript">

function runInventory() {
	  document.inventoryReport.action="inventory/rancidReportExec.htm";
	  document.inventoryReport.submit();	
}

</script>

