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
	java.util.ArrayList,
	java.util.List,
        java.util.Map,
        java.util.TreeMap,
        java.util.Enumeration,
	org.opennms.netmgt.config.CollectdConfigFactory,
	org.opennms.netmgt.config.collectd.CollectdConfiguration,
	org.opennms.netmgt.config.collectd.Collector,
        org.opennms.netmgt.config.PollerConfigFactory,
        org.opennms.netmgt.config.PollerConfig,
        org.opennms.netmgt.config.poller.Package,
        org.opennms.netmgt.config.poller.Service,
        org.opennms.netmgt.config.poller.Parameter,
        org.opennms.netmgt.model.OnmsMonitoredService,
        org.opennms.netmgt.poller.ServiceMonitor,
        org.opennms.netmgt.poller.DefaultPollContext
        "
%>
<%@ page import="java.util.Optional" %>
<%@ page import="org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation" %>
<%@ page import="org.opennms.web.element.NetworkElementFactory" %>
<%@ page import="org.opennms.netmgt.model.OnmsOutage" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.opennms.web.services.ServiceJspUtil" %>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@ page import="org.opennms.core.mate.api.Interpolator" %>
<%@ page import="org.opennms.core.mate.api.FallbackScope" %>
<%@ page import="org.opennms.core.mate.api.Scope" %>
<%@ page import="org.opennms.core.utils.InetAddressUtils" %>
<%@ page import="org.opennms.core.mate.api.MapScope" %>
<%@ page import="org.opennms.netmgt.poller.ServiceMonitorLocator" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>

<%
    OnmsMonitoredService service = (OnmsMonitoredService)request.getAttribute("service");
    Collection<OnmsOutage> outages = NetworkElementFactory.getInstance(getServletContext()).currentOutagesForServiceFromPerspectivePoller(service);

    String ipAddr = service.getIpAddress().getHostAddress();
    String serviceName = service.getServiceName();

    //Collectd
    Boolean isServiceCollectionEnabled = new CollectdConfigFactory().isServiceCollectionEnabled(service);
    CollectdConfigFactory collectdConfigFactory = new CollectdConfigFactory();
    CollectdConfiguration collectdConfig =  collectdConfigFactory.getCollectdConfig();
    List<String> collectdPackageNames = new ArrayList<String>();
    Map<String,String> collectdParameters = new TreeMap<String,String>();

    if (isServiceCollectionEnabled) { // Service exists in any collection package and is enabled
        for (org.opennms.netmgt.config.collectd.Package pkg : collectdConfig.getPackages()) {
            if (pkg.serviceInPackageAndEnabled(serviceName)) {
                collectdPackageNames.add(pkg.getName()); //there should be at least one, right?
            }
            for (org.opennms.netmgt.config.collectd.Service collectdSvc : pkg.getServices()) {
                if (collectdSvc.getName().equals(serviceName)) {
		    pageContext.setAttribute("collectdInterval", collectdSvc.getInterval());
                    for (org.opennms.netmgt.config.collectd.Parameter p : collectdSvc.getParameters()) {
                        if (p.getKey().toLowerCase().contains("password")) {
                            continue; // Hide passwords for security reasons
                        } else {
                            collectdParameters.put(p.getKey(), p.getValue());
                        }
                    }
                    pageContext.setAttribute("collectdParameters", collectdParameters);
                }
            }
        }
        String collectorClassName = null;
        for (Collector collectdCollector : collectdConfig.getCollectors()) {
            if (collectdCollector.getService().equals(serviceName)) {
		pageContext.setAttribute("collectorClassName", collectdCollector.getClassName());
    	        break;
    	    }
        }
    pageContext.setAttribute("collectdPackageNames", collectdPackageNames);
    }

    // Pollerd
    PollerConfigFactory.init();
    PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
    pollerCfgFactory.rebuildPackageIpListMap();

    Package lastPkg = null;
    Enumeration<Package> en = pollerCfgFactory.enumeratePackage();
    while (en.hasMoreElements()) {
        Package pkg = en.nextElement();
        if (!pkg.getPerspectiveOnly() &&
            pollerCfgFactory.isServiceInPackageAndEnabled(serviceName, pkg) &&
            pollerCfgFactory.isInterfaceInPackage(ipAddr, pkg)) {
    lastPkg = pkg;
        }
    }

    if (lastPkg != null) {
        pageContext.setAttribute("packageName", lastPkg.getName());

        Package.ServiceMatch serviceMatch = lastPkg.findService(serviceName).orElse(null);
        if (serviceMatch != null) {
            pageContext.setAttribute("pollerName", serviceMatch.service.getName());
            pageContext.setAttribute("pollerPattern", serviceMatch.service.getPattern());
            pageContext.setAttribute("patternVariables", serviceMatch.patternVariables);

            Optional<ServiceMonitorLocator> monitor = pollerCfgFactory.getServiceMonitorLocator(serviceMatch.service.getName());
            pageContext.setAttribute("monitorClass", monitor.isEmpty() ? "N/A" : monitor.get().getServiceLocatorKey());

            pageContext.setAttribute("interval", serviceMatch.service.getInterval());

            Map<String,String> parameters = new TreeMap<String,String>();
            Map<String,String> xmlParams  = new TreeMap<String,String>();
            for (Parameter p : serviceMatch.service.getParameters()) {
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
            pageContext.setAttribute("parameters", parameters);
            pageContext.setAttribute("xmlParams", xmlParams);
        }
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

<script type="text/javascript" >
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
    });
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
         <li class="list-inline-item"><a href="${eventUrl}">View Events</a></li>

          <c:url var="metaDataLink" value="element/service-metadata.jsp">
              <c:param name="node" value="${service.ipInterface.node.id}"/>
              <c:param name="ipAddr" value="${service.ipInterface.ipAddress.hostAddress}"/>
              <c:param name="service" value="${service.serviceName}"/>
          </c:url>

          <li class="list-inline-item"><a href="<c:out value="${metaDataLink}"/>">Meta-Data</a></li>

       <sec:authorize url="admin/deleteService">
         <li class="list-inline-item"><a href="admin/deleteService" onClick="return doDelete()">Delete</a></li>
       </sec:authorize>



      </ul>



      <sec:authorize url="admin/deleteService">
         </form>
      </sec:authorize>

      <div class="row">
      <div class="col-md-6">
            <!-- general info box -->
            <div class="card">
            <div class="card-header">
              <span>General</span>
            </div>
            <table class="table table-sm">
              <tr>
                <c:url var="nodeLink" value="element/node.jsp">
                  <c:param name="node" value="${service.ipInterface.node.id}"/>
                </c:url>
                <th>Node</th>
                <td><a href="${fn:escapeXml(nodeLink)}"><c:out value="${service.ipInterface.node.label}"/></a></td>
              </tr>
              <tr>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="ipinterfaceid" value="${service.ipInterface.id}"/>
                </c:url>
                <th>Interface</th>
                <td><a href="${fn:escapeXml(interfaceLink)}">${service.ipInterface.ipAddress.hostAddress}</a></td>
              </tr>
            </table>
            </div>
            <!-- Polling info box -->
            <div class="card">
            <div class="card-header">
              <span>Polling</span>
            </div>
            <table class="table table-sm">
              <tr>
                <th>Polling Status</th>
                <td>${service.statusLong}</td>
              </tr>
              <tr>
                <th>Polling Package</th>
                <td>${empty packageName ? "N/A" : packageName}</td>
              </tr>
              <tr>
                <th>Poller Name</th>
                <td>${empty pollerName ? "N/A" : pollerName}</td>
              </tr>
              <tr>
                <th>Poller Pattern</th>
                <td>${empty pollerPattern ? "N/A" : fn:escapeXml(pollerPattern)}</td>
              </tr>
              <tr>
                <th>Monitor Class</th>
                <td>${empty monitorClass ? "N/A" : monitorClass}</td>
              </tr>
                <tr>
                    <th>Interval</th>
                    <c:choose>
                        <c:when test="${interval != null}"><td>${interval}</td></c:when>
                        <c:otherwise><td>Unknown</td></c:otherwise>
                    </c:choose>
                </tr>
              <c:choose>
                <%-- Hide the last good/fail timestamp rows when poll timestamp tracking is disabled. --%>
                <c:when test="${!DefaultPollContext.DISABLE_POLL_TIMESTAMP_TRACKING}">
                  <tr>
                    <th>Last Good</th>
                    <c:choose>
                        <c:when test="${service.lastGood != null}"><td><onms:datetime date="${service.lastGood}" /></td></c:when>
                        <c:otherwise><td>Unknown</td></c:otherwise>
                    </c:choose>
                  </tr>
                  <tr>
                    <th>Last Fail</th>
                    <c:choose>
                        <c:when test="${service.lastFail != null}"><td><onms:datetime date="${service.lastFail}" /></td></c:when>
                        <c:otherwise><td>Unknown</td></c:otherwise>
                    </c:choose>
                  </tr>
                </c:when>
              </c:choose>
            </table>
            </div>
            <!-- collection info box -->
            <div class="card">
            <div class="card-header">
              <span>Collection</span>
            </div>
            <table class="table table-sm">
	      <% if (isServiceCollectionEnabled) { %>
              <tr>
                <th>Collection Status</th>
                <td>Enabled</td>
              </tr>
              <c:forEach var="pkg" items="${collectdPackageNames}">
                  <tr>
                      <th>Collection Package</th>
                      <td>${pkg}</td>
                  </tr>
              </c:forEach>
             <tr>
               <th>Collection Interval</th>
               <c:choose>
               <c:when test="${collectdInterval != null}"><td>${collectdInterval}</td></c:when>
                   <c:otherwise><td>Unknown</td></c:otherwise>
               </c:choose>
             </tr>
             <tr>
               <th>Collector Class</th>
               <c:choose>
               <c:when test="${collectorClassName != null}"><td>${collectorClassName}</td></c:when>
                   <c:otherwise><td>Unknown or Missing</td></c:otherwise>
               </c:choose>
             </tr>
	      <% } else { %>
	      <tr>
                <th>Collection Status</th>
                <td>Not Enabled for Collection</td>
              </tr>
	      <% } %>
            </table>
            </div>

            <!-- Availability box -->
            <jsp:include page="/includes/serviceAvailability-box.jsp" flush="false" />

            <!-- Perspective status Box -->
            <%
              ServiceJspUtil util = new ServiceJspUtil(service, outages);
              if (!util.getAllPerspectives().isEmpty()) {
            %>
            <div class="card">
              <div class="card-header"><span>Application Perspective Monitoring</span></div>
              <table class="table table-sm severity">
                <tr>
                  <th>Perspective</th>
                  <th>Polling Status</th>
                  <th>Outage ID</th>
                </tr>
                <%
                  for(OnmsMonitoringLocation location : util.getAllPerspectives()) {
                      Optional<OnmsOutage> outage = util.getOutageForPerspective(location);
                %>
                <% if(outage.isPresent()) { %>
                <tr class="severity-Critical">
                <% } else { %>
                <tr class="severity-Cleared">
                <% } %>
                  <td class="divider"><%=location.getLocationName()%></td>
                  <td class="divider bright"><%=outage.isPresent() ? "<b>DOWN</b>" : "UP"%></td>
                  <td class="divider"><%=outage.isPresent() ? util.getOutageUrl(outage.get()) : ""%></td>
                </tr>
                <% } %>
              </table>
            </div>
            <% } %>

            <jsp:include page="/includes/serviceApplication-box.htm" flush="false" />

      </div> <!-- content-left" -->

      <div class="col-md-6">
            <!-- patterns variables box -->
            <c:if test="${patternVariables != null}">
              <div class="card">
                  <div class="card-header">
                      <span>Pattern Variables</span>
                  </div>
                  <table class="table table-sm">
                      <c:forEach var="entry" items="${patternVariables}">
                          <tr>
                              <th nowrap>${entry.key}</th>
                              <td>${entry.value}</td>
                          </tr>
                      </c:forEach>
                  </table>
              </div>
            </c:if>
            <!-- simple parameters box -->
            <c:if test="${parameters != null}">
              <div class="card">
              <div class="card-header">
                <span>Poller Service Parameters</span>
              </div>
                  <table class="table table-sm severity">
                      <tr>
                          <th colspan="2">Parameter</th>
                          <th>Value</th>
                          <th>Effective</th>
                      </tr>
                      <%
                          final Scope nodeScope = NetworkElementFactory.getInstance(getServletContext()).getScopeForNode(service.getNodeId());
                          final Scope interfaceScope = NetworkElementFactory.getInstance(getServletContext()).getScopeForInterface(service.getNodeId(), ipAddr);
                          final Scope serviceScope = NetworkElementFactory.getInstance(getServletContext()).getScopeForService(service.getNodeId(), InetAddressUtils.getInetAddress(ipAddr), serviceName);
                          final Map<String, String> patternVariables = (Map<String, String>) pageContext.getAttribute("patternVariables");
                          final Scope scope = new FallbackScope(nodeScope, interfaceScope, serviceScope, MapScope.singleContext(Scope.ScopeName.SERVICE, "pattern", patternVariables));

                          for(Map.Entry<String,String> entry : ((Map<String,String>)pageContext.getAttribute("parameters")).entrySet()) {
                              %>
                              <tr>
                                  <td colspan="2"><%=WebSecurityUtils.sanitizeString(entry.getKey())%></td>
                                  <td><%=WebSecurityUtils.sanitizeString(entry.getValue())%></td>
                                  <%
                                      final Interpolator.Result result = Interpolator.interpolate(entry.getValue(), scope);

                                      if (result.parts.size() == 1) {
                                          %>
                                            <td><%=WebSecurityUtils.sanitizeString(result.output)%> <span data-toggle="tooltip" data-placement="left" title="match='<%=WebSecurityUtils.sanitizeString(result.parts.get(0).match)%>', scope='<%=WebSecurityUtils.sanitizeString(result.parts.get(0).value.scopeName.toString())%>'">&#9432;</span></td>
                                          <%
                                      } else {
                                          %>
                                          <td><%=WebSecurityUtils.sanitizeString(result.output)%>
                                          <%
                                      }
                                      if (result.parts.size() > 1) {
                                          int counter = 1;
                                          %>
                                          </td>
                                          <%
                                          for(Interpolator.ResultPart part : result.parts) {
                                            %>
                                            </tr>
                                                <tr class="CellStatus">
                                                    <td class="severity-Cleared nobright spacer"></td>
                                                    <td><%=WebSecurityUtils.sanitizeString(entry.getKey())%> #<%=counter++%></td>
                                                    <td><%=WebSecurityUtils.sanitizeString(part.input)%></td>
                                                    <td><%=WebSecurityUtils.sanitizeString(part.value.value)%> <span data-toggle="tooltip" data-placement="left" title="match='<%=WebSecurityUtils.sanitizeString(part.match)%>', scope='<%=WebSecurityUtils.sanitizeString(part.value.scopeName.toString())%>'">&#9432;</span></td>
                                            <%
                                          }
                                      }
                              %>
                              </tr>
                              <%
                          }
                      %>
                  </table>
              </div>
            </c:if>
	    <!-- Collectd service parameters -->
            <c:if test="${collectdParameters != null}">
              <div class="card">
              <div class="card-header">
                <span>Collectd Service Parameters</span>
              </div>
                  <table class="table table-sm severity">
                      <tr>
                          <th colspan="2">Parameter</th>
                          <th>Value</th>
                          <th>Effective</th>
                      </tr>
                      <%
                          final Scope nodeScope = NetworkElementFactory.getInstance(getServletContext()).getScopeForNode(service.getNodeId());
                          final Scope interfaceScope = NetworkElementFactory.getInstance(getServletContext()).getScopeForInterface(service.getNodeId(), ipAddr);
                          final Scope serviceScope = NetworkElementFactory.getInstance(getServletContext()).getScopeForService(service.getNodeId(), InetAddressUtils.getInetAddress(ipAddr), serviceName);
                          final Scope scope = new FallbackScope(nodeScope, interfaceScope, serviceScope);

                          for(Map.Entry<String,String> entry : ((Map<String,String>)pageContext.getAttribute("collectdParameters")).entrySet()) {
                              %>
                              <tr>
                                  <td colspan="2"><%=WebSecurityUtils.sanitizeString(entry.getKey())%></td>
                                  <td><%=WebSecurityUtils.sanitizeString(entry.getValue())%></td>
                                  <%
                                      final Interpolator.Result result = Interpolator.interpolate(entry.getValue(), scope);

                                      if (result.parts.size() == 1) {
                                          %>
                                            <td><%=WebSecurityUtils.sanitizeString(result.output)%> <span data-toggle="tooltip" data-placement="left" title="match='<%=WebSecurityUtils.sanitizeString(result.parts.get(0).match)%>', scope='<%=WebSecurityUtils.sanitizeString(result.parts.get(0).value.scopeName.toString())%>'">&#9432;</span></td>
                                          <%
                                      } else {
                                          %>
                                          <td><%=WebSecurityUtils.sanitizeString(result.output)%>
                                          <%
                                      }
                                      if (result.parts.size() > 1) {
                                          int counter = 1;
                                          %>
                                          </td>
                                          <%
                                          for(Interpolator.ResultPart part : result.parts) {
                                            %>
                                            </tr>
                                                <tr class="CellStatus">
                                                    <td class="severity-Cleared nobright spacer"></td>
                                                    <td><%=WebSecurityUtils.sanitizeString(entry.getKey())%> #<%=counter++%></td>
                                                    <td><%=WebSecurityUtils.sanitizeString(part.input)%></td>
                                                    <td><%=WebSecurityUtils.sanitizeString(part.value.value)%> <span data-toggle="tooltip" data-placement="left" title="match='<%=WebSecurityUtils.sanitizeString(part.match)%>', scope='<%=WebSecurityUtils.sanitizeString(part.value.scopeName.toString())%>'">&#9432;</span></td>
                                            <%
                                          }
                                      }
                              %>
                              </tr>
                              <%
                          }
                      %>
                  </table>
              </div>
            </c:if>
            <!-- XML parameters box -->
            <c:if test="${xmlParams != null}">
              <c:forEach var="entry" items="${xmlParams}">
                <div class="card">
                  <div class="card-header">
                    <span>${entry.key}</span>
                  </div>
                  <div class="card-body" style="overflow-x:scroll;">${entry.value}</div>
                </div>
              </c:forEach>
            </c:if>

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
