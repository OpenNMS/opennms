<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="NRTG Graphing" />
  <jsp:param name="quiet" value="true" />
  <jsp:param name="nobreadcrumbs" value="true" />
  <jsp:param name="usebackshift" value="true" />
</jsp:include>

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
