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
