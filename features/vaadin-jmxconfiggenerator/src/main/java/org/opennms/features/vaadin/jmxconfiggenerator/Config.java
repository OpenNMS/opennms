/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

/**
 * Config class.
 *
 * @author Markus von Rüden
 */
public interface Config {

    int ATTRIBUTES_ALIAS_MAX_LENGTH = 19;
    int CONFIG_SNIPPET_MAX_LINES = 2500;

    /**
     * This class provides the application with icons. If any icon changes or new
     * icons are needed, please put them below.
     *
     * @author Markus von Rüden
     */
    interface Icons {
        Resource DUMMY = FontAwesome.SQUARE;
       	Resource BUTTON_SAVE = FontAwesome.FLOPPY_O;
        Resource BUTTON_NEXT = FontAwesome.CHEVRON_RIGHT;
        Resource BUTTON_PREVIOUS = FontAwesome.CHEVRON_LEFT;
		Resource SELECTED = FontAwesome.CHECK_SQUARE_O;
		Resource NOT_SELECTED = FontAwesome.SQUARE_O;
        Resource HELP = FontAwesome.QUESTION;
    }
}
