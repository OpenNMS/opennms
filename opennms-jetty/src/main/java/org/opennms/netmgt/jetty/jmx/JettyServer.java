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
package org.opennms.netmgt.jetty.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

/**
 * <p>JettyServer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JettyServer extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.jetty.JettyServer> implements JettyServerMBean {

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.jetty.JettyServer.getLoggingCategory();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "jettyServerContext";
    }

    /** {@inheritDoc} */
    @Override
    public long getHttpsConnectionsTotal() {
        return getDaemon().getHttpsConnectionsTotal();
    }

    /** {@inheritDoc} */
    @Override
    public long getHttpsConnectionsOpen() {
        return getDaemon().getHttpsConnectionsOpen();
    }

    /** {@inheritDoc} */
    @Override
    public long getHttpsConnectionsOpenMax() {
        return getDaemon().getHttpsConnectionsOpenMax();
    }

    /** {@inheritDoc} */
    @Override
    public long getHttpConnectionsTotal() {
        return getDaemon().getHttpConnectionsTotal();
    }

    /** {@inheritDoc} */
    @Override
    public long getHttpConnectionsOpen() {
        return getDaemon().getHttpConnectionsOpen();
    }

    /** {@inheritDoc} */
    @Override
    public long getHttpConnectionsOpenMax() {
        return getDaemon().getHttpConnectionsOpenMax();
    }
    
}
