<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.*,org.opennms.web.graph.*,org.opennms.web.response.*,java.util.Calendar" %>

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
    if( nodeIdString == null ) {
        throw new MissingParameterException("node");
    }

    int nodeId = Integer.parseInt(nodeIdString);

    String intf = request.getParameter("intf");    
    
    PrefabGraph[] graphs = null;
    
    if(intf == null) {
        throw new MissingParameterException("intf");
    }
    else {
        boolean includeNodeQueries = true;
        graphs = this.model.getQueries(nodeId, intf, includeNodeQueries);    
    }

    //order the graphs by their order in the properties file
    //(PrefabGraph implements the Comparable interface)
    Arrays.sort(graphs);
    
    Calendar now = Calendar.getInstance();
    Calendar yesterday = Calendar.getInstance();
    yesterday.add( Calendar.DATE, -1 );
%>

<html>
<head>
  <title>Choose Report and Date | Response Time | Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
  function validateReport()
  {
      var isChecked = false
      for( i = 0; i < document.report.reports.length; i++ ) 
      {
         //make sure something is checked before proceeding
         if (document.report.reports[i].selected)
         {
            isChecked=true;
         }
      }
      
      if (!isChecked)
      {
          alert("Please check the reports that you would like to see.");
      }
      return isChecked;
  }
  
  function submitForm()
  {
      if (validateReport())
      {
          document.report.submit();
      }
  }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='report/index.jsp'>Reports</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='response/index.jsp'>Response Time</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Choose Report and Date"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Report and Date" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->


<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
    <form METHOD="GET" NAME="report" ACTION="response/results.jsp" >
      <%=Util.makeHiddenTags(request)%>

      <table width="100%" cellspacing="0" cellpadding="2" border="0">
        <tr>
          <td colspan="2">
            <h3> Network Response Time Data </h3>
          </td>
        </tr>

        <tr>
          <td rowspan="2" valign="top" width="40%">
            <p>Please choose one or more of the following queries to perform on the interface.</p>

            <p>
              <select name="reports" multiple="multiple" size="10">
              <% for( int i = 0; i < graphs.length; i++ ) { %>
                <option VALUE=<%=graphs[i].getName()%>><%=graphs[i].getTitle()%></option>
              <% } %>
              </select>
            </p>
          </td>

          <td valign="top" width="60%">
            Query Start Time<br>

            <select name="startMonth" size="1">
              <% for( int i = 0; i < 12; i++ ) { %>
                 <option value="<%=i%>" <% if( yesterday.get( Calendar.MONTH ) == i ) out.print("selected ");%>><%=months[i]%></option>
              <% } %>
            </select>

            <input type="text" name="startDate" size="4" maxlength="2" value="<%=yesterday.get( Calendar.DATE )%>" />
            <input type="text" name="startYear" size="6" maxlength="4" value="<%=yesterday.get( Calendar.YEAR )%>" />

            <select name="startHour" size="1">
              <%
                 int yesterdayHour = yesterday.get( Calendar.HOUR_OF_DAY );
                 for( int i = 1; i < 25; i++ ) {
              %>
                <option value="<%=i%>" <% if( yesterdayHour == i ) out.print( "selected " ); %>>
                  <%=(i<13) ? i : i-12%>&nbsp;<%=(i<12 | i>23) ? "AM" : "PM"%>
                </option>
              <% } %>
            </select>

          </td>
        </tr>

        <tr>
          <td valign="top">
            <p>Query End Time</p>

            <select name="endMonth" size="1">
              <% for( int i = 0; i < 12; i++ ) { %>
                 <option value="<%=i%>" <% if( now.get( Calendar.MONTH ) == i ) out.print("selected ");%>><%=months[i]%></option>
              <% } %>
            </select>

            <input type="text" name="endDate" size="4" maxlength="2" value="<%=now.get( Calendar.DATE )%>" />
            <input type="text" name="endYear" size="6" maxlength="4" value="<%=now.get( Calendar.YEAR )%>" />

            <select name="endHour" size="1">
              <%
                 int nowHour = now.get( Calendar.HOUR_OF_DAY );
                 for( int i = 1; i < 25; i++ ) {
              %>
                <option value="<%=i%>" <% if( nowHour == i ) out.print( "selected " ); %>>
                  <%=(i<13) ? i : i-12%>&nbsp;<%=(i<12 | i>23) ? "AM" : "PM"%>
                </option>
              <% } %>
            </select>

          </td>
        </tr>

        <tr>
          <td colspan="2">&nbsp;</td>
        </tr>

        <tr>
          <td colspan="2" >
            <input type="button" value="Submit" onclick="submitForm()"/>
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


<%!
    //note these run from 0-11, this is because of java.util.Calendar!
    public static String[] months = new String[] {
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };
%>
