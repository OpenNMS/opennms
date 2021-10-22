/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.backup.client;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opennms.features.backup.BackupRequestBuilder;
import org.opennms.features.backup.api.BackupAttributeDTO;
import org.opennms.features.backup.api.BackupRequestDTO;
import org.opennms.features.backup.api.Config;
import org.opennms.features.backup.api.ConfigType;

public class BackupRequestBuilderImpl implements BackupRequestBuilder {

    private final LocationAwareBackupClientImpl locationAwareBackupClient;

    private String location;
    private String host;
    private Long ttl;
    private Map<String,String> attributes = new LinkedHashMap<>();

    public BackupRequestBuilderImpl(LocationAwareBackupClientImpl locationAwareBackupClient) {
        this.locationAwareBackupClient = Objects.requireNonNull(locationAwareBackupClient);
    }

    @Override
    public BackupRequestBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public BackupRequestBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public BackupRequestBuilder withTimeToLive(Long ttlInMs) {
        this.ttl = ttlInMs;
        return this;
    }

    @Override
    public BackupRequestBuilder withAttribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    @Override
    public BackupRequestBuilder withAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public CompletableFuture<Config> execute() {
        if (host == null) {
            throw new IllegalArgumentException("host is required");
        }
        if (location == null) {
            throw new IllegalArgumentException("location is required");
        }

        BackupRequestDTO requestDTO = new BackupRequestDTO();
        requestDTO.setLocation(location);
        requestDTO.setHost(host);
        requestDTO.setAttributes(attributes.entrySet().stream()
                        .map(e -> new BackupAttributeDTO(e.getKey(), e.getValue()))
                                .collect(Collectors.toList()));
        return locationAwareBackupClient.execute(requestDTO).thenApply(response -> {
            Config config = new Config();
            config.setType(ConfigType.TEXT);
            if (response.getResponse() != null) {
                config.setData(response.getResponse().getBytes(StandardCharsets.UTF_8));
            }
            config.setRetrievedAt(new Date());
            return config;
        });
    }
}
