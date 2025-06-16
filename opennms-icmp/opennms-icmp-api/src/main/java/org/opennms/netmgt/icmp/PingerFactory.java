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
package org.opennms.netmgt.icmp;

public interface PingerFactory {
    public static final int MAX_DSCP = (1 << 16) - 1;
    public static final int FRAG_FALSE = 1;
    public static final int FRAG_TRUE = 2;

    /**
     * Returns an implementation of the default {@link Pinger} class
     *
     * @param tc the traffic control value to set, use "0" for none
     * @param allowFragmentation whether to allow fragmentation
     *
     * @return a {@link Pinger} object.
     */
    public Pinger getInstance();

    /**
     * Returns an implementation of the {@link Pinger} class associated with the
     * socket configured for the given traffic control and fragmentation bits.
     *
     * @param tc the traffic control value to set, use "0" for none
     * @param allowFragmentation whether to allow fragmentation
     *
     * @return a {@link Pinger} object.
     */
    public Pinger getInstance(final int tc, final boolean allowFragmentation);
}
