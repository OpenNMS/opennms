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

/**
 * ServiceDetector
 * 
 * Note: the isServiceDetected method is not defined here because there is
 * a synchronous version of the method and an asynchronous one that are defined
 * in sub interfaces.  This interface is used for the configuration so all service
 * detectors can be found since the would all be initialized and configured the same
 * way.
 *
 * @author <a href="mailto:brozow@opennms.org>Mathew Brozowski</a>
 * @version $Id: $
 */
public interface ServiceDetector {
    
    /**
     * Perform any necessary initialization after construction and before detecting.
     */
    void init();
    
    /**
     * Requires that all implementations of this API return a service name.
     *
     * @return a {@link java.lang.String} object.
     */
    String getServiceName();

    /**
     * Service name is mutable so that we can create new instances of each implementation
     * and define a new service detector using the underlying protocol.
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    void setServiceName(String serviceName);

    /**
     * Get the port where this service will be detected.
     */
    int getPort();

    /**
     * Set the port where the service will be detected.
     *
     * @param port
     */
    void setPort(int port);

    /**
     * Get the timeout for detecting the service.
     */
    int getTimeout();

    /**
     * Set the timeout for detecting the service.
     *
     * @param port
     */
    void setTimeout(int timeout);

    /**
     * Get the IPLIKE rule for detecting the service.
     */
    String getIpMatch();

    /**
     * Set the IPLIKE rule for detecting the service.
     */
    void setIpMatch(String ipMatch);

    /**
     * The detector should clean up after itself in this method if necessary.
     */
    void dispose();

}
