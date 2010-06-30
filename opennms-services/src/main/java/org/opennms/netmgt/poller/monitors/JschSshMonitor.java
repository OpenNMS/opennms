
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc. All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

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
final public class JschSshMonitor extends IPv4Monitor {
    private SshMonitor m_monitor;
    
    /**
     * <p>Constructor for JschSshMonitor.</p>
     */
    public JschSshMonitor() {
        m_monitor = new SshMonitor();
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public PollStatus poll(InetAddress address, Map parameters) {
        return m_monitor.poll(address, parameters);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public PollStatus poll(MonitoredService svc, Map parameters) {
        return m_monitor.poll(svc, parameters);
    }

}
