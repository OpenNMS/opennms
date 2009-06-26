<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.svclayer.ProgressMonitor
		"
%>
<%
String view = "/WEB-INF/jsp/progressBar.jsp";

ProgressMonitor monitor = new ProgressMonitor();
monitor.setPhaseCount(3);
for (int i = 0; i < 2; i++) {
    monitor.beginNextPhase("Loading nodes for Routers");
}
request.setAttribute("progress", monitor);

RequestDispatcher dispatcher =
    getServletContext().getRequestDispatcher(view);
dispatcher.forward(request, response);

%>
