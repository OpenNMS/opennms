/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

    private static final long serialVersionUID = -2712181407338192347L;

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
        in.readFully(buf, 0, buf.length);

        m_dhcpmsg = new DHCPMessage(buf);
    }
}
