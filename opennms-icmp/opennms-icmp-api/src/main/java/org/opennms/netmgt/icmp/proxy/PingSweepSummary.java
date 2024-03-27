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
package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.Map;

import com.google.common.collect.Maps;

public class PingSweepSummary {

    private Map<InetAddress, Double> responses = Maps.newConcurrentMap();

    public Map<InetAddress, Double> getResponses() {
        return responses;
    }

    public void setResponses(Map<InetAddress, Double> m_responses) {
        this.responses = m_responses;
    }

    public void addResponse(Map<InetAddress, Double> response) {
        this.responses.putAll(response);
    }

    public boolean isSuccess() {
        return responses.isEmpty() ? Boolean.FALSE : Boolean.TRUE;
    }

    public int numberOfPingsReturned() {
        return responses.size();
    }

}
