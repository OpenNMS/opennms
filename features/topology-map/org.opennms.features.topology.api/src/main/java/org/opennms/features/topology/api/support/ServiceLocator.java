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

package org.opennms.features.topology.api.support;

import org.opennms.features.topology.api.topo.MetaTopologyProvider;

import java.util.List;
import java.util.function.Predicate;

/**
 * Helper interface to lookup all available {@link MetaTopologyProvider}s.
 * This should make tests easier.
 */
public interface ServiceLocator {

    /**
     * Finds a service registered with the OSGI Service Registry of type <code>clazz</code>.
     * If a <code>bundleContextFilter</code> is provided, it is used to query for the service, e.g. "(operation.label=My Label*)".
     * In addition each clazz of type T found in the OSGI Service Registry must afterwards pass the provided <code>postFilter</code>.
     *
     * If multiple services are found, only the first one is returned.
     *
     * @return A object of type <code>clazz</code> or null.
     */
    <T> T findSingleService(Class<T> clazz, Predicate<T> postFilter, String bundleContextFilter);

    /**
     * Find services of class <code>clazz</code> registered in the OSGI Service Registry.
     * The optional filter criteria <code>query</code> is used.
     *
     * @return All found services registered in the OSGI Service Registry of type <code>clazz</code>.
     */
    <T> List<T> findServices(Class<T> clazz, String query);
}
