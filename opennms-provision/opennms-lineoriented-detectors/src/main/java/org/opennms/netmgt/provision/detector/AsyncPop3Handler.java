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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * @author thedesloge
 *
 */
public class AsyncPop3Handler extends IoHandlerAdapter {
    
    public void sessionOpened(IoSession session) {
        // Set reader idle time to 10 seconds.
        // sessionIdle(...) method will be invoked when no data is read
        // for 10 seconds.
        //session.setIdleTime(IdleStatus.READER_IDLE, 10);
        System.out.println("session Opened on Client");
        session.write("QUIT");
        
    }
    
    public void exceptionCaught(IoSession session, Throwable cause)    throws Exception {
        cause.printStackTrace();
    }

    public void sessionClosed(IoSession session) {
        // Print out total number of bytes read from the remote peer.
        System.err.println("Total " + session.getReadBytes() + " byte(s)");
    }

    public void sessionIdle(IoSession session, IdleStatus status) {
        // Close the connection if reader is idle.
        if (status == IdleStatus.READER_IDLE)
            session.close();
    }

    public void messageReceived(IoSession session, Object message) {
        System.out.println("yo: " + message.toString().trim());
        
    }

    
}
