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
package org.opennms.netmgt.flows.api;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.flows.filter.api.Filter;

import com.google.common.collect.Table;

/**
 * Used to query statistics and time series derived from flow data.
 */
public interface FlowQueryService {

    CompletableFuture<Long> getFlowCount(List<Filter> filters);

    CompletableFuture<List<String>> getApplications(String matchingPrefix, long limit, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(int N, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(Set<String> applications, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(Set<String> applications, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(int N, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<String>> getConversations(String locationPattern, String protocolPattern,
                                                     String lowerIPPattern, String upperIPPattern,
                                                     String applicationPattern, long limit, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(int N, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(Set<String> conversations, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(Set<String> conversations, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(int N, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<String>> getHosts(String regex, long limit, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(int N, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(Set<String> hosts, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(Set<String> hosts, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(int N, long step, boolean includeOther, List<Filter> filters);

    CompletableFuture<List<String>> getFieldValues(LimitedCardinalityField field, List<Filter> filters);

    CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(LimitedCardinalityField field, List<Filter> filters);

    CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(LimitedCardinalityField field, long step, List<Filter> filters);

}
