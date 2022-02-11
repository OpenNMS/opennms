/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmp;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * IP Address Range
 */

@XmlRootElement(name="range")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"begin", "end"})
@JsonPropertyOrder({"begin","end"})
/**
 * Keep the XML annotation is due to existing UI still using xml output
 */
public class Range implements Serializable {
    private static final long serialVersionUID = 3817543154652004131L;

    /**
     * Starting IP address of the range.
     */
    @XmlAttribute(name="begin", required=true)
    private String begin;

    /**
     * Ending IP address of the range.
     */
    @XmlAttribute(name="end", required=true)
    private String end;

    public Range() {
        super();
    }

    public Range(final String begin, final String end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Starting IP address of the range.
     */
    public String getBegin() {
        return this.begin;
    }

    public void setBegin(final String begin) {
        this.begin = begin;
    }

    /**
     * Ending IP address of the range.
     */
    public String getEnd() {
        return this.end;
    }

    public void setEnd(final String end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.begin == null) ? 0 : this.begin.hashCode());
        result = prime * result + ((this.end == null) ? 0 : this.end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Range)) {
            return false;
        }
        Range other = (Range) obj;
        if (this.begin == null) {
            if (other.begin != null) {
                return false;
            }
        } else if (!this.begin.equals(other.begin)) {
            return false;
        }
        if (this.end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!this.end.equals(other.end)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Range [begin=" + this.begin + ", end=" + this.end + "]";
    }
}
