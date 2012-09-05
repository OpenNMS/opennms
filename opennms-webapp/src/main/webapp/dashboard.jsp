<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true"  %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Dashboard" />
	<jsp:param name="meta">
	  <jsp:attribute name="value">
	    <meta name='gwt:module' content='org.opennms.dashboard.Dashboard' />
	  </jsp:attribute>
	</jsp:param>
    <jsp:param name="meta">
	  <jsp:attribute name="value">
        <link media="screen" href="css/dashboard.css" type="text/css" rel="stylesheet">
	  </jsp:attribute>
	</jsp:param>
	
</jsp:include>

<script type="text/javascript" language='javascript' src='dashboard/dashboard.nocache.js'></script>
<table class="dashboard" cellspacing="5" width="100%">
  <tbody>
    <tr>
      <td class="dashletCell"id="surveillanceView"></td>
    </tr>
    <tr>
      <td class="dashletCell" id="alarms"></td>
    </tr>
    <tr>
      <td class="dashletCell" id="notifications"></td>
    </tr>
    <tr>
      <td class="dashletCell" id="nodeStatus"></td>
    </tr>
    <tr>
      <td class="dashletCell" id="graphs"></td>
    </tr>
    <tr>
      <td class="dashletCell" id="outages"></td>
    </tr>
  </tbody>
</table>


<jsp:include page="/includes/footer.jsp" flush="false" />
