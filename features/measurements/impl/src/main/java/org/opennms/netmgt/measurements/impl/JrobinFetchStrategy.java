/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.impl;

import java.io.IOException;
import java.util.Map;

import org.jrobin.core.RrdException;
import org.jrobin.data.DataProcessor;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Source;

import com.google.common.collect.Maps;

/**
 * Used to fetch measurements from JRB files.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
public class JrobinFetchStrategy extends AbstractRrdBasedFetchStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    protected FetchResults fetchMeasurements(long start, long end, long step, int maxrows,
            Map<Source, String> rrdsBySource, Map<String, Object> constants) throws RrdException {

        final long startInSeconds = (long) Math.floor(start / 1000d);
        final long endInSeconds = (long) Math.floor(end / 1000d);

        long stepInSeconds = (long) Math.floor(step / 1000d);
        // The step must be strictly positive
        if (stepInSeconds <= 0) {
            stepInSeconds = 1;
        }

        final DataProcessor dproc = new DataProcessor(startInSeconds, endInSeconds);
        if (maxrows > 0) {
            dproc.setPixelCount(maxrows);
        }
        dproc.setFetchRequestResolution(stepInSeconds);

        for (final Map.Entry<Source, String> entry : rrdsBySource.entrySet()) {
            final Source source = entry.getKey();
            final String rrdFile = entry.getValue();
            dproc.addDatasource(source.getLabel(), rrdFile, source.getEffectiveDataSource(),
                    source.getAggregation());
        }

        try {
            dproc.processData();
        } catch (IOException e) {
            throw new RrdException("JRB processing failed.", e);
        }

        final long[] timestamps = dproc.getTimestamps();

        // Convert the timestamps to milliseconds
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] *= 1000;
        }

        final Map<String, double[]> columns = Maps.newHashMapWithExpectedSize(rrdsBySource.keySet().size());
        for (Source source : rrdsBySource.keySet()) {
            columns.put(source.getLabel(), dproc.getValues(source.getLabel()));
        }

        return new FetchResults(timestamps, columns, dproc.getStep() * 1000, constants);
    }
}
