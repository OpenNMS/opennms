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
package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.Map;

/**
 * Groups all of the parameters required for making calls to {@link SyncServiceDetector#detect}
 * and {@link AsyncServiceDetector#detect}.
 *
 * The runtime attributes here differ from the properties and attributes that are configured
 * on the detector i.e. port, ipMatch, etc... These are used to store additional attributes
 * which pertain to the system's state and/or agent specific attributes i.e. the SNMP read community
 * of the agent (which is not defined the the detector's configuration).
 *
 * These requests should be created by calls to {@link ServiceDetectorFactory#buildRequest}.
 *
 * @author jwhite
 */
public interface DetectRequest extends  PreDetectCallback {

    /**
     * @return the address of the host against with the detector should be invoked.
     */
    InetAddress getAddress();

    /**
     * @return additional attributes stored outside of the detector's configuration that
     * may be required when running the detector.
     */
    Map<String, String> getRuntimeAttributes();


}
