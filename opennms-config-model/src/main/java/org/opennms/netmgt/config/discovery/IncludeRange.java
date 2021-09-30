/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.discovery;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "include-range")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("discovery-configuration.xsd")
public class IncludeRange implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The monitoring location where this include range
     *  will be executed.
     */
    @XmlAttribute(name = "location")
    private String m_location;

    /**
     * The number of times a ping is retried for this
     *  address range. If there is no response after the first ping to an
     *  address, it is tried again for the specified number of retries. This
     *  retry count overrides the default.
     */
    @XmlAttribute(name = "retries")
    private Integer m_retries;

    /**
     * The timeout on each poll for this address range. This
     *  timeout overrides the default.
     */
    @XmlAttribute(name = "timeout")
    private Long m_timeout;

    @XmlAttribute(name = "foreign-source")
    private String m_foreignSource;

    /**
     * Starting address of the range.
     */
    @XmlElement(name = "begin", required = true)
    private String m_begin;

    /**
     * Ending address of the range. If the starting
     *  address is greater than the ending address, they are
     *  swapped.
     */
    @XmlElement(name = "end", required = true)
    private String m_end;

    public IncludeRange() {
    }

    public IncludeRange(final String begin, final String end) {
        setBegin(begin);
        setEnd(end);
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(m_location);
    }

    public void setLocation(final String location) {
        m_location = ConfigUtils.normalizeString(location);
    }

    public Optional<Integer> getRetries() {
        return Optional.ofNullable( m_retries);
    }

    public void setRetries(final Integer retries) {
        m_retries = retries;
    }

    public Optional<Long> getTimeout() {
        return Optional.ofNullable(m_timeout);
    }

    public void setTimeout(final Long timeout) {
        if (timeout != null && timeout == 0) {
            throw new IllegalArgumentException("Can't have a 0 timeout!");
        }
        m_timeout = timeout;
    }

    public Optional<String> getForeignSource() {
        return Optional.ofNullable(m_foreignSource);
    }

    public void setForeignSource(final String foreignSource) {
        m_foreignSource = ConfigUtils.normalizeString(foreignSource);
    }

    public String getBegin() {
        return m_begin;
    }

    public void setBegin(final String begin) {
        m_begin = ConfigUtils.assertNotEmpty(begin, "begin");
    }

    public String getEnd() {
        return m_end;
    }

    public void setEnd(final String end) {
        m_end = ConfigUtils.assertNotEmpty(end, "end");
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_location, 
                            m_retries, 
                            m_timeout, 
                            m_foreignSource, 
                            m_begin, 
                            m_end);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof IncludeRange) {
            final IncludeRange temp = (IncludeRange)obj;
            return Objects.equals(temp.m_location, m_location)
                    && Objects.equals(temp.m_retries, m_retries)
                    && Objects.equals(temp.m_timeout, m_timeout)
                    && Objects.equals(temp.m_foreignSource, m_foreignSource)
                    && Objects.equals(temp.m_begin, m_begin)
                    && Objects.equals(temp.m_end, m_end);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IncludeRange [location=" + m_location + ", retries="
                + m_retries + ", timeout=" + m_timeout
                + ", foreignSource=" + m_foreignSource + ", begin="
                + m_begin + ", end=" + m_end + "]";
    }

}
