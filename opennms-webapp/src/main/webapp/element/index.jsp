<%--

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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.element.*,
		org.opennms.web.asset.*
		"
%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Element Search" />
  <jsp:param name="headTitle" value="Element Search" />
  <jsp:param name="location" value="element" />
  <jsp:param name="breadcrumb" value="Search" />
</jsp:include>

  <div class="TwoColLeft">
      <h3><spring:message code="search.searchnodes"/></h3>
		<div class="boxWrapper">
            <form action="element/nodeList.htm" method="GET">
					<p align="right"><spring:message code="search.name"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <input type="text" name="nodename" />
              <input type="submit" value='<spring:message code="search.button"/>'/></p>                
            </form>

            <form action="element/nodeList.htm" method="GET">
					<p align="right"><spring:message code="search.tcpipaddress"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <input type="text" name="iplike" value="*.*.*.*" />
              <input type="submit" value='<spring:message code="search.button"/>'/></p>                
            </form>

            <form action="element/nodeList.htm" method="GET">
					<p align="right">
					    <select name="snmpParm" size="1">
                            <option>ifAlias</option> 
                            <option>ifName</option>
                            <option>ifDescr</option>
                        </select>
                        <select name="snmpParmMatchType" size="1">
                            <option value="contains"><spring:message code="search.contains"/></option> 
                            <option value="equals"><spring:message code="search.equals"/></option>
                        </select>:        
						<input type="hidden" name="listInterfaces" value="false"/>
						<input type="text" name="snmpParmValue" />
						<input type="submit" value='<spring:message code="search.button"/>'/></p>                
            </form>

            <form action="element/nodeList.htm" method="GET">
					<p align="right"><spring:message code="search.providingservice"/>
						<input type="hidden" name="listInterfaces" value="false"/>
						<select name="service" size="1">
						  <% while( serviceNameIterator.hasNext() ) { %>
						    <% String name = (String)serviceNameIterator.next(); %> 
						    <option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
						  <% } %>          
						</select>
						<input type="submit" value='<spring:message code="search.button"/>'/></p>                
            </form>
            
            <form action="element/nodeList.htm" method="GET">
					<p align="right"><spring:message code="search.macaddress"/>
						<input type="hidden" name="listInterfaces" value="false"/>
						<input type="text" name="maclike" />
						<input type="submit" value='<spring:message code="search.button"/>'/></p>
            </form>
                        
			<ul class="plain">
				<li><a href="element/nodeList.htm"><spring:message code="search.allnodes"/></a></li>
				<li><a href="element/nodeList.htm?listInterfaces=true"><spring:message code="search.allnodesandinterfaces"/></a></li>
			</ul>
		</div>
		
		<h3><spring:message code="search.assetinfo"/></h3>
		<div class="boxWrapper">
        <%-- category --%>
        <form action="asset/nodelist.jsp" method="GET">
          <p align="right"><spring:message code="search.category"/>
          <input type="hidden" name="column" value="category" />
          <select name="searchvalue" size="1">
            <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
              <option><%=Asset.CATEGORIES[i]%></option> 
            <% } %>
          </select>
          <input type="submit" value='<spring:message code="search.button"/>' />
			</p>
        </form>
		
        <form action="asset/nodelist.jsp" method="GET">
          <p align="right"><spring:message code="search.field"/>
				<select name="column" size="1">
				  <% for( int i=0; i < this.columns.length; i++ ) { %>
				    <option value="<%=this.columns[i][1]%>"><%=this.columns[i][0]%></option>
				  <% } %>
				</select><br />
				<spring:message code="search.containingtext"/>&nbsp;<input type="text" name="searchvalue" />
				<input type="submit" value='<spring:message code="search.button"/>' /></p>
        </form>
		<ul class="plain">
			<li><a href="asset/nodelist.jsp?column=<%=this.columns[0][1]%>&searchvalue="><spring:message code="search.allnodesassetinfo"/></a></li>
      </ul>
		</div>
  </div>


  <div class="TwoColRight">
      <h3><spring:message code="search.options"/></h3>
     <div class="boxWrapper"> 
      <!-- TODO: update this message to include info about % being a multi-char wildcard -->
      <p><spring:message code="search.optionsdescript1"/></p>
      <p>
      <spring:message code="search.optionsdescript2"/>
      </p>
        
      <p>
      <spring:message code="search.optionsdescript3"/>
      </p>
      
        <ul>
            <li>192.168.*.*
            <li>192.168.0-255.0-255
            <li>192.168.0,1,2,3-255.*
        </ul>

      <p>
      <spring:message code="search.optionsdescript4"/>
      </p>

      <p>
      <spring:message code="search.optionsdescript5"/>
      </p>

      <p>
      <spring:message code="search.optionsdescript6"/>
      </p>

      <p>
      <spring:message code="search.optionsdescript7"/>
      </p>

      <p>
      <spring:message code="search.optionsdescript8"/>
      </p>
		</div>
  </div>
<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
