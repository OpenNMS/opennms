<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="
        java.util.Map,
        java.util.TreeMap,
        java.util.Enumeration,
        org.opennms.netmgt.config.PollerConfigFactory,
        org.opennms.netmgt.config.PollerConfig,
        org.opennms.netmgt.config.poller.Package,
        org.opennms.netmgt.config.poller.Service,
        org.opennms.netmgt.config.poller.Parameter,
        org.opennms.netmgt.model.OnmsMonitoredService,
        org.opennms.netmgt.poller.ServiceMonitor
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%
    OnmsMonitoredService service = (OnmsMonitoredService)request.getAttribute("service");

    String ipAddr = service.getIpAddress().getHostAddress();
    String serviceName = service.getServiceName();

    PollerConfigFactory.init();
    PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
    pollerCfgFactory.rebuildPackageIpListMap();

    Package lastPkg = null;
    Enumeration<Package> en = pollerCfgFactory.enumeratePackage();
    while (en.hasMoreElements()) {
        Package pkg = en.nextElement();
        if (!pkg.getRemote() &&
    pollerCfgFactory.isServiceInPackageAndEnabled(serviceName, pkg) &&
    pollerCfgFactory.isInterfaceInPackage(ipAddr, pkg)) {
    lastPkg = pkg;
        }
    }
    pageContext.setAttribute("packageName", lastPkg == null ? "N/A" : lastPkg.getName());

    ServiceMonitor monitor = pollerCfgFactory.getServiceMonitor(serviceName);
    pageContext.setAttribute("monitorClass", monitor == null ? "N/A" : monitor.getClass().getName());

    Map<String,String> parameters = new TreeMap<String,String>();
    Map<String,String> xmlParams  = new TreeMap<String,String>();
    if (lastPkg != null) {
        for (Service s : lastPkg.getServices()) {
            if (s.getName().equalsIgnoreCase(serviceName)) {
                for (Parameter p : s.getParameters()) {
                    if (p.getKey().toLowerCase().contains("password")) {
                        continue; // Hide passwords for security reasons
                    }
                    if (p.getValue() == null) {
                        String xmlData  = org.opennms.core.xml.JaxbUtils.marshal(p.getAnyObject());
                        String xmlFixed = xmlData.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("[\\r\\n]+", "<br/>").replaceAll(" ","&nbsp;").replaceAll("(password|user-info)=\"[^\"]+\"", "$1=\"XXXX\"").replaceAll("key=\"([^\"]*pass(word|wd)[^\"]*)\"(\\s|&nbsp;)+value=\"[^\"]+\"", "key=\"$1\" value=\"XXXX\"");
                        xmlParams.put(p.getKey(), xmlFixed);
                    } else {
                        parameters.put(p.getKey(), p.getValue());
                    }
                }
            }
        }
        pageContext.setAttribute("parameters", parameters);
        pageContext.setAttribute("xmlParams", xmlParams);
    }
%>

<c:url var="eventUrl" value="event/list.htm">
  <c:param name="filter" value="node=${service.ipInterface.node.id}"/> 
  <c:param name="filter" value="interface=${service.ipInterface.ipAddress.hostAddress}"/>
  <c:param name="filter" value="service=${service.serviceId}"/>
</c:url>
<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="${service.ipInterface.node.id}"/>
</c:url>
<c:url var="interfaceLink" value="element/interface.jsp">
  <c:param name="ipinterfaceid" value="${service.ipInterface.id}"/>
</c:url>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="headTitle" value="${service.serviceName} Service on ${service.ipInterface.ipAddress.hostAddress}" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='${fn:escapeXml(nodeLink)}'>Node</a>" />
  <jsp:param name="breadcrumb" value="<a href='${fn:escapeXml(interfaceLink)}'>Interface</a>" />
  <jsp:param name="breadcrumb" value="Service" />
</jsp:include>
  
  
<sec:authorize url="admin/deleteService">

<script type="text/javascript" >
function doDelete() {
     if (confirm("Are you sure you want to proceed? This action will permanently delete this service and cannot be undone."))
     {
         document.forms["delete"].submit();
     }
     return false;
}
</script>

</sec:authorize>
	
      <h4>${service.serviceName} service on ${service.ipAddress.hostAddress}</h4>

       <sec:authorize url="admin/deleteService">
         <form method="post" name="delete" action="admin/deleteService">
         <input type="hidden" name="node" value="${service.ipInterface.node.id}"/>
         <input type="hidden" name="intf" value="${service.ipInterface.ipAddress.hostAddress}"/>
         <input type="hidden" name="service" value="${service.serviceType.id}"/>
       </sec:authorize>

        
      <ul class="list-inline">
         <li><a href="${eventUrl}">View Events</a></li>
         
 	
       <sec:authorize url="admin/deleteService">
         <li><a href="admin/deleteService" onClick="return doDelete()">Delete</a></li>
       </sec:authorize>

	

      </ul>
 
          

      <sec:authorize url="admin/deleteService">
         </form>
      </sec:authorize>

      <div class="row">
      <div class="col-md-6">
            <!-- general info box -->
            <div class="panel panel-default">
            <div class="panel-heading">
              <h3 class="panel-title">General</h3>
            </div>
            <table class="table table-condensed">
              <tr>
                <c:url var="nodeLink" value="element/node.jsp">
                  <c:param name="node" value="${service.ipInterface.node.id}"/>
                </c:url>
                <th>Node</th>
                <td><a href="${fn:escapeXml(nodeLink)}">${service.ipInterface.node.label}</a></td>
              </tr>
              <tr>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="ipinterfaceid" value="${service.ipInterface.id}"/> 
                </c:url>
                <th>Interface</th> 
                <td><a href="${fn:escapeXml(interfaceLink)}">${service.ipInterface.ipAddress.hostAddress}</a></td>
              </tr>              
              <tr>
                <th>Polling Status</th>
                <td>${service.statusLong}</td>
              </tr>
              <tr>
                <th>Polling Package</th>
                <td>${packageName}</td>
              </tr>
              <tr>
                <th>Monitor Class</th>
                <td>${monitorClass}</td>
              </tr>
            </table>
            </div>
            <!-- simple parameters box -->
            <c:if test="${parameters != null}">
              <div class="panel panel-default">
              <div class="panel-heading">
                <h3 class="panel-title">Service Parameters</h3>
              </div>
              <table class="table table-condensed">
              <c:forEach var="entry" items="${parameters}">
                <tr>
                  <th nowrap>${entry.key}</th>
                  <td>${entry.value}</td>
                </tr>
              </c:forEach>
              </table>
            </div>
            </c:if>
            <!-- XML parameters box -->
            <c:if test="${xmlParams != null}">
              <c:forEach var="entry" items="${xmlParams}">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <h3 class="panel-title">${entry.key}</h3>
                  </div>
                  <div class="panel-body" style="overflow-x:scroll;">${entry.value}</div>
                </div>
              </c:forEach>
            </c:if>

            <!-- Availability box -->
            <jsp:include page="/includes/serviceAvailability-box.jsp" flush="false" />
            
            <jsp:include page="/includes/serviceApplication-box.htm" flush="false" />
            
      </div> <!-- content-left" -->

      <div class="col-md-6">
            <!-- events list box -->
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="${service.ipInterface.node.id}" />
              <jsp:param name="ipAddr" value="${service.ipInterface.ipAddress.hostAddress}" />
              <jsp:param name="service" value="${service.serviceType.id}" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<a href='${eventUrl}'>Recent Events</a>" />
              <jsp:param name="moreUrl" value="${eventUrl}" />
            </jsp:include>
      
            <!-- Recent outages box -->
            <jsp:include page="/outage/serviceOutages-box.htm" flush="false" />
      </div> <!-- content-right -->
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


