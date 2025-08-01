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
package org.opennms.netmgt.provision.support;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.provision.DetectRequest;

public class DetectRequestImpl implements DetectRequest {

    private final InetAddress address;
    private final Integer port;
    private final Map<String, String> runtimeAttributes;

    public DetectRequestImpl(InetAddress address, Integer port) {
        this(address, port, Collections.emptyMap());
    }

    public DetectRequestImpl(InetAddress address, Integer port, Map<String, String> runtimeAttributes) {
        this.address = Objects.requireNonNull(address);
        this.port = port;
        this.runtimeAttributes = Objects.requireNonNull(runtimeAttributes);
    }

    public InetAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public Map<String, String> getRuntimeAttributes() {
        return runtimeAttributes;
    }

    @Override
    public void preDetect() {
        // pass
    }
}
