//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 06: Pull DAO-ish calls into EventConfDao. - dj@opennms.org
// 2008 Jan 06: Duplicate all EventConfigurationManager functionality in
//              EventconfFactory. - dj@opennms.org
// 2008 Jan 05: Add a few new constructors and make them all public,
//              eliminate static fields except for s_instance. - dj@opennms.org
// 2008 Jan 05: Simplify init()/reload()/getInstance(). - dj@opennms.org
// 2008 Jan 05: Organize imports, format code, refactor some, and line up some
//              functionality with EventConfigurationManager. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 29: Added include files for eventconf.xml
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.dao.DataAccessException;

public interface EventConfDao {

    /**
     * 
     */
    void reload() throws DataAccessException;

    /**
     * 
     */
    List<Event> getEvents(String uei);

    /**
     */
    List<String> getEventUEIs();

    /**
     */
    Map<String, String> getEventLabels();

    /**
     */
    String getEventLabel(String uei);

    /**
     * 
     */
    void saveCurrent();

    List<Event> getEventsByLabel();

    /**
     * Adds the event to the root level event config storage (file).
     * Does not save (you must save independently with saveCurrent)
     * 
     * @param event The fully configured Event object to add.  
     */
    void addEvent(Event event);

    /**
     * Adds the given event to the programmatic event store.  This store currently implemented as a file (referenced from eventconf.xml)
     * The programmatic store is a separate storage area, so that incidental programmatic editing of events (e.g. custom UEIs for thresholds, edited 
     * through the Web-UI) does not clutter up the otherwise carefully maintained event files.  This method does not save (persist) the changes
     * 
     * @param event The fully configured Event object to add.
     */
    void addEventToProgrammaticStore(Event event);

    /**
     * Removes the given event from the programmatic event store.  This store currently implemented as a file (referenced from eventconf.xml)
     * The programmatic store is a separate storage area, so that incidental programmatic editing of events (e.g. custom UEIs for thresholds, edited 
     * through the Web-UI) does not clutter up the otherwise carefully maintained event files.  This method does not save (persist) the changes
     * 
     * @param event The fully configured Event object to remove.
     * @returns true if the event was removed, false if it wasn't found (either not in the programmatic store, or the store didn't exist)
     */
    boolean removeEventFromProgrammaticStore(Event event);

    boolean isSecureTag(String tag);

    Event findByUei(String uei);

    Event findByEvent(org.opennms.netmgt.xml.event.Event matchingEvent);

}