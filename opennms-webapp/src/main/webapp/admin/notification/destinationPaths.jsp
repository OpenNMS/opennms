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
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.destinationPaths.*
	"
%>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Destination Paths")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications", "admin/notification/index.jsp")
          .breadcrumb("Destination Paths")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

    function editPath() 
    {
        if (document.path.paths.selectedIndex==-1)
        {
            alert("Please select a path to edit.");
        }
        else
        {
            document.path.userAction.value="edit";
            document.path.submit();
        }
    }
    
    function newPath()
    {
        document.path.userAction.value="new";
        return true;
    }
    
    function deletePath()
    {
        if (document.path.paths.selectedIndex==-1)
        {
            alert("Please select a path to delete.");
        }
        else
        {
            message = "Are you sure you want to delete the path " + document.path.paths.options[document.path.paths.selectedIndex].value + "?";
            if (confirm(message))
            {
                document.path.userAction.value="delete";
                document.path.submit();
            }
        }
    }

    function testPath() {
        if (document.path.paths.selectedIndex === -1) {
            alert("Please select a path to test.");
        } else {
            var destinationPath = encodeURIComponent(document.path.paths.options[document.path.paths.selectedIndex].value);
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function readystatechange() {
                try {
                    if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 202) {
                        console.log("successfully triggered", destinationPath);
                    }
                } catch (err) {
                    console.error("failed to trigger notifications", err);
                }
            };
            xhr.open('POST', 'rest/notifications/destination-paths/' + destinationPath + '/trigger');
            xhr.send();
        }
    }
</script>

<form method="post" name="path" action="admin/notification/destinationWizard" onsubmit="return newPath();">
    <input type="hidden" name="userAction" value=""/>
    <input type="hidden" name="sourcePage" value="destinationPaths.jsp"/>
    <div class="row">
        <div class="col-md-6">
    <div class="card">
        <div class="card-header">
            <div class="pull-left">
                <h4>Destination Paths</h4>
            </div>
                <input type="submit" class="btn btn-secondary pull-right" value="New Path"/>
        </div>
        <div class="card-body">

            <div class="mb-2">
                <select NAME="paths" class="custom-select">
                    <% Map<String, Path> pathsMap = new TreeMap<String, Path>(DestinationPathFactory.getInstance().getPaths());
                        for (String key : pathsMap.keySet()) {
                    %>
                    <option VALUE=<%=key%>><%=key%>
                    </option>
                    <% } %>
                </select>
            </div>
            <input type="button" class="btn btn-secondary" value="Edit" onclick="editPath()"/>
            <input type="button" class="btn btn-secondary" value="Delete" onclick="deletePath()"/>
            <input type="button" class="btn btn-success pull-right" value="Test" onclick="testPath()"/>
        </div>
    </div>
        </div>
    </div>
</form>
    
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
