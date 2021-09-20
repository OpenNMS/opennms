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

package org.opennms.features.apilayer.requisition;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.mapstruct.factory.Mappers;
import org.opennms.features.apilayer.requisition.mappers.RequisitionMapper;
import org.opennms.integration.api.v1.requisition.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionRequest;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class RequisitionProviderImpl implements org.opennms.netmgt.provision.persist.RequisitionProvider {
    private static final RequisitionMapper MAPPER = Mappers.getMapper(RequisitionMapper.class);

    private final RequisitionProvider delegate;

    public RequisitionProviderImpl(RequisitionProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public RequisitionRequest getRequest(Map<String, String> parameters) {
        // Delegate and wrap the request
        return new WrappedRequisitionRequest(delegate.getRequest(parameters));
    }

    @Override
    public Requisition getRequisition(RequisitionRequest request) {
        final org.opennms.integration.api.v1.requisition.RequisitionRequest apiRequest = getRequestFromWrapper(request);
        return MAPPER.map(delegate.getRequisition(apiRequest));
    }

    @Override
    public String marshalRequest(RequisitionRequest request) {
        final org.opennms.integration.api.v1.requisition.RequisitionRequest apiRequest = getRequestFromWrapper(request);
        return new String(delegate.marshalRequest(apiRequest), StandardCharsets.UTF_8);
    }

    @Override
    public RequisitionRequest unmarshalRequest(String marshaledRequest) {
        final byte[] bytes = marshaledRequest.getBytes(StandardCharsets.UTF_8);
        // Unmarshal and wrap the request
        return new WrappedRequisitionRequest(delegate.unmarshalRequest(bytes));
    }

    private static org.opennms.integration.api.v1.requisition.RequisitionRequest getRequestFromWrapper(RequisitionRequest request) {
        if (!(request instanceof WrappedRequisitionRequest)) {
            throw new IllegalArgumentException("Given request must be one returned by getRequest(), but got: " + request);
        }
        final WrappedRequisitionRequest wrappedRequest = (WrappedRequisitionRequest)request;
        return wrappedRequest.getRequest();
    }

    private static class WrappedRequisitionRequest implements RequisitionRequest {
        private final org.opennms.integration.api.v1.requisition.RequisitionRequest request;

        public WrappedRequisitionRequest(org.opennms.integration.api.v1.requisition.RequisitionRequest request) {
            this.request = Objects.requireNonNull(request);
        }

        public org.opennms.integration.api.v1.requisition.RequisitionRequest getRequest() {
            return request;
        }
    }
}
