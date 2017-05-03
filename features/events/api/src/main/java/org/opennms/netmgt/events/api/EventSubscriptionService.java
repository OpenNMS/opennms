/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api;

import java.util.Collection;


/**
 * <p>EventSubscriptionService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventSubscriptionService {
    
    /**
     * Registers an event listener that is interested in all events
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     */
    public void addEventListener(EventListener listener);

    /**
     * Registers an event listener interested in the UEIs in the passed list
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void addEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Registers an event listener interested in the passed UEI
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void addEventListener(EventListener listener, String uei);

    /**
     * Removes a registered event listener
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     */
    public void removeEventListener(EventListener listener);

    /**
     * Removes a registered event listener - the UEI list indicates the list of
     * events the listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void removeEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Removes a registered event listener - the UEI indicates an event the
     * listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void removeEventListener(EventListener listener, String uei);

    /**
     * Checks if there is at least one listener for the given uei.
     *
     * @param uei the uie to check for
     *
     * @return {@code true} iff there is at least one listener
     */
    public boolean hasEventListener(final String uei);


}
