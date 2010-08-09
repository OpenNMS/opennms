<%@page language="java" contentType="text/html; charset=UTF-8" %>
<%
  String mapImplementation = System.getProperty("gwt.maptype", "");
  String openlayersUrl = System.getProperty("gwt.openlayers.wms.url", "http://www.opennms.org:8080/geowebcache/service/wms");
  String openlayersLayer = System.getProperty("gwt.openlayers.wms.layer", "openstreetmap");
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
			  window.openlayersLayer = "<%= openlayersLayer %>";
			</script>
			<% if (mapImplementation.equalsIgnoreCase("googlemaps")) { %>
				<script src="<%= URLEncoder.encode("http://maps.google.com/maps?gwt=1&amp;file=api&amp;v=2.x" + apiKey, "UTF-8") %>"></script>
			<% } else if (mapImplementation.equalsIgnoreCase("mapquest")) { %>
				<script type="text/javascript" src="<%= URLEncoder.encode("http://btilelog.access.mapquest.com/tilelog/transaction?transaction=script&itk=true&v=5.3.s&ipkg=controls1" + apiKey, "UTF-8") %>"></script>
				<script type="text/javascript" src="mapquest/debug/mqutils.js"></script>
				<script type="text/javascript" src="mapquest/debug/mqobjects.js"></script>
			<% } else if (mapImplementation.equalsIgnoreCase("openlayers")) { %>
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
