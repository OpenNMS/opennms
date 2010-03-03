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
// 2008 Aug 31: Use new alarm list URL. - dj@opennms.org
// 2008 May 20: Remove the extra box around the interfaces table. - dj@opennms.org
// 2007 Nov 12: change interfaces box to table
// 2007 Jun 02: Refactor to MVC pattern and pull reusable code into
//              for dealing with nodes into ElementUtil. - dj@opennms.org
// 2007 May 27: Organize imports, cleanup breadcrumbs. - dj@opennms.org
// 2007 Feb 01: Don't display the "Site Status" link if the building
//              column in assets is a zero-length string. - dj@opennms.org
// 2006 Oct 30: Convert to use Java 5 generics and clean up warnings.
//              - dj@opennms.org
// 2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor
// 2004 Jan 15: Added node admin function.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 01: Added response time link (Bug #684) and HTTP link (Bug #469).
// 2002 Nov 26: Fixed breadcrumbs issue.
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
	import="org.opennms.web.element.*,
		java.util.*,
		java.net.*,
        java.sql.SQLException,
        org.opennms.core.utils.IPSorter,
        org.opennms.web.springframework.security.Authentication,
        org.opennms.web.svclayer.ResourceService,
        org.opennms.web.asset.Asset,
        org.opennms.web.asset.AssetModel,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%!
    private int m_telnetServiceId;
    private int m_httpServiceId;
    private int m_dellServiceId;
    private int m_snmpServiceId;
    private ResourceService m_resourceService;
	private AssetModel m_model = new AssetModel();

    public void init() throws ServletException {
        try {
            m_telnetServiceId = NetworkElementFactory.getServiceIdFromName("Telnet");
        } catch (Exception e) {
            throw new ServletException("Could not determine the Telnet service ID", e);
        }        

        try {
            m_httpServiceId = NetworkElementFactory.getServiceIdFromName("HTTP");
        } catch (Exception e) {
            throw new ServletException("Could not determine the HTTP service ID", e);
        }

        try {
            m_dellServiceId = NetworkElementFactory.getServiceIdFromName("Dell-OpenManage");
        } catch (Exception e) {
            throw new ServletException("Could not determine the Dell-OpenManage service ID", e);
        }

        try {
            m_snmpServiceId = NetworkElementFactory.getServiceIdFromName("SNMP");
        } catch (Exception e) {
            throw new ServletException("Could not determine the SNMP service ID", e);
        }

	    WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
    }
    
    public static String getStatusStringWithDefault(Node node_db) {
        String status = ElementUtil.getNodeStatusString(node_db);
        if (status != null) {
            return status;
        } else {
            return "Unknown";
        }
    }
    
    public static String findServiceAddress(int nodeId, int serviceId) throws SQLException, UnknownHostException {
        Service[] services = NetworkElementFactory.getServicesOnNode(nodeId, serviceId);
        if (services == null || services.length == 0) {
            return null;
        }
        
        List<InetAddress> ips = new ArrayList<InetAddress>();
        for (Service service : services) {
            ips.add(InetAddress.getByName(service.getIpAddress()));
        }

        InetAddress lowest = IPSorter.getLowestInetAddress(ips);

        if (lowest != null) {
            return lowest.getHostAddress();
        } else {
            return null;
        }
    }
    
    public static Collection<Map<String, String>> createLinkForService(int nodeId, int serviceId, String linkText, String linkPrefix, String linkSuffix) throws SQLException, UnknownHostException {
        String ip = findServiceAddress(nodeId, serviceId);
        if (ip == null) {
            Map<String, String> empty = new HashMap<String, String>(0);
            return Collections.singleton(empty);
        }
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("text", linkText);
        map.put("url", linkPrefix + ip + linkSuffix);
        return Collections.singleton(map);
    }
%>

<%
    Node node_db = ElementUtil.getNodeByParams(request);
    int nodeId = node_db.getNodeId();
    
    Map<String, Object> nodeModel = new TreeMap<String, Object>();
    nodeModel.put("id", Integer.toString(nodeId));
    nodeModel.put("label", node_db.getLabel());

    List<Map<String, String>> links = new ArrayList<Map<String, String>>();
    links.addAll(createLinkForService(nodeId, m_telnetServiceId, "Telnet", "telnet://", ""));
    links.addAll(createLinkForService(nodeId, m_httpServiceId, "HTTP", "http://", "/"));
    links.addAll(createLinkForService(nodeId, m_dellServiceId, "OpenManage", "https://", ":1311"));
    nodeModel.put("links", links);

    Asset asset = m_model.getAsset(nodeId);
    nodeModel.put("asset", asset);
    if (asset != null && asset.getBuilding() != null && asset.getBuilding().length() > 0) {
        nodeModel.put("statusSite", asset.getBuilding());
    }
    
    nodeModel.put("resources", m_resourceService.findNodeChildResources(nodeId));
    nodeModel.put("vlans", NetworkElementFactory.getVlansOnNode(nodeId));
    nodeModel.put("admin", request.isUserInRole(Authentication.ADMIN_ROLE));
    
    // get the child interfaces
    Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode(nodeId);
    if (intfs != null) { 
        nodeModel.put("intfs", intfs);
    } else {
        nodeModel.put("intfs", new Interface[0]);
    }

    // see if any interfaces have ifAliases
    nodeModel.put("hasIfAliases", NetworkElementFactory.nodeHasIfAliases(nodeId));
    
    Service[] snmpServices = NetworkElementFactory.getServicesOnNode(nodeId, m_snmpServiceId);
    if (snmpServices != null && snmpServices.length > 0) {
        for (Interface intf : intfs) {
            if ("P".equals(intf.getIsSnmpPrimary())) {
                nodeModel.put("snmpPrimaryIntf", intf);
                break;
            }
        }
    }
    
    nodeModel.put("status", getStatusStringWithDefault(node_db));
    nodeModel.put("showIpRoute", NetworkElementFactory.isRouteInfoNode(nodeId));
    nodeModel.put("showBridge", NetworkElementFactory.isBridgeNode(nodeId));
    nodeModel.put("showRancid","true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled")));
    
    nodeModel.put("node", node_db);
    
    pageContext.setAttribute("model", nodeModel);
%>

<%@page import="org.opennms.core.resource.Vault"%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node" />
  <jsp:param name="headTitle" value="${model.label}" />
  <jsp:param name="headTitle" value="Node" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="Node" />
  <jsp:param name="enableExtJS" value="true"/>
  <jsp:param name="script" value="<script type='text/javascript' src='js/opennms/ux/PageableGrid.js'></script>"/>
  <jsp:param name="script" value="<script type='text/javascript' src='js/opennms/ux/SearchFilterLayout.js'></script>"/>
  <jsp:param name="script" value="<script type='text/javascript' src='js/opennms/ux/SearchFilterGrid.js'></script>"/>
  <jsp:param name="script" value="<script type='text/javascript' src='js/opennms/ux/IPInterfaceGrid.js'></script>"/>
  <jsp:param name="script" value="<script type='text/javascript' src='js/opennms/ux/SNMPInterfaceGrid.js'></script>"/>
  <jsp:param name="script" value="<script type='text/javascript' src='js/nodePageView.js'></script>" />
</jsp:include>


<div class="onms">
<h2>Node: ${model.label}</h2>
<div id="linkbar">
  <ul class="o-menu">
    <c:url var="eventLink" value="event/list">
      <c:param name="filter" value="node=${model.id}"/>
    </c:url>
    <li class="o-menuitem">
      <a href="${eventLink}">View Events</a>
    </li>

    <c:url var="alarmLink" value="alarm/list.htm">
      <c:param name="filter" value="node=${model.id}"/>
    </c:url>
    <li class="o-menuitem">
      <a href="${alarmLink}">View Alarms</a>
    </li>
    
    <c:url var="assetLink" value="asset/modify.jsp">
      <c:param name="node" value="${model.id}"/>
    </c:url>
    <li class="o-menuitem">
      <a href="${assetLink}">Asset Info</a>
    </li>

    <c:if test="${! empty model.statusSite}">
      <c:url var="siteLink" value="siteStatusView.htm">
        <c:param name="statusSite" value="${model.statusSite}"/>
      </c:url>
      <li class="o-menuitem">
        <a href="${siteLink}">Site Status</a>
      </li>
    </c:if>

    <c:forEach items="${model.links}" var="link">
      <li class="o-menuitem">
        <a href="${link.url}">${link.text}</a>
      </li>
    </c:forEach>
    
    <c:if test="${! empty model.resources}">
      <c:url var="resourceGraphsUrl" value="graph/chooseresource.htm">
        <c:param name="parentResourceType" value="node"/>
        <c:param name="parentResource" value="${model.id}"/>
        <c:param name="reports" value="all"/>
      </c:url>
      <li class="o-menuitem">
        <a href="${resourceGraphsUrl}">Resource Graphs</a>
      </li>
    </c:if>
    
    <c:if test="${model.admin}">
      <c:url var="rescanLink" value="element/rescan.jsp">
        <c:param name="node" value="${model.id}"/>
      </c:url>
      <li class="o-menuitem">
        <a href="${rescanLink}">Rescan</a>
      </li>
      
      <c:url var="adminLink" value="admin/nodemanagement/index.jsp">
        <c:param name="node" value="${model.id}"/>
      </c:url>
      <li class="o-menuitem">
        <a href="${adminLink}">Admin</a>
      </li>

      <c:if test="${! empty model.snmpPrimaryIntf}">
        <c:url var="updateSnmpLink" value="admin/updateSnmp.jsp">
          <c:param name="node" value="${model.id}"/>
          <c:param name="ipaddr" value="${model.snmpPrimaryIntf.ipAddress}"/>
        </c:url>
        <li class="o-menuitem">
          <a href="${updateSnmpLink}">Update SNMP</a>
        </li>
      </c:if>
    </c:if>
  </ul>
</div>
</div>
<div class="TwoColLeft">
  
  <!-- general info box -->
  <h3 class="o-box">General (Status: ${model.status})</h3>
  <div class="boxWrapper">
    <ul class="plain o-box">
      <c:if test="${model.showRancid}">
        <c:url var="rancidLink" value="inventory/rancid.htm">
          <c:param name="node" value="${model.id}"/>
        </c:url>
        <li>
          <a href="${rancidLink}">View Node Rancid Inventory Info </a>
        </li>
      </c:if>

      <c:if test="${model.showIpRoute}">
        <c:url var="ipRouteLink" value="element/routeipnode.jsp">
          <c:param name="node" value="${model.id}"/>
        </c:url>
        <li>
          <a href="${ipRouteLink}">View Node Ip Route Info</a>
        </li>
      </c:if>
     
      <c:if test="${model.showBridge}">
        <c:url var="bridgeLink" value="element/bridgenode.jsp">
          <c:param name="node" value="${model.id}"/>
        </c:url>
        <li>
          <a href="${bridgeLink}">View Node Bridge/STP Info</a>
        </li>
      </c:if>

      <c:url var="detailLink" value="element/linkednode.jsp">
        <c:param name="node" value="${model.id}"/>
      </c:url>
      <li>
        <a href="${detailLink}">View Node Link Detailed Info</a>
      </li>
    </ul>	     
  </div>
	
	<c:choose>
		<c:when test="${fn:length( model.intfs ) < 10}">
			<!-- Availability box -->
  			<jsp:include page="/includes/nodeAvailability-box.jsp" flush="false" />	
		</c:when>
		
		<c:otherwise>
			<script type='text/javascript'>
				Ext.onReady(function(){
					initPageView('interfaces-panel', ${model.id});
				})
			</script>
			
			<!-- Interface box - generated by ExtJs -->
			<div id="interfaces-panel"></div>
		</c:otherwise>
	</c:choose>
  

  <!-- Asset box, if info available --> 
  <c:if test="${! empty model.asset && (! empty model.asset.description || ! empty model.asset.comments)}">
    <h3 class="o-box">Asset Information</h3>
    <table class="o-box">
      <tr>
        <th>Description</th>
        <td>${model.asset.description}</td>
      </tr>
      
      <tr>
        <th>Comments</th>
        <td>${model.asset.comments}</td>
      </tr>
    </table>
  </c:if>

  <!-- SNMP box, if info available -->
  <c:if test="${! empty model.node.nodeSysId}">
    <h3 class="o-box">SNMP Attributes</h3>
    <table class="o-box">
      <tr>
        <th>Name</th>
        <td>${model.node.nodeSysName}</td>
      </tr>
      <tr>
        <th>Object&nbsp;ID</th>
        <td>${model.node.nodeSysId}</td>
      </tr>
      <tr>
        <th>Location</th>
        <td>${model.node.nodeSysLocn}</td>
      </tr>
      <tr>
        <th>Contact</th>
        <td>${model.node.nodeSysContact}</td>
      </tr>
      <tr>
        <th valign="top">Description</th>
        <td valign="top">${model.node.nodeSysDescr}</td>
      </tr>
    </table>
  </c:if>

  <!-- Interface box - generated by ExtJs -->
<!--  <div id="interfaces-panel"></div>-->
  <c:if test="${fn:length( model.intfs ) < 10}">
  <!-- Interface box -->
  <h3 class="o-box">Interfaces</h3>
  <table class="o-box">
    <tr>
      <th>Interface</th>
      <th>Index</th>
      <th>Description</th>
      <c:if test="${model.hasIfAliases}">
        <th>IfAlias</th>
      </c:if>
    </tr>
    <c:forEach items="${model.intfs}" var="intf">
      <c:url var="interfaceLink" value="element/interface.jsp">
        <c:param name="ipinterfaceid" value="${intf.id}"/>
      </c:url>
      <tr>
        <td>
          <c:choose>
            <c:when test="${intf.ipAddress == '0.0.0.0'}">
              <c:choose>
                <c:when test="${intf.snmpIfName != null && intf.snmpIfName != ''}">
                  <a href="${interfaceLink}">${intf.snmpIfName}</a>
                </c:when>
                <c:when test="${intf.snmpIfDescription != null && intf.snmpIfDescription != ''}">
                  <a href="${interfaceLink}">${intf.snmpIfDescription}</a>
                </c:when>
                <c:otherwise>
                  <a href="${interfaceLink}">Non-IP</a>
                </c:otherwise>
              </c:choose>
            </c:when>
            <c:otherwise>
              <a href="${interfaceLink}">${intf.ipAddress}</a>
              <c:if test="${intf.ipAddress != intf.hostname}">
                (${intf.hostname})
              </c:if>
            </c:otherwise>
          </c:choose>
        </td>
        <td>
          <c:choose>
            <c:when test="${intf.ifIndex > 0}">
              ${intf.ifIndex}
            </c:when>
            <c:otherwise>
              &nbsp;
            </c:otherwise>
          </c:choose>
        </td>
        <td>
          <c:choose>
            <c:when test="${intf.snmpIfDescription != null && intf.snmpIfDescription != ''}">
              ${intf.snmpIfDescription}
            </c:when>
            <c:when test="${intf.snmpIfName != null && intf.snmpIfName != '' && intf.ipAddress != '0.0.0.0'}">
              ${intf.snmpIfName}
             </c:when>
            <c:otherwise>
              &nbsp;
            </c:otherwise>
          </c:choose>
        </td>
        <c:if test="${model.hasIfAliases}">
          <td>
            <c:if test="${intf.snmpIfAlias != null && intf.snmpIfAlias != ''}">
              ${intf.snmpIfAlias}
            </c:if>
          </td>
        </c:if>
      </tr>
    </c:forEach>
  </table>
  </c:if>
  
  
  <!-- Vlan box if available -->
  <c:if test="${! empty model.vlans}">
    <h3 class="o-box">VLAN Information</h3>
    <table class="o-box">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Type</th>
          <th>Status</th>
          <th>Status</th>
          <th>Last Poll Time</th>
        </tr>
      </thead>
  
      <c:forEach items="${model.vlans}" var="vlan">
        <tr>
          <td>${vlan.vlanId}</td>
          <td>${vlan.vlanName}</td>
          <td>${vlan.vlanTypeString}</td>
          <td>${vlan.vlanStatusString}</td>
          <td>${vlan.statusString}</td>
          <td>${vlan.lastPollTime}</td>
        </tr>
      </c:forEach>
    </table>
  </c:if>

  <!-- Category box -->
  <jsp:include page="/includes/nodeCategory-box.htm" flush="false" />
</div>

<div class="TwoColRight">
  
  <!-- notification box -->
  <jsp:include page="/includes/notification-box.jsp" flush="false" >
    <jsp:param name="node" value="${model.id}" />
  </jsp:include>
  
  <!-- events list  box -->
  <c:url var="eventListUrl" value="event/list">
    <c:param name="filter" value="node=${model.id}"/>
  </c:url>
  <jsp:include page="/includes/eventlist.jsp" flush="false" >
    <jsp:param name="node" value="${model.id}" />
    <jsp:param name="throttle" value="5" />
    <jsp:param name="header" value="<a href='${eventListUrl}'>Recent Events</a>" />
    <jsp:param name="moreUrl" value="${eventListUrl}" />
  </jsp:include>
  
  <!-- Recent outages box -->
  <jsp:include page="/outage/nodeOutages-box.htm" flush="false" />
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
