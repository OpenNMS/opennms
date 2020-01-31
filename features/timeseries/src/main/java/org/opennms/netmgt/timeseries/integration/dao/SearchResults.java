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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.opennms.newts.api.Resource;

/** TODO: Patrick: was directly copied from Newts -> we probably can get rid of it. */
public class SearchResults implements Iterable<SearchResults.Result> {
    private final List<Result> m_results = Lists.newArrayList();

    public void addResult(Resource resource, Collection<String> metrics) {
        this.m_results.add(new Result(resource, metrics));
    }

    public void addResult(Result result) {
        this.m_results.add(result);
    }

    public int size() {
        return this.m_results.size();
    }

    public boolean isEmpty() {
        return this.m_results.isEmpty();
    }

    public Iterator<Result> iterator() {
        return this.m_results.iterator();
    }

    public static class Result {
        private final Resource m_resource;
        private final Collection<String> m_metrics;

        Result(Resource resource, Collection<String> metrics) {
            this.m_resource = Preconditions.checkNotNull(resource, "resource argument");
            this.m_metrics = Preconditions.checkNotNull(metrics, "metrics argument");
        }

        public Resource getResource() {
            return this.m_resource;
        }

        public Collection<String> getMetrics() {
            return this.m_metrics;
        }
    }
}
