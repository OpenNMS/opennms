<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

-->

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
    String endUrl = request.getParameter("endUrl");
    
    if( nodeId == null ) {
        throw new MissingParameterException( "node", new String[] {"node", "endUrl"} );
    }
    
    if( endUrl == null ) {
        throw new MissingParameterException( "endUrl", new String[] {"node", "endUrl"} );
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
      function validateRRD()
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
          if(validateRRD())
          {
              document.report.submit();
          }
      }
  </script>
  </head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='report/index.jsp'>Reports</a>"; %>
<% String breadcrumb2 = "<a href='performance/index.jsp'>Performance</a>"; %>
<% String breadcrumb3 = "Choose Interface"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Interface" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br />
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <form method="get" name="report" action="<%=endUrl%>" >
        <%=Util.makeHiddenTags(request, new String[] {"endUrl"})%>

        <table width="100%" cellspacing="2" cellpadding="2" border="0">
          <tr>
            <td>
                <h3>Choose an Interface to Query</h3>
            </td>
          </tr>

          <tr>
            <td valign="top">
                <p>
                  The node that you have chosen has performance data for multiple interfaces.
                  Please choose the interface that you wish to query.
                </p>

                <select name="intf" size="10">
                  <% for(int i=0; i < intfs.length; i++) { %>
                    <option value="<%=intfs[i]%>" <%=(i==0) ? "selected" : ""%>><%=this.model.getHumanReadableNameForIfLabel(Integer.parseInt(nodeId), intfs[i])%></option>
                  <% } %>
              </select>
            </td>
          </tr>

          <tr>
            <td valign="top" >
                <input type="button" value="Submit" onclick="submitForm()" />
            </td>
          </tr>
        </table>
      </form>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br/>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>
