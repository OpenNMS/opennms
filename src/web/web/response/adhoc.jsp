<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.*,org.opennms.web.response.*" %>

<%!
    public ResponseTimeModel model = null;
  
    public void init() throws ServletException {
        try {
            this.model = new ResponseTimeModel( org.opennms.web.ServletInitializer.getHomeDir() );
        }
        catch( Exception e ) {
            throw new ServletException( "Could not initialize the ResponseTimeModel", e );
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
    ArrayList intfs = this.model.getQueryableInterfacesForNode(nodeId);
  
    // Add the readable name and the file path to the Map
    for(int i=0; i < intfs.size(); i++) {
        intfMap.put(this.model.getHumanReadableNameForIfLabel(nodeId, (String)intfs.get(i)), (String)intfs.get(i));
    }
%>

<html>
<head>
  <title>Custom | Response Time | Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='report/index.jsp'>Reports</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='response/index.jsp'>Response Time</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Custom"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Response Time Reporting" />
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
      <form method="get" action="response/adhoc2.jsp" >
        <%=Util.makeHiddenTags(request)%>
  
        <table width="100%" cellspacing="2" cellpadding="2" border="0">
          <tr>
            <td><h3>Step 1: Choose the Interface to Query</h3></td>
          </tr>
          <tr>
            <td valign="top">
              <select name="intf" size="10">
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
