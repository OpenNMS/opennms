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
package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProportionalSumQueryTest {

    @Test
    public void pluginStrategyShouldRenderLegacyProportionalSum() {
        final String agg = ProportionalSumQuery.aggregationFor(ProportionalSumQuery.Strategy.PLUGIN,
                300_000L, 1_500_000L, 3_000_000L,
                "netflow.delta_switched", "netflow.last_switched", "netflow.bytes", "netflow.sampling_interval");
        final JsonObject json = JsonParser.parseString(agg).getAsJsonObject();
        final JsonObject proportionalSum = json.getAsJsonObject("proportional_sum");
        assertThat(proportionalSum.get("interval").getAsString(), equalTo("300000ms"));
        assertThat(proportionalSum.get("start").getAsLong(), equalTo(1_500_000L));
        assertThat(proportionalSum.get("end").getAsLong(), equalTo(3_000_000L));
        final JsonArray fields = proportionalSum.getAsJsonArray("fields");
        assertThat(fields.size(), equalTo(4));
        assertThat(fields.get(0).getAsString(), equalTo("netflow.delta_switched"));
        assertThat(fields.get(3).getAsString(), equalTo("netflow.sampling_interval"));
    }

    @Test
    public void painlessStrategyShouldRenderScriptedMetric() {
        final String agg = ProportionalSumQuery.aggregationFor(ProportionalSumQuery.Strategy.PAINLESS,
                300_000L, 1_500_000L, 3_000_000L,
                "range_start", "range_end", "bytes_ingress", null);
        final JsonObject json = JsonParser.parseString(agg).getAsJsonObject();
        final JsonObject scriptedMetric = json.getAsJsonObject("scripted_metric");
        final JsonObject params = scriptedMetric.getAsJsonObject("params");
        assertThat(params.get("interval").getAsLong(), equalTo(300_000L));
        assertThat(params.get("qstart").getAsLong(), equalTo(1_500_000L));
        assertThat(params.get("qend").getAsLong(), equalTo(3_000_000L));
        for (String script : Arrays.asList("init_script", "map_script", "combine_script", "reduce_script")) {
            assertThat(script + " must be present", scriptedMetric.has(script), equalTo(true));
        }
        // Field names are inlined into the script source (per-doc params lookups are slow);
        // without a sampling field the sampling block must not be emitted at all.
        final String mapScript = scriptedMetric.get("map_script").getAsString();
        assertThat(mapScript, Matchers.containsString("doc['range_start']"));
        assertThat(mapScript, Matchers.containsString("doc['bytes_ingress']"));
        assertThat(mapScript, Matchers.not(Matchers.containsString("sampling")));
        assertThat(mapScript, Matchers.not(Matchers.containsString("params.startField")));
        // 6 buckets -> dense variant with an array state sized via the nBuckets param
        assertThat(params.get("nBuckets").getAsLong(), equalTo(6L));
        assertThat(scriptedMetric.get("init_script").getAsString(), Matchers.containsString("new double["));
    }

    @Test
    public void painlessStrategyShouldInlineSamplingFieldWhenConfigured() {
        final String agg = ProportionalSumQuery.aggregationFor(ProportionalSumQuery.Strategy.PAINLESS,
                300_000L, 1_500_000L, 3_000_000L,
                "netflow.delta_switched", "netflow.last_switched", "netflow.bytes", "netflow.sampling_interval");
        final String mapScript = JsonParser.parseString(agg).getAsJsonObject()
                .getAsJsonObject("scripted_metric").get("map_script").getAsString();
        assertThat(mapScript, Matchers.containsString("doc['netflow.sampling_interval']"));
        assertThat(mapScript, Matchers.containsString("doc['netflow.delta_switched']"));
    }

    @Test
    public void painlessStrategyShouldFallBackToSparseScriptsForHugeBucketCounts() {
        // 30 days at a 1 second step -> ~2.6M buckets, far above the dense limit
        final long start = 1_500_000L;
        final long end = start + 30L * 24 * 3600 * 1000;
        final String agg = ProportionalSumQuery.aggregationFor(ProportionalSumQuery.Strategy.PAINLESS,
                1000L, start, end, "range_start", "range_end", "bytes_ingress", null);
        final JsonObject scriptedMetric = JsonParser.parseString(agg).getAsJsonObject()
                .getAsJsonObject("scripted_metric");
        assertThat(scriptedMetric.getAsJsonObject("params").has("nBuckets"), equalTo(false));
        assertThat(scriptedMetric.get("init_script").getAsString(), Matchers.containsString("new HashMap"));
        assertThat(scriptedMetric.get("map_script").getAsString(), Matchers.containsString("state.sums.put"));
    }

    @Test
    public void strategyParsingShouldDefaultToPainless() {
        assertThat(ProportionalSumQuery.Strategy.parse(null), equalTo(ProportionalSumQuery.Strategy.PAINLESS));
        assertThat(ProportionalSumQuery.Strategy.parse(""), equalTo(ProportionalSumQuery.Strategy.PAINLESS));
        assertThat(ProportionalSumQuery.Strategy.parse("painless"), equalTo(ProportionalSumQuery.Strategy.PAINLESS));
        assertThat(ProportionalSumQuery.Strategy.parse("bogus"), equalTo(ProportionalSumQuery.Strategy.PAINLESS));
        assertThat(ProportionalSumQuery.Strategy.parse("plugin"), equalTo(ProportionalSumQuery.Strategy.PLUGIN));
        assertThat(ProportionalSumQuery.Strategy.parse(" Plugin "), equalTo(ProportionalSumQuery.Strategy.PLUGIN));
    }

    @Test
    public void seriesTemplatesShouldRenderValidJsonForBothStrategies() {
        for (ProportionalSumQuery.Strategy strategy : ProportionalSumQuery.Strategy.values()) {
            final SearchQueryProvider provider = new SearchQueryProvider(strategy);
            final List<org.opennms.netmgt.flows.filter.api.Filter> filters =
                    Collections.singletonList(new TimeRangeFilter(1_500_000L, 3_000_000L));
            for (String query : Arrays.asList(
                    provider.getSeriesFromQuery(Arrays.asList("http", "https"), 300_000L, 1_500_000L, 3_000_000L,
                            "netflow.application", filters),
                    provider.getSeriesFromQuery(10, 300_000L, 1_500_000L, 3_000_000L,
                            "netflow.application", filters),
                    provider.getSeriesFromMissingQuery(300_000L, 1_500_000L, 3_000_000L,
                            "netflow.application", "Unknown", filters),
                    provider.getSeriesFromOthersQuery(Arrays.asList("http", "https"), 300_000L, 1_500_000L, 3_000_000L,
                            "netflow.application", false, filters))) {
                final JsonObject json = JsonParser.parseString(query).getAsJsonObject();
                final String expectedAggType = strategy == ProportionalSumQuery.Strategy.PLUGIN
                        ? "proportional_sum" : "scripted_metric";
                assertThat("query for strategy " + strategy + " must contain a " + expectedAggType + " aggregation: " + query,
                        query.contains("\"" + expectedAggType + "\""), equalTo(true));
                assertThat(json.has("aggs"), equalTo(true));
            }
        }
    }

    @Test
    public void shouldParsePluginBucketsShape() {
        final JsonObject agg = JsonParser.parseString("{\"buckets\": [" +
                "{\"key\": 1500000, \"key_as_string\": \"1500000\", \"doc_count\": 2, \"value\": 42.5}," +
                "{\"key\": 1800000, \"key_as_string\": \"1800000\", \"doc_count\": 0, \"value\": 0.0}]}")
                .getAsJsonObject();
        final ProportionalSumAggregation parsed = new ProportionalSumAggregation("bytes", agg);
        assertThat(parsed.getBuckets(), hasSize(2));
        assertThat(parsed.getBuckets().get(0).getTime(), equalTo(1_500_000L));
        assertThat(parsed.getBuckets().get(0).getValue(), closeTo(42.5, 1e-9));
    }

    @Test
    public void shouldParseScriptedMetricValueShape() {
        // Keys deliberately unordered: the parser must sort buckets by time
        final JsonObject agg = JsonParser.parseString(
                "{\"value\": {\"1800000\": 7.25, \"1500000\": 42.5, \"2100000\": 0.0}}").getAsJsonObject();
        final ProportionalSumAggregation parsed = new ProportionalSumAggregation("bytes", agg);
        assertThat(parsed.getBuckets(), hasSize(3));
        assertThat(parsed.getBuckets().stream()
                        .map(ProportionalSumAggregation.DateHistogram::getTime)
                        .collect(Collectors.toList()),
                contains(1_500_000L, 1_800_000L, 2_100_000L));
        assertThat(parsed.getBuckets().get(0).getValue(), closeTo(42.5, 1e-9));
        assertThat(parsed.getBuckets().get(1).getValue(), closeTo(7.25, 1e-9));
    }
}
