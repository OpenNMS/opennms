<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true"  %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal" id="${param.prefix}-modal" tabindex="-1" role="dialog" aria-labelledby="${param.title}" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">${param.title}</h4>
            </div>
            <div class="modal-body">
                <div class="${param.prefix}-deps-failed">
                    <p>One or more dependencies required for forecasting were not found or configured incorrectly.</p>
                    <p>Make sure the <b>R</b> and <b>R-zoo</b> packages are installed. You can verify this by running:</p>
                    <pre>Rscript -e 'library(zoo)'</pre>
                </div>
                <form class="form-horizontal ${param.prefix}-deps-ok">
                    <label for="${param.prefix}-template">Choose a template:</label>
                    <select class="form-control" id="${param.prefix}-template">
                        <option value="1day" selected>1 day forecast</option>
                        <option value="7day">7 day forecast</option>
                        <option value="31day">31 day forecast</option>
                        <option value="">Custom</option>
                    </select>
                    <span class="help-block">Choose from one of the available forecasting templates, or configure your own options.</span>

                    <div role="tabpanel" id="${param.prefix}-options">
                        <!-- Nav tabs -->
                        <ul class="nav nav-tabs" role="tablist">
                            <li role="presentation" class="active"><a href="#time" role="tab" data-toggle="tab">Time Span</a></li>
                            <li role="presentation"><a href="#stat" role="tab" data-toggle="tab">Trend and Forecast</a></li>
                            <c:if test="${param.showNetworkTab != null }"><li role="presentation"><a href="#network" role="tab" data-toggle="tab">Network Interface</a></li></c:if>
                        </ul>

                        <p> </p>

                        <!-- Tab panes -->
                        <div class="tab-content">
                            <div role="tabpanel" class="tab-pane active" id="time">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Training Start</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-training-start" value="31">
                                        <span class="help-block">Samples from this number of days ago will be used to train the model, but won't be shown on the graph.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Graph Start</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-graph-start" value="7">
                                        <span class="help-block">Samples from this number of days ago will be shown on the graph.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Forecasts</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-forecasts" value="1">
                                        <span class="help-block">Number of seasons to forecast.</span>
                                    </div>
                                </div>
                            </div>
                            <div role="tabpanel" class="tab-pane" id="stat">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Season</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-season" value="1">
                                        <span class="help-block">Seasonality in days of the sample data. The training set must contain at least two seasons worth of data.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Trend Order</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-trend-order" value="2">
                                        <span class="help-block">Order of the polynomial used to estimate the trend. Set to this 1 for a line or higher for a curve.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Confidence Level</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-confidence-level" value="0.95">
                                        <span class="help-block">Level used to calculate the upper and lower confidence bounds.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Outlier Threshold</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-outlier-threshold" value="0.975">
                                        <span class="help-block">Percentile used to eliminate outliers. Outliers and missing values are automatically interpolated.</span>
                                    </div>
                                </div>
                            </div>
                            <c:if test="${param.showNetworkTab != null }">
                            <div role="tabpanel" class="tab-pane" id="network">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Downstream Bandwidth</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-downstream-bandwidth" value="0">
                                        <span class="help-block">Total available downstream bandwidth in bytes (overwrites the value set on the device when set)</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Upstream Bandwidth</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="${param.prefix}-upstream-bandwidth" value="0">
                                        <span class="help-block">Total available upstream bandwidth in bytes (overwrites the value set on the device when set)</span>
                                    </div>
                                </div>
                            </div>
                            </c:if>
                        </div>
                    </div>
                    <input type="hidden" id="${param.prefix}-node-id">
                    <input type="hidden" id="${param.prefix}-resource-id">
                </form>
                <form id="${param.prefix}-db-report-parameters" class="hide" action="/opennms/report/database/onlineReport.htm?reportId=local_${param.reportId}" target="_blank" method="post"></form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button type="button" id="${param.prefix}-advanced" class="btn btn-primary ${param.prefix}-deps-ok">Advanced</button>
                <button type="button" id="${param.prefix}-render" class="btn btn-success ${param.prefix}-deps-ok">Render</button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
(function(){
    <c:import url="${param.jsimpl}" />

    function checkDependencies() {
        var depsSatisfied = true;
        $('.${param.prefix}-deps-ok').toggleClass('hide', !depsSatisfied);
        $('.${param.prefix}-deps-failed').toggleClass('hide', depsSatisfied);
    }

    function hideOrShowOptions() {
        if ($('#${param.prefix}-template').val() !== "") {
            $('#${param.prefix}-options').addClass('hide');
        } else {
            $('#${param.prefix}-options').removeClass('hide');
        }
    }

    function resetValues() {
        var trainingStart = 14;
        var graphStart = 7;
        var forecasts = 1;
        var season = "1.0";

        var template = $('#${param.prefix}-template').val();
        if (template === "1day") {
            trainingStart = 14;
            graphStart = 7;
            forecasts = 1;
            season = "1.0";
        } else if (template === "7day") {
            trainingStart = 60;
            graphStart = 30;
            forecasts = 7;
            season = "1.0";
        } else if (template === "31day") {
            trainingStart = 365;
            graphStart = 90;
            forecasts = 4;
            season = "7.0";
        } else if (template === "") {
            // do nothing
        } else {
            console.log("Unknown template: " + template);
        }

        $('#${param.prefix}-training-start').val(trainingStart);
        $('#${param.prefix}-graph-start').val(graphStart);
        $('#${param.prefix}-forecasts').val(forecasts);
        $('#${param.prefix}-season').val(season);
        $('#${param.prefix}-trend-order').val(1);
        $('#${param.prefix}-confidence-level').val(0.95);
        $('#${param.prefix}-outlier-threshold').val(0.975);
        $('#${param.prefix}-downstream-bandwidth').val(0);
        $('#${param.prefix}-upstream-bandwidth').val(0);
    }

    function formatDate(daysAgo) {
        var oneDayInMs = 1000 * 60 * 60 * 24;
        var now = new Date().getTime();
        var then = new Date(now - daysAgo * oneDayInMs);
        return then.getFullYear() + "-" + (then.getMonth() + 1) + "-" + then.getDate();
    }

    $('#${param.prefix}-modal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget); // Button that triggered the modal
        var parent = button.parent(); // Parent div for the menu bar

        // Extract info from data-* attributes
        var resourceId = parent.data('resource-id');

        // Parse using implementation specific function
        var parsedResourceId = parseResourceId(resourceId);
        if(parsedResourceId == null) {
            return false;
        }

        // Update the modal with the extracted values
        var modal = $(this);
        modal.find('#${param.prefix}-node-id').val(parsedResourceId.nodeId);
        modal.find('#${param.prefix}-resource-id').val(parsedResourceId.resourceId);

        resetValues();
        hideOrShowOptions();
        checkDependencies();
    });

    $('#${param.prefix}-template').change(function() {
        resetValues();
        hideOrShowOptions();
    });


    $('#${param.prefix}-advanced').click(function() {
        if(generateReportParametersForm()) {
            window.open('/opennms/report/database/onlineReport.htm?reportId=local_${param.reportId}&' + $('#${param.prefix}-db-report-parameters').serialize(), '_blank');
        }
    });

    $('#${param.prefix}-render').click(function() {
        if(generateReportParametersForm()) {
            $('#${param.prefix}-db-report-parameters').submit();
        }
    });

    $(document).ready(function() {
        var it = $(".graph-aux-controls");
        if (it == null) {
            return;
        }

        var graphNames = "${param.graphNames}".split(",");

        $(".graph-aux-controls").each(function() {
            var supported = false;
            var graphName = $(this).data("graph-name");
            graphNames.forEach(function(entry) {
                if (entry === graphName) {
                    supported = true;
                }
			});

            if(!supported) {
                return;
            }

            var html = '<button type="button" class="btn btn-default btn-xs" title="${param.title}" data-toggle="modal" data-target="#${param.prefix}-modal"><span class="glyphicon glyphicon-stats"></span</button>';
            $(this).append(html);
        });
    });
})();
</script>
