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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2Exception;
import org.opennms.netmgt.rrd.jrrd2.impl.JRrd2Jni;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JRrd2FetchStrategy extends AbstractRrdBasedFetchStrategy {

    private JRrd2 jrrd2 = new JRrd2Jni();

    @Override
    protected FetchResults fetchMeasurements(long start, long end, long step,
            int maxrows, Map<Source, String> rrdsBySource,
            Map<String, Object> constants, QueryMetadata metadata) throws RrdException {

        final long startInSeconds = (long) Math.floor(start / 1000d);
        final long endInSeconds = (long) Math.floor(end / 1000d);

        long stepInSeconds = (long) Math.floor(step / 1000d);
        // The step must be strictly positive
        if (stepInSeconds <= 0) {
            stepInSeconds = 1;
        }

        // Use labels without spaces when executing the xport command
        // These are mapped back to the requested labels in the response
        final Map<String, String> labelMap = Maps.newHashMap();

        int k = 0;
        List<String> argv = Lists.newLinkedList();
        for (final Map.Entry<Source, String> entry : rrdsBySource.entrySet()) {
            final Source source = entry.getKey();
            final String rrdFile = entry.getValue();
            final String tempLabel = Integer.toString(++k);
            labelMap.put(tempLabel, source.getLabel());

            // Limit datasource names to 19 chars
            if (source.getEffectiveDataSource().length() > 19) {
                source.setDataSource(source.getEffectiveDataSource().substring(0, 19));
            }

            argv.add(String.format("DEF:%s=%s:%s:%s",
                    tempLabel, Utils.escapeColons(rrdFile), Utils.escapeColons(source.getEffectiveDataSource()),
                    source.getAggregation()));
            argv.add(String.format("XPORT:%s:%s", tempLabel,
            		tempLabel));
        }

        org.opennms.netmgt.rrd.jrrd2.api.FetchResults xportResults;
        try {
            xportResults = jrrd2.xport(startInSeconds, endInSeconds, stepInSeconds, maxrows, argv.toArray(new String[argv.size()]));
        } catch (JRrd2Exception e) {
            throw new RrdException("Xport failed.", e);
        }

        // Convert to ms
        final long[] timestamps = xportResults.getTimestamps();
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] *= 1000;
        }

        // Map the column labels from their temporary values to their requested values
        Map<String, double[]> valuesByTempLabel = xportResults.getColumnsWithValues();
        Map<String, double[]> valuesByLabel = Maps.newLinkedHashMap();
        for (Entry<String, double[]> entry : valuesByTempLabel.entrySet()) {
        	valuesByLabel.put(labelMap.get(entry.getKey()), entry.getValue());
        }

        return new FetchResults(timestamps, valuesByLabel, xportResults.getStep() * 1000, constants, metadata);
    }
}
