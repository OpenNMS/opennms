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
    import="
            org.opennms.web.servlet.MissingParameterException,
            org.opennms.web.api.Util
    "%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
final String baseHref = Util.calculateUrlBase(request);
final String report = request.getParameter("report");
final String resourceId = request.getParameter("resourceId");
String[] requiredParameters = new String[] {"report", "resourceId"};

if (report == null) {
    throw new MissingParameterException("report", requiredParameters);
} else if (resourceId == null) {
    throw new MissingParameterException("resourceId", requiredParameters);
}

pageContext.setAttribute("report", report);
pageContext.setAttribute("resourceId", resourceId);
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .flags("nobreadcrumbs", "quiet", "usebackshift")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row-fluid">
    <div class="col-md-12">
        <div class="card">
      <div class="card-header text-center">
        <span>NRTG Graph for <c:out value="${report}"/> on <c:out value="${resourceId}"/> </span>
      </div> <!-- card-header -->
            <div class="card-body text-center">
        <div class="graph-container" data-graph-report="<c:out value="${report}"/>" data-graph-resource="<c:out value="${resourceId}"/>"></div>
            </div>
            <div class="card-footer">
             <form>
              <div class="form-row align-items-center">
               <div class="col-auto my-1">
                <label class="mr-sm-2" for="nrtgInterval">Preference</label>
                <select class="custom-select mr-sm-2" id="nrtgInterval">
                    <option value="250">0.25s</option>
                    <option value="1000">1.00s</option>
                    <option value="5000">5.00s</option>
                    <option value="10000">10.00s</option>
                    <option value="30000">30.00s</option>
                    <option value="60000">60.00s</option>
                </select>
               </div>
               <div class="col-auto my-1">
                   <div class="custom-control custom-checkbox mr-sm-2">
                        <input class="form-check-input" type="checkbox" value="" id="nrtgPause">
                            <label class="form-check-label" for="nrtgPause">Pause</label>
                          </div>
                      </div>
                      <div class="col-auto my-1">
                          <div class="custom-control custom-checkbox mr-sm-2">
                              <input class="form-check-input" type="checkbox" value="" id="nrtgCompress">
                              <label class="form-check-label" for="nrtgCompress">Compress</label>
                          </div>
                      </div>
                  </div>
              </form>
          </div> <!-- card-footer -->
      </div> <!-- panel -->
    </div> <!-- col-md-12 -->
</div> <!-- row -->

<script type="text/javascript">
    var defaultPollingInterval = 10000,
            defaultSlidingWindow = 30;

    function getBaseHref() {
        return "<%= baseHref %>";
    }

    $(document).ready(function () {
        var first = true;
        jQuery("div[data-graph-report]").each(function () {
            var report = jQuery(this).data('graph-report');
            var resource = jQuery(this).data('graph-resource');

            var url = getBaseHref() + 'rest/graphs/' + encodeURIComponent(report);

            // Pull in the graph definition
            jQuery.ajax({
                url: url,
                dataType: 'json',
                context: jQuery(this)
            }).done(function (graphDef) {
                // Convert the graph definition to a supported model
                var rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
                    graphDef: graphDef,
                    resourceId: resource
                });
                var graphModel = rrdGraphConverter.model;

                // Build the data-source
                var ds = new Backshift.DataSource.NRTG({
                    url: getBaseHref() + "nrt/starter",
                    pollingInterval: defaultPollingInterval,
                    metrics: [
                        {
                            resourceId: resource,
                            report: report
                        }
                    ]
                });

                // Build and render the graph
                var graph = new Backshift.Graph.Flot({
                    width: $(window).width() * 2/3,
                    height: $(window).width() * 1/3,
                    element: $(this)[0],
                    dataSource: ds,
                    model: graphModel,
                    title: graphModel.title,
                    verticalLabel: graphModel.verticalLabel
                });
                graph.render();

                // Wire in the controls
                var nrtgInterval = jQuery('#nrtgInterval');
                nrtgInterval.val(defaultPollingInterval);
                nrtgInterval.bind('change', function() {
                    console.log("Updating NRTG polling interval to " + nrtgInterval.val() + "ms");
                    ds.updatePollingInterval(nrtgInterval.val());
                });

                var nrtgPause = jQuery('#nrtgPause');
                nrtgPause.bind('change', function() {
                    var isPaused = $(this).is(':checked');

                    if (isPaused) {
                        console.log("Pausing NRTG stream.");
                        ds.stopStreaming();
                    } else {
                        console.log("Resuming NRTG stream.");
                        ds.startStreaming();
                    }
                });

                var nrtgCompress = jQuery('#nrtgCompress');
                nrtgCompress.bind('change', function() {
                    var isCompressed = $(this).is(':checked');

                    if (isCompressed) {
                        console.log("Enabling NRTG graph compression.");
                        ds.updateSlidingWindow(0);
                    } else {
                        console.log("Disabling NRTG graph compression.");
                        ds.updateSlidingWindow(defaultSlidingWindow);
                    }
                });
            });
        });
    });
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false">
  <jsp:param name="quiet" value="true" />
</jsp:include>
