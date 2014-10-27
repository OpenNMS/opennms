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
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Import/Export Assets" />
  <jsp:param name="headTitle" value="Import/Export" />
  <jsp:param name="headTitle" value="Assets" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Import/Export Assets" />
</jsp:include>

<div class="TwoColLAdmin">
  <h3>Import and Export Assets</h3>

  <p>
    <a href="admin/asset/import.jsp">Import Assets</a>
  </p>

  <p>
    <a href="admin/asset/assets.csv">Export Assets</a>
  </p>
</div>

<div class="TwoColRAdmin">
  <h3>Importing Asset Information</h3>

  <p>
    The asset import page imports a comma-separated value file (.csv),
    (probably exported from spreadsheet) into the assets database.
  </p>

  <h3>Exporting Asset Information</h3>

  <p>
    All the nodes with asset information will be exported to a 
    comma-separated value file (.csv), which is suitable for use in a 
    spreadsheet application. 
  </p>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
