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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.measurements.utils.Utils;
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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Used to retrieve measurements from {@link org.opennms.newts.api.SampleRepository}.
 *
 * If a request to {@link #fetch} spans multiple resources, separate calls to
 * the {@link SampleRepository} will be performed in parallel.
 *
 * @author jwhite
 */
public class NewtsFetchStrategy implements MeasurementFetchStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsFetchStrategy.class);

    public static final long MIN_STEP_MS = Long.getLong("org.opennms.newts.query.minimum_step", 30*1000);

    public static final int INTERVAL_DIVIDER = Integer.getInteger("org.opennms.newts.query.interval_divider", 2);

    public static final long DEFAULT_HEARTBEAT_MS = Long.getLong("org.opennms.newts.query.heartbeat", 450*1000);

    @Autowired
    private Context m_context;

    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private SampleRepository m_sampleRepository;

    @Override
    public FetchResults fetch(long start, long end, long step, int maxrows, Long interval, Long heartbeat, List<Source> sources) {
        final LateAggregationParams lag = getLagParams(step, interval, heartbeat);
        final Optional<Timestamp> startTs = Optional.of(Timestamp.fromEpochMillis(start));
        final Optional<Timestamp> endTs = Optional.of(Timestamp.fromEpochMillis(end));
        final Map<String, Object> constants = Maps.newHashMap();

        // Group the sources by resource id to avoid calling the ResourceDao
        // multiple times for the same resource
        Map<String, List<Source>> sourcesByResourceId = sources.stream()
                .collect(Collectors.groupingBy(Source::getResourceId));

        // Lookup the resources in parallel
        Map<OnmsResource, List<Source>> sourcesByResource = sourcesByResourceId.entrySet()
                .parallelStream()
                .collect(Collectors.toMap(
                    e -> {
                        final OnmsResource resource = m_resourceDao.getResourceById(e.getKey());
                        if (resource == null) {
                            LOG.error("No resource with id: {}", e.getKey());
                            throw new IllegalArgumentException("No resource with id: " + e.getKey());
                        }
                        // The attributes are typically lazy loaded, so we trigger the load here
                        // while we're in a parallel context
                        resource.getAttributes();
                        return resource;
                    },
                    e -> e.getValue()
                    ));

        // Now group the sources by Newts Resource ID, which differs from the OpenNMS Resource ID.
        Map<String, List<Source>> sourcesByNewtsResourceId = Maps.newHashMap();
        for (Entry<OnmsResource, List<Source>> entry : sourcesByResource.entrySet()) {
            final OnmsResource resource = entry.getKey();
            for (Source source : entry.getValue()) {
                // Gather the values from strings.properties
                Utils.convertStringAttributesToConstants(source.getLabel(), resource.getStringPropertyAttributes(), constants);

                // Grab the attribute that matches the source
                RrdGraphAttribute rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());

                if (rrdGraphAttribute == null && !Strings.isNullOrEmpty(source.getFallbackAttribute())) {
                    LOG.error("No attribute with name '{}', using fallback-attribute with name '{}'", source.getAttribute(), source.getFallbackAttribute());
                    source.setAttribute(source.getFallbackAttribute());
                    source.setFallbackAttribute(null);
                    rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());
                }

                if (rrdGraphAttribute == null) {
                    LOG.error("No attribute with name: {}", source.getAttribute());
                    return null;
                }

                // The Newts Resource ID is stored in the rrdFile attribute
                String newtsResourceId = rrdGraphAttribute.getRrdRelativePath();
                // Remove the file separator prefix, added by the RrdGraphAttribute class
                if (newtsResourceId.startsWith(File.separator)) {
                    newtsResourceId = newtsResourceId.substring(File.separator.length(), newtsResourceId.length());
                }

                List<Source> listOfSources = sourcesByNewtsResourceId.get(newtsResourceId);
                // Create the list if it doesn't exist
                if (listOfSources == null) {
                    listOfSources = Lists.newLinkedList();
                    sourcesByNewtsResourceId.put(newtsResourceId, listOfSources);
                }
                listOfSources.add(source);
            }
        }

        // The Newts API only allows us to perform a query using a single (Newts) Resource ID,
        // so we perform multiple queries in parallel, and aggregate the results.
        final AtomicReference<long[]> timestamps = new AtomicReference<>();
        final Map<String, double[]> columns = Maps.newConcurrentMap();

        sourcesByNewtsResourceId.entrySet().parallelStream().forEach(entry -> {
            final String newtsResourceId = entry.getKey();
            final List<Source> listOfSources = entry.getValue();

            ResultDescriptor resultDescriptor = new ResultDescriptor(lag.getInterval());
            for (Source source : listOfSources) {
                final String metricName = source.getAttribute();
                final String name = source.getLabel();
                final AggregationFunction fn = toAggregationFunction(source.getAggregation());

                resultDescriptor.datasource(name, metricName, lag.getHeartbeat(), fn);
                resultDescriptor.export(name);
            }

            LOG.debug("Querying Newts for resource id {} with result descriptor: {}", newtsResourceId, resultDescriptor);
            Results<Measurement> results = m_sampleRepository.select(m_context, new Resource(newtsResourceId), startTs, endTs,
                    resultDescriptor, Optional.of(Duration.millis(lag.getStep())));
            Collection<Row<Measurement>> rows = results.getRows();
            LOG.debug("Found {} rows.", rows.size());

            final int N = rows.size();
            final Map<String, double[]> myColumns = Maps.newHashMap();

            timestamps.updateAndGet(existing -> {
                if (existing == null) {
                    // this is the first thread that has returned, build the array of timestamps
                    // the timestamps should bet the same against all result sets
                    final long[] tses = new long[rows.size()];
                    int k=0;
                    for (final Row<Measurement> row : results.getRows()) {
                        tses[k] = row.getTimestamp().asMillis();
                        k++;
                    }
                    return tses;
                }
                return existing;
            });

            int k = 0;
            for (Row<Measurement> row : results.getRows()) {
                for (Measurement measurement : row.getElements()) {
                    double[] column = myColumns.get(measurement.getName());
                    if (column == null) {
                        column = new double[N];
                        myColumns.put(measurement.getName(), column);
                    }
                    column[k] = measurement.getValue();
                }
                k += 1;
            }

            columns.putAll(myColumns);
        });

        FetchResults fetchResults = new FetchResults(timestamps.get(), columns, lag.getStep(), constants);
        LOG.trace("Fetch results: {}", fetchResults);
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
            throw new IllegalArgumentException("Unsupported aggregation function: " + fn);
        }
    }

    @VisibleForTesting
    protected static class LateAggregationParams {
        final long step;
        final long interval;
        final long heartbeat;

        public LateAggregationParams(long step, long interval, long heartbeat) {
            this.step = step;
            this.interval = interval;
            this.heartbeat = heartbeat;
        }

        public long getStep() {
            return step;
        }

        public long getInterval() {
            return interval;
        }

        public long getHeartbeat() {
            return heartbeat;
        }
    }

    /**
     * Calculates the parameters to use for late aggregation.
     *
     * Since we're in the process of transitioning from an RRD-world, most queries won't
     * contain a specified interval or heartbeat. For this reason, we need to derive sensible
     * values that will allow users to visualize the data on the graphs without too many NaNs.
     *
     * The given step size will be variable based on the time range and the pixel width of the
     * graph, so we need to derive the interval and heartbeat accordingly.
     *
     * Let S = step, I = interval and H = heartbeat, the constraints are as follows:
     *    0 < S
     *    0 < I
     *    0 < H
     *    S = aI      for some integer a >= 2
     *    H = bI      for some integer b >= 2
     *
     * While achieving these constraints, we also want to optimize for:
     *    min(|S - S*|)
     * where S* is the user supplied step and S is the effective step.
     *
     */
    @VisibleForTesting
    protected static LateAggregationParams getLagParams(long step, Long interval, Long heartbeat) {

        // Limit the step with a lower bound in order to prevent extremely large queries
        long effectiveStep = Math.max(MIN_STEP_MS, step);
        if (effectiveStep != step) {
            LOG.warn("Requested step size {} is too small. Using {}.", step, effectiveStep);
        }

        // If the interval is specified, and already a multiple of the step then use it as is
        long effectiveInterval = 0;
        if (interval != null && interval < effectiveStep && (effectiveStep % interval) == 0) {
            effectiveInterval = interval;
        } else {
            // Otherwise, make sure the step is evenly divisible by the INTERVAL_DIVIDER
            if (effectiveStep % INTERVAL_DIVIDER != 0) {
                effectiveStep += effectiveStep % INTERVAL_DIVIDER;
            }
            effectiveInterval = effectiveStep / INTERVAL_DIVIDER;
        }

        // Use the given heartbeat if specified, fall back to the default
        long effectiveHeartbeat = heartbeat != null ? heartbeat : DEFAULT_HEARTBEAT_MS;
        if (effectiveInterval < effectiveHeartbeat) {
            if (effectiveHeartbeat % effectiveInterval != 0) {
                effectiveHeartbeat += effectiveHeartbeat % effectiveInterval;
            } else {
                // Existing heartbeat is valid
            }
        } else {
            effectiveHeartbeat = effectiveInterval + 1;
            effectiveHeartbeat += effectiveHeartbeat % effectiveInterval;
        }

        return new LateAggregationParams(effectiveStep, effectiveInterval, effectiveHeartbeat);
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
