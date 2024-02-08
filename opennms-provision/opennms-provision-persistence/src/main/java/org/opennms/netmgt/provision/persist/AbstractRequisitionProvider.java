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
package org.opennms.netmgt.provision.persist;

import java.util.Objects;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public abstract class AbstractRequisitionProvider<T extends RequisitionRequest> implements RequisitionProvider {

    private final Class<T> clazz;

    public AbstractRequisitionProvider(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz);
    }

    public abstract Requisition getRequisitionFor(T request);

    @Override
    public Requisition getRequisition(RequisitionRequest request) {
        if (request == null || !(clazz.isAssignableFrom(request.getClass()))) {
            throw new IllegalArgumentException("Invalid request: " + request);
        }
        return getRequisitionFor(clazz.cast(request));
    }

    @Override
    public String marshalRequest(RequisitionRequest request) {
        return JaxbUtils.marshal(request);
    }

    @Override
    public RequisitionRequest unmarshalRequest(String marshaledRequest) {
        return JaxbUtils.unmarshal(clazz, marshaledRequest);
    }

}
