<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<div class="modal" id="trafficUtilizationForecastModal" tabindex="-1" role="dialog" aria-labelledby="Traffic Forecast Report Options" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">Traffic Utilization Forecast</h4>
            </div>
            <div class="modal-body">
                <div class="tuf-deps-failed">
                    <p>One or more dependencies required for forecasting were not found or configured incorrectly.</p>
                    <p>Please run the following command for details:</p>
                    <pre>$OPENNMS_HOME/bin/check-forecasting-dependencies</pre>
                </div>
                <form class="form-horizontal tuf-deps-ok">
                    <label for="tuf-template">Choose a template:</label>
                    <select class="form-control" id="tuf-template">
                        <option value="1day" selected>1 day forecast</option>
                        <option value="7day">7 day forecast</option>
                        <option value="31day">31 day forecast</option>
                        <option value="">Custom</option>
                    </select>
                    <span class="help-block">Choose from one of the available forecasting templates, or configure your own options.</span>

                    <div role="tabpanel" id="tuf-options">
                        <!-- Nav tabs -->
                        <ul class="nav nav-tabs" role="tablist">
                            <li role="presentation" class="active"><a href="#time" role="tab" data-toggle="tab">Time Span</a></li>
                            <li role="presentation"><a href="#stat" role="tab" data-toggle="tab">Trend and Forecast</a></li>
                            <li role="presentation"><a href="#network" role="tab" data-toggle="tab">Network Interface</a></li>
                        </ul>

                        <p> </p>

                        <!-- Tab panes -->
                        <div class="tab-content">
                            <div role="tabpanel" class="tab-pane active" id="time">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Training Start</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-training-start" value="31">
                                        <span class="help-block">Samples from this number of days ago will be used to train the model, but won't be shown on the graph.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Graph Start</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-graph-start" value="7">
                                        <span class="help-block">Samples from this number of days ago will be shown on the graph.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Forecasts</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-forecasts" value="1">
                                        <span class="help-block">Number of seasons to forecast.</span>
                                    </div>
                                </div>
                            </div>
                            <div role="tabpanel" class="tab-pane" id="stat">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Season</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-season" value="1">
                                        <span class="help-block">Seasonality in days of the sample data. The training set must contain at least two seasons worth of data.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Trend Order</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-trend-order" value="2">
                                        <span class="help-block">Order of the polynomial used to estimate the trend. Set to this 1 for a line or higher for a curve.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Confidence Level</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-confidence-level" value="0.95">
                                        <span class="help-block">Level used to calculate the upper and lower confidence bounds.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Outlier Threshold</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-outlier-threshold" value="0.975">
                                        <span class="help-block">Percentile used to eliminate outliers. Outliers and missing values are automatically interpolated.</span>
                                    </div>
                                </div>
                            </div>
                            <div role="tabpanel" class="tab-pane" id="network">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Downstream Bandwidth</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-downstream-bandwidth" value="0">
                                        <span class="help-block">Total available downstream bandwidth in bytes (overwrites the value set on the device when set)</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label">Upstream Bandwidth</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tuf-upstream-bandwidth" value="0">
                                        <span class="help-block">Total available upstream bandwidth in bytes (overwrites the value set on the device when set)</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <input type="hidden" id="tuf-node-id">
                    <input type="hidden" id="tuf-resource-id">
                </form>
                <form id="db-report-parameters" class="hide" action="/opennms/report/database/onlineReport.htm?reportId=local_interfaceUtilizationForecast" target="_blank" method="post"></form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button type="button" id="tuf-schedule" class="btn btn-primary tuf-deps-ok">Schedule</button>
                <button type="button" id="tuf-render" class="btn btn-success tuf-deps-ok">Render</button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function checkDependencies() {
        var depsSatisfied = true;
        $('.tuf-deps-ok').toggleClass('hide', !depsSatisfied);
        $('.tuf-deps-failed').toggleClass('hide', depsSatisfied);
    }

    function hideOrShowOptions() {
        if ($('#tuf-template').val() !== "") {
            $('#tuf-options').addClass('hide');
        } else {
            $('#tuf-options').removeClass('hide');
        }
    }

    function resetValues() {
        $('#tuf-training-start').val(14);
        $('#tuf-graph-start').val(7);
        $('#tuf-forecasts').val(1);
        $('#tuf-season').val("1.0");
        $('#tuf-trend-order').val(1);
        $('#tuf-confidence-level').val(0.95);
        $('#tuf-outlier-threshold').val(0.975);
        $('#tuf-downstream-bandwidth').val(0);
        $('#tuf-upstream-bandwidth').val(0);
    }

    function formatDate(daysAgo) {
        var date = new Date();
        date.setDate(date.getDate() - daysAgo);
        return date.getFullYear() + "-" + date.getMonth() + 1 + date.getDate();
    }

    function generateReportParametersForm() {
        var id2parm = {
            //'tuf-training-start': 'dateParms[0].value',
            //'tuf-graph-start': 'dateParms[1].value',
            'tuf-forecasts': 'intParms[0].value',
            'tuf-season': 'intParms[0].value',
            'tuf-trend-order': 'intParms[0].value',
            'tuf-confidence-level': 'doubleParms[0].value',
            'tuf-outlier-threshold': 'doubleParms[1].value',
            'tuf-downstream-bandwidth': 'intParms[1].value',
            'tuf-upstream-bandwidth': 'intParms[1].value'
        };

        var form = $('#db-report-parameters');
        form.empty();
        for (var id in id2parm) {
            if (id2parm.hasOwnProperty(id)) {
                form.append('<input type="hidden" name="' + id2parm[id] + '" value="' + $('#' + id).val() + '">');
            }
        }
        form.append('<input type="hidden" name="format" value="PDF">');
    }

    $('#trafficUtilizationForecastModal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget); // Button that triggered the modal
        var parent = button.parent(); // Parent div for the menu bar

        // Extract info from data-* attributes
        var resourceId = parent.data('resource-id');

        // Parse the node id and interface id from the resource id
        var regexp = /node\[(\d+)]\.interfaceSnmp\[(.+)]$/;
        var match = regexp.exec(resourceId);
        if (match === null) {
            console.log("Unsupported resourceId '" + resourceId + "'");
            return false;
        }
        var nodeId = match[1];
        var ifResourceId = match[2];

        // Update the modal with the extracted values
        var modal = $(this);
        modal.find('#tuf-node-id').val(nodeId);
        modal.find('#tuf-resource-id').val(ifResourceId);

        resetValues();
        hideOrShowOptions();
        checkDependencies();
    });

    $('#tuf-template').change(function() {
        resetValues();
        hideOrShowOptions();
    });

    $('#tuf-schedule').click(function() {
        generateReportParametersForm();
        window.open('/opennms/report/database/onlineReport.htm?reportId=local_interfaceUtilizationForecast&' + $('#db-report-parameters').serialize(), '_blank');
    });

    $('#tuf-render').click(function() {
        generateReportParametersForm();
        $('#db-report-parameters').submit();
    });

    $(document).ready(function() {
        $(".graph-aux-controls").each(function() {
            if (! /mib2\.traffic-inout$/.test($(this).data("graph-name"))) {
                return;
            }
            var html = '<button type="button" class="btn btn-default btn-xs" title="Traffic Utilization Forecast" data-toggle="modal" data-target="#trafficUtilizationForecastModal"><span class="glyphicon glyphicon-stats"></span</button>';
            $(this).append(html);
        });

    });
</script>
