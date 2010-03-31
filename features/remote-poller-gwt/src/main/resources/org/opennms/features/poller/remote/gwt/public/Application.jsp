<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<%@page language="java" contentType="text/html; charset=UTF-8" %>
<% String mapImplementation = System.getProperty("gwt.maptype"); %>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Application</title>
    
    <% if (mapImplementation.equalsIgnoreCase("googlemaps")) { %>
    <script src="http://maps.google.com/maps?gwt=1&file=api&v=2.x&key=<%= System.getProperty("gwt.apikey") %>"></script>
    <% } else if (mapImplementation.equalsIgnoreCase("mapquest")) { %>
    <script type="text/javascript" src="http://btilelog.access.mapquest.com/tilelog/transaction?transaction=script&itk=true&v=5.3.s&ipkg=controls1&key=<%= System.getProperty("gwt.apikey") %>"></script>
    <script type="text/javascript" src="mapquest/debug/mqutils.js"></script>
    <script type="text/javascript" src="mapquest/debug/mqobjects.js"></script>
    <% } %>

    <script type="text/javascript" language="javascript" src="org.opennms.features.poller.remote.gwt.Application.nocache.js"></script>
  </head>

  <body>

    <div id="remotePollerMap"></div>

    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

  </body>
</html>
