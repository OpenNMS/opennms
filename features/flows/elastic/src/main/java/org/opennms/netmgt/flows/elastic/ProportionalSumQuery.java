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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Renders the aggregation used to distribute a flow's bytes proportionally over the
 * time buckets its [start,end] range overlaps, clipped to the query window.
 *
 * Two strategies are supported:
 * <ul>
 *   <li>{@link Strategy#PAINLESS} (default): a {@code scripted_metric} aggregation built
 *       from inline Painless scripts. Works against stock Elasticsearch and OpenSearch;
 *       requires inline scripting to be enabled (it is by default).</li>
 *   <li>{@link Strategy#PLUGIN}: the {@code proportional_sum} aggregation provided by the
 *       elasticsearch-drift-plugin. Retained as a fallback for clusters that already run
 *       the plugin and disallow inline scripts. Note that the plugin produces incorrect
 *       results when the window start is not a multiple of the step (mixed bucket grids,
 *       totals cut off at the first epoch-grid boundary).</li>
 * </ul>
 *
 * The Painless strategy has two internal forms with identical output: a dense one whose
 * per-cell state is a flat double[] indexed by bucket (faster; used when the window/step
 * combination yields at most {@link #DENSE_BUCKET_LIMIT} buckets) and a sparse HashMap-based
 * fallback for arbitrarily fine-grained windows.
 *
 * Bucket semantics of the Painless variant: buckets are aligned to the window start, each
 * document contributes {@code value * overlap(bucket, [start,end]) / (end - start)} to every
 * bucket it overlaps within the window, documents with {@code start == end} contribute their
 * whole value to the single bucket containing their start, and the value is multiplied by the
 * sampling field when that field is present, finite and non-zero. Gaps between the first and
 * last non-empty bucket are zero-filled, mirroring the plugin's min_doc_count=0 behavior.
 */
public final class ProportionalSumQuery {

    public enum Strategy {
        PAINLESS,
        PLUGIN;

        public static Strategy parse(String value) {
            if (value != null && "plugin".equalsIgnoreCase(value.trim())) {
                return PLUGIN;
            }
            return PAINLESS;
        }
    }

    /**
     * With the dense scripts, every terms cell allocates a double[nBuckets] up front, and
     * depth-first terms collection can hold many cells at once. Queries whose window/step
     * combination produces more buckets than this fall back to the sparse (HashMap) scripts,
     * which only allocate touched buckets. UI-driven queries stay far below this limit.
     */
    private static final long DENSE_BUCKET_LIMIT = 4096;

    /**
     * Shared prologue of both map scripts: doc-value extraction and window math.
     * Field names are inlined into the script source at render time: a per-document
     * {@code doc[params.field]} lookup benchmarked ~1.5x slower than {@code doc['literal']}
     * on whole-window queries, and only three field combinations exist, so Elasticsearch's
     * script cache still holds a handful of compiled scripts. The {@code @SAMPLING@} block
     * is emitted only when a sampling field is configured. Documents with a missing or
     * inverted range are skipped; the plugin fails the whole search in the inverted case,
     * which is not worth preserving.
     */
    private static final String MAP_PROLOGUE_TEMPLATE = "" +
            "if (doc[@START@].size() > 0 && doc[@END@].size() > 0 && doc[@VALUE@].size() > 0) {\n" +
            "  def sv = doc[@START@].value;\n" +
            "  long rs = sv instanceof ZonedDateTime ? sv.toInstant().toEpochMilli() : ((Number) sv).longValue();\n" +
            "  def ev = doc[@END@].value;\n" +
            "  long re = ev instanceof ZonedDateTime ? ev.toInstant().toEpochMilli() : ((Number) ev).longValue();\n" +
            "  if (re >= rs && rs >= 0) {\n" +
            "    double v = ((Number) doc[@VALUE@].value).doubleValue();\n" +
            "@SAMPLING@" +
            "    long interval = ((Number) params.interval).longValue();\n" +
            "    long qstart = ((Number) params.qstart).longValue();\n" +
            "    long qend = ((Number) params.qend).longValue();\n" +
            "    long off = qstart - (qstart / interval) * interval;\n" +
            "    long duration = re - rs;\n" +
            "    long first = ((((rs > qstart) ? rs : qstart) - off) / interval) * interval + off;\n" +
            "    long last = ((((re < qend) ? re : qend) - off) / interval) * interval + off;\n" +
            "    for (long b = first; b <= last; b += interval) {\n" +
            "      long nb = b + interval;\n" +
            "      double ratio;\n" +
            "      if (duration != 0) {\n" +
            "        long tin = (rs > nb || re < b) ? 0L : (((nb < re) ? nb : re) - ((b > rs) ? b : rs));\n" +
            "        ratio = tin / (double) duration;\n" +
            "      } else {\n" +
            "        ratio = 1.0;\n" +
            "      }\n";

    private static final String SAMPLING_BLOCK_TEMPLATE = "" +
            "    if (doc.containsKey(@SAMPLING_FIELD@) && doc[@SAMPLING_FIELD@].size() > 0) {\n" +
            "      double smp = ((Number) doc[@SAMPLING_FIELD@].value).doubleValue();\n" +
            "      if (smp == smp && smp != Double.POSITIVE_INFINITY && smp != Double.NEGATIVE_INFINITY && smp != 0.0) {\n" +
            "        v *= smp;\n" +
            "      }\n" +
            "    }\n";

    private static String mapPrologue(String startField, String endField, String valueField,
                                      String samplingField) {
        final String sampling = samplingField == null ? ""
                : SAMPLING_BLOCK_TEMPLATE.replace("@SAMPLING_FIELD@", painlessStringLiteral(samplingField));
        return MAP_PROLOGUE_TEMPLATE
                .replace("@START@", painlessStringLiteral(startField))
                .replace("@END@", painlessStringLiteral(endField))
                .replace("@VALUE@", painlessStringLiteral(valueField))
                .replace("@SAMPLING@", sampling);
    }

    private static String painlessStringLiteral(String fieldName) {
        // Field names come from our own query providers; refuse anything that could
        // escape the string literal rather than attempting to quote it.
        if (fieldName.indexOf('\'') >= 0 || fieldName.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Unsupported characters in field name: " + fieldName);
        }
        return "'" + fieldName + "'";
    }

    // --- dense variant: per-cell double[] indexed by (bucket - qstart) / interval; the
    // touched index range is tracked so the response keeps the plugin's min..max zero-fill.

    private static final String INIT_SCRIPT_DENSE = "" +
            "state.sums = new double[(int) ((Number) params.nBuckets).longValue()];" +
            " state.min = Integer.MAX_VALUE; state.max = -1;";

    private static final String MAP_LOOP_DENSE = "" +
            "      int idx = (int) ((b - qstart) / interval);\n" +
            "      state.sums[idx] += v * ratio;\n" +
            "      if (idx < state.min) { state.min = idx; }\n" +
            "      if (idx > state.max) { state.max = idx; }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    private static final String COMBINE_SCRIPT_DENSE = "" +
            "List out = new ArrayList();\n" +
            "out.add(state.min);\n" +
            "out.add(state.max);\n" +
            "if (state.max >= 0) {\n" +
            "  for (int i = (int) state.min; i <= (int) state.max; i++) {\n" +
            "    out.add(state.sums[i]);\n" +
            "  }\n" +
            "}\n" +
            "return out;\n";

    private static final String REDUCE_SCRIPT_DENSE = "" +
            "int gmin = Integer.MAX_VALUE;\n" +
            "int gmax = -1;\n" +
            "for (s in states) {\n" +
            "  if (s != null) {\n" +
            "    int mx = ((Number) s.get(1)).intValue();\n" +
            "    if (mx >= 0) {\n" +
            "      int mn = ((Number) s.get(0)).intValue();\n" +
            "      if (mn < gmin) { gmin = mn; }\n" +
            "      if (mx > gmax) { gmax = mx; }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "Map m = new HashMap();\n" +
            "if (gmax >= 0) {\n" +
            "  double[] tot = new double[gmax - gmin + 1];\n" +
            "  for (s in states) {\n" +
            "    if (s != null) {\n" +
            "      int mx = ((Number) s.get(1)).intValue();\n" +
            "      if (mx >= 0) {\n" +
            "        int mn = ((Number) s.get(0)).intValue();\n" +
            "        for (int i = mn; i <= mx; i++) {\n" +
            "          tot[i - gmin] += ((Number) s.get(2 + i - mn)).doubleValue();\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  long interval = ((Number) params.interval).longValue();\n" +
            "  long qstart = ((Number) params.qstart).longValue();\n" +
            "  for (int i = 0; i < tot.length; i++) {\n" +
            "    m.put(String.valueOf(qstart + (long) (gmin + i) * interval), tot[i]);\n" +
            "  }\n" +
            "}\n" +
            "return m;\n";

    // --- sparse variant: HashMap keyed by bucket start; allocation proportional to touched
    // buckets only, used when nBuckets exceeds DENSE_BUCKET_LIMIT.

    private static final String INIT_SCRIPT_SPARSE = "state.sums = new HashMap();";

    private static final String MAP_LOOP_SPARSE = "" +
            "      String k = String.valueOf(b);\n" +
            "      double prev = state.sums.containsKey(k) ? (double) state.sums.get(k) : 0.0;\n" +
            "      state.sums.put(k, prev + v * ratio);\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    private static final String COMBINE_SCRIPT_SPARSE = "return state.sums;";

    private static final String REDUCE_SCRIPT_SPARSE = "" +
            "Map merged = new HashMap();\n" +
            "for (s in states) {\n" +
            "  if (s != null) {\n" +
            "    for (e in s.entrySet()) {\n" +
            "      String k = e.getKey();\n" +
            "      double val = ((Number) e.getValue()).doubleValue();\n" +
            "      double prev = merged.containsKey(k) ? (double) merged.get(k) : 0.0;\n" +
            "      merged.put(k, prev + val);\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "if (!merged.isEmpty()) {\n" +
            "  long interval = ((Number) params.interval).longValue();\n" +
            "  long minKey = Long.MAX_VALUE;\n" +
            "  long maxKey = Long.MIN_VALUE;\n" +
            "  for (k in merged.keySet()) {\n" +
            "    long key = Long.parseLong(k);\n" +
            "    if (key < minKey) { minKey = key; }\n" +
            "    if (key > maxKey) { maxKey = key; }\n" +
            "  }\n" +
            "  for (long b = minKey; b <= maxKey; b += interval) {\n" +
            "    String k = String.valueOf(b);\n" +
            "    if (!merged.containsKey(k)) { merged.put(k, 0.0); }\n" +
            "  }\n" +
            "}\n" +
            "return merged;\n";

    private ProportionalSumQuery() {
    }

    /**
     * Renders the aggregation body (the JSON object placed after the aggregation name)
     * for the given strategy.
     *
     * @param samplingField optional field the value is multiplied by; may be null
     */
    public static String aggregationFor(Strategy strategy, long stepMs, long start, long end,
                                        String startField, String endField, String valueField,
                                        String samplingField) {
        if (strategy == Strategy.PLUGIN) {
            return pluginAggregation(stepMs, start, end, startField, endField, valueField, samplingField);
        }
        return painlessAggregation(stepMs, start, end, startField, endField, valueField, samplingField);
    }

    private static String pluginAggregation(long stepMs, long start, long end,
                                            String startField, String endField, String valueField,
                                            String samplingField) {
        final JsonArray fields = new JsonArray();
        fields.add(startField);
        fields.add(endField);
        fields.add(valueField);
        if (samplingField != null) {
            fields.add(samplingField);
        }
        final JsonObject proportionalSum = new JsonObject();
        proportionalSum.add("fields", fields);
        proportionalSum.addProperty("interval", stepMs + "ms");
        proportionalSum.addProperty("start", start);
        proportionalSum.addProperty("end", end);
        final JsonObject agg = new JsonObject();
        agg.add("proportional_sum", proportionalSum);
        return agg.toString();
    }

    private static String painlessAggregation(long stepMs, long start, long end,
                                              String startField, String endField, String valueField,
                                              String samplingField) {
        final JsonObject params = new JsonObject();
        params.addProperty("interval", stepMs);
        params.addProperty("qstart", start);
        params.addProperty("qend", end);

        final long offset = start % stepMs;
        final long lastBucket = ((end - offset) / stepMs) * stepMs + offset;
        final long nBuckets = (lastBucket - start) / stepMs + 1;

        final String prologue = mapPrologue(startField, endField, valueField, samplingField);
        final JsonObject scriptedMetric = new JsonObject();
        scriptedMetric.add("params", params);
        if (nBuckets <= DENSE_BUCKET_LIMIT) {
            params.addProperty("nBuckets", nBuckets);
            scriptedMetric.addProperty("init_script", INIT_SCRIPT_DENSE);
            scriptedMetric.addProperty("map_script", prologue + MAP_LOOP_DENSE);
            scriptedMetric.addProperty("combine_script", COMBINE_SCRIPT_DENSE);
            scriptedMetric.addProperty("reduce_script", REDUCE_SCRIPT_DENSE);
        } else {
            scriptedMetric.addProperty("init_script", INIT_SCRIPT_SPARSE);
            scriptedMetric.addProperty("map_script", prologue + MAP_LOOP_SPARSE);
            scriptedMetric.addProperty("combine_script", COMBINE_SCRIPT_SPARSE);
            scriptedMetric.addProperty("reduce_script", REDUCE_SCRIPT_SPARSE);
        }
        final JsonObject agg = new JsonObject();
        agg.add("scripted_metric", scriptedMetric);
        return agg.toString();
    }
}
