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

package org.opennms.netmgt.threshd.api;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * Thresholding API Service.
 */
public interface ThresholdingService {

    /**
     * Creates a session to perform Thresholding against. 
     * 
     * The Session is keyed by the combination of nodeId, hostAddress and serviceName.
     * 
     * @param nodeId
     *            The Node Id.
     * @param hostAddress
     *            The Host IP Address.
     * @param serviceName
     *            The Service name.
     * @param rrdRepository
     *            Must not be null. Will be used to resolve Resource Filters and for genertaing Event labels.
     * @param serviceParameters
     *            Must not be null. Required by some existing {@link CollectionResource} objects to evaluate whether to apply thresholds when accepting a {@link CollectionSet}. 
     *            If your {@link CollectionResource} does not require this, pass an empty {@link ServiceParameters} object.
     * @return A {@link ThresholdingSession}
     * @throws ThresholdInitializationException
     *             if there is an error creating the {@link ThresholdingSession} because of invalid Thresholding Configuration.
     */
    ThresholdingSession createSession(int nodeId, String hostAddress, String serviceName, RrdRepository rrdRepository, ServiceParameters serviceParameters)
            throws ThresholdInitializationException;

    ThresholdingSetPersister getThresholdingSetPersister();
}
