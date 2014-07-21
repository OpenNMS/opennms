<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.element.*,
		org.opennms.web.asset.*
		"
%>

<%!
    protected AssetModel model;
    protected String[][] columns;

    public void init() throws ServletException {
        this.model = new AssetModel();
        this.columns = this.model.getColumns();
    }
%>

<%
    Map serviceNameMap = new TreeMap(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
    List serviceNameList = new ArrayList(serviceNameMap.keySet());
    Collections.sort(serviceNameList);
    Iterator serviceNameIterator = serviceNameList.iterator();
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Element Search" />
  <jsp:param name="headTitle" value="Element Search" />
  <jsp:param name="location" value="element" />
  <jsp:param name="breadcrumb" value="Search" />
</jsp:include>

  <div class="TwoColLeft">
      <h3>Search for Nodes</h3>
		<div class="boxWrapper">
            <form action="element/nodeList.htm" method="get">
					<p align="right">Name containing:          
              <input type="hidden" name="listInterfaces" value="false"/>
              <input type="text" name="nodename" />
              <input type="submit" value="Search"/></p>                
            </form>

            <form action="element/nodeList.htm" method="get">
					<p align="right">TCP/IP Address like:          
              <input type="hidden" name="listInterfaces" value="false"/>
              <input type="text" name="iplike" value="" placeholder="*.*.*.*" />
              <input type="submit" value="Search"/></p>                
            </form>

            <form action="element/nodeList.htm" method="get">
					<p align="right">
					    <select name="snmpParm" size="1">
                            <option>ifAlias</option> 
                            <option>ifName</option>
                            <option>ifDescr</option>
                        </select>
                        <select name="snmpParmMatchType" size="1">
                            <option>contains</option> 
                            <option>equals</option>
                        </select>:        
						<input type="hidden" name="listInterfaces" value="false"/>
						<input type="text" name="snmpParmValue" />
						<input type="submit" value="Search"/></p>                
            </form>

            <form action="element/nodeList.htm" method="get">
					<p align="right">Providing service:          
						<input type="hidden" name="listInterfaces" value="false"/>
						<select name="service" size="1">
						  <% while( serviceNameIterator.hasNext() ) { %>
						    <% String name = (String)serviceNameIterator.next(); %> 
						    <option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
						  <% } %>          
						</select>
						<input type="submit" value="Search"/></p>                
            </form>
            
            <form action="element/nodeList.htm" method="get">
					<p align="right">MAC Address like:          
						<input type="hidden" name="listInterfaces" value="false"/>
						<input type="text" name="maclike" />
						<input type="submit" value="Search"/></p>                
            </form>
            
            <form action="element/nodeList.htm" method="get">
                    <p align="right">Foreign Source name like:
                        <input type="hidden" name="listInterfaces" value="false"/>
                        <input type="text" name="foreignSource"/>
                        <input type="submit" value="Search"/>
                    </p>
            </form>
                        
			<ul class="plain">
				<li><a href="element/nodeList.htm">All nodes</a></li>
				<li><a href="element/nodeList.htm?listInterfaces=true">All nodes and their interfaces</a></li>
			</ul>
		</div>
		
		<h3>Search Asset Information</h3>
		<div class="boxWrapper">
        <%-- category --%>
        <form action="asset/nodelist.jsp" method="get">
          <p align="right">Category: 
          <input type="hidden" name="column" value="category" />
          <select name="searchvalue" size="1">
            <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
              <option><%=Asset.CATEGORIES[i]%></option> 
            <% } %>
          </select>
          <input type="submit" value="Search" />
			</p>
        </form>
		
        <form action="asset/nodelist.jsp" method="get">
          <p align="right">Field:
				<select name="column" size="1">
				  <% for( int i=0; i < this.columns.length; i++ ) { %>
				    <option value="<%=this.columns[i][1]%>"><%=this.columns[i][0]%></option>
				  <% } %>
				</select><br />
				Containing text: <input type="text" name="searchvalue" />
				<input type="submit" value="Search" /></p>
        </form>
		<ul class="plain">
			<li><a href="asset/nodelist.jsp?column=_allNonEmpty">All nodes with asset info</a></li>
      </ul>
		</div>
  </div>


  <div class="TwoColRight">
      <h3>Search Options</h3>
     <div class="boxWrapper"> 
      <p>Searching by name is a case-insensitive, inclusive search. For example,
        searching on <em>serv</em> would find any of <em>serv</em>, <em>Service</em>, 
        <em>Reserved</em>, <em>NTSERV</em>, <em>UserVortex</em>, etc. The underscore
        character acts as a single character wildcard. The percent character acts as a multiple
        character wildcard.
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
      </p>
      
        <ul>
            <li>192.168.*.*
            <li>192.168.0-255.0-255
            <li>192.168.0,1,2,3-255.*
        </ul>

      <p>A search for ifAlias, ifName, or ifDescr "contains" will find nodes with interfaces
        that match the given search string. This is a case-insensitive inclusive search
        similar to the "name" search described above. If the search modifier is "equals" rather
         than "contains" an exact match must be found.
      </p>

      <p>To search by Service, click the down arrow and select the service you would
        like to search for.
      </p>

      <p>Searching by MAC Address allows you to find interfaces with hardware (MAC) addresses
         matching the search string. This is a case-insensitive partial string match. For
         example, you can find all interfaces with a specified manufacturer's code by entering
         the first 6 characters of the mac address. Octet separators (dash or colon) are optional.
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
		</div>
  </div>
<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
