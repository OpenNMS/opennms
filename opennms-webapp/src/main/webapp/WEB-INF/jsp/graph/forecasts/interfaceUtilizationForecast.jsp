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

function parseResourceId(resourceId) {
    // Parse the node id and interface id from the resource id
    var regexp = /node\[(\d+)]\.interfaceSnmp\[(.+)]$/;
    var match = regexp.exec(resourceId);
    if (match === null) {
        console.log("Unsupported resourceId '" + resourceId + "'");
        return null;
    }
    return {'nodeId': match[1], 'resourceId': match[2]};
}

function generateReportParametersForm() {
    var trainingStart = parseInt($('#${param.prefix}-training-start').val());
    var graphStart = parseInt($('#${param.prefix}-graph-start').val());
    var season = parseInt($('#${param.prefix}-season').val());
    var seasonInSeconds = season * 86400;
    var numForecasts = parseInt($('#${param.prefix}-forecasts').val());

    if (graphStart > trainingStart) {
        alert('The training start must be on or before the graph start.');
        return false;
    } else if (season > (2*trainingStart)) {
        alert('The training period must include at least two seasons.');
        return false;
    }

    var dbReportParms = {
        'stringParms[0].value' : $('#${param.prefix}-resource-id').val(),
        'intParms[0].value' : seasonInSeconds,
        'intParms[1].value' : numForecasts,
        'intParms[2].value' : $('#${param.prefix}-trend-order').val(),
        'intParms[3].value' : numForecasts * seasonInSeconds,
        'intParms[4].value' : $('#${param.prefix}-node-id').val(),
        'doubleParms[0].value' : $('#${param.prefix}-confidence-level').val(),
        'doubleParms[1].value' : $('#${param.prefix}-outlier-threshold').val(),
        'doubleParms[2].value' : $('#${param.prefix}-downstream-bandwidth').val(),
        'doubleParms[3].value' : $('#${param.prefix}-upstream-bandwidth').val(),
        'dateParms[0].date' : formatDate(trainingStart),
        'dateParms[0].hours' : 0,
        'dateParms[0].minutes' : 0,
        'dateParms[1].date' : formatDate(graphStart),
        'dateParms[1].hours' : 0,
        'dateParms[1].minutes' : 0
    };

    var form = $('#${param.prefix}-db-report-parameters');
    form.empty();
    for (var id in dbReportParms) {
        if (dbReportParms.hasOwnProperty(id)) {
            form.append('<input type="hidden" name="' + id + '" value="' + dbReportParms[id] + '">');
        }
    }
    form.append('<input type="hidden" name="format" value="PDF">');

    return true;
}
