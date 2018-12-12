/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.vaadin.user;

import java.time.ZoneId;

import org.opennms.core.time.CentralizedDateTimeFormat;

import com.vaadin.ui.UI;

public final class UserTimeZoneExtractor {

    public static ZoneId extractUserTimeZoneIdOrNull(final UI ui) {
        // Verify if a ui is provided, still attached and has a session
        if (ui != null && ui.isAttached() && ui.getSession() != null && ui.getSession().getSession() != null) {
            // Only the wrapped session has the attribute set, the VaadinSession does not!
            return (ZoneId) ui.getSession().getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);
        }
        return null;
    }
}
