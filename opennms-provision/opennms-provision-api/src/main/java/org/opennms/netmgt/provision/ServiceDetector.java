/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
