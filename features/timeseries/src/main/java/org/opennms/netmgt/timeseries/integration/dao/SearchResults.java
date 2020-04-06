/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.dao;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opennms.newts.api.Resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SearchResults implements Iterable<SearchResults.Result> {
    private final List<Result> results = Lists.newArrayList();

    public void addResult(Resource resource, Collection<String> metrics) {
        this.results.add(new Result(resource, metrics));
    }

    public void addResult(Result result) {
        this.results.add(result);
    }

    public int size() {
        return this.results.size();
    }

    public boolean isEmpty() {
        return this.results.isEmpty();
    }

    public Iterator<Result> iterator() {
        return this.results.iterator();
    }

    public static class Result {
        private final Resource resource;
        private final Collection<String> metrics;

        Result(Resource resource, Collection<String> metrics) {
            this.resource = Preconditions.checkNotNull(resource, "resource argument");
            this.metrics = Preconditions.checkNotNull(metrics, "metrics argument");
        }

        public Resource getResource() {
            return this.resource;
        }

        public Collection<String> getMetrics() {
            return this.metrics;
        }
    }
}
