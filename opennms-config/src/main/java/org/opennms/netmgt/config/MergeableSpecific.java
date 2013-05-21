/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.net.InetAddress;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.InetAddressUtils;

/**
 * This class is used as a wrapper object for the generated Specific class in the
 * config package.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public final class MergeableSpecific implements Comparable<String> {
    private String m_specific;
    private static final SpecificComparator m_comparator = new SpecificComparator();
    private byte[] m_value;

    /**
     * <p>Constructor for MergeableSpecific.</p>
     *
     * @param specific a {@link java.lang.String} object.
     */
    public MergeableSpecific(final String specific) {
        m_specific = specific;
        final InetAddress addr = InetAddressUtils.addr(specific);
        if (addr == null) {
        	throw new IllegalArgumentException("Unable to get InetAddress for " + specific);
        }
		m_value = addr.getAddress();
    }
    
    /**
     * Uses a comparable comparing to Specifics from the config package.
     *
     * @param specific a {@link java.lang.String} object.
     * @return a int.
     */
    @Override
    public int compareTo(String specific) {
        return m_comparator.compare(m_specific, specific);
    }
    
    @Override
    public boolean equals(final Object o) {
    	if (!(o instanceof MergeableSpecific)) return false;
    	final MergeableSpecific that = (MergeableSpecific)o;
    	return new EqualsBuilder()
    		.append(this.getValue(), that.getValue())
    		.append(this.getSpecific(), that.getSpecific())
    		.isEquals();
    }

    @Override
    public int hashCode() {
    	return new HashCodeBuilder(7, 51)
    		.append(getValue())
    		.append(getSpecific())
    		.toHashCode();
    }
    /**
     * <p>getSpecific</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSpecific() {
        return m_specific;
    }
    /**
     * <p>setSpecific</p>
     *
     * @param specific a {@link java.lang.String} object.
     */
    public void setSpecific(String specific) {
        m_specific = specific;
    }
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return m_specific;
    }
    
    /**
     * <p>getValue</p>
     *
     * @return the value
     */
    public byte[] getValue() {
        return m_value;
    }

}
