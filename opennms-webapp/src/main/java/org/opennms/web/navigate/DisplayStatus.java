/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.navigate;

/**
 * <p>DisplayStatus class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public enum DisplayStatus {
    NO_DISPLAY(false, false), DISPLAY_NO_LINK(true, false), DISPLAY_LINK(true, true);
    
    private boolean m_display;
    private boolean m_displayLink;
    
    DisplayStatus(boolean display, boolean displayLink) {
        m_display = display;
        m_displayLink = displayLink;
    }

    /**
     * <p>isDisplay</p>
     *
     * @return a boolean.
     */
    public boolean isDisplay() {
        return m_display;
    }

    /**
     * <p>isDisplayLink</p>
     *
     * @return a boolean.
     */
    public boolean isDisplayLink() {
        return m_displayLink;
    }
}
