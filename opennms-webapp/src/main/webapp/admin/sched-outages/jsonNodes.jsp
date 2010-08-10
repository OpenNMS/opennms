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
public static class AutocompleteRecord {
	private String m_label;
	private int m_value;

	public AutocompleteRecord(String label, int value) {
		m_label = label;
		m_value = value;
	}

	public String getLabel() {
		return m_label;
	}

	public int getValue() {
		return m_value;
	}
}
%>
<%
List<org.opennms.web.element.Node> items = Arrays.asList(NetworkElementFactory.getAllNodes());
%>
[
<% 
boolean printedFirst = false;
int recordCounter = 1;
final int recordLimit = 200;
for (org.opennms.web.element.Node item : items) {
	String autocomplete = request.getParameter("term");
	// Check to see if the item matches the search term
	if (
		autocomplete == null || 
		"".equals(autocomplete) || 
		item.getLabel().contains(autocomplete)
	) {
		String label = item.getLabel() + " (Node ID " + item.getNodeId() + ")";
		if (autocomplete != null && !"".equals(autocomplete)) {
			label = label.replace(autocomplete, "<strong>" + autocomplete + "</strong>");
		}
		// If we've already printed the first item, separate the items with a comma
		if (printedFirst) {
			out.println(",");
		}
		out.println(JSONSerializer.toJSON(new AutocompleteRecord(label, item.getNodeId())));
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
