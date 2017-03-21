/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api;

/**
 * A common interface for all elements which now something about a namespace.
*/
public interface NamespaceAware {

    /**
     * A string used to identify references belonging to this provider
     *
     * May only contain characters that make for a reasonable Java identifier
     * such as letters digits and underscore (no colons, periods, commas etc.)
     *
     * @return the namespace, e.g. "nodes".
     */
    String getNamespace();

    /**
     * Defines if the current implementation contributes to the given namespace.
     * This is usually the case, when the given namespaces equals {@link #getNamespace()}, but is not a requirement.
     *
     * @param namespace the namespace to contribute to
     * @return <code>true</code>, if the current implementation contributes to the given namespace, otherwise <code>false</code>.
     */
    boolean contributesTo(String namespace);
}
