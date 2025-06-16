<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.element.*,
		org.opennms.web.asset.*,
		org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation"%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

<%!
    protected AssetModel model;
    protected String[][] columns;

    public void init() throws ServletException {
        this.model = new AssetModel();
        this.columns = AssetModel.getColumns();
    }
%>

<%
    Map<String,Integer> serviceNameMap = new TreeMap<String,Integer>(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
    List<String> serviceNameList = new ArrayList<String>(serviceNameMap.keySet());
    Collections.sort(serviceNameList);

    List<OnmsMonitoringLocation> monitoringLocations = NetworkElementFactory.getInstance(getServletContext()).getMonitoringLocations();
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Element Search")
          .breadcrumb("Search")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-lg-6 col-md-8">
    <div class="card">
      <div class="card-header">
        <span>Search for Nodes</span>
      </div>
      <div class="card-body">
        <div>
          <ul class="list-unstyled">
            <li><a href="element/nodeList.htm">All nodes</a></li>
            <li><a href="element/nodeList.htm?listInterfaces=true">All nodes and their interfaces</a></li>
          </ul>
        </div>
          <%-- Search by name --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="post">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="byname_nodename">Name containing</label>
              <div class="input-group">
                  <input type="text" class="form-control" id="byname_nodename" name="nodename"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

          <%-- Search by ip --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="post">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="byip_iplike">TCP/IP Address like</label>
              <div class="input-group">
                  <input type="text" class="form-control" id="byip_iplike" name="iplike" value="" placeholder="*.*.*.* or *:*:*:*:*:*:*:*:*"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

          <%-- Search by mib2 param --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label class="hidden-sm hidden-md hidden-lg">System attribute</label>
              <div class="input-group">
                  <select class="custom-select" name="mib2Parm">
                      <option>sysDescription</option>
                      <option>sysObjectId</option>
                      <option>sysContact</option>
                      <option>sysName</option>
                      <option>sysLocation</option>
                  </select>
                  <select class="custom-select" name="mib2ParmMatchType">
                      <option>contains</option>
                      <option>equals</option>
                  </select>
                  <input type="text" class="form-control" id="bymib2_mib2ParmValue" name="mib2ParmValue"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

        <%-- Search by interface param --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label class="hidden-sm hidden-md hidden-lg">Interface attribute</label>
              <div class="input-group">
                  <select class="custom-select" name="snmpParm">
                      <option>ifAlias</option>
                      <option>ifName</option>
                      <option>ifDescr</option>
                  </select>
                  <select class="custom-select" name="snmpParmMatchType">
                      <option>contains</option>
                      <option>equals</option>
                  </select>
                  <input type="text" class="form-control" id="byif_snmpParmValue" name="snmpParmValue"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

        <%-- Search by location --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="bymonitoringLocation_monitoringLocation">Location:</label>
              <div class="input-group">
                  <select class="custom-select" id="bymonitoringLocation_monitoringLocation" name="monitoringLocation">
                      <% for (OnmsMonitoringLocation monitoringLocation : monitoringLocations) { %>
                      <option value="<%=WebSecurityUtils.sanitizeString(monitoringLocation.getLocationName())%>"><%=WebSecurityUtils.sanitizeString(monitoringLocation.getLocationName())%>
                      </option>
                      <% } %>
                  </select>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

        <%-- Search by service --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="byservice_service">Providing service</label>
              <div class="input-group">
                  <select class="custom-select" id="byservice_service" name="service">
                      <% for (String name : serviceNameList) { %>
                      <option value="<%=serviceNameMap.get(name)%>"><%=name%>
                      </option>
                      <% } %>
                  </select>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

        <%-- Search by MAC --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="bymac_maclike">MAC Address like</label>
              <div class="input-group">
                  <input class="form-control" type="text" name="maclike"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

        <%-- Search by foreign source --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="byfs_foreignSource">Foreign Source name like</label>
              <div class="input-group">
                  <input type="text" class="form-control" name="foreignSource"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

          <%-- Search by flow data --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="listInterfaces" value="false"/>
              <label for="byflows_flows">Flows</label>
              <div class="input-group">
                  <select class="custom-select" id="byflows_flows" name="flows">
                      <option value="true">Nodes with flow data</option>
                      <option value="false">Nodes without flow data</option>
                  </select>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

          <%-- Search by Enhanced Linkd topology data --%>
          <form role="form" class="form-group" action="element/nodeList.htm" method="get">
              <input type="hidden" name="listInterfaces" value="false"/>
              <label class="hidden-sm hidden-md hidden-lg">Enhanced Linkd topology</label>
              <div class="input-group">
                  <input type="text" class="form-control" id="byif_topology" name="topology"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>

      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span>Search Asset Information</span>
      </div>
      <div class="card-body">
        <div>
            <ul class="list-unstyled">
              <li><a href="asset/nodelist.jsp?column=_allNonEmpty">All nodes with asset info</a></li>
            </ul>
        </div>

        <form role="form" class="form-group" action="asset/nodelist.jsp" method="get">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="column" value="category"/>
            <label for="bycat_value">Category</label>
            <div class="input-group">
                <select id="bycat_value" class="form-control custom-select" name="searchvalue">
                <% for (int i = 0; i < Asset.CATEGORIES.length; i++) { %>
                    <option><%=Asset.CATEGORIES[i]%></option>
                <% } %>
                </select>
                <div class="input-group-append">
                    <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                </div>
            </div>
        </form>

            <%-- Search by field --%>
          <form role="form" class="form-group" action="asset/nodelist.jsp" method="get">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <label for="byfield_column">Field</label>
              <div class="input-group">
                  <select id="byfield_column" class="form-control custom-select" name="column">
                      <% for (int i = 0; i < this.columns.length; i++) { %>
                      <option value="<%=this.columns[i][1]%>"><%=this.columns[i][0]%>
                      </option>
                      <% } %>
                  </select>
                  <input type="text" class="form-control" id="byfield_value" name="searchvalue" placeholder="Containing text"/>
                  <div class="input-group-append">
                      <button type="submit" class="btn btn-secondary"><i class="fa fa-search"></i></button>
                  </div>
              </div>
          </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-lg-6 col-md-4">
    <div class="card">
      <div class="card-header">
        <span>Search Options</span>
      </div>
      <div class="card-body">
          <p>Searching by name is a case-insensitive, inclusive search. For example,
            searching on <em>serv</em> would find any of <em>serv</em>, <em>Service</em>,
            <em>Reserved</em>, <em>NTSERV</em>, <em>UserVortex</em>, etc. The underscore
            character acts as a single character wildcard. The percent character acts as a multiple
            character wildcard.
          </p>

          <p>Searching by TCP/IP address uses a very flexible search format, allowing you
            to separate the four or eight (in case of IPv6) fields of a TCP/IP address into
            specific searches. An asterisk (*) in place of any octet matches any value for that
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
                <li>2001:6a8:3c80:8000-8fff:*:*:*:*
                <li>fc00,fe80:*:*:*:*:*:*:*
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

          <p>Search for Enhanced Linkd topology information allows you to find nodes with CDP/LLDP data
              matching the given search string.
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
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
