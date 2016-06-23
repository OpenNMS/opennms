/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.jmxconfig.query;

import javax.management.MBeanAttributeInfo;
import java.util.regex.Pattern;

/**
 * Represents a filter criteria to be used by the {@link MBeanServerQuery}.
 */
public class FilterCriteria {
    public String objectName;
    public String attributeName;

    FilterCriteria() {

    }

    public FilterCriteria(String objectName, String attributeName) {
        this.objectName = objectName;
        this.attributeName = attributeName;
    }

    public static FilterCriteria parse(String input) {
        FilterCriteria qd = new FilterCriteria();
        String[] split = input.split(":");
        if (split.length == 3) {
            qd.objectName = input.substring(0, input.lastIndexOf(":"));
            qd.attributeName = input.substring(input.lastIndexOf(":") + 1);
        } else {
            qd.objectName = input;
        }
        return qd;
    }

    public String toString() {
        if (attributeName != null) {
            return objectName + ":" + attributeName;
        }
        return objectName;
    }

    public boolean matches(MBeanAttributeInfo eachAttribute) {
        Pattern pattern = attributeName != null ? Pattern.compile(attributeName) : null;
        return pattern == null || pattern.matcher(eachAttribute.getName()).matches();
    }
}