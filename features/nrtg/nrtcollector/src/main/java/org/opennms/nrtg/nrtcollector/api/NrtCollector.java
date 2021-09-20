/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.nrtcollector.api;

import org.opennms.nrtg.api.model.CollectionJob;

/**
 * Technology free definition of a collection satellite.
 * <p>
 * a collection satellite receives {@link CollectionJob} from a collection
 * commander and collects the data for them. The result should be send back to
 * the commander via the same technology the job was received.
 * </p>
 *
 * @author Simon Walter
 */
public interface NrtCollector {

    /**
     * Starts the satellite. Must return after initialization and start.
     */
    void start();

    /**
     * Tells the main thread if the satellite instance is terminated.
     *
     * @return
     */
    boolean terminated();

    /**
     * Stops the nrtcollector and cleans up all the resources
     */
    void stop();
}
