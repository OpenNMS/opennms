<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.response.*,org.opennms.web.*,org.opennms.web.graph.*,java.io.File" %>

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
    //required parameter node
    String nodeIdString = request.getParameter("node");
    if(nodeIdString == null) {
        throw new MissingParameterException( "node", new String[] {"node", "intf"} );
    }

    //required parameter intf, a value of "" means to discard the intf
    String intf = request.getParameter("intf");
    if(intf == null) {
        throw new MissingParameterException( "intf", new String[] {"node", "intf"} );
    }
    
    int nodeId = Integer.parseInt(nodeIdString);

    File rrdPath = null;
    String rrdDir = null;
    if("".equals(intf)) {
        throw new MissingParameterException( "intf", new String[] {"node", "intf"} );
    }
    else {
        rrdPath = new File(this.model.getRrdDirectory(), intf);
        rrdDir = intf;
    }


    File[] rrds = rrdPath.listFiles(GraphUtil.RRD_FILENAME_FILTER);

    if(rrds == null) {
        this.log("Invalid rrd directory: " + rrdPath);
        throw new IllegalArgumentException("Invalid rrd directory: " + rrdPath);
    }

    String nodeLabel = org.opennms.web.element.NetworkElementFactory.getNodeLabel(nodeId);     
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

<br/>

<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
    <form method="get" action="response/adhoc3.jsp" >
      <%=Util.makeHiddenTags(request, new String[] {"node", "intf"})%>
      <input type="hidden" name="rrddir" value="<%=rrdDir%>" />

      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td colspan="2">
            <h3>Step 2: Choose the Data Sources</h3> 
            <% if("".equals(intf)) { %>             
              Node: <%=nodeLabel%>
            <% } else { %>
              Node: <%=nodeLabel%> &nbsp;&nbsp; Interface: <%=this.model.getHumanReadableNameForIfLabel(nodeId, intf)%>
            <% } %>
          </td>
        </tr>

        <tr>
          <td colspan="2">&nbsp;</td>
        </tr>

        <% for(int dsindex=0; dsindex < 4; dsindex++ ) { %>
        <!-- Data Source <%=dsindex+1%> -->     
        <tr>
          <td valign="top">
            Data Source <%=dsindex+1%> <%=(dsindex==0) ? "(required)" : "(optional)"%>:<br>
            <select name="ds" size="6">
                <% for(int i=0; i < rrds.length; i++ ) { %>
                  <% String rrdName = rrds[i].getName(); %>
                  <% String dsName  = rrdName.substring(0, rrdName.length() - GraphUtil.RRD_SUFFIX.length()); %>
                  <option <%=(dsindex==0 && i==0) ? "selected" : ""%>>
                    <%=dsName%>
              </option>
                <% } %>    
            </select>
          </td>

          <td valign="top">
            <table width="100%" cellspacing="0" cellpadding="2">
              <tr>
                <td width="5%">Title:</td>
                <td><input type="input" name="dstitle" value="DataSource <%=dsindex+1%>" /></td>
              </tr>

              <tr>
                <td width="5%">Color:</td> 
                <td> 
                  <select name="color">
                    <option value="ff0000"<%=(dsindex==0) ? " selected=\"selected\"" : ""%>>Red</option>
                    <option value="00ff00"<%=(dsindex==1) ? " selected=\"selected\"" : ""%>>Green</option>
                    <option value="0000ff"<%=(dsindex==2) ? " selected=\"selected\"" : ""%>>Blue</option>
                    <option value="000000"<%=(dsindex==3) ? " selected=\"selected\"" : ""%>>Black</option>
                  </select>
                </td>
              </tr>

              <tr>
                <td width="5%">Style:</td> 
                <td> 
                  <select name="style">
                    <option value="LINE1">Thin Line</option>
                    <option value="LINE2" selected="selected">Medium Line</option>
                    <option value="LINE3">Thick Line</option>
                    <option value="AREA">Area</option>
                    <% if( dsindex > 0 ) { %>
                    <option value="STACK">Stack</option>
                    <% } %>
                  </select>
                </td>
              </tr>

              <tr>
                <td width="5%">Value&nbsp;Type:</td> 
                <td> 
                  <select name="agfunction">
                    <option value="AVERAGE" selected="selected">Average</option>
                    <option value="MIN">Minimum</option>
                    <option value="MAX">Maximum</option>
                  </select>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <tr><td colspan="2"><hr></td></tr>
        <% } %>
        
        <tr>
          <td>
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
