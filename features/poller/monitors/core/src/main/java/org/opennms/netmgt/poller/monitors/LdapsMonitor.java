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

import com.novell.ldap.LDAPConnection;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the LDAPS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 */
final public class LdapsMonitor extends LdapMonitor {

    @Override
    protected int determinePort(Map<String, Object> parameters) {
        return ParameterMap.getKeyedInteger(parameters, "port", LDAPConnection.DEFAULT_SSL_PORT);
    }

    @Override
    protected SocketWrapper getSocketWrapper() {
        return new SslSocketWrapper();
    }
}
