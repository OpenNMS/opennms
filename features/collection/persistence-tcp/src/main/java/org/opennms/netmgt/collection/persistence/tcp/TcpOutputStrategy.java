/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.tcp;

import java.util.List;

/**
 * Defines an abstract strategy for manipulating TCP output socket.
 */
public interface TcpOutputStrategy {

    /**
     * Updates the TCP output stream with the supplied values
     *
     * @param path
     *            the hierarchy path for the data
     * @param owner
     *            the owner of the data
     * @paeam time
     *            the timestamp for the data
     * @param dblValues
     *            a list of Double values
     * @param strValues
     *            a list of String values
     * @throws java.lang.Exception
     *             if an error occurs updating the file
     */
    public void updateData(String path, String owner, Long timestamp, List<Double> dblValues, List<String> strValues) throws Exception;
}
