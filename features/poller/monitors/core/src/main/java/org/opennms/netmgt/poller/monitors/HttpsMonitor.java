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
import org.opennms.core.utils.SocketWrapper;
import org.opennms.core.utils.SslSocketWrapper;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the HTTPS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 */
public class HttpsMonitor extends HttpMonitor {

    /**
     * Default HTTPS ports.
     */
    private static final int[] DEFAULT_PORTS = { 443 };

    /** {@inheritDoc} */
    @Override
    protected int[] determinePorts(Map<String, Object> parameters) {
        return ParameterMap.getKeyedIntegerArray(parameters, "port", DEFAULT_PORTS);
    }

    /** {@inheritDoc} */
    @Override
    protected SocketWrapper getSocketWrapper() {
        return new SslSocketWrapper();
    }

}
