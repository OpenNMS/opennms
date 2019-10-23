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

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.xml.event.Event;

public interface ThresholdingSession extends AutoCloseable {

    /**
     * Accepts a {@link CollectionSet} for threshold evaluation. The service will send {@link Event}s if Thresholds are triggered or re-armed.
     * 
     * @param collectionSet
     * @throws ThresholdInitializationException
     *             if the Thresholding Configuration has not yet been initialized ot there is an error initializing it. 
     *             I.E. reading as parsing the configuration files.
     */
    void accept(CollectionSet collectionSet) throws ThresholdInitializationException;

    ThresholdingSessionKey getKey();
    
    BlobStore getBlobStore();

    /**
     * @return true if we are thresholding in a distributed environment (i.e. Sentinel) false otherwise (i.e. OpenNMS)
     */
    boolean isDistributed();
    
    ThresholdStateMonitor getThresholdStateMonitor();
}
