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
package org.opennms.core.daemon.loader;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.provision.DetectorRequestBuilder;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.PreDetectCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;

/**
 * A local implementation of {@link LocationAwareDetectorClient} for standalone
 * daemon containers that don't have the RPC infrastructure. Returns a no-op
 * {@link DetectorRequestBuilder} that always returns {@code false} (service not
 * detected), since standalone Provisiond does not perform service detection.
 *
 * <p>Service detection in the Delta-V architecture is delegated to Minion
 * or handled by the Provisiond on core. This stub satisfies the @Autowired
 * injection requirement.</p>
 */
public class LocalDetectorClient implements LocationAwareDetectorClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalDetectorClient.class);

    @Override
    public DetectorRequestBuilder detect() {
        return new LocalDetectorRequestBuilder();
    }

    private static class LocalDetectorRequestBuilder implements DetectorRequestBuilder {
        private String serviceName;
        private String className;
        private InetAddress address;
        private final Map<String, String> attributes = new HashMap<>();

        @Override
        public DetectorRequestBuilder withLocation(String location) {
            return this;
        }

        @Override
        public DetectorRequestBuilder withSystemId(String systemId) {
            return this;
        }

        @Override
        public DetectorRequestBuilder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public DetectorRequestBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        @Override
        public DetectorRequestBuilder withAddress(InetAddress address) {
            this.address = address;
            return this;
        }

        @Override
        public DetectorRequestBuilder withAttribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        @Override
        public DetectorRequestBuilder withAttributes(Map<String, String> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        @Override
        public DetectorRequestBuilder withNodeId(Integer nodeId) {
            return this;
        }

        @Override
        public DetectorRequestBuilder withParentSpan(Span span) {
            return this;
        }

        @Override
        public DetectorRequestBuilder withPreDetectCallback(PreDetectCallback preDetectCallback) {
            return this;
        }

        @Override
        public CompletableFuture<Boolean> execute() {
            LOG.debug("LocalDetectorClient: detection request for service={} class={} address={} " +
                    "-- returning false (no local detection in standalone container)", serviceName, className, address);
            return CompletableFuture.completedFuture(Boolean.FALSE);
        }
    }
}
