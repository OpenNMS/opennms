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
package org.opennms.netmgt.icmp.jna;

import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.Pinger;

/**
 * A pinger factory for the JNA socket.
 *
 * @deprecated The JNA socket adds latency, which makes ICMP response times less accurate.
 * It will be removed in a future release.
 * Use {@link org.opennms.netmgt.icmp.ffm.FfmPingerFactory}.
 */
@Deprecated(since = "37", forRemoval = true)
public class JnaPingerFactory extends AbstractPingerFactory {

    @Override
    @SuppressWarnings("removal")
    public Class<? extends Pinger> getPingerClass() {
        return JnaPinger.class;
    }
}
