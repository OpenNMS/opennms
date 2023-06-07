/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.timeseries.plugin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.features.timeseries.plugin.InMemoryStorage;

/**
 * Simple diagnostic for looking at timeseries data
 *
 */
@Command(scope = "opennms", name="get-tss-plugin-metrics", description = "Print all collected metrics")
@Service
public class GetMetricsCommand implements Action {

    @Reference
    private TimeSeriesStorage storage;

    @Override
    public Object execute() {
        ((InMemoryStorage)storage).getAllMetrics().entrySet().forEach(entry -> {
            System.out.println(String.format("Metric <%s> has %d data points",
                    entry.getKey().getKey(),
                    entry.getValue().size()));
            entry.getValue().stream().forEach(dataPoint ->
                System.out.printf("\t%d:%f\n", dataPoint.getTime().toEpochMilli(), dataPoint.getValue()));
        });
        return null;
    }

}
