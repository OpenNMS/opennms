<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html; charset=UTF-8" %>
<%
  String mapImplementation = System.getProperty("gwt.maptype", "");
  String openlayersUrl = System.getProperty("gwt.openlayers.url", "http://otile1.mqcdn.com/tiles/1.0.0/osm/${z}/${x}/${y}.png");
  String apiKey = System.getProperty("gwt.apikey", "");

	if (!apiKey.equals("")) {
		apiKey = "&key=" + apiKey;
	}
%>
<!-- The HTML 4.01 Transitional DOCTYPE declaration will set the browser's rendering engine into "Quirks Mode".
	Replacing this declaration with a "Standards Mode" doctype is supported, but may lead to some differences in layout. -->

<%@page import="java.net.URLEncoder"%><jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="OpenNMS Remote Poller Map" />

	<jsp:param name="docType" value="html" />
	<jsp:param name="disableCoreWeb" value="true" />
	<jsp:param name="nobase" value="true" />
	<jsp:param name="nostyles" value="true" />
	<jsp:param name="nobreadcrumbs" value="true" />
	<jsp:param name="basehref" value=".." />
	<jsp:param name="nonavbar" value="true" />
	<jsp:param name="nofaq" value="true" />

	<jsp:param name="script">
		<jsp:attribute name="value">
			<script type="text/javascript" language="javascript">
			  window.mapImplementation = "<%= mapImplementation %>";
			  window.openlayersUrl = "<%= openlayersUrl %>";
			</script>
			<% if (mapImplementation.equalsIgnoreCase("googlemaps")) { %>
				<script src="<%= URLEncoder.encode("http://maps.google.com/maps?gwt=1&amp;file=api&amp;v=2.x" + apiKey, "UTF-8") %>"></script>
			<% } else if (mapImplementation.equalsIgnoreCase("mapquest")) { %>
				<script type="text/javascript" src="<%= URLEncoder.encode("http://btilelog.access.mapquest.com/tilelog/transaction?transaction=script&itk=true&v=5.3.s&ipkg=controls1" + apiKey, "UTF-8") %>"></script>
				<script type="text/javascript" src="mapquest/debug/mqutils.js"></script>
				<script type="text/javascript" src="mapquest/debug/mqobjects.js"></script>
			<% } else if (mapImplementation.equalsIgnoreCase("openlayers")) { %>
				<script type="text/javascript" src="opennms-openlayers.js"></script>
				<script type="text/javascript" src="openlayers/OpenLayers.js"></script>
			<% } %>
			<script type="text/javascript" language="javascript" src="RemotePollerMap.nocache.js"></script>
		</jsp:attribute>
	</jsp:param>

	<jsp:param name="link">
		<jsp:attribute name="value">
			<link rel='stylesheet' type='text/css' href='styles.css' />
		</jsp:attribute>
	</jsp:param>
</jsp:include>

	<div id="map"></div>

	<!-- OPTIONAL: include this if you want history support -->
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

<jsp:include page="/includes/footer.jsp" flush="false" />
