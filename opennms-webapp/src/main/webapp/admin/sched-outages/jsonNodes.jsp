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
        contentType="application/json"
        session="true"
        import="java.util.*, java.util.regex.*,
        org.opennms.web.element.*,
        org.opennms.netmgt.model.OnmsNode,
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

%>
[
<% 
boolean printedFirst = false;
int recordCounter = 1;
final int recordLimit = 200;
String autocomplete = request.getParameter("term");

List<OnmsNode> items;
if(autocomplete == null || autocomplete.equals("")){
    items = NetworkElementFactory.getInstance(getServletContext()).getAllNodes();
} else{
    items = NetworkElementFactory.getInstance(getServletContext()).getNodesLike(autocomplete);
}
for (OnmsNode item : items) {
	// Check to see if the item matches the search term

		StringBuffer result = new StringBuffer();

        result.append(item.getLabel());
		result.append(" (Node ID ").append(item.getId()).append(")");
		// If we've already printed the first item, separate the items with a comma
		if (printedFirst) {
			out.println(",");
		}
		out.println(JSONSerializer.toJSON(new AutocompleteRecord(result.toString(), item.getId())));
		printedFirst = true;
		// Don't print more than a certain number of records to limit the
		// performance impact in the web browser
		if (recordCounter++ >= recordLimit) {
			break;
		}
}
%>
]
