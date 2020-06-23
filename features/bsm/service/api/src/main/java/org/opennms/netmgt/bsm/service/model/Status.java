/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.model;

import java.io.Serializable;
import java.util.Arrays;

public enum Status implements Serializable {
    INDETERMINATE("Indeterminate"),
    NORMAL("Normal"),
    WARNING("Warning"),
    MINOR("Minor"),
    MAJOR("Major"),
    CRITICAL("Critical");

    private String m_label;

    Status(final String label) {
        m_label = label;
    }

    public String getLabel() {
        return m_label;
    }

    public boolean isLessThan(final Status other) {
        return compareTo(other) < 0;
    }

    public boolean isLessThanOrEqual(final Status other) {
        return compareTo(other) <= 0;
    }

    public boolean isGreaterThan(final Status other) {
        return compareTo(other) > 0;
    }

    public boolean isGreaterThanOrEqual(final Status other) {
        return compareTo(other) >= 0;
    }

    public int getId() {
        return ordinal();
    }

    public static Status get(int ordinal) {
        for (Status eachStatus : values()) {
            if (eachStatus.ordinal() == ordinal) {
                return eachStatus;
            }
        }
        throw new IllegalArgumentException("Cannot create Status from unknown ordinal " + ordinal);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public static Status of(String input) {
        for (Status eachStatus : values()) {
            if (eachStatus.name().equalsIgnoreCase(input)) {
                return eachStatus;
            }
        }
        throw new IllegalArgumentException("Cannot create Status from unknown name '" + input + "'. Supported values are " + Arrays.toString(values()));
    }
}
