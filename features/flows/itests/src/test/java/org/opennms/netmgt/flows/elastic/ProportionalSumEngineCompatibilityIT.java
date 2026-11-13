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
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Runs the Painless proportional-sum aggregation on engines other than the ES 8 used by the
 * other flow ITs. ES 7.x and OpenSearch 2.x return date doc values as
 * JodaCompatibleZonedDateTime rather than ZonedDateTime, so date handling must be checked
 * there. ES 9.x and OpenSearch 3.x cover the newest releases. Results are compared against
 * a Java reference of the intended semantics.
 */
@RunWith(Parameterized.class)
public class ProportionalSumEngineCompatibilityIT {

    private static final long T0 = 1735689600000L;
    // rs, re, bytes, sampling (0 = none)
    private static final long[][] DOCS = {
            {T0 + 1000, T0 + 2000, 1000, 0},
            {T0 + 1500, T0 + 1500, 70, 2},
            {T0 + 500, T0 + 3500, 300, 0},
            {T0 + 2900, T0 + 4100, 120, 10},
            {T0 + 100, T0 + 200, 999, 0},
    };
    private static final long QS = T0 + 1077;
    private static final long QE = T0 + 3999;

    private static GenericContainer<?> container;
    private static String containerImage;
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> engines() {
        return Arrays.asList(new Object[][]{
                {"docker.elastic.co/elasticsearch/elasticsearch:7.17.0"},
                {"opensearchproject/opensearch:2.19.1"},
                {"docker.elastic.co/elasticsearch/elasticsearch:9.5.5"},
                {"opensearchproject/opensearch:3.9.0"},
        });
    }

    private final String baseUrl;

    public ProportionalSumEngineCompatibilityIT(String image) throws IOException, InterruptedException {
        if (!image.equals(containerImage)) {
            stopContainer();
            container = new GenericContainer<>(DockerImageName.parse(image))
                    .withExposedPorts(9200)
                    .withEnv("discovery.type", "single-node");
            if (image.contains("opensearch")) {
                container.withEnv("DISABLE_SECURITY_PLUGIN", "true")
                        .withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
                        .withEnv("OPENSEARCH_JAVA_OPTS", "-Xms512m -Xmx512m");
            } else {
                container.withEnv("xpack.security.enabled", "false")
                        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
            }
            container.waitingFor(new HttpWaitStrategy().forPort(9200).forPath("/_cluster/health")
                            .withStartupTimeout(Duration.ofMinutes(3)));
            container.start();
            containerImage = image;
        }
        baseUrl = "http://" + container.getHost() + ":" + container.getMappedPort(9200);
        for (String type : new String[]{"date", "long"}) {
            loadIndex(type);
        }
    }

    @AfterClass
    public static void stopContainer() {
        if (container != null) {
            container.stop();
            container = null;
            containerImage = null;
        }
    }

    @Test
    public void denseSeriesMatchesReference() throws Exception {
        verify(300, QS, QE);
    }

    @Test
    public void sparseSeriesMatchesReference() throws Exception {
        // > 4096 buckets selects the HashMap variant
        verify(1, QS, QE + 5000);
    }

    @Test
    public void totalsMatchReference() throws Exception {
        verify(QE - QS + 1, QS, QE);
    }

    @Test
    public void zeroLengthWindowMatchesReference() throws Exception {
        verify(0, QS, QS);
    }

    private void verify(long step, long start, long end) throws Exception {
        final Map<Long, Double> expected = reference(Math.max(step, 1), start, end);
        for (String type : new String[]{"date", "long"}) {
            final String agg = ProportionalSumQuery.aggregationFor(ProportionalSumQuery.Strategy.PAINLESS,
                    step, start, end, "rs", "re", "bytes", "smp");
            final JsonObject res = request("POST", "/psq-" + type + "/_search?request_cache=false",
                    "{\"size\":0,\"aggs\":{\"x\":" + agg + "}}");
            final JsonObject value = res.getAsJsonObject("aggregations").getAsJsonObject("x").getAsJsonObject("value");
            final Map<Long, Double> actual = new TreeMap<>();
            value.entrySet().forEach(e -> actual.put(Long.parseLong(e.getKey()), e.getValue().getAsDouble()));
            assertThat(type + " bucket keys", actual.keySet(), equalTo(expected.keySet()));
            for (Map.Entry<Long, Double> e : expected.entrySet()) {
                assertThat(type + " bucket " + e.getKey(), actual.get(e.getKey()), closeTo(e.getValue(), 1e-9));
            }
        }
    }

    private static Map<Long, Double> reference(long step, long qs, long qe) {
        final long off = qs % step;
        final Map<Long, Double> out = new TreeMap<>();
        for (long[] d : DOCS) {
            final long rs = d[0], re = d[1];
            final double v = d[3] != 0 ? d[2] * (double) d[3] : d[2];
            final long first = ((Math.max(rs, qs) - off) / step) * step + off;
            final long last = ((Math.min(re, qe) - off) / step) * step + off;
            for (long b = first; b <= last; b += step) {
                final long nb = b + step;
                final double ratio = re == rs ? 1.0
                        : (rs > nb || re < b ? 0 : Math.min(nb, re) - Math.max(b, rs)) / (double) (re - rs);
                out.merge(b, v * ratio, Double::sum);
            }
        }
        if (!out.isEmpty()) {
            final long min = ((TreeMap<Long, Double>) out).firstKey(), max = ((TreeMap<Long, Double>) out).lastKey();
            for (long b = min; b <= max; b += step) {
                out.putIfAbsent(b, 0.0);
            }
        }
        return out;
    }

    private void loadIndex(String type) throws IOException, InterruptedException {
        final String index = "/psq-" + type;
        if (send("HEAD", index, null).statusCode() == 200) {
            return;
        }
        request("PUT", index, "{\"settings\":{\"number_of_shards\":2,\"number_of_replicas\":0},"
                + "\"mappings\":{\"properties\":{\"rs\":{\"type\":\"" + type + "\"},\"re\":{\"type\":\"" + type + "\"},"
                + "\"bytes\":{\"type\":\"long\"},\"smp\":{\"type\":\"double\"}}}}");
        for (int i = 0; i < DOCS.length; i++) {
            final long[] d = DOCS[i];
            request("PUT", index + "/_doc/" + i, "{\"rs\":" + d[0] + ",\"re\":" + d[1] + ",\"bytes\":" + d[2]
                    + (d[3] != 0 ? ",\"smp\":" + d[3] : "") + "}");
        }
        request("POST", index + "/_refresh", null);
    }

    private JsonObject request(String method, String path, String body) throws IOException, InterruptedException {
        final HttpResponse<String> res = send(method, path, body);
        if (res.statusCode() >= 300) {
            throw new IllegalStateException(method + " " + path + " failed with " + res.statusCode() + ": " + res.body());
        }
        return JsonParser.parseString(res.body()).getAsJsonObject();
    }

    private HttpResponse<String> send(String method, String path, String body) throws IOException, InterruptedException {
        return HTTP.send(HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build(), HttpResponse.BodyHandlers.ofString());
    }
}
