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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.AggregationFunction;
import org.opennms.newts.api.query.ResultDescriptor;
import org.opennms.newts.api.query.StandardAggregationFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

public class NewtsFetchStrategy implements MeasurementFetchStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsFetchStrategy.class);

    // The heartbeat will default to this constant * the requested step
    private static final int HEARTBEAT_MULTIPLIER = 3;

    private static final int RESOLUTION_MULTIPLIER = 2;

    private static final int STEP_LOWER_BOUND_IN_MS = 120 * 1000;

    @Autowired
    private Context m_context;

    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private SampleRepository m_sampleRepository;

    @Override
    public FetchResults fetch(long start, long end, long step, int maxrows,
            List<Source> sources) throws Exception {
        // Limit the step with a lower bound in order to prevent
        // extremely large queries
        final long fetchStep = Math.max(STEP_LOWER_BOUND_IN_MS, step);

        Optional<Timestamp> startTs = Optional.of(Timestamp.fromEpochMillis(start));
        Optional<Timestamp> endTs = Optional.of(Timestamp.fromEpochMillis(end));

        // Newts only allows to to perform a query using single resource id
        // To work around this, we group the sources by resource id,
        // perform multiple queries and aggregate the results
        Map<String, List<Source>> sourcesByNewtsResourceId = Maps.newHashMap();

        for (Source source : sources) {
            // Grab the resource
            final OnmsResource resource = m_resourceDao.getResourceById(source
                    .getResourceId());
            if (resource == null) {
                LOG.error("No resource with id: {}", source.getResourceId());
                return null;
            }

            // Grab the attribute
            final RrdGraphAttribute rrdGraphAttribute = resource
                    .getRrdGraphAttributes().get(source.getAttribute());
            if (rrdGraphAttribute == null) {
                LOG.error("No attribute with name: {}", source.getAttribute());
            }

            final String newtsResourceId = rrdGraphAttribute.getRrdRelativePath();

            List<Source> listOfSources = sourcesByNewtsResourceId.get(newtsResourceId);
            // Create the list if it doesn't exist
            if (listOfSources == null) {
                listOfSources = Lists.newArrayList();
                sourcesByNewtsResourceId.put(newtsResourceId, listOfSources);
            }

            listOfSources.add(source);
        }

        // Used to aggregate the results
        final AtomicReference<List<Long>> timestamps = new AtomicReference<>();
        final Map<String, double[]> columns = Maps.newConcurrentMap();
        final Map<String, Object> constants = Maps.newConcurrentMap();

        sourcesByNewtsResourceId.entrySet().parallelStream().forEach(entry -> {
            String newtsResourceId = entry.getKey();
            List<Source> listOfSources = entry.getValue();

            ResultDescriptor resultDescriptor = new ResultDescriptor(fetchStep);
            for (Source source : listOfSources) {
                final String metricName = source.getAttribute();
                final String name = source.getLabel();
                final AggregationFunction fn = toAggregationFunction(source.getAggregation());

                resultDescriptor.datasource(name, metricName, HEARTBEAT_MULTIPLIER*fetchStep, fn);
                resultDescriptor.export(name);
            }

            // HACK - not sure where the / is coming from right meow
            if (newtsResourceId.startsWith("/")) {
                newtsResourceId = newtsResourceId.substring(1, newtsResourceId.length());
            }

            LOG.debug("Querying Newts for resource id {} with result descriptor: {}", newtsResourceId, resultDescriptor);
            Results<Measurement> results = m_sampleRepository.select(m_context, new Resource(newtsResourceId), startTs, endTs,
                    resultDescriptor, Optional.of(Duration.millis(RESOLUTION_MULTIPLIER*fetchStep)));
            Collection<Row<Measurement>> rows = results.getRows();
            LOG.debug("Found {} rows.", rows.size());

            final int N = rows.size();

            final Map<String,Object> attributes = new HashMap<>();

            final List<Long> ts = new ArrayList<>(N);

            final Map<String,double[]> myColumns = Maps.newHashMap();

            int k = 0;
            for (Row<Measurement> row : results.getRows()) {
                for (Measurement measurement : row.getElements()) {
                    myColumns.putIfAbsent(measurement.getName(), new double[N]);
                    myColumns.get(measurement.getName())[k] = measurement.getValue();
                    if (measurement.getAttributes() != null) {
                        attributes.putAll(measurement.getAttributes());
                    }
                }

                ts.add(row.getTimestamp().asMillis());
                k += 1;
            }

            myColumns.entrySet().stream().forEach(e -> {
                columns.put(e.getKey(), e.getValue());
            });

            timestamps.compareAndSet(null, ts);
        });

        FetchResults fetchResults = new FetchResults(Longs.toArray(timestamps.get()), columns, fetchStep, constants);
        LOG.debug("Fetch results: {}", fetchResults);

        return fetchResults;
    }

    private static AggregationFunction toAggregationFunction(String fn) {
        if ("average".equalsIgnoreCase(fn) || "avg".equalsIgnoreCase(fn)) {
            return StandardAggregationFunctions.AVERAGE;
        } else if ("max".equalsIgnoreCase(fn)) {
            return StandardAggregationFunctions.MAX;
        } else if ("min".equalsIgnoreCase(fn)) {
            return StandardAggregationFunctions.MIN;
        } else {
            throw new RuntimeException("What? " + fn);
        }
    }

    @VisibleForTesting
    protected void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    @VisibleForTesting
    protected void setSampleRepository(SampleRepository sampleRepository) {
        m_sampleRepository = sampleRepository;
    }

    @VisibleForTesting
    protected void setContext(Context context) {
        m_context = context;
    }
}
