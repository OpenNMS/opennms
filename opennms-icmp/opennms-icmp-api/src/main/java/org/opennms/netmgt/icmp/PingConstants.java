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

/**
 * PingConstants
 *
 * @author brozow
 * @version $Id: $
 */
public interface PingConstants {
    
    /** Constant <code>DEFAULT_RETRIES=2</code> */
    public static final int DEFAULT_RETRIES = 2;
    /** Constant <code>DEFAULT_TIMEOUT=800</code> */
    public static final int DEFAULT_TIMEOUT = 800;
    /** Constant <code>DEFAULT_PACKET_SIZE=64</code> */
    public static final int DEFAULT_PACKET_SIZE = 64;
    /** Constant <code>DEFAULT_PACKETS_PER_SECOND=3</code> */
    public static final double DEFAULT_PACKETS_PER_SECOND = 1.0;

}
