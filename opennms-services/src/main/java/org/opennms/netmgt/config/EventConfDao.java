/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.dao.DataAccessException;

public interface EventConfDao {

    /**
     * 
     */
    public abstract void reload() throws DataAccessException;

    /**
     * 
     */
    public abstract List<Event> getEvents(String uei);

    /**
     */
    public abstract List<String> getEventUEIs();

    /**
     */
    public abstract Map<String, String> getEventLabels();

    /**
     */
    public abstract String getEventLabel(String uei);

    /**
     * 
     */
    public abstract void saveCurrent();

    public abstract List<Event> getEventsByLabel();

    /**
     * Adds the event to the root level event config storage (file).
     * Does not save (you must save independently with saveCurrent)
     * 
     * @param event The fully configured Event object to add.  
     */
    public abstract void addEvent(Event event);

    /**
     * Adds the given event to the programmatic event store.  This store currently implemented as a file (referenced from eventconf.xml)
     * The programmatic store is a separate storage area, so that incidental programmatic editing of events (e.g. custom UEIs for thresholds, edited 
     * through the Web-UI) does not clutter up the otherwise carefully maintained event files.  This method does not save (persist) the changes
     * 
     * @param event The fully configured Event object to add.
     */
    public abstract void addEventToProgrammaticStore(Event event);

    /**
     * Removes the given event from the programmatic event store.  This store currently implemented as a file (referenced from eventconf.xml)
     * The programmatic store is a separate storage area, so that incidental programmatic editing of events (e.g. custom UEIs for thresholds, edited 
     * through the Web-UI) does not clutter up the otherwise carefully maintained event files.  This method does not save (persist) the changes
     * 
     * @param event The fully configured Event object to remove.
     * @returns true if the event was removed, false if it wasn't found (either not in the programmatic store, or the store didn't exist)
     */
    public abstract boolean removeEventFromProgrammaticStore(Event event);

    public abstract boolean isSecureTag(String tag);

    public abstract Event findByUei(String uei);

    public abstract Event findByEvent(org.opennms.netmgt.xml.event.Event matchingEvent);

}