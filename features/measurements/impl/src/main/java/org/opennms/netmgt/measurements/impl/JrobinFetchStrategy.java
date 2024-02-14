/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.measurements.impl;

import java.io.IOException;
import java.util.Map;

import org.jrobin.core.RrdException;
import org.jrobin.data.DataProcessor;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.QueryMetadata;
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
            Map<Source, String> rrdsBySource, Map<String, Object> constants, QueryMetadata metadata) throws RrdException {

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

            /* Limit datasource names to 19 chars per RRD spec - JRB-32 */
            if(source.getEffectiveDataSource().length() > 19) {
                source.setDataSource(source.getEffectiveDataSource().substring(0, 19));
            }

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

        return new FetchResults(timestamps, columns, dproc.getStep() * 1000, constants, metadata);
    }
}
