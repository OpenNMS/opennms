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
package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.monitors.support.LoopPlugin;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

/**
 * <p>LoopMonitor class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class LoopMonitor extends AbstractServiceMonitor {

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        final LoopPlugin lp = new LoopPlugin();
        boolean isAvailable = lp.isProtocolSupported(svc.getAddress(), parameters);
        int status = (isAvailable ? PollStatus.SERVICE_AVAILABLE : PollStatus.SERVICE_UNAVAILABLE);
        final StringBuilder sb = new StringBuilder();
        sb.append("LoopMonitor configured with is-supported =  ");
        sb.append(ParameterMap.getKeyedString(parameters, "is-supported", "false"));
        sb.append(" for ip-match: ");
        sb.append(ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*"));

        return PollStatus.get(status, sb.toString());
    }
}
