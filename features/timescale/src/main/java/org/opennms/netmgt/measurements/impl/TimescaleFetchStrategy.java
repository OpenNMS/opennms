/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.timeseries.api.domain.Utils.asMap;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.QueryNode;
import org.opennms.netmgt.measurements.model.QueryResource;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.measurements.utils.Utils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Aggregation;
import org.opennms.netmgt.timeseries.integration.CommonTagNames;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.Sample;
import org.opennms.netmgt.timeseries.api.domain.TimeSeriesFetchRequest;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.SampleSelectCallback;
import org.opennms.newts.api.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Used to retrieve measurements from {@link org.opennms.newts.api.SampleRepository}.
 *
 * If a request to {@link #fetch} spans multiple resources, separate calls to
 * the {@link SampleRepository} will be performed in parallel.
 *
 * Reading the samples and computing the aggregated values can be very CPU intensive.
 * The "parallelism" attribute is used to set an upper limit on how may concurrent threads
 * can be used to perform these calculations. By default, this is set to the number of
 * cores, but can be reduced if the operator wishes to ensure cores are available
 * for other purposes.
 *
 * @author jwhite
 */
public class TimescaleFetchStrategy implements MeasurementFetchStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(TimescaleFetchStrategy.class);

    public static final long MIN_STEP_MS = SystemProperties.getLong("org.opennms.newts.query.minimum_step", 5L * 60L * 1000L);

    public static final int INTERVAL_DIVIDER = SystemProperties.getInteger("org.opennms.newts.query.interval_divider", 2);

    public static final long DEFAULT_HEARTBEAT_MS = SystemProperties.getLong("org.opennms.newts.query.heartbeat", 450L * 1000L);

    public static final int PARALLELISM = SystemProperties.getInteger("org.opennms.newts.query.parallelism", Runtime.getRuntime().availableProcessors());

    @Autowired
    private ResourceDao m_resourceDao;

    private final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("NewtsFetchStrateg-%d").build();

    private final ExecutorService threadPool = Executors.newCachedThreadPool(namedThreadFactory);

    // Used to limit the number of threads that are performing aggregation calculations in parallel
    private final Semaphore availableAggregationThreads = new Semaphore(PARALLELISM);

    @Autowired
    private TimeSeriesStorage storage;

    @Override
    public FetchResults fetch(long start, long end, long step, int maxrows, Long interval, Long heartbeat, List<Source> sources, boolean relaxed) {
        final LateAggregationParams lag = getLagParams(step, interval, heartbeat);
        final Optional<Timestamp> startTs = Optional.of(Timestamp.fromEpochMillis(start));
        final Optional<Timestamp> endTs = Optional.of(Timestamp.fromEpochMillis(end));
        final Map<String, Object> constants = Maps.newHashMap();
        final List<QueryResource> resources = new ArrayList<>();

        // Group the sources by resource id to avoid calling the ResourceDao
        // multiple times for the same resource
        Map<ResourceId, List<Source>> sourcesByResourceId = sources.stream()
                .collect(Collectors.groupingBy((source) -> ResourceId.fromString(source.getResourceId())));

        // Lookup the OnmsResources in parallel
        Map<ResourceId, Future<OnmsResource>> resourceFuturesById = Maps.newHashMapWithExpectedSize(sourcesByResourceId.size());
        for (ResourceId resourceId : sourcesByResourceId.keySet()) {
            resourceFuturesById.put(resourceId, threadPool.submit(getResourceByIdCallable(resourceId)));
        }

        // Gather the results, fail if any of the resources were not found
        Map<OnmsResource, List<Source>> sourcesByResource = Maps.newHashMapWithExpectedSize(sourcesByResourceId.size());
        for (Entry<ResourceId, Future<OnmsResource>> entry : resourceFuturesById.entrySet()) {
            try {
                OnmsResource resource = entry.getValue().get();
                if (resource == null) {
                    if (relaxed) continue;
                    LOG.error("No resource with id: {}", entry.getKey());
                    return null;
                }
                sourcesByResource.put(resource, sourcesByResourceId.get(entry.getKey()));
            } catch (ExecutionException | InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        // Now group the sources by Newts Resource ID, which differs from the OpenNMS Resource ID.
        Map<String, List<Source>> sourcesByNewtsResourceId = Maps.newHashMap();
        for (Entry<OnmsResource, List<Source>> entry : sourcesByResource.entrySet()) {
            final OnmsResource resource = entry.getKey();
            for (Source source : entry.getValue()) {
                // Gather the values from strings.properties
                Utils.convertStringAttributesToConstants(source.getLabel(), resource.getStringPropertyAttributes(), constants);

                resources.add(getResourceInfo(resource, source));

                // Grab the attribute that matches the source
                RrdGraphAttribute rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());

                if (rrdGraphAttribute == null && !Strings.isNullOrEmpty(source.getFallbackAttribute())) {
                    LOG.error("No attribute with name '{}', using fallback-attribute with name '{}'", source.getAttribute(), source.getFallbackAttribute());
                    source.setAttribute(source.getFallbackAttribute());
                    source.setFallbackAttribute(null);
                    rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());
                }

                if (rrdGraphAttribute == null) {
                    if (relaxed) continue;
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
        Map<String, Future<Collection<Row<Measurement>>>> measurementsByNewtsResourceId = Maps.newHashMapWithExpectedSize(sourcesByNewtsResourceId.size());
        for (Entry<String, List<Source>> entry : sourcesByNewtsResourceId.entrySet()) {
            measurementsByNewtsResourceId.put(entry.getKey(), threadPool.submit(
                    getMeasurementsForResourceCallable(entry.getKey(), entry.getValue(), startTs, endTs, lag)));
        }

        long[] timestamps = null;
        Map<String, double[]> columns = Maps.newHashMap();

        for (Entry<String, Future<Collection<Row<Measurement>>>> entry : measurementsByNewtsResourceId.entrySet()) {
            Collection<Row<Measurement>> rows;
            try {
                rows = entry.getValue().get();
            } catch (InterruptedException | ExecutionException e) {
                throw Throwables.propagate(e);
            }

            final int N = rows.size();

            if (timestamps == null) {
                timestamps = new long[N];
                int k = 0;
                for (final Row<Measurement> row : rows) {
                    timestamps[k] = row.getTimestamp().asMillis();
                    k++;
                }
            }

            int k = 0;
            for (Row<Measurement> row : rows) {
                for (Measurement measurement : row.getElements()) {
                    double[] column = columns.get(measurement.getName());
                    if (column == null) {
                        column = new double[N];
                        columns.put(measurement.getName(), column);
                    }
                    column[k] = measurement.getValue();
                }
                k += 1;
            }
        }

        FetchResults fetchResults = new FetchResults(timestamps, columns, lag.getStep(), constants, new QueryMetadata(resources));
        if (relaxed) {
            Utils.fillMissingValues(fetchResults, sources);
        }
        LOG.trace("Fetch results: {}", fetchResults);
        return fetchResults;
    }

    private Callable<Collection<Row<Measurement>>> getMeasurementsForResourceCallable(final String resourceId, final List<Source> listOfSources, final Optional<Timestamp> start, final Optional<Timestamp> end, final LateAggregationParams lag) {
        return new Callable<Collection<Row<Measurement>>>() {
            @Override
            public Collection<Row<Measurement>> call() throws Exception {

                List<List<Sample>> allSamples = new ArrayList<>();

                // Get results for all sources
                for (Source source : listOfSources) {
                    // Use the datasource as the metric name if set, otherwise use the name of the attribute
                    final String metricName = source.getDataSource() != null ? source.getDataSource() : source.getAttribute();
                    final Aggregation aggregation = toAggregation(source.getAggregation());

                    final Metric metric = Metric.builder()
                            .tag(CommonTagNames.resourceId, resourceId)
                            .tag(CommonTagNames.name, metricName)
                            .tag(Metric.MandatoryTag.mtype.name(), Metric.Mtype.gauge.name())  // TODO Patrick: where do we get the type from?
                            .tag(Metric.MandatoryTag.unit.name(), "ms") // TODO Patrick: where do we get the units from?
                            .build();

                    TimeSeriesFetchRequest request = TimeSeriesFetchRequest.builder()
                            .metric(metric)
                            .start(Instant.ofEpochMilli(start.or(Timestamp.fromEpochMillis(0)).asMillis()))
                            .end(Instant.ofEpochMilli(end.or(Timestamp.now()).asMillis()))
                            .step(Duration.ofMillis(lag.getStep()))
                            .aggregation(aggregation)
                            .build();

                    LOG.debug("Querying TimeseriesStorage for resource id {} with request: {}", resourceId, request);
                    List<Sample> samples = storage.getTimeseries(request);
                    allSamples.add(samples);
                }

                // build Rows from the results. One row can contain multiple columns. A Sample represents a cell in the "table"
                List<Row<Measurement>> rows =  new ArrayList<>();
                for(int rowIndex = 0; rowIndex < allSamples.get(0).size(); rowIndex++) {
                    Sample sampleOfFirstList = allSamples.get(0).get(rowIndex);

                    Timestamp timestamp = Timestamp.fromEpochMillis(sampleOfFirstList.getTime().toEpochMilli());
                    Resource resource = new Resource(sampleOfFirstList.getMetric().getFirstTagByKey("resourceId").getValue(),  Optional.of(asMap(sampleOfFirstList.getMetric().getMetaTags())));
                    Row<Measurement> row = new Row<>(timestamp, resource);

                    // Let's iterate over all lists and add the samples of row i
                    for(int columnIndex = 0; columnIndex < allSamples.size(); columnIndex++) {
                        List<Sample> list = allSamples.get(columnIndex);
                        Sample sampleOfCurrentList = list.get(rowIndex);
                        // final String name = listOfSources.get(columnIndex).getLabel();
                        final String name = sampleOfCurrentList.getMetric().getFirstTagByKey(CommonTagNames.name).getValue();
                        row.addElement(new Measurement(timestamp, resource, name, sampleOfCurrentList.getValue()));
                    }

                    rows.add(row);
                }

                LOG.debug("Found {} rows.", rows.size());
                return rows;
            }
        };
    }

    private static Aggregation toAggregation(String fn) {
        if ("average".equalsIgnoreCase(fn) || "avg".equalsIgnoreCase(fn)) {
            return Aggregation.AVERAGE;
        } else if ("max".equalsIgnoreCase(fn)) {
            return Aggregation.MAX;
        } else if ("min".equalsIgnoreCase(fn)) {
            return Aggregation.MIN;
        } else {
            throw new IllegalArgumentException("Unsupported aggregation function: " + fn);
        }
    }



    private Callable<OnmsResource> getResourceByIdCallable(final ResourceId resourceId) {
        return new Callable<OnmsResource>() {
            @Override
            public OnmsResource call() throws IllegalArgumentException {
                final OnmsResource resource = m_resourceDao.getResourceById(resourceId);
                if (resource != null) {
                    // The attributes are typically lazy loaded, so we trigger the load here
                    // while we're in a threaded context
                    resource.getAttributes();
                }
                return resource;
            }
        };
    }

    private SampleSelectCallback limitConcurrentAggregationsCallback = new SampleSelectCallback() {

        @Override
        public void beforeProcess() {
            try {
                availableAggregationThreads.acquire();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public void afterProcess() {
            availableAggregationThreads.release();
        }
    };

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
                effectiveHeartbeat += effectiveInterval - (effectiveHeartbeat % effectiveInterval);
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
    protected void setTimeseriesStorage(final TimeSeriesStorage timeseriesStorage) {
        this.storage = timeseriesStorage;
    }

    private OnmsNode getNode(final OnmsResource resource, final Source source) {
        OnmsNode node = null;
        try {
            node = ResourceTypeUtils.getNodeFromResourceRoot(resource);
        } catch (final ObjectRetrievalFailureException e) {
        }
        if (node == null) {
            final OnmsResource otherResource = m_resourceDao.getResourceById(ResourceId.fromString(source.getResourceId()).getParent());
            node = ResourceTypeUtils.getNodeFromResource(otherResource);
        }
        return node;
    }

    private QueryResource getResourceInfo(final OnmsResource resource, final Source source) {
        if (resource == null) return null;
        final OnmsNode node = getNode(resource, source);
        return new QueryResource(
                                resource.getId().toString(),
                                resource.getParent() == null? null : resource.getParent().getId().toString(),
                                resource.getLabel(),
                                resource.getName(),
                                node == null? null : new QueryNode(node.getId(), node.getForeignSource(), node.getForeignId(), node.getLabel())
                );
    }
}
