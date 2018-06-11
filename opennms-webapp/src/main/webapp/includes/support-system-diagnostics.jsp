<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">System Diagnostics</h3>
    </div>
    <div class="panel-body">
        <table class="table">
            <tr>
                <td style="border-top: none;"><a href="admin/support/systemReport.htm" class="btn btn-default" role="button" style="width: 100%">Generate System Report</a></td>
                <td style="border-top: none;">Generate &quot;support-friendly&quot; information about your OpenNMS instance and system environment.</td>
            </tr>
            <tr>
                <td style="border-top: none;"><a href="admin/nodemanagement/instrumentationLogReader.jsp" class="btn btn-default" role="button" style="width: 100%">Collectd Statistics</a></td>
                <td style="border-top: none;">Get detailed statistics about your configured performance data collection.</td>
            </tr>
            <tr>
                <td style="border-top: none;"><a href="about/index.jsp" class="btn btn-default" role="button" style="width: 100%">About OpenNMS</a></td>
                <td style="border-top: none;">Get an overview about your running OpenNMS instance such as Java Version, Operating System, PostgreSQL version and Time Series Strategy.</td>
            </tr>
        </table>
    </div>
</div>
