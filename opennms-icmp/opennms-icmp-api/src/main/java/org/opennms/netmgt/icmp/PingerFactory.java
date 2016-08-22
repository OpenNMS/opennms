/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp;

public interface PingerFactory {
    public static final int MAX_DSCP = (1 << 16) - 1;
    public static final int FRAG_FALSE = 1;
    public static final int FRAG_TRUE = 2;

    /**
     * Returns an implementation of the default {@link Pinger} class
     *
     * @param tc the traffic control value to set, use "0" for none
     * @param allowFragmentation whether to allow fragmentation
     *
     * @return a {@link Pinger} object.
     */
    public Pinger getInstance();

    /**
     * Returns an implementation of the {@link Pinger} class associated with the
     * socket configured for the given traffic control and fragmentation bits.
     *
     * @param tc the traffic control value to set, use "0" for none
     * @param allowFragmentation whether to allow fragmentation
     *
     * @return a {@link Pinger} object.
     */
    public Pinger getInstance(final int tc, final boolean allowFragmentation);
}
