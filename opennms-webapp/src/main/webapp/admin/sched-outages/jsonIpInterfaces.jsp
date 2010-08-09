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
 * This class will be serialized into JSON format. It needs to correspond to the
 * format that the jQuery autocomplete is expecting.
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

<%-- Use this segment to test large numbers of JSON objects
<%!
private static class MyInterface extends org.opennms.web.element.Interface {
	public String ipAddress;
	public String name;
	public String getIpAddress() { return ipAddress; }
	public String getName() { return name; }
}
%>
<%
List<org.opennms.web.element.Interface> interfaces = new ArrayList<org.opennms.web.element.Interface>();
for (int i = 0; i < 50000; i++) {
	MyInterface intf = new MyInterface();
	intf.name = ("really_super_long_hostname_that_is_longer_than_normal_" + i);
	intf.ipAddress = ("192.168." + Integer.valueOf(i / 256) + "." + (i % 256));
	interfaces.add(intf);
}
%>
--%>

[
<% 
boolean printedFirst = false;
int recordCounter = 1;
final int recordLimit = 200;
for (org.opennms.web.element.Interface intf : interfaces) {
	String autocomplete = request.getParameter("term");
	// Check to see if the interface matches the search term
	if (
		autocomplete == null || 
		"".equals(autocomplete) || 
		intf.getName().contains(autocomplete) || 
		intf.getIpAddress().contains(autocomplete)
	) {
		String hostnameClause = (
			intf.getName() == null || 
			"".equals(intf.getName())) || 
			intf.getName().equals(intf.getIpAddress()
		) ? "" : " (" + intf.getName() + ")";

		String label = intf.getIpAddress() + hostnameClause;
		if (autocomplete != null) {
			label = label.replace(autocomplete, "<strong>" + autocomplete + "</strong>");
		}
		// If we've already printed the first item, separate the items with a comma
		if (printedFirst) {
			out.println(",");
		}
		out.println(JSONSerializer.toJSON(new InterfaceRecord(label, intf.getIpAddress())));
		printedFirst = true;
		// Don't print more than a certain number of records to limit the
		// performance impact in the web browser
		if (recordCounter++ >= recordLimit) {
			break;
		}
	}
}
%>
]
