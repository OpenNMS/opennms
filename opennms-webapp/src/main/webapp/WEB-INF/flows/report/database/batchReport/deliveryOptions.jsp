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
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="onms"%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Run" />
</jsp:include>

<div class="OneColLAdmin">
<h3>Report Delivery Options</h3>
<div class="boxWrapper">
    <form:form commandName="deliveryOptions" cssClass="stdform">
        <table>
            <tr>
                <td width="170px">
                    <form:label path="instanceId" cssClass="label">Unique name</form:label>
                </td>
                <td>
                    <form:input path="instanceId" size="50"/>
                    <onms:tooltip id="uniqueNameTT">A name to identify this report. Must be unique overall reports.</onms:tooltip>
                </td>
            </tr>
            <tr>
                <td>
                    <form:label path="format" cssClass="label">Format</form:label>
                </td>
                <td>
                    <form:select path="format">
                        <form:options items="${formats}"/>
                    </form:select>
                </td>
            </tr>
            <tr>
                <td>
                    <span class="label">Email report</span>
                </td>
                <td>
                    <form:checkbox path="sendMail"/>
                    <onms:tooltip id="emailReportTT">Indicates whether the generated report is send via email to the defined recipient.</onms:tooltip>
                </td>
            <tr>
            <tr>
                <td>
                    <form:label path="mailTo" cssClass="label">Recipient</form:label>
                </td>
                <td>
                    <form:input path="mailTo"  size="50"/>
                </td>
            </tr>
            <tr>
                <td>
                    <form:label path="persist" cssClass="label" >Save a copy of this report</form:label>
                    <onms:tooltip id="persistTT">Indicates whether a copy of the generated report is stored on disk.</onms:tooltip>
                </td>
                <td>
                    <form:checkbox path="persist"/>
                </td>
            </tr>
        </table>

        <span class="indent">
            <input type="submit" id="proceed" name="_eventId_proceed" value="Proceed"/>&#160;
            <input type="submit" name="_eventId_revise" value="Revise"/>&#160;
            <input type="submit" name="_eventId_cancel" value="Cancel"/>&#160;
        </span>

    </form:form>
</div>
</div>
  
<jsp:include page="/includes/footer.jsp" flush="false" />