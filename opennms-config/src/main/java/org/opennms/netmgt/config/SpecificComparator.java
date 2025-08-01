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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Comparator;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;

/**
 * This class is used to compare Specific object from the config SNMP package.
 *
 * @author <a href="mailto:david@openmms.org">David Hustace</a>
 */
public class SpecificComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 5791618124389187729L;

	/**
     * returns the difference of spec1 - spec2
     *
     * @param spec1 a {@link java.lang.String} object.
     * @param spec2 a {@link java.lang.String} object.
     * @return -1 for spec1 < spec2, 0 for spec1 == spec2, 1 for spec1 > spec2
     */
        @Override
    public int compare(final String spec1, final String spec2) {
    	final InetAddress addr1 = InetAddressUtils.addr(spec1);
		final InetAddress addr2 = InetAddressUtils.addr(spec2);
		return new ByteArrayComparator().compare(addr1 == null? null : addr1.getAddress(), addr2 == null? null : addr2.getAddress());
    }
}

