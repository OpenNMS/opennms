<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page import="org.opennms.web.enlinkd.LldpElementNode"%>
<%@page import="org.opennms.web.enlinkd.CdpElementNode"%>
<%@page import="org.opennms.web.enlinkd.OspfElementNode"%>
<%@page import="org.opennms.web.enlinkd.IsisElementNode"%>
<%@page import="org.opennms.web.enlinkd.BridgeElementNode"%>
<%@page import="org.opennms.web.enlinkd.EnLinkdElementFactory"%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="
        java.util.*,
        java.net.*,
        java.sql.SQLException,
        org.opennms.core.spring.BeanUtils,
        org.opennms.core.soa.ServiceRegistry,
        org.opennms.core.utils.InetAddressUtils,
        org.opennms.core.utils.WebSecurityUtils,
        org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao,
        org.opennms.netmgt.config.poller.outages.Outage,
        org.opennms.netmgt.model.OnmsNode,
        org.opennms.netmgt.dao.hibernate.PathOutageManagerDaoImpl,
        org.opennms.web.api.Authentication,
        org.opennms.web.asset.Asset,
        org.opennms.web.asset.AssetModel,
        org.opennms.web.element.*,
        org.opennms.web.navigate.*,
        org.springframework.util.StringUtils,
        org.springframework.web.context.WebApplicationContext,
        org.springframework.web.context.support.WebApplicationContextUtils"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%!private int m_telnetServiceId;
    private int m_sshServiceId;
    private int m_httpServiceId;
    private int m_httpsServiceId;
    private int m_dellServiceId;
    private int m_rdpServiceId;
    private int m_snmpServiceId;
	private AssetModel m_model = new AssetModel();

	public void init() throws ServletException {
        try {
            m_telnetServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("Telnet");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the Telnet service ID", e);
        }        

        try {
            m_sshServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("SSH");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the SSH service ID", e);
        } 

        try {
            m_httpServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("HTTP");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the HTTP service ID", e);
        }

        try {
            m_httpsServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("HTTPS");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the HTTPS service ID", e);
        }

        try {
            m_dellServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("Dell-OpenManage");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the Dell-OpenManage service ID", e);
        }

        try {
            m_rdpServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("MS-RDP");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the Mirosoft Remote Desktop service ID", e);
        }


        try {
            m_snmpServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("SNMP");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the SNMP service ID", e);
        }
    }

	public static String getStatusStringWithDefault(OnmsNode node_db) {
        String status = ElementUtil.getNodeStatusString(node_db);
        if (status != null) {
            return status;
        } else {
            return "Unknown";
        }
    }
    
    public static String findServiceAddress(int nodeId, int serviceId, ServletContext servletContext) throws SQLException, UnknownHostException {
        Service[] services = NetworkElementFactory.getInstance(servletContext).getServicesOnNode(nodeId, serviceId);
        if (services == null || services.length == 0) {
            return null;
        }
        
        List<InetAddress> ips = new ArrayList<>();
        for (Service service : services) {
            ips.add(InetAddressUtils.addr(service.getIpAddress()));
        }

        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

        if (lowest != null) {
            return lowest.getHostAddress();
        } else {
            return null;
        }
    }
    
    public static Collection<Map<String, String>> createLinkForService(int nodeId, int serviceId, String linkText, String linkPrefix, String linkSuffix, ServletContext servletContext) throws SQLException, UnknownHostException {
        String ip = findServiceAddress(nodeId, serviceId, servletContext);
        if (ip == null) {
            return new ArrayList<Map<String,String>>();
        }
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("text", linkText);
        map.put("url", linkPrefix + ip + linkSuffix);
        return Collections.singleton(map);
    }%>

<%
	OnmsNode node_db = ElementUtil.getNodeByParams(request, getServletContext());
    int nodeId = node_db.getId();
    
    Map<String, Object> nodeModel = new TreeMap<String, Object>();
    nodeModel.put("id", Integer.toString(nodeId));
    nodeModel.put("label", WebSecurityUtils.sanitizeString(node_db.getLabel()));
    nodeModel.put("foreignId", node_db.getForeignId());
    nodeModel.put("foreignSource", node_db.getForeignSource());
    nodeModel.put("location", node_db.getLocation().getLocationName());

    List<Map<String, String>> links = new ArrayList<Map<String, String>>();
    links.addAll(createLinkForService(nodeId, m_telnetServiceId, "Telnet", "telnet://", "", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_sshServiceId, "SSH", "ssh://", "", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_httpServiceId, "HTTP", "http://", "/", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_httpsServiceId, "HTTPS", "https://", "/", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_dellServiceId, "OpenManage", "https://", ":1311", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_rdpServiceId, "Microsoft RDP", "rdp://", ":3389", getServletContext()));
    nodeModel.put("links", links);

    Asset asset = m_model.getAsset(nodeId);
    nodeModel.put("asset", asset);
    if (asset != null && asset.getBuilding() != null && asset.getBuilding().length() > 0) {
        nodeModel.put("statusSite", WebSecurityUtils.sanitizeString(asset.getBuilding(),true));
    }
    
    nodeModel.put("lldp",    EnLinkdElementFactory.getInstance(getServletContext()).getLldpElement(nodeId));
    nodeModel.put("cdp",    EnLinkdElementFactory.getInstance(getServletContext()).getCdpElement(nodeId));
    nodeModel.put("ospf",    EnLinkdElementFactory.getInstance(getServletContext()).getOspfElement(nodeId));
    nodeModel.put("isis",    EnLinkdElementFactory.getInstance(getServletContext()).getIsisElement(nodeId));
    nodeModel.put("bridges", EnLinkdElementFactory.getInstance(getServletContext()).getBridgeElements(nodeId));

    nodeModel.put("criticalPath", PathOutageManagerDaoImpl.getInstance().getPrettyCriticalPath(nodeId));
    nodeModel.put("noCriticalPath", PathOutageManagerDaoImpl.NO_CRITICAL_PATH);
    nodeModel.put("admin", request.isUserInRole(Authentication.ROLE_ADMIN));
    nodeModel.put("provision", request.isUserInRole(Authentication.ROLE_PROVISION));
    nodeModel.put("existsInRequisition", NetworkElementFactory.getInstance(getServletContext()).nodeExistsInRequisition(node_db.getForeignSource(), node_db.getForeignId()));

    // get the child interfaces
    Interface[] intfs = NetworkElementFactory.getInstance(getServletContext()).getActiveInterfacesOnNode(nodeId);
    if (intfs != null) { 
        nodeModel.put("intfs", intfs);
    } else {
        nodeModel.put("intfs", new Interface[0]);
    }

    Service[] snmpServices = NetworkElementFactory.getInstance(getServletContext()).getServicesOnNode(nodeId, m_snmpServiceId);
    if (snmpServices != null && snmpServices.length > 0) {
        for (Interface intf : intfs) {
            if ("P".equals(intf.getIsSnmpPrimary())) {
                nodeModel.put("snmpPrimaryIntf", intf);
                break;
            }
        }
    }
    
    nodeModel.put("status", getStatusStringWithDefault(node_db));
    nodeModel.put("showRancid","true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled")));
    
    nodeModel.put("node", node_db);
    nodeModel.put("sysName", WebSecurityUtils.sanitizeString(node_db.getSysName()));
    nodeModel.put("sysLocation", WebSecurityUtils.sanitizeString(node_db.getSysLocation()));
    nodeModel.put("sysContact", WebSecurityUtils.sanitizeString(node_db.getSysContact(), true));
    nodeModel.put("sysDescription", WebSecurityUtils.sanitizeString(node_db.getSysDescription()));
    
    pageContext.setAttribute("model", nodeModel);

	final WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
	final ServiceRegistry registry = webAppContext.getBean(ServiceRegistry.class);

	final List<String> renderedLinks = new ArrayList<>();
	final Collection<ConditionalPageNavEntry> navLinks = registry.findProviders(ConditionalPageNavEntry.class, "(Page=node)");
	for (final ConditionalPageNavEntry link : navLinks) {
	    final DisplayStatus displayStatus = link.evaluate(request, node_db);
	    if (displayStatus == null) continue;
	    if (displayStatus == DisplayStatus.DISPLAY_NO_LINK) {
	        renderedLinks.add(link.getName());
	    } else if (displayStatus == DisplayStatus.DISPLAY_LINK) {
	        renderedLinks.add("<a href=\"" + link.getUrl().replace("%25nodeid%25", ""+nodeId) + "\">" + link.getName() + "</a>");
	    }
	}
	
	pageContext.setAttribute("navEntries", renderedLinks);

    final List<String> schedOutages = new ArrayList<>();

    ReadablePollOutagesDao pollOutagesDao = BeanUtils.getBean("pollerConfigContext", "pollOutagesDao",
            ReadablePollOutagesDao.class);
    
    for (final Outage outage : pollOutagesDao.getReadOnlyConfig().getOutages()) {
        if (pollOutagesDao.isCurTimeInOutage(outage)) {
            boolean inOutage = pollOutagesDao.isNodeIdInOutage(nodeId, outage);
            if (!inOutage) {
                for (final Interface i : intfs) {
                    if (pollOutagesDao.isInterfaceInOutage(i.getIpAddress(), outage)) {
                        inOutage = true;
                        break;
                    }
                }
            }
            if (inOutage) {
                final String name = outage.getName();
                final String link = "<a href=\"admin/sched-outages/editoutage.jsp?name=" + URLEncoder.encode(name, "UTF-8") + "\">" +name + "</a>";
                schedOutages.add(request.isUserInRole(Authentication.ROLE_ADMIN) ? link : name);
            }
        }
    }

	pageContext.setAttribute("schedOutages", schedOutages.isEmpty() ? null : StringUtils.collectionToDelimitedString(schedOutages, ", "));
    pageContext.setAttribute("maxInterfaceCount", System.getProperty("org.opennms.interfaceAvailabilityBox.maxInterfaceCount", "10"));
%>

<%@page import="org.opennms.core.resource.Vault"%>
<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Node" />
  <jsp:param name="headTitle" value="${model.label}" />
  <jsp:param name="headTitle" value="ID ${model.id}" />
  <jsp:param name="headTitle" value="Node" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="Node" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="angular-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="onms-interfaces-app" />
</jsp:include>

<script type="text/javascript">
function confirmAssetEdit() {
  var confirmText = "You are about to edit asset fields for a node that was provisioned " +
    "through a requisition. Any edits made here will be rolled back the next " +
    "time the requisition \"${model.node.foreignSource}\" is " +
    "synchronized (typically every 24 hours) or the node manually rescanned.\n\n" +
    "To learn the best way to make permanent asset changes, talk to your " +
    "OpenNMS administrator.";
<c:if test="${model.foreignSource != null}">
<% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
    return confirm(confirmText);
<% } else { %>
    return true;
<% } %>
</c:if>
<c:if test="${model.foreignSource == null}">
  return true;
</c:if>
}
</script>

<h5>
  <c:if test="${model.foreignSource != null}">
    <div class="NPnode">Node: <strong>${model.label}</strong>&nbsp;&nbsp;&nbsp;<span class="NPdbid badge badge-secondary " title="Database ID: ${model.id}"><i class="fa fa-database"></i>&nbsp;${model.id}</span>&nbsp;<span class="NPfs badge badge-secondary " title="Requisition: ${model.foreignSource}"><i class="fa fa-list-alt"></i>&nbsp;${model.foreignSource}</span>&nbsp;<span class="NPfid badge badge-secondary " title="Foreign ID: ${model.foreignId}"><i class="fa fa-qrcode"></i>&nbsp;${model.foreignId}</span>&nbsp;<span class="NPloc badge badge-secondary " title="Location: ${model.location}"><i class="fa fa-map-marker"></i>&nbsp;${model.location}</span> <c:if test="${model.node.hasFlows}"><span class="NPflows badge badge-secondary " title="Flows: flow data available"><i class="fa fa-exchange"></i> flow data</span></c:if></div>
  </c:if>
  <c:if test="${model.foreignSource == null}">
    <div class="NPnode">Node: <strong>${model.label}</strong>&nbsp;&nbsp;&nbsp;<span class="NPdbid badge badge-secondary " title="Database ID: ${model.id}"><i class="fa fa-database"></i>&nbsp;${model.id}</span>&nbsp;<span class="NPloc badge badge-secondary " title="Location: ${model.location}"><i class="fa fa-map-marker"></i>&nbsp;${model.location}</span> <c:if test="${model.node.hasFlows}"><span class="NPflows badge badge-secondary " title="Flows: flow data available"><i class="fa fa-exchange"></i> flow data</span></c:if></div>
  </c:if>
</h5>

  <ul class="list-inline">
    <c:url var="eventLink" value="event/list">
      <c:param name="filter" value="node=${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${eventLink}"/>">View Events</a>
    </li>

    <c:url var="alarmLink" value="alarm/list.htm">
      <c:param name="filter" value="node=${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${alarmLink}"/>">View Alarms</a>
    </li>
    
    <c:url var="outageLink" value="outage/list.htm">
      <c:param name="filter" value="node=${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${outageLink}"/>">View Outages</a>
    </li>
    
    <c:url var="assetLink" value="asset/modify.jsp">
      <c:param name="node" value="${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${assetLink}"/>" onclick="return confirmAssetEdit()">Asset Info</a>
    </li>

    <c:url var="metaDataLink" value="element/node-metadata.jsp">
        <c:param name="node" value="${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${metaDataLink}"/>">Meta-Data</a>
    </li>

    <c:url var="hardwareLink" value="hardware/list.jsp">
      <c:param name="node" value="${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${hardwareLink}"/>">Hardware Info</a>
    </li>

    <c:url var="intfAvailabilityLink" value="element/availability.jsp">
      <c:param name="node" value="${model.id}"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${intfAvailabilityLink}"/>">Availability</a>
    </li>

    <c:if test="${! empty model.statusSite}">
      <c:url var="siteLink" value="siteStatusView.htm">
        <c:param name="statusSite" value="${model.statusSite}"/>
      </c:url>
      <li class="list-inline-item">
        <a href="<c:out value="${siteLink}"/>">Site Status</a>
      </li>
    </c:if>

    <c:forEach items="${model.links}" var="link">
      <li class="list-inline-item">
        <a href="<c:out value="${link.url}"/>">${link.text}</a>
      </li>
    </c:forEach>
    
    <%-- TODO In order to show the following link only when there are metrics, an
              inexpensive method has to be implemented on either ResourceService
              or ResourceDao --%>
    <c:url var="resourceGraphsUrl" value="graph/chooseresource.jsp">
      <c:param name="node" value="${model.id}"/>
      <c:param name="reports" value="all"/>
    </c:url>
    <li class="list-inline-item">
      <a href="<c:out value="${resourceGraphsUrl}"/>">Resource Graphs</a>
    </li>
    
    <c:if test="${model.admin}">
      <c:url var="rescanLink" value="element/rescan.jsp">
        <c:param name="node" value="${model.id}"/>
      </c:url>
      <li class="list-inline-item">
        <a href="<c:out value="${rescanLink}"/>">Rescan</a>
      </li>
      
      <c:url var="adminLink" value="admin/nodemanagement/index.jsp">
        <c:param name="node" value="${model.id}"/>
      </c:url>
      <li class="list-inline-item">
        <a href="<c:out value="${adminLink}"/>">Admin</a>
      </li>

      <c:if test="${! empty model.snmpPrimaryIntf}">
        <c:url var="updateSnmpLink" value="admin/updateSnmp.jsp">
          <c:param name="node" value="${model.id}"/>
          <c:param name="ipaddr" value="${model.snmpPrimaryIntf.ipAddress}"/>
        </c:url>
        <li class="list-inline-item">
          <a href="<c:out value="${updateSnmpLink}"/>">Update SNMP</a>
        </li>
      </c:if>
      
      <c:url var="createOutage" value="admin/sched-outages/editoutage.jsp">
	<c:param name="newName" value="${model.label}"/>
	<c:param name="addNew" value="true"/>
	<c:param name="nodeID" value="${model.id}"/>
      </c:url>
      <li class="list-inline-item">
        <a href="<c:out value="${createOutage}"/>">Schedule Outage</a>
      </li>
    </c:if>

    <c:if test="${model.existsInRequisition && (model.admin || model.provision)}">
        <li class="list-inline-item">
            <a href="<c:out value="admin/ng-requisitions/index.jsp#/requisitions/${model.foreignSource}/nodes/${model.foreignId}"/>">Edit in Requisition</a>
        </li>
    </c:if>

    <c:forEach items="${navEntries}" var="entry">
      <li class="list-inline-item">
      	<c:out value="${entry}" escapeXml="false" />
      </li>
    </c:forEach>
  </ul>


<c:if test="${! empty schedOutages}">
  <table class="table table-sm severity">
    <tr class="severity-Critical">
      <td align="left" class="bright">
        <b>This node is currently affected by the following scheduled outages: </b> ${schedOutages}
      </td>
    </tr>
  </table>
</c:if>

<% String showNodeStatusBar = System.getProperty("opennms.nodeStatusBar.show", "false");
   if (Boolean.parseBoolean(showNodeStatusBar)) { %>
<jsp:include page="/includes/nodeStatus-box.jsp?nodeId=${model.id}" flush="false" />
<% } %>

<div class="row">
<div class="col-md-6">
  
  <!-- Asset box, if info available --> 
  <c:if test="${! empty model.asset && (! empty model.asset.description || ! empty model.asset.comments)}">
    <div class="card">
    <div class="card-header">
      <span>Asset Information</span>
    </div>
    <table class="table table-sm">
      <tr>
        <th>Description</th>
        <td>${model.asset.description}</td>
      </tr>
      
      <tr>
        <th>Comments</th>
        <td>${model.asset.comments}</td>
      </tr>
    </table>
    </div>
  </c:if>

  <!-- SNMP box, if info available -->
  <c:if test="${! empty model.node.sysObjectId}">
    <div class="card">
    <div class="card-header">
      <span>SNMP Attributes</span>
    </div>
    
    <table class="table table-sm">
      <tr>
        <th>Name</th>
        <td>${model.sysName}</td>
      </tr>
      <tr>
        <th>sysObjectID</th>
        <td>${model.node.sysObjectId}</td>
      </tr>
      <tr>
        <th>Location</th>
        <td>${model.sysLocation}</td>
      </tr>
      <tr>
        <th>Contact</th>
        <td>${model.sysContact}</td>
      </tr>
      <tr>
        <th valign="top">Description</th>
        <td valign="top">${model.sysDescription}</td>
      </tr>
    </table>
    </div>
  </c:if>

  <!-- Critical Path info, if info available -->
  <c:if test="${model.criticalPath != model.noCriticalPath}">
    <div class="card">
    <div class="card-header">
      <span>Path Outage - Critical Path</span>
    </div>
    <div class="card-body">
      <ul class="list-unstyled mb-0">
        <li class="list-inline-item">
          ${model.criticalPath}
        </li>
      </ul> 
    </div>          
    </div>    
  </c:if>
	
  <!-- Availability box -->
  <c:if test="${fn:length( model.intfs ) <= maxInterfaceCount}">
    <jsp:include page="/includes/nodeAvailability-box.jsp" flush="false" >
      <jsp:param name="node" value="${model.id}" />
    </jsp:include>
  </c:if>

  <div id="onms-interfaces" class="card">
    <div class="card-header">
        <span>Node Interfaces</span>
    </div>
    <onms-interfaces node="${model.id}"/>
  </div>

  <!-- LLDP box, if info available --> 
  <c:if test="${! empty model.lldp }">
    <div class="card">
    <div class="card-header">
      <span>LLDP Information</span>
    </div>
    <table class="table table-sm">
      <tr><th width="50%">chassis id</th><td width="50%">${model.lldp.lldpChassisId}</td></tr>
      <tr><th width="50%">sysname</th><td width="50%">${model.lldp.lldpSysName}</td></tr>
      <tr><th width="50%">last poll time</th><td width="50%">${model.lldp.lldpLastPollTime}</td></tr>
    </table>
    </div>
    </c:if>

  <!-- CDP box, if info available --> 
  <c:if test="${! empty model.cdp }">
    <div class="card">
    <div class="card-header">
      <span>CDP Information</span>
    </div>
    <table class="table table-sm">
      <tr><th width="50%">global device id</th><td width="50%">${model.cdp.cdpGlobalDeviceId}</td></tr>
      <tr><th width="50%">global run</th><td width="50%">${model.cdp.cdpGlobalRun}</td></tr>
      <tr><th width="50%">last poll time</th><td width="50%">${model.cdp.cdpLastPollTime}</td></tr>
    </table>
  </div>
  </c:if>
  <!--End CDP box, if info available --> 

  <!-- OSPF box, if info available -->
  <c:if test="${! empty model.ospf }">
    <div class="card">
    <div class="card-header">
      <span>OSPF Information</span>
    </div>
    <table class="table table-sm">
      <tr><th width="50%">Router Id</th><td width="50%">${model.ospf.ospfRouterId}</td></tr>
      <tr><th width="50%">Status</th><td width="50%">${model.ospf.ospfAdminStat} version:${model.ospf.ospfVersionNumber}</td></tr>
      <tr><th>last poll time</th><td>${model.ospf.ospfLastPollTime}</td></tr>
    </table>
  </div>
  </c:if>

  <!-- IS-IS box, if info available -->
  <c:if test="${! empty model.isis }">
    <div class="card">
    <div class="card-header">
      <span>IS-IS Information</span>
    </div>
    <table class="table table-sm">
      <tr><th width="50%">Sys ID</th><td width="50%">${model.isis.isisSysID}</td></tr>
      <tr><th width="50%">Admin State</th><td width="50%">${model.isis.isisSysAdminState}</td></tr>
      <tr><th width="50%">last poll time</th><td width="50%">${model.isis.isisLastPollTime}</td></tr>
    </table>
    </div>
  </c:if>

  <!-- Bridge box if available -->
  <c:if test="${! empty model.bridges}">
    <div class="card">
   	<div class="card-header">
   	  <span>Bridge Information</span>
   	</div>
	<table class="table table-sm">
	<c:forEach items="${model.bridges}" var="bridge">
   	<tr>
   	<th width="50%"><c:if test="${! empty bridge.vlanname}">Vlan ${bridge.vlanname}</c:if>
   	    <c:if test="${! empty bridge.vlan}">(vlanid ${bridge.vlan})</c:if>
   	    <c:if test="${empty bridge.vlan}">Default</c:if>
   	    (${bridge.baseNumPorts} port assigned)
   	</th>
    <td width="50%"> baseAddress:${bridge.baseBridgeAddress} type:${bridge.baseType} 
    	<c:if test="${! empty bridge.stpProtocolSpecification}">stpProtocolSpec:${bridge.stpProtocolSpecification}</c:if>
 	    <c:if test="${! empty bridge.stpPriority && bridge.stpPriority > 0}">Priority:${bridge.stpPriority}</c:if>
 	    <c:if test="${! empty bridge.stpDesignatedRoot}">DesignatedRoot:${bridge.stpDesignatedRoot}</c:if>
 	    <c:if test="${! empty bridge.stpRootPort && bridge.stpRootPort > 0}">RootPort:${bridge.stpRootPort}</c:if>
 	    <c:if test="${! empty bridge.stpRootCost && bridge.stpRootCost > 0}">RootCost:${bridge.stpRootCost}</c:if>
	</tr>
	</c:forEach>
    </table>
    </div>
  </c:if>

</div> <!-- end of tag col-md-6 -->

<div class="col-md-6">
  
  <!-- general info box -->
  <div class="card">
    <div class="card-header">
  	<span>General (Status: ${model.status})</span>
    </div>
  <div class="card-body">
    <ul class="list-unstyled mb-0">
      <c:if test="${model.showRancid}">
        <c:url var="rancidLink" value="inventory/rancid.htm">
          <c:param name="node" value="${model.id}"/>
        </c:url>
        <li class="list-inline-item">
          <a href="<c:out value="${rancidLink}"/>">View Node Rancid Inventory Info </a>
        </li>
      </c:if>
      <c:url var="detailLink" value="element/linkednode.jsp">
        <c:param name="node" value="${model.id}"/>
      </c:url>
      <li class="list-inline-item">
        <a href="<c:out value="${detailLink}"/>">View Node Link Detailed Info</a>
      </li>
    </ul>
    </div>	     
  </div>
  
  <!-- Category box -->
  <jsp:include page="/includes/nodeCategory-box.htm" flush="false" >
    <jsp:param name="node" value="${model.id}" />
  </jsp:include>
  
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
  <jsp:include page="/outage/nodeOutages-box.htm" flush="false"> 
    <jsp:param name="node" value="${model.id}" />
  </jsp:include>

</div>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
