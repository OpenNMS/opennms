/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;

/**
 * <p>EventTranslatorConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventTranslatorConfig {
	
    static final String TRANSLATOR_NAME = "event-translator";

    /**
     * Get the list of UEIs that are registered in the passive status configuration.
     *
     * @return list of UEIs
     */
    List<String> getUEIList();
    
    /**
     * Determine if the @param e is a translation event
     *
     * @param e Event
     * @return true if e is a translation event
     */
    boolean isTranslationEvent(Event e);

	/**
	 * Translate the @param e to a new event
	 *
	 * @param e Event
	 * @return a translated event
	 */
	List<Event> translateEvent(Event e);
	
	/**
	 * <p>update</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	void update() throws Exception;

}
