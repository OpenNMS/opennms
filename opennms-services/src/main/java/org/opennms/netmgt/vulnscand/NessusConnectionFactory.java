//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.vulnscand;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

class NessusConnectionFactory {
    /**
     * <p>getConnection</p>
     *
     * @param hostname a {@link java.net.InetAddress} object.
     * @param hostport a int.
     * @return a {@link java.net.Socket} object.
     */
    static public Socket getConnection(InetAddress hostname, int hostport) {
        Category log = ThreadCategory.getInstance(NessusConnectionFactory.class);

        try {
            Socket retval = new Socket(hostname, hostport);
            return retval;
        } catch (UnknownHostException ex) {
            log.warn(ex);
            return null;
        } catch (IOException ex) {
            log.warn(ex);
            return null;
        }
    }

    /**
     * <p>releaseConnection</p>
     *
     * @param socket a {@link java.net.Socket} object.
     */
    static public void releaseConnection(Socket socket) {
        Category log = ThreadCategory.getInstance(NessusConnectionFactory.class);

        try {
            socket.close();
        } catch (IOException ex) {
            log.error("Could not close socket", ex);
        }
    }
}
