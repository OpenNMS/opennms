<%@page language="java" contentType="text/html"
	import="java.io.File,
		java.util.LinkedList,org.opennms.core.resource.Vault,org.opennms.web.acegisecurity.Authentication,org.opennms.web.navigate.NavBarEntry"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%
                if (request.getAttribute("navBar") == null) {
                String mapEnableLocation = Vault.getHomeDir()
                        + File.separator + "etc" + File.separator
                        + "map.enable";
                File mapEnableFile = new File(mapEnableLocation);

                String vulnEnableLocation = Vault.getHomeDir()
                        + File.separator + "etc" + File.separator
                        + "vulnerabilities.enable";
                File vulnEnableFile = new File(vulnEnableLocation);

                LinkedList navBar = new LinkedList();
                navBar.add(new NavBarEntry("nodelist",
                                           "element/nodelist.jsp",
                                           "Node List"));
                navBar.add(new NavBarEntry("element", "element/index.jsp",
                                           "Search"));
                navBar.add(new NavBarEntry("outages", "outage/index.jsp",
                                           "Outages"));
                navBar.add(new NavBarEntry("pathOutage",
                                           "pathOutage/index.jsp",
                                           "Path Outages"));
                navBar.add(new NavBarEntry("event", "event/index.jsp",
                                           "Events"));
                navBar.add(new NavBarEntry("alarm", "alarm/index.jsp",
                                           "Alarms"));
                navBar.add(new NavBarEntry("notification",
                                           "notification/index.jsp",
                                           "Notification"));
                navBar.add(new NavBarEntry("asset", "asset/index.jsp",
                                           "Assets"));
                //navBar.add(new NavBarEntry("security", "security.jsp", "Security"));
                navBar.add(new NavBarEntry("report", "report/index.jsp",
                                           "Reports"));
                navBar.add(new NavBarEntry("chart", "charts/index.jsp",
                                           "Charts"));
                if (mapEnableFile.exists()) {
                    navBar.add(new NavBarEntry("vulnerability",
                                               "vulnerability/index.jsp",
                                               "Vulnerabilities"));
                }
                if (mapEnableFile.exists()) {
                    navBar.add(new NavBarEntry("map", "map/index.jsp", "Map"));
                }
                if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
                    navBar.add(new NavBarEntry("admin", "admin/index.jsp",
                                               "Admin"));
                }
                navBar.add(new NavBarEntry("help", "help/index.jsp", "Help"));

                request.setAttribute("navBar", navBar);
                request.setAttribute("location",
                                     request.getParameter("location"));
            }
%>

<div class="navbar">
<ul>
	<c:forEach var="navEntry" items="${navBar}">
		<c:choose>
			<c:when test="${navEntry.name == 'Help'}">
				<li class="last">
			</c:when>
			<c:otherwise>
				<li>
			</c:otherwise>
		</c:choose>

		<c:choose>
			<c:when test="${location == navEntry.locationMatch}">
				<c:out value="${navEntry.name}" />
			</c:when>
			<c:otherwise>
				<a href="<c:out value="${navEntry.URL}"/>"><c:out
					value="${navEntry.name}" /></a>
			</c:otherwise>
		</c:choose>
		</li>
	</c:forEach>
</ul>
</div>
<!-- id="navbar" -->
