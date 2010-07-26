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
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Stop = 8
//

package org.opennms.netmgt.dhcpd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

import edu.bucknell.net.JDHCP.DHCPMessage;

/**
 * <p>Message class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private DHCPMessage m_dhcpmsg;

    private InetAddress m_target;

    Message() // server and serialization only
    {
        m_dhcpmsg = null;
        m_target = null;
    }

    /**
     * <p>Constructor for Message.</p>
     *
     * @param target a {@link java.net.InetAddress} object.
     * @param msg a {@link edu.bucknell.net.JDHCP.DHCPMessage} object.
     */
    public Message(InetAddress target, DHCPMessage msg) {
        m_dhcpmsg = msg;
        m_target = target;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_target;
    }

    /**
     * <p>getMessage</p>
     *
     * @return a {@link edu.bucknell.net.JDHCP.DHCPMessage} object.
     */
    public DHCPMessage getMessage() {
        return m_dhcpmsg;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(m_target);

        byte[] buf = m_dhcpmsg.externalize();
        out.writeInt(buf.length);
        out.write(buf);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        m_target = (InetAddress) in.readObject();

        byte[] buf = new byte[in.readInt()];
        in.read(buf, 0, buf.length);

        m_dhcpmsg = new DHCPMessage(buf);
    }
}
