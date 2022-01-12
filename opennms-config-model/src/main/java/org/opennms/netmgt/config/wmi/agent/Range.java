/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.wmi.agent;

import org.opennms.netmgt.config.utils.ConfigUtils;

import java.io.Serializable;
import java.util.Objects;

public class Range implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Starting IP address of the range.
     */
    private String begin;

    /**
     * Ending IP address of the range.
     */
    private String end;

    public Range() {
    }

    public String getBegin() {
        return this.begin;
    }

    public void setBegin(final String begin) {
        this.begin = ConfigUtils.assertNotEmpty(begin, "begin");
    }

    public String getEnd() {
        return this.end;
    }

    public void setEnd(final String end) {
        this.end = ConfigUtils.assertNotEmpty(end, "end");
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Range) {
            final Range that = (Range) obj;
            return Objects.equals(this.begin, that.begin)
                    && Objects.equals(this.end, that.end);
        }
        return false;
    }
}
