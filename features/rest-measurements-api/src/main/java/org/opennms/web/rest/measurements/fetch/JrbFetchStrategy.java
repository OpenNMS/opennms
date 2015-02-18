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

package org.opennms.web.rest.measurements.fetch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.jrobin.core.RrdException;
import org.jrobin.data.DataProcessor;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.rest.measurements.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Used to fetch measurements from JRB files.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
public class JrbFetchStrategy implements MeasurementFetchStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(JrbFetchStrategy.class);

    private final ResourceDao m_resourceDao;

    public JrbFetchStrategy(final ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FetchResults fetch(long start, long end, long step, int maxrows,
            List<Source> sources) throws IOException, RrdException {

        final long startInSeconds = (long) Math.floor(start / 1000);
        final long endInSeconds = (long) Math.floor(end / 1000);
        final long stepInSeconds = (long) Math.floor(step / 1000);

        final DataProcessor dproc = new DataProcessor(startInSeconds, endInSeconds);
        if (maxrows > 0) {
            dproc.setPixelCount(maxrows);
        }
        dproc.setFetchRequestResolution(stepInSeconds);

        for (final Source source : sources) {
            final OnmsResource resource = m_resourceDao.getResourceById(source
                    .getResourceId());
            if (resource == null) {
                LOG.error("No resource with id: {}", source.getResourceId());
                return null;
            }

            final RrdGraphAttribute rrdGraphAttribute = resource
                    .getRrdGraphAttributes().get(source.getAttribute());
            if (rrdGraphAttribute == null) {
                LOG.error("No attribute with name: {}", source.getAttribute());
                return null;
            }

            final String file = System.getProperty("rrd.base.dir")
                    + File.separator + rrdGraphAttribute.getRrdRelativePath();

            dproc.addDatasource(source.getLabel(), file, source.getAttribute(),
                    source.getAggregation());
        }

        SortedMap<Long, Map<String, Double>> rows = Maps.newTreeMap();

        dproc.processData();

        long[] timestamps = dproc.getTimestamps();

        for (int i = 0; i < timestamps.length; i++) {
            final long timestampInSeconds = timestamps[i] - dproc.getStep();

            Map<String, Double> data = new HashMap<String, Double>();
            for (Source source : sources) {
                data.put(source.getLabel(),
                        dproc.getValues(source.getLabel())[i]);
            }

            rows.put(timestampInSeconds * 1000, data);
        }

        return new FetchResults(rows, dproc.getStep() * 1000);
    }
}
