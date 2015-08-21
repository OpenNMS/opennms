<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.opennms.web.api.Util" %>
<%
final String baseHref = Util.calculateUrlBase(request);
%>
window.ONMS_BASE_HREF='<%=baseHref%>';
