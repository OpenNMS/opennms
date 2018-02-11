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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jrobin.core.RrdException;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Source;
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
            Map<String, Object> constants) throws RrdException {

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

        return new FetchResults(timestamps, valuesByLabel, xportResults.getStep() * 1000, constants);
    }
}
