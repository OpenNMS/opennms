/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.container.web.bridge.proxy;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.ServiceReference;

public final class Utils {

    private Utils() {}

    public static List<String> getListProperty(ServiceReference reference, String key) {
        final List<String> returnList = new ArrayList<>();
        final Object property = reference.getProperty(key);
        if (property instanceof String) {
            final String value = ((String) property).trim();
            if (value != null && !"".equals(property)) {
                returnList.add(value);
            }
        }
        return returnList;
    }

    public static String getStringProperty(ServiceReference reference, String key) {
        final Object property = reference.getProperty(key);
        if (property instanceof String) {
            return ((String) property).trim();
        }
        return null;
    }
}
