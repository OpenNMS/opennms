/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 9, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.notifd;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotificationManager;

/**
 * Send notifications to an IRCcat bot.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class IrcCatNotificationStrategy implements NotificationStrategy {
    public IrcCatNotificationStrategy() {
    }

    public int send(List<Argument> arguments) {
        try {
            String message = buildMessage(arguments);
            Socket s = new Socket(getRemoteAddr(), getRemotePort());
            PrintStream stream = new PrintStream(s.getOutputStream());
            stream.println(message);
            stream.close();
        } catch (Exception e) {
            log().error("send: Error sending IRCcat notification: " + e, e);
            return 1;
        }
        return 0;
    }

    private InetAddress getRemoteAddr() throws UnknownHostException {
        return InetAddress.getByName(System.getProperty("irccat.host", "127.0.0.1"));
    }

    private int getRemotePort() {
        return Integer.parseInt(System.getProperty("irccat.port", "12345"));
    }

    private String buildMessage(List<Argument> arguments) {
        String recipient = null;
        String message = null;
        
        for (Argument arg : arguments) {
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) {
                recipient = arg.getValue();
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                message = arg.getValue();
            } else {
                throw new IllegalArgumentException("Unsupported notification argument switch '" + arg.getSwitch() + "'");
            }
        }
        
        if (recipient == null) {
            // FIXME We should have a better Exception to use here for configuration problems
            throw new IllegalArgumentException("no recipient specified, but is required");
        }
        if (message == null) {
            // FIXME We should have a better Exception to use here for configuration problems
            throw new IllegalArgumentException("no message specified, but is required");
        }

        return recipient + " " + message;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
