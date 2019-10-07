/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
    private final boolean elasticsearchEnabled;
    private final boolean telemetryProcessingEnabled;
    private final IpcStrategy ipcStrategy;
    private final TimeSeriesStrategy timeSeriesStrategy;
    private final BlobStoreStrategy blobStoreStrategy;
    private final JsonStoreStrategy jsonStoreStrategy;

    private StackModel(Builder builder) {
        // Profiles
        opennms = builder.opennms;
        minions = builder.minions;
        sentinels = builder.sentinels;

        // Flags
        elasticsearchEnabled = builder.elasticsearchEnabled;
        telemetryProcessingEnabled = builder.telemetryProcessingEnabled;

        // Enums
        ipcStrategy = builder.ipcStrategy;
        timeSeriesStrategy = builder.timeSeriesStrategy;
        blobStoreStrategy = builder.blobStoreStrategy;
        jsonStoreStrategy = builder.jsonStoreStrategy;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private OpenNMSProfile opennms = OpenNMSProfile.DEFAULT;
        private List<MinionProfile> minions = new LinkedList<>();
        private List<SentinelProfile> sentinels = new LinkedList<>();
        private boolean elasticsearchEnabled = false;
        private boolean telemetryProcessingEnabled = false;

        private IpcStrategy ipcStrategy = IpcStrategy.JMS;
        private TimeSeriesStrategy timeSeriesStrategy = TimeSeriesStrategy.RRD;
        
        private BlobStoreStrategy blobStoreStrategy = BlobStoreStrategy.NOOP;
        private JsonStoreStrategy jsonStoreStrategy;

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

    public boolean isElasticsearchEnabled() {
        return elasticsearchEnabled;
    }

    public boolean isTelemetryProcessingEnabled() {
        return telemetryProcessingEnabled;
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
}