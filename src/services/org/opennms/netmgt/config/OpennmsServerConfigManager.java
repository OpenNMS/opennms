//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.server.LocalServer;

public class OpennmsServerConfigManager {

    /**
     * The config class loaded from the config file
     */
    private LocalServer m_config;
    
    protected OpennmsServerConfigManager(Reader rdr) throws MarshalException, ValidationException {
        m_config = (LocalServer) Unmarshaller.unmarshal(LocalServer.class, rdr);
    }

    /**
     * Return the local opennms server name.
     * 
     * @return the name of the local opennms server
     */
    public synchronized String getServerName() {
        return m_config.getServerName();
    }

    /**
     * Return the boolean flag verify server to determine if poller what to use
     * server to restrict services to poll.
     * 
     * @return boolean flag
     */
    public synchronized boolean verifyServer() {
        String flag = m_config.getVerifyServer();
        if (flag.equals("true"))
            return true;
        else
            return false;
    }

}
