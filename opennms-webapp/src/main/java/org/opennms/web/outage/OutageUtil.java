/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.outage;

/**
 * <p>Abstract OutageUtil class.</p>
 *
 * @author ranger
 */
public abstract class OutageUtil {

    /**
     * Returns the icon to use for an outage, if no icon then it returns null.
     *
     * @param outage a {@link org.opennms.web.outage.Outage} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getStatusLabel(Outage outage) {
        if (outage == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String label = null;

        if (outage.getRegainedServiceTime() == null) {
            label = "DOWN";
        }

        return label;
    }

}
