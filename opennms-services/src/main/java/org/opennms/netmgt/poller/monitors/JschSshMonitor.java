
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;

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

@Distributable
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
