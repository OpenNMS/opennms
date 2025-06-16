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
