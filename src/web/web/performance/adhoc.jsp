<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.*,org.opennms.web.performance.*" %>

<%!
    public PerformanceModel model = null;
  
    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel( org.opennms.web.ServletInitializer.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the PerformanceModel", e );
        }
    }
%>
<%
    String nodeIdString = request.getParameter("node");    
    if(nodeIdString == null) {
        throw new MissingParameterException("node");
    }

    int nodeId = Integer.parseInt(nodeIdString);
    
    TreeMap intfMap = new TreeMap();  
    String[] intfs = this.model.getQueryableInterfacesForNode(nodeId);
  
    // Add the readable name and the file path to the Map
    for(int i=0; i < intfs.length; i++) {
        intfMap.put(this.model.getHumanReadableNameForIfLabel(nodeId, intfs[i]), intfs[i]);
    }
%>

<html>
<head>
  <title>Custom | Performance | Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='report/index.jsp'>Reports</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='performance/index.jsp'>Performance</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Custom"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Performance Reporting" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
      <form method="get" action="performance/adhoc2.jsp" >
        <%=Util.makeHiddenTags(request)%>
  
        <table width="100%" cellspacing="2" cellpadding="2" border="0">
          <tr>
            <td><h3>Step 1: Choose the Interface to Query</h3></td>
          </tr>
          <tr>
            <td valign="top">
              <select name="intf" size="10">
                <option value="">Node-level Performance Data</option>              
                <% Iterator iterator = intfMap.keySet().iterator(); %>
                <% while(iterator.hasNext()) { %>
                  <% String key = (String)iterator.next(); %>
                  <option value="<%=intfMap.get(key)%>"><%=key%></option>
                <% } %>
              </select>
            </td>
          </tr>
          <tr>
            <td colspan="3">
              <input type="submit" value="Next"/>
              <input type="reset" />
            </td>
          </tr>
        </table>
      </form>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
