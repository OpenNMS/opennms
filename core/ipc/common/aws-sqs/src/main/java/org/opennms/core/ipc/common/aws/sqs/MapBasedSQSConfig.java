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

package org.opennms.core.ipc.common.aws.sqs;

import com.amazonaws.regions.Regions;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SQS configuration from a map of values.
 *
 * The associated keys are defined in {@link AmazonSQSConstants}.
 *
 */
public class MapBasedSQSConfig implements AmazonSQSConfig {
    public static final String DEFAULT_REGION = Regions.US_EAST_1.getName();
    private final String queuePrefix;
    private final Regions region;
    private final String accessKey;
    private final String secretKey;
    private final boolean useHttp;
    private final AmazonSQSQueueConfig sinkQueueConfig;
    private final AmazonSQSQueueConfig rpcQueueConfig;

    public MapBasedSQSConfig() {
        this(getConfigMapFromSystemProperties());
    }

    public MapBasedSQSConfig(Map<String,String> sqsConfig) {
        queuePrefix = sqsConfig.get(AmazonSQSConstants.AWS_QUEUE_NAME_PREFIX);
        region = Regions.fromName(sqsConfig.getOrDefault(AmazonSQSConstants.AWS_REGION, DEFAULT_REGION));
        accessKey = sqsConfig.get(AmazonSQSConstants.AWS_ACCESS_KEY_ID);
        secretKey = sqsConfig.get(AmazonSQSConstants.AWS_SECRET_ACCESS_KEY);
        useHttp = Boolean.TRUE.toString().equals(sqsConfig.get(AmazonSQSConstants.AWS_USE_HTTP));
        sinkQueueConfig = new AmazonSQSQueueConfig(filterKeysByPrefix(sqsConfig, AmazonSQSConstants.SINK_QUEUE_PROP_PREFIX));
        rpcQueueConfig = new AmazonSQSQueueConfig(filterKeysByPrefix(sqsConfig, AmazonSQSConstants.RPC_QUEUE_PROP_PREFIX));
    }

    protected static Map<String, String> getConfigMapFromSystemProperties() {
        return filterKeysByPrefix(System.getProperties().entrySet(), AmazonSQSConstants.AWS_CONFIG_SYS_PROP_PREFIX);
    }

    @Override
    public String getQueuePrefix() {
        return queuePrefix;
    }

    @Override
    public Regions getRegion() {
        return region;
    }

    @Override
    public boolean hasStaticCredentials() {
        return accessKey != null && secretKey != null;
    }

    @Override
    public String getAccessKey() {
        return accessKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public boolean isUseHttp() {
        return useHttp;
    }

    @Override
    public AmazonSQSQueueConfig getSinkQueueConfig() {
        return sinkQueueConfig;
    }

    @Override
    public AmazonSQSQueueConfig getRpcQueueConfig() {
        return rpcQueueConfig;
    }

    @Override
    public String toString() {
        return "MapBasedSQSConfig{" +
                "queuePrefix='" + queuePrefix + '\'' +
                ", region='" + region + '\'' +
                ", accessKey='" + accessKey != null ? "********" : accessKey + '\'' +
                ", secretKey='" + secretKey != null ? "********" : secretKey + '\'' +
                ", useHttp=" + useHttp +
                ", sinkQueueConfig=" + sinkQueueConfig +
                ", rpcQueueConfig=" + rpcQueueConfig +
                '}';
    }

    private static Map<String, String> filterKeysByPrefix(Map<String, String> map, String prefix) {
        return filterKeysByPrefix(map.entrySet(), prefix);
    }

    private static <K,V> Map<String, String> filterKeysByPrefix(Set<Map.Entry<K, V>> entrySet, String prefix) {
        return entrySet.stream()
                // Filter out invalid keys and/or values
                .filter(e -> e.getKey() != null && e.getKey() instanceof String)
                .filter(e -> e.getValue() == null || e.getValue() instanceof String)
                .map(e -> (Map.Entry<String, String>)e)
                // Extract the keys from the map that are prefixed
                .filter(e -> e.getKey().startsWith(prefix))
                // Remove the prefix, and collect the results back into a map
                .collect(Collectors.toMap(e -> e.getKey().substring(prefix.length(), e.getKey().length()),
                        e -> e.getValue()));
    }
}
