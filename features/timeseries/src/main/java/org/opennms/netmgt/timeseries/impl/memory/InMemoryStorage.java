/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.impl.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.Sample;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.Tag;
import org.opennms.netmgt.timeseries.api.domain.TimeSeriesFetchRequest;

/**
 * Simulates a timeseries storage in memory. The implementation is super simple and not very efficient.
 * For testing and evaluating purposes only, not for production.
 */
public class InMemoryStorage implements TimeSeriesStorage {

    private final Map<Metric, Collection<Sample>> data = new ConcurrentHashMap<>();

    @Override
    public void store(List<Sample> samples) throws StorageException {
        Objects.requireNonNull(samples);
        for(Sample sample : samples) {
            Collection<Sample> timeseries = data.computeIfAbsent(sample.getMetric(), k -> new ConcurrentLinkedQueue<Sample>());
            timeseries.add(sample);
        }
    }

    @Override
    public List<Metric> getMetrics(final Collection<Tag> tags) {
        Objects.requireNonNull(tags);
        return data.keySet().stream().filter(metric -> containsAll(metric, tags)).collect(Collectors.toList());
    }

    private boolean containsAll(final Metric metric, final Collection<Tag> tags) {
        for(Tag tag: tags) {
            if(!metric.getTags().contains(tag) && !metric.getMetaTags().contains(tag)){
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Sample> getTimeseries(TimeSeriesFetchRequest request) throws StorageException {
        Objects.requireNonNull(request);
        if(!data.containsKey(request.getMetric())){
            return Collections.emptyList();
        }
        return data.get(request.getMetric()).stream()
                .filter(sample -> sample.getTime().isAfter(request.getStart()))
                .filter(sample -> sample.getTime().isBefore(request.getEnd()))
                // TODO Patrick: bucket and aggregate with the given aggregation function
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Metric metric) {
        Objects.requireNonNull(metric);
        this.data.remove(metric);
    }
}
