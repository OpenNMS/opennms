/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;

/**
 * Shows statistics of the time series layer.
 * Install: feature:install opennms-timeseries-shell
 * Usage: type opennms:ts-stats in karaf console
 */
@Command(scope = "opennms", name = "ts-stats",
        description = "Prints statistics about the timeseries integration layer.")
@Service
public class TimeseriesStatsCommand implements Action {

    @Reference
    private TimeseriesStorageManager storageManager;

    @Override
    public Object execute() {
        System.out.printf("Active TimeSeriesStorage plugin: %s%n", storageManager.get().getClass().getName());
        System.out.printf("Metrics with highest number of tags:%n%s%n%n", storageManager.getStats().getTopNMetricsWithMostTags());
        System.out.printf("Tags with highest number of unique values:%n%s%n", storageManager.getStats().getTopNMetricsWithMostTags());
        return null;
    }
}
