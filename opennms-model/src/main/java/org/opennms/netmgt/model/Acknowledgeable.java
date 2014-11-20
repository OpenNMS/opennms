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

package org.opennms.netmgt.model;

import java.util.Date;

/**
 * Entities that have the capability of being acknowledge should implement this interface for
 * Ackd acknowledgment behavior.
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface Acknowledgeable {
    
    /**
     * <p>acknowledge</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void acknowledge(String ackUser);
    /**
     * <p>unacknowledge</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void unacknowledge(String ackUser);
    /**
     * <p>clear</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void clear(String ackUser);
    /**
     * <p>escalate</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void escalate(String ackUser);
    
    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.model.AckType} object.
     */
    AckType getType();

    /**
     * <p>getAckId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getAckId();

    /**
     * <p>getAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getAckUser();

    /**
     * <p>getAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getAckTime();
    
    /**
     * Might be null but probably supported already by most implementations, but still, here for convenience.  Also
     * guarantees that this is available in this API if the model changes where the node is not directly related and de-facto
     * support is removed.
     *
     * @return the related OnmsNode, null if non available or doesn't make sense
     */
    OnmsNode getNode();
    
}
