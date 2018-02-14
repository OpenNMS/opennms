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

package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.Map;

/**
 * Responsible for instantiating detectors, gathering state information or agent specific details,
 * and optionally handling post-processing of the requests.
 *
 * @author jwhite
 *
 * @param <T> detector type
 */
public interface ServiceDetectorFactory<T extends ServiceDetector> {
    
    /**
     * Used by the detector registry to track and index the detector types.
     */
    Class<T> getDetectorClass();

    /**
     * Instantiates a new detector.
     *
     * Detectors are treated as protoypes and should only be used for a
     * single call to "isServiceDetected".
     *
     */
    T createDetector();

    /**
     * Builds the request that will be used to invoke the detector.
     *
     * @param location name of the location in which the detector will be invoked
     * @param address address of the agent against which the detector will be invoked
     * @param port port of the agent against which the detector will be invoked
     *
     * @return a new {@link DetectRequest}
     */
    DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes);

    /**
     * 
     * @param request
     * @param results
     * @param nodeId
     */
    void afterDetect(DetectRequest request, DetectResults results, Integer nodeId);
}
