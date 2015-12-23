/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api;

/**
 * North bound Interface API Exception
 * <p>Intention is to wrap all throwables as a Runtime Exception.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class NorthbounderException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new northbounder exception.
     *
     * @param t the throwable
     */
    public NorthbounderException(Throwable t) {
        super(t);
    }

    /**
     * Instantiates a new northbounder exception.
     *
     * @param message the message
     */
    public NorthbounderException(String message) {
        super(message);
    }

    /**
     * Instantiates a new northbounder exception.
     *
     * @param message the message
     * @param t the t
     */
    public NorthbounderException(String message, Throwable t) {
        super(message, t);
    }

}
