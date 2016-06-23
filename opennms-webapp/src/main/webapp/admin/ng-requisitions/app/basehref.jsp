<%@ page contentType="text/javascript; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
final String baseHref = org.opennms.web.api.Util.calculateUrlBase(request);
%>
window.ONMS_BASE_HREF='<%=baseHref%>';
