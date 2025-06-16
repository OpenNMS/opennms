/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
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
        Socket s = null;
        try {
            String message = buildMessage(arguments);
            s = new Socket(getRemoteAddr(), getRemotePort());
            PrintStream stream = new PrintStream(s.getOutputStream());
            stream.println(message);
            stream.close();
        } catch (Throwable e) {
            LOG.error("send: Error sending IRCcat notification", e);
            return 1;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    LOG.error("send: Error closing IRCcat socket", e);
                }
            }
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
