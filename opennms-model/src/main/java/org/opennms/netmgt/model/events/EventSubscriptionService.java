/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.events;

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
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     */
    public void addEventListener(EventListener listener);

    /**
     * Registers an event listener interested in the UEIs in the passed list
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void addEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Registers an event listener interested in the passed UEI
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void addEventListener(EventListener listener, String uei);

    /**
     * Removes a registered event listener
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     */
    public void removeEventListener(EventListener listener);

    /**
     * Removes a registered event listener - the UEI list indicates the list of
     * events the listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void removeEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Removes a registered event listener - the UEI indicates an event the
     * listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void removeEventListener(EventListener listener, String uei);


}
