/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.api.receiver;

import java.nio.ByteBuffer;

/**
 * A listener may define multiple parsers, in order to dispatch it to only one queue,
 * the parser must decide if it can handle the incoming data.
 *
 * A parser implementing the {@link Dispatchable} interface is capable of making this decision.
 *
 * @author mvrueden
 */
public interface Dispatchable {

    /**
     * Returns true if the implementor can handle the incoming data, otherwise false.
     *
     * @param buffer Representing the incoming data
     * @return true if the implementor can handle the data, otherwise false.
     */
    boolean handles(final ByteBuffer buffer);
}
