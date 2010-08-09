<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*,
        org.opennms.netmgt.config.*,
        org.opennms.netmgt.config.common.*,
        org.opennms.netmgt.config.poller.*,
        org.opennms.web.WebSecurityUtils,
        org.opennms.web.element.*,
        org.opennms.netmgt.EventConstants,
        org.opennms.netmgt.xml.event.Event,
        org.opennms.netmgt.utils.*,
        org.opennms.web.Util,
        java.net.*,
        java.io.*,
        java.text.NumberFormat,
        java.text.SimpleDateFormat,
        net.sf.json.JSONSerializer
        "
%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%!
/**
 * This class will be serialized into JSON format.
 */
public static class InterfaceRecord {
	private String m_label;
	private String m_value;

	public InterfaceRecord(String label, String value) {
		m_label = label;
		m_value = value;
	}

	public String getLabel() {
		return m_label;
	}

	public String getValue() {
		return m_value;
	}
}
%>
<%
List<org.opennms.web.element.Interface> interfaces = Arrays.asList(NetworkElementFactory.getAllManagedIpInterfaces(false));
%>
[
<% 
boolean printedFirst = false;
for (org.opennms.web.element.Interface intf : interfaces) {
	String autocomplete = request.getParameter("term");
	// Check to see if the interface matches the search term
	if (autocomplete == null || "".equals(autocomplete) || intf.getName().contains(request.getParameter("term"))) {
		String hostnameClause = (intf.getName() == null || "".equals(intf.getName())) ? "" : " (" + intf.getName() + ")";
		String label = intf.getIpAddress() + hostnameClause;
		// If we've already printed the first item, separate the items with a comma
		if (printedFirst) {
			out.println(",");
		}
		out.println(JSONSerializer.toJSON(new InterfaceRecord(label, intf.getIpAddress())));
		printedFirst = true;
	}
}
%>
]
