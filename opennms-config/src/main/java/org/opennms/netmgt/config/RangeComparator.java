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
import org.opennms.netmgt.config.snmp.Range;

/**
 * Use this class to compare two JAXB-generated Range objects from the SnmpConfig class.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class RangeComparator implements Comparator<Range>, Serializable {
	private static final long serialVersionUID = -6090811413071744038L;

	/**
     * <p>compare</p>
     *
     * @param rng1 a {@link org.opennms.netmgt.config.common.Range} object.
     * @param rng2 a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a int.
     */
        @Override
    public int compare(final Range rng1, final Range rng2) {
    	final InetAddress addr1 = InetAddressUtils.addr(rng1.getBegin());
		final InetAddress addr2 = InetAddressUtils.addr(rng2.getBegin());
		return new ByteArrayComparator().compare(addr1 == null? null : addr1.getAddress(), addr2 == null? null : addr2.getAddress());
    }
}
