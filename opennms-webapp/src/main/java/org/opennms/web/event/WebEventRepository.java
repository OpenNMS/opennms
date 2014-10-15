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

package org.opennms.web.event;

import java.util.Date;

import org.opennms.web.event.filter.EventCriteria;

/**
 * <p>WebEventRepository interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebEventRepository {
    
    /**
     * <p>countMatchingEvents</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingEvents(EventCriteria criteria);
    
    /**
     * <p>countMatchingEventsBySeverity</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     * @return an array of int.
     */
    public abstract int[] countMatchingEventsBySeverity(EventCriteria criteria);
    
    /**
     * <p>getEvent</p>
     *
     * @param eventId a int.
     * @return a {@link org.opennms.web.event.Event} object.
     */
    public abstract Event getEvent(int eventId);
    
    /**
     * <p>getMatchingEvents</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     */
    public abstract Event[] getMatchingEvents(EventCriteria criteria);
    
    /**
     * <p>acknowledgeMatchingEvents</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     */
    public abstract void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria);
    
    /**
     * <p>acknowledgeAll</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     */
    public abstract void acknowledgeAll(String user, Date timestamp);
    
    /**
     * <p>unacknowledgeMatchingEvents</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     */
    public abstract void unacknowledgeMatchingEvents(EventCriteria criteria);
    
    /**
     * <p>unacknowledgeAll</p>
     */
    public abstract void unacknowledgeAll();
}
