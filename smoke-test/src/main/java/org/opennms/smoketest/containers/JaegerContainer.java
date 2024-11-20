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
package org.opennms.smoketest.containers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.SystemInfoUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

public class JaegerContainer extends GenericContainer<JaegerContainer> implements TestLifecycleAware {
    public static final String ALIAS = "jaeger";
    public static final int WEB_PORT = 16686;
    public static final int THRIFT_HTTP_PORT = 14268;
    public static final int GRPC_HTTP_PORT = 14268;
    public static final String IMAGE = "jaegertracing/all-in-one:1.39";

    public JaegerContainer() {
        super(IMAGE);
        withNetwork(Network.SHARED);
        withNetworkAliases(ALIAS);
        withExposedPorts(WEB_PORT);
    }

    public URL getURL(String path) throws MalformedURLException {
        Objects.requireNonNull(path);
        return new URL("http://" + getHost() + ":" + getMappedPort(WEB_PORT).toString() + path);
    }

    /**
     * Gets the Thrift HTTP URL.
     * @return String suitable to pass to JAEGER_ENDPOINT
     */
    public static String getThriftHttpURL() {
        return String.format("http://%s:%d/api/traces", ALIAS, THRIFT_HTTP_PORT);
    }

    /**
     * Gets the gRPC HTTP URL.
     * @return String suitable to pass to OpenTelemetry otel.exporter.jaeger.endpoint
     */
    public static String getGrpcHttpURL() {
        return String.format("http://%s:%d/", ALIAS, GRPC_HTTP_PORT);
    }

    @Override
    public void afterTest(final TestDescription description, final Optional<Throwable> throwable) {
        retainLogsIfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsIfNeeded(String prefix, boolean failed) {
        // This can take a few seconds, so we only do it on failures
        if (failed) {
            try {
                Path opennms = Paths.get("target", "logs", prefix, ALIAS, "opennms-traces.json");
                FileUtils.copyURLToFile(getURL("/api/traces?service=" +
                        URLEncoder.encode(SystemInfoUtils.getInstanceId(), Charset.defaultCharset())),
                        opennms.toFile());
                logger().info("OpenNMS Jaeger trace JSON: {}", opennms.toUri());

                Path minion = Paths.get("target", "logs", prefix, ALIAS, "minion-traces.json");
                FileUtils.copyURLToFile(getURL("/api/traces?service=Minion"), minion.toFile());
                logger().info("Minion Jaeger trace JSON: {}", minion.toUri());

                Path sentinel = Paths.get("target", "logs", prefix, ALIAS, "sentinel-traces.json");
                FileUtils.copyURLToFile(getURL("/api/traces?service=Sentinel"), sentinel.toFile());
                logger().info("Sentinel Jaeger trace JSON: {}", sentinel.toUri());
            } catch (Exception e) {
                System.err.println("Received exception while trying to save all Jaeger traces");
                e.printStackTrace(System.err);
            }
        }
    }
}
