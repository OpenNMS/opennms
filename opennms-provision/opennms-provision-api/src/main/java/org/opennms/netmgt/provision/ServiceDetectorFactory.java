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

import org.opennms.netmgt.model.OnmsNode;

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
     * Instantiates a new detector and set bean properties.
     * One of the ways to set bean properties is using Spring @{@link org.springframework.beans.BeanWrapper}
     * <pre>
     * {@code
     *         BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(serviceDetector);
     *         wrapper.setPropertyValues(properties);
     * }
     * </pre>
     * Detectors are treated as protoypes and should only be used for a
     * single call to "isServiceDetected".
     *
     * @param properties  are used to set properties on detector bean.
     */
    T createDetector(Map<String, String> properties);

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
     * Optional implementation.
     * @param request {@link DetectRequest}
     * @param results {@link DetectResults}
     * @param nodeId  {@link OnmsNode#getNodeId()}
     */
    void afterDetect(DetectRequest request, DetectResults results, Integer nodeId);

}
