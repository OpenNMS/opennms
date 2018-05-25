/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.flows.filter.api.Filter;

import com.google.common.collect.Table;

public interface FlowRepository {

    void persist(Collection<Flow> packets, FlowSource source) throws FlowException;

    CompletableFuture<Long> getFlowCount(List<Filter> filters);

    CompletableFuture<Set<Integer>> getExportersWithFlows(int limit, List<Filter> filters);

    CompletableFuture<Set<Integer>> getSnmpInterfaceIdsWithFlows(int limit, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<String>>> getTopNApplications(int N, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationsSeries(int N, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversations(int N, List<Filter> filters);

    CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationsSeries(int N, long step, List<Filter> filters);

}
