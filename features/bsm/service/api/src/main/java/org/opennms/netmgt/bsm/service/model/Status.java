/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
