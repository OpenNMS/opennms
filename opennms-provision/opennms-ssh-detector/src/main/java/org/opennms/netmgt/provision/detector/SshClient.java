/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ssh.SshMonitor;

/**
 * @author thedesloge
 *
 */
public class SshClient implements Client<NullRequest, SshResponse> {
    
    private boolean m_isAvailable = false;
    private Map<String, Object> m_parameters = new HashMap<String, Object>();
    
    public void close() {
        
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        SshMonitor m = new SshMonitor();
        m_parameters.put("port", port);
        
        m_isAvailable = m.poll(address, m_parameters).isAvailable();
    }

    public SshResponse receiveBanner() throws IOException, Exception {
        SshResponse response = new SshResponse();
        response.receive(m_isAvailable);
        return response;
    }

    public SshResponse sendRequest(NullRequest request) throws IOException, Exception {
        return null;
    }
    
    public void setBanner(String banner) {
        m_parameters.put("banner", banner);
    }
    
    public void setMatch(String match) {
        m_parameters.put("match", match);
    }
    
    public void setClientBanner(String clientBanner) {
        m_parameters.put("client-banner", clientBanner);
    }

}
