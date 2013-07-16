/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send notifications to an IRCcat bot.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class IrcCatNotificationStrategy implements NotificationStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(IrcCatNotificationStrategy.class);
    
    /**
     * <p>Constructor for IrcCatNotificationStrategy.</p>
     */
    public IrcCatNotificationStrategy() {
    }

    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {
        try {
            String message = buildMessage(arguments);
            Socket s = new Socket(getRemoteAddr(), getRemotePort());
            PrintStream stream = new PrintStream(s.getOutputStream());
            stream.println(message);
            stream.close();
        } catch (Throwable e) {
            LOG.error("send: Error sending IRCcat notification", e);
            return 1;
        }
        return 0;
    }

    private InetAddress getRemoteAddr() throws UnknownHostException {
        return InetAddressUtils.addr(System.getProperty("irccat.host", "127.0.0.1"));
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
            } else if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) {
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
}
