<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.*,org.opennms.web.performance.*,java.util.Calendar" %>

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
    String nodeId = request.getParameter("node");
    String intfId = request.getParameter("intf");
    if( nodeId == null ) {
        throw new MissingParameterException( "node", new String[] {"node", "endUrl"} );
    }
    if( intfId == null ) {
        intfId = "";
    }
    
    String[] intfs = this.model.getQueryableInterfacesForNode(nodeId);
    
    Arrays.sort(intfs);        
%>

<html>
<head>
  <title>Choose Interface | Performance | Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
  <script language="Javascript" type="text/javascript" >
      function validateInterface()
      {
          var isChecked = false
          for( i = 0; i < document.report.intf.length; i++ )
          {
              //make sure something is checked before proceeding
              if (document.report.intf[i].selected)
              {
                  isChecked=true;
              }
          }
  
          if (!isChecked)
          {
              alert("Please check the interfaces that you would like to report on.");
          }
          return isChecked;
      }
  
      function submitForm()
      {
          if(validateInterface())
          {
              document.report.submit();
          }
      }
  </script>
  </head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("report/index.jsp") + "'>Reports</a>"; %>
<% String breadcrumb2 = java.net.URLEncoder.encode("KSC Reports"); %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>
<br />

<!-- Body -->

<h3 align="center"> Customized Report - Graph Definition </h3>


<table width="100%" align="center">
  <tr>
    <td align="center">
      <form method="get" name="report" action="KSC/custom_graph3.jsp" >
        <input type=hidden name="node" value="<%=nodeId%>">
        <table>
          <tr>
            <td>
                <h3>Step 2: Choose an Interface to Query</h3>
            </td>
          </tr>

          <tr>
            <td>
                <select name="intf" size="10">
                  <% for(int i=0; i < intfs.length; i++) { %>
                      <% if (intfId.equals(intfs[i])) { %>
                          <option value="<%=intfs[i]%>" SELECTED> 
                      <% } else { %>
                          <option value="<%=intfs[i]%>"> 
                      <% } %>
                      <%=this.model.getHumanReadableNameForIfLabel(Integer.parseInt(nodeId), intfs[i])%></option>
                  <% } %>
              </select>
            </td>
          </tr>

          <tr>
            <td >
                <input type="button" value="Submit" onclick="submitForm()" />
            </td>
          </tr>
        </table>
      </form>
    </td>
  </tr>
</table>

<br/>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
