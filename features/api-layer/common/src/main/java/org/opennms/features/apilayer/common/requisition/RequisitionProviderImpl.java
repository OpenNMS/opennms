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
package org.opennms.features.apilayer.common.requisition;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.mapstruct.factory.Mappers;
import org.opennms.features.apilayer.common.requisition.mappers.RequisitionMapper;
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
