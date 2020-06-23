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
