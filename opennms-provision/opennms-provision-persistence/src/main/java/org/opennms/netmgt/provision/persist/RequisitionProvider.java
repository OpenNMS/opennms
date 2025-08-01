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

import java.util.Map;

import org.opennms.netmgt.provision.persist.requisition.Requisition;

/**
 * Used to generate {@link Requisition}s from some source.
 *
 * @author jwhite
 */
public interface RequisitionProvider {

    /**
     * A string used to identify the provider type.
     *
     * This string should be unique for every implementation.
     *
     * @return the type string
     */
    String getType();

    /**
     * Generate a request.
     *
     * @param parameters
     * @return
     */
    RequisitionRequest getRequest(Map<String, String> parameters);

    /**
     * Execute the request.
     *
     * @param request
     * @return
     */
    Requisition getRequisition(RequisitionRequest request);

    /**
     * Marshals the request to a {@link java.lang.String}.
     *
     * Used before sending the request to a Minion.
     *
     * @param request
     * @return
     */
    String marshalRequest(RequisitionRequest request);

    /**
     * Unmarshals the request from a {@link java.lang.String}.
     *
     * Used when received the request on a Minion.
     *
     * @param marshaledRequest
     * @return
     */
    RequisitionRequest unmarshalRequest(String marshaledRequest);

}
