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
package org.opennms.smoketest.stacks;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Defines which components and services are enabled in a particular stack.
 *
 * This model should be used to encapsulate high level features and functionality
 * provided by OpenNMS and related components.
 *
 * @author jwhite
 */
public class StackModel {

    private final OpenNMSProfile opennms;
    private final List<MinionProfile> minions;
    private final List<SentinelProfile> sentinels;
    private final boolean jaegerEnabled;
    private final boolean elasticsearchEnabled;
    private final boolean telemetryProcessingEnabled;
    private final boolean simulateRestricedOpenShiftEnvironment;
    private final IpcStrategy ipcStrategy;
    private final TimeSeriesStrategy timeSeriesStrategy;
    private final BlobStoreStrategy blobStoreStrategy;
    private final JsonStoreStrategy jsonStoreStrategy;
    private final KafkaCompressionStrategy kafkaCompressionStrategy;

    private StackModel(Builder builder) {
        // Profiles
        opennms = builder.opennms;
        minions = builder.minions;
        sentinels = builder.sentinels;

        // Flags
        jaegerEnabled = builder.jaegerEnabled;
        elasticsearchEnabled = builder.elasticsearchEnabled;
        telemetryProcessingEnabled = builder.telemetryProcessingEnabled;
        simulateRestricedOpenShiftEnvironment = builder.simulateRestricedOpenShiftEnvironment;

        // Enums
        ipcStrategy = builder.ipcStrategy;
        timeSeriesStrategy = builder.timeSeriesStrategy;
        blobStoreStrategy = builder.blobStoreStrategy;
        jsonStoreStrategy = builder.jsonStoreStrategy;
        kafkaCompressionStrategy = builder.kafkaCompressionStrategy;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private OpenNMSProfile opennms = OpenNMSProfile.DEFAULT;
        private List<MinionProfile> minions = new LinkedList<>();
        private List<SentinelProfile> sentinels = new LinkedList<>();
        public boolean jaegerEnabled = false;
        private boolean elasticsearchEnabled = false;
        private boolean telemetryProcessingEnabled = false;
        private boolean simulateRestricedOpenShiftEnvironment = false;

        private IpcStrategy ipcStrategy = IpcStrategy.JMS;
        private TimeSeriesStrategy timeSeriesStrategy = TimeSeriesStrategy.RRD;
        
        private BlobStoreStrategy blobStoreStrategy = BlobStoreStrategy.NOOP;
        private JsonStoreStrategy jsonStoreStrategy;
        private KafkaCompressionStrategy kafkaCompressionStrategy = KafkaCompressionStrategy.NONE;

        /**
         * Profile for the OpenNMS container.
         *
         * Right now there can only be one, but we could extend this model to have
         * many connected the same resources but using different database names and
         * prefixes. Could also support different "roles" like a Web only server, etc...
         *
         * @param opennms profile to use
         * @return this builder
         */
        public Builder withOpenNMS(OpenNMSProfile opennms) {
            this.opennms = Objects.requireNonNull(opennms);
            return this;
        }

        /**
         * Enable one Minion using the default profile.
         *
         * @return this builder
         */
        public Builder withMinion() {
            minions = Collections.singletonList(MinionProfile.DEFAULT);
            return this;
        }

        /**
         * Enable many Minions using the given profiles.
         *
         * @param minions profiles to use
         * @return this builder
         */
        public Builder withMinions(MinionProfile... minions) {
            this.minions = Arrays.asList(minions);
            return this;
        }

        /**
         * Enable a Minion using the given configuration.
         *
         * @param configuration minion configuration to use
         * @return this builder
         */

        /**
         * Enable many Minions using the given configurations.
         *
         * @param configurations minions configurations to use
         * @return this builder
         */

        /**
         * Enable one Sentinel using the default profile.
         *
         * @return this builder
         */
        public Builder withSentinel() {
            sentinels = Collections.singletonList(SentinelProfile.DEFAULT);
            return this;
        }

        /**
         * Enable many Sentinels using the given profiles.
         *
         * @param sentinels profiles to use
         * @return this builder
         */
        public Builder withSentinels(SentinelProfile... sentinels) {
            this.sentinels = Arrays.asList(sentinels);
            return this;
        }

        /**
         * Enable Jaeger for tracing.
         *
         * @return this builder
         */
        public Builder withJaeger() {
            jaegerEnabled = true;
            return this;
        }

        /**
         * Enable Elastiscearch.
         *
         * @return this builder
         */
        public Builder withElasticsearch() {
            elasticsearchEnabled = true;
            return this;
        }

        /**
         * Type of service used to communicate between Minion/OpenNMS/Sentinel
         *
         * @param ipcStrategy JMS vs Kafka, etc...
         * @return this builder
         */
        public Builder withIpcStrategy(IpcStrategy ipcStrategy) {
            this.ipcStrategy = Objects.requireNonNull(ipcStrategy);
            return this;
        }

        /**
         * Type of service used to persist time series data.
         *
         * @param timeSeriesStrategy RRD vs Newts
         * @return this builder
         */
        public Builder withTimeSeriesStrategy(TimeSeriesStrategy timeSeriesStrategy) {
            this.timeSeriesStrategy = Objects.requireNonNull(timeSeriesStrategy);
            return this;
        }

        /**
         * Type of compression used with Kafka messages.
         *
         * @param kafkaCompressionStrategy GZIP, SNAPPY, LZ4, ZSTD, or NONE
         * @return this builder
         */
        public Builder withKafkaCompressionStrategy(KafkaCompressionStrategy kafkaCompressionStrategy) {
            this.kafkaCompressionStrategy = Objects.requireNonNull(kafkaCompressionStrategy);
            return this;
        }

        /**
         * Enable the processing of telemetry & flows.
         *
         * This will automatically enable Elasticsearch and Newts if Sentinel is being used.
         *
         * @return this builder
         */
        public Builder withTelemetryProcessing() {
            telemetryProcessingEnabled = true;
            return this;
        }

        /**
         * Simulate a restricted OpenShift environment by using a random UID when
         * starting the container, and using a JDK w/o capabilities set
         *
         * @return this builder
         */
        public Builder withSimulateRestricedOpenShiftEnvironment() {
            simulateRestricedOpenShiftEnvironment = true;
            return this;
        }

        /**
         * Choose the key value store to use for blobs.
         *
         * @return this builder
         */
        public Builder withBlobStoreStrategy(BlobStoreStrategy blobStoreStrategy) {
            this.blobStoreStrategy = blobStoreStrategy;
            return this;
        }

        /**
         * Choose the key value store to use for JSON documents.
         *
         * @return this builder
         */
        public Builder withJsonStoreStrategy(JsonStoreStrategy jsonStoreStrategy) {
            this.jsonStoreStrategy = jsonStoreStrategy;
            return this;
        }

        /**
         * Build the stack model
         *
         * @return an immutable stack model
         */
        public StackModel build() {
            if (telemetryProcessingEnabled) {
                // Enable Elasticsearch when telemetry/flows are enabled
                elasticsearchEnabled = true;
                // If Sentinels are being used, then enable Newts
                if (!sentinels.isEmpty()) {
                    timeSeriesStrategy = TimeSeriesStrategy.NEWTS;
                }
            }
            return new StackModel(this);
        }

    }

    public OpenNMSProfile getOpenNMS() {
        return opennms;
    }

    public List<MinionProfile> getMinions() {
        return minions;
    }

    public List<SentinelProfile> getSentinels() {
        return sentinels;
    }

    public boolean isJaegerEnabled() {
        return jaegerEnabled;
    }

    public boolean isElasticsearchEnabled() {
        return elasticsearchEnabled;
    }

    public boolean isTelemetryProcessingEnabled() {
        return telemetryProcessingEnabled;
    }

    public boolean isSimulateRestricedOpenShiftEnvironment() {
        return simulateRestricedOpenShiftEnvironment;
    }

    public IpcStrategy getIpcStrategy() {
        return ipcStrategy;
    }

    public TimeSeriesStrategy getTimeSeriesStrategy() {
        return timeSeriesStrategy;
    }

    public BlobStoreStrategy getBlobStoreStrategy() {
        return blobStoreStrategy;
    }

    public JsonStoreStrategy getJsonStoreStrategy() {
        return jsonStoreStrategy;
    }

    public KafkaCompressionStrategy getKafkaCompressionStrategy() { return kafkaCompressionStrategy; }
}