<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*"
 %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="No Email" />
  <jsp:param name="headTitle" value="No Email" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='availability/index.jsp'>Availability Report</a>" />
  <jsp:param name="breadcrumb" value="No Email" />
</jsp:include>

<h3>No Email Address Configured</h3>

      <p>
        The Availability report you requested could not be generated because you
        do not have an email address configured.  You will need to have your
        system administrator setup a valid email address for you.  Then, when
        you request this outage report, it will be emailed to you.
      </p>
      
      <p>
        <a href="availability/index.jsp">Go back to the Availability Report page</a>
      </p>

<jsp:include page="/includes/footer.jsp" flush="false"/>
