/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.common;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DetectorRequestBuilderImpl implements DetectorRequestBuilder {

    private String location;

    private Map<String, String> attributes;

    private String address;

    private String service;

    private DelegatingLocationAwareDetectorClientImpl client;

    public DetectorRequestBuilderImpl(
            DelegatingLocationAwareDetectorClientImpl client) {
        super();
        this.client = client;
    }

    @Override
    public DetectorRequestBuilder atLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public DetectorRequestBuilder withAttributes(
            Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public DetectorRequestBuilder atAddress(String address) {
        this.address = address;
        return this;
    }

    @Override
    public DetectorRequestBuilder byService(String service) {
        this.service = service;
        return this;
    }

    @Override
    public CompletableFuture<DetectorResponseDTO> execute() {
        final DetectorRequestDTO detectorRequestDTO = new DetectorRequestDTO();
        final CompletableFuture<DetectorResponseDTO> future = new CompletableFuture<DetectorResponseDTO>();
        detectorRequestDTO.setAddress(address);
        detectorRequestDTO.setAttributes(attributes);
        detectorRequestDTO.setLocation(location);
        detectorRequestDTO.setServiceName(service);
        try {
            return client.getDetectorRequestExecutor(location).execute(detectorRequestDTO).thenApply(res -> processResponse(res));
        } catch (Exception e) {
            DetectorResponseDTO response = new DetectorResponseDTO();
            response.setDetected(false);
            response.setFailureMesage(e.getMessage());
            future.complete(response);
            return future;
        }

    }

    DetectorResponseDTO processResponse(DetectorResponseDTO response) {

        return response;
    }

}
