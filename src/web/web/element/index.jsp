<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com///

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.element.*,org.opennms.web.asset.*" %>

<%!
    protected AssetModel model;
    protected String[][] columns;

    public void init() throws ServletException {
        this.model = new AssetModel();
        this.columns = this.model.getColumns();
    }
%>

<%
    Map serviceNameMap = new TreeMap(NetworkElementFactory.getServiceNameToIdMap());
    Set serviceNameSet = serviceNameMap.keySet();
    Iterator serviceNameIterator = serviceNameSet.iterator();
%>

<html>
<head>
  <title>Element Search | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "Search"; %>
<jsp:include page="/WEB-INF/jspf/header.jspf" flush="false" >
  <jsp:param name="title" value="Element Search" />
  <jsp:param name="location" value="element" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">
      <h3>Search for Nodes</h3>

      <p>
        <table width="50%" border="0" cellpadding="2" cellspacing="0" >
          <tr>
            <td colspan="2">Name containing:</td>
          </tr>
          <tr>
            <form action="element/nodelist.jsp" method="GET">          
              <td><input type="text" name="nodename" /></td>
              <td><input type="submit" value="Search"/></td>                
            </form>
          </tr>
          
          <tr>
            <td colspan="2">TCP/IP Address like:</td>
          </tr>
          <tr>
            <form action="element/nodelist.jsp" method="GET">          
              <td><input type="text" name="iplike" value="*.*.*.*" /></td>
              <td><input type="submit" value="Search"/></td>                
            </form>
          </tr>

          <tr>
            <td colspan="2">Providing service:</td>
          </tr>
          <tr>
            <form action="element/nodelist.jsp" method="GET">          
              <td>
                <select name="service" size="1">
                  <% while( serviceNameIterator.hasNext() ) { %>
                    <% String name = (String)serviceNameIterator.next(); %> 
                    <option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
                  <% } %>          
                </select>
              </td>
              <td><input type="submit" value="Search"/></td>                
            </form>
          </tr>          
        </table>
      </p>

      <p>
        <a href="element/nodelist.jsp">List all nodes</a>
      </p>

      <h3>Search Asset Information</h3>
      <p>
        <%-- category --%>
        <form action="asset/nodelist.jsp" method="GET">
          Assets by category: <br>
          <input type="hidden" name="column" value="category" />
          <select name="searchvalue" size="1">
            <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
              <option><%=Asset.CATEGORIES[i]%></option> 
            <% } %>
          </select>
          <input type="submit" value="Search" />
        </form>

        <form action="asset/nodelist.jsp" method="GET">
          <table width="50%" cellspacing="0" cellpadding="2" border="0">
            <tr>
              <td>Asset Field:</td>
              <td>Containing Text:</td>
            </tr>
            <tr>
              <td>
                <select name="column" size="1">
                  <% for( int i=0; i < this.columns.length; i++ ) { %>
                    <option value="<%=this.columns[i][1]%>"><%=this.columns[i][0]%></option>
                  <% } %>
                </select>
              </td>
              <td>
                <input type="text" name="searchvalue" />
              </td>
            </tr>
            <tr>
              <td colspan="2"><input type="submit" value="Search" /></td>
            </tr>
          </table>
        </form>

        <a href="asset/nodelist.jsp?column=<%=this.columns[0][1]%>&searchvalue=">List all nodes with asset info</a>
      </p>
    </td>

    <td>&nbsp;</td>

    <td width="60%" valign="top">
      <h3>Search Options</h3>
      
      <p>Searching by name is a case-insensitive, inclusive search. For example,
        searching on <em>serv</em> would find any of <em>serv</em>, <em>Service</em>, 
        <em>Reserved</em>, <em>NTSERV</em>, <em>UserVortex</em>, etc.
      </p>
        
      <p>Searching by TCP/IP address uses a very flexible search format, allowing you
        to separate the four octets (fields) of a TCP/IP address into specific
        searches.  An asterisk (*) in place of any octet matches any value for that
        octet. Ranges are indicated by two numbers separated by a dash (-), and
        commas are used for list demarcation.
      </p>
        
      <p>For example, the following search fields are all valid and would each create
        the same result set--all TCP/IP addresses from 192.168.0.0 through
        192.168.255.255:
      
        <ul>
            <li>192.168.*.*
            <li>192.168.0-255.0-255
            <li>192.168.0,1,2,3-255.*
        </ul>
      </p>

      <p>To search by Service, click the down arrow and select the service you would
        like to search for.
      </p>

      <p>Searching for assets allows you to search for all assets which have been
        associated with a particular category, as well as to select a specific asset
        field (with all available fields listed in the drop-down list box) and
        search for text which matches its current value.  The latter search is very
        similar to the text search for node names described above.
      </p>

      <p>Also note that you can quickly search for all nodes which have asset
        information assigned by clicking the <em>List all nodes with asset info</em> link.
      </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/WEB-INF/jspf/footer.jspf" flush="false" >
  <jsp:param name="location" value="element" />
</jsp:include>

</body>
</html>
