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
        org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao,
        org.opennms.netmgt.config.poller.outages.Outage,
        org.opennms.netmgt.model.OnmsNode,
        org.opennms.netmgt.dao.api.PathOutageManager,
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


<%!
    private int m_telnetServiceId;
    private int m_sshServiceId;
    private int m_httpServiceId;
    private int m_dellServiceId;
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
            m_dellServiceId = NetworkElementFactory.getInstance(getServletContext()).getServiceIdFromName("Dell-OpenManage");
        } catch (Throwable e) {
            throw new ServletException("Could not determine the Dell-OpenManage service ID", e);
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
    nodeModel.put("label", node_db.getLabel());
    nodeModel.put("foreignId", node_db.getForeignId());
    nodeModel.put("foreignSource", node_db.getForeignSource());

    List<Map<String, String>> links = new ArrayList<Map<String, String>>();
    links.addAll(createLinkForService(nodeId, m_telnetServiceId, "Telnet", "telnet://", "", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_sshServiceId, "SSH", "ssh://", "", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_httpServiceId, "HTTP", "http://", "/", getServletContext()));
    links.addAll(createLinkForService(nodeId, m_dellServiceId, "OpenManage", "https://", ":1311", getServletContext()));
    nodeModel.put("links", links);

    Asset asset = m_model.getAsset(nodeId);
    nodeModel.put("asset", asset);
    if (asset != null && asset.getBuilding() != null && asset.getBuilding().length() > 0) {
        nodeModel.put("statusSite", asset.getBuilding());
    }
    
    nodeModel.put("lldp",    EnLinkdElementFactory.getInstance(getServletContext()).getLldpElement(nodeId));
    nodeModel.put("cdp",    EnLinkdElementFactory.getInstance(getServletContext()).getCdpElement(nodeId));
    nodeModel.put("ospf",    EnLinkdElementFactory.getInstance(getServletContext()).getOspfElement(nodeId));
    nodeModel.put("isis",    EnLinkdElementFactory.getInstance(getServletContext()).getIsisElement(nodeId));
    nodeModel.put("bridges", EnLinkdElementFactory.getInstance(getServletContext()).getBridgeElements(nodeId));

    nodeModel.put("criticalPath", PathOutageManagerDaoImpl.getInstance().getPrettyCriticalPath(nodeId));
    nodeModel.put("noCriticalPath", PathOutageManager.NO_CRITICAL_PATH);
    nodeModel.put("admin", request.isUserInRole(Authentication.ROLE_ADMIN));
    
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
    
    nodeModel.put("node", node_db);
    
    if(!(node_db.getForeignSource() == null) && !(node_db.getForeignId() == null)) {
        nodeModel.put("parentRes", node_db.getForeignSource() + ":" + node_db.getForeignId());
        nodeModel.put("parentResType", "nodeSource");
    } else {
        nodeModel.put("parentRes", Integer.toString(nodeId));
        nodeModel.put("parentResType", "node");
    }

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
	        renderedLinks.add("<a href=\"" + link.getUrl().replace("%nodeid%", ""+nodeId) + "\">" + link.getName() + "</a>");
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
                final String link = "<a href=\"admin/sched-outages/editoutage.jsp?name=" + name + "\">" + name + "</a>";
                schedOutages.add(request.isUserInRole(Authentication.ROLE_ADMIN) ? link : name);
            }
        }
    }

	pageContext.setAttribute("schedOutages", schedOutages.isEmpty() ? null : StringUtils.collectionToDelimitedString(schedOutages, ", "));
%>

<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>

<%@page import="org.opennms.core.resource.Vault"%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("${model.label}")
          .headTitle("ID ${model.id}")
          .headTitle("Availability")
          .breadcrumb("Search", "element/index.jsp")
          .breadcrumb("Node", "${nodeLink}")
          .breadcrumb("Availability")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="onms">
  <h2>Node: <%=WebSecurityUtils.sanitizeString((String) nodeModel.get("label")) %> (ID: ${model.id})</h2>

  <!-- Availability box -->
  <jsp:include page="/includes/nodeAvailability-box.jsp" flush="false" >
    <jsp:param name="node" value="${model.id}" />
  </jsp:include>
</div>
  
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
