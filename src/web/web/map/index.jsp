<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Map | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Map"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Map" />
  <jsp:param name="location" value="map" />  
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
      <h3>Mapping</h3>    

      <p>
        <table width="50%" border="0" cellpadding="2" cellspacing="0" >
          <form action="map/map.jsp" method="GET">
          <tr>
            <td>Map Type:</td>
            <td><select name="type">
                  <option value="tree">Tree-like map</option>
                  <option value="boring">Just show the nodes</option>
                </select></td>
          </tr>
          <tr>
            <td>Map Format:</td>
            <td><select name="format">
                  <option value="svg">SVG</option>
                  <option value="png">PNG Imagemap</option>
                </select></td>
          </tr>
          <tr>
            <td>View "fullscreen":</td>
            <td><select name="fullscreen">
                  <option value="y">Yes</option>
                  <option value="n" selected>No</option>
                </select></td>
          </tr>
          <tr>
            <td>Auto-refresh:</td>
            <td><select name="refresh">
                  <option value="1">1 minute</option>
                  <option value="2">2 minutes</option>
                  <option value="3">3 minutes</option>
                  <option value="5" selected>5 minutes</option>
                  <option value="10">10 minutes</option>
                  <option value="15">15 minutes</option>
                </select></td>
          </tr>
          <tr>
             <td colspan="2"><input type="submit" value="view map"/></td>
          </tr>
          </form>
        </table>
      </p>      

      <p>
        <table width="50%" border="0" cellpadding="2" cellspacing="0">
          <tr>
            <td><a href="map/parent.jsp">Set Parent&lt;-&gt;Child Relationships</a></td>
          </tr>
        </table>
      </p>
    </td>

    <td>&nbsp;</td>

    <td valign="top" width="60%" >
      <h3>Mapping</h3>

      <p>
         The mapping function retrieves nodes from the database and
         generates a map for them.  It can be viewed as an SVG image
         embedded in a webpage or it can be viewed as a PNG image in a
         webpage if you do not have an SVG viewer for your browser.
         </p>

      <p>
         The built-in conversion from SVG to PNG format can take quite
         a bit of memory if there are a lot of nodes on your network
         and there is some chance of failure.
      </p>

      <p>Use the "Set Parent-Child Relationships" link to establish a
      heirarchy for your nodes so they do not all appear at the same
      "level".</p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>                                    
                                     
<br />

    <jsp:include page="/includes/footer.jsp" flush="false" >
      <jsp:param name="location" value="map" />
    </jsp:include>

  </body>
</html>
