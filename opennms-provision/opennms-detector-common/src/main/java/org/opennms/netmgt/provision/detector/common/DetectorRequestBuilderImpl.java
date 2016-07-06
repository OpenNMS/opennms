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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DetectorRequestBuilderImpl implements DetectorRequestBuilder {

    private String location;

    private List<String> attributes;

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
    public DetectorRequestBuilder withAttributes(List<String> attributes) {
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
    public CompletableFuture<Boolean> execute() {
        final DetectorRequestDTO detectorRequestDTO = new DetectorRequestDTO();
        detectorRequestDTO.setAddress(address);
        detectorRequestDTO.setProperties(attributes);
        detectorRequestDTO.setLocation(location);
        detectorRequestDTO.setServiceName(service);
        try {
            return client.getDetectorRequestExecutor(location).execute(detectorRequestDTO).thenApply(res -> processResponse(res));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    boolean processResponse(DetectorResponseDTO response) {

        return response.isDetected();
    }

  

}
