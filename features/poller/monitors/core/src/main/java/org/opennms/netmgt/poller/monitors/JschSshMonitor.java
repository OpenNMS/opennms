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

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of SSH remote interfaces. The class implements the ServiceMonitor
 * interface that allows it to be used along with other plug-ins by the service
 * poller framework.
 * </P>
 * <P>
 * This plugin is just an exact copy of the {@link SshMonitor} now.
 * </P>
 *
 * @deprecated use {@link SshMonitor} instead
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 */
final public class JschSshMonitor extends AbstractServiceMonitor {
    private SshMonitor m_monitor;
    
    /**
     * <p>Constructor for JschSshMonitor.</p>
     */
    public JschSshMonitor() {
        m_monitor = new SshMonitor();
    }
    
    /** {@inheritDoc} */
    public PollStatus poll(InetAddress address, Map<String, Object> parameters) {
        return m_monitor.poll(address, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        return m_monitor.poll(svc, parameters);
    }

}
