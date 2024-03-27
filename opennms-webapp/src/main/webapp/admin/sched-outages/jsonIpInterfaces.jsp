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
<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*,
        org.opennms.web.element.*,
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
	private String m_value;

	public AutocompleteRecord(String label, String value) {
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
List<org.opennms.web.element.Interface> items = new ArrayList<>();
for (int i = 0; i < 50000; i++) {
	MyInterface item = new MyInterface();
	item.name = ("really_super_long_hostname_that_is_longer_than_normal_" + i);
	item.ipAddress = ("192.168." + Integer.valueOf(i / 256) + "." + (i % 256));
	items.add(item);
}
%>
--%>

[
<% 
boolean printedFirst = false;
int recordCounter = 1;
final int recordLimit = 200;

String autocomplete = request.getParameter("term");
List<org.opennms.web.element.Interface> items;
if(autocomplete == null || autocomplete.equals("")) {
    items = Arrays.asList(NetworkElementFactory.getInstance(getServletContext()).getAllManagedIpInterfaces(false));
} else{
    items = Arrays.asList(NetworkElementFactory.getInstance(getServletContext()).getAllManagedIpInterfacesLike(autocomplete));
}

for (org.opennms.web.element.Interface item : items) {

	// Check to see if the interface matches the search term

    String hostnameClause = (
        item.getName() == null ||
        "".equals(item.getName())) ||
        item.getName().equals(item.getIpAddress()
    ) ? "" : " (" + item.getName() + ")";

    String label = item.getIpAddress() + hostnameClause;

    // If we've already printed the first item, separate the items with a comma
    if (printedFirst) {
        out.println(",");
    }
    out.println(JSONSerializer.toJSON(new AutocompleteRecord(label, item.getIpAddress())));
    printedFirst = true;
    // Don't print more than a certain number of records to limit the
    // performance impact in the web browser
    if (recordCounter++ >= recordLimit) {
        break;
    }

}
%>
]
