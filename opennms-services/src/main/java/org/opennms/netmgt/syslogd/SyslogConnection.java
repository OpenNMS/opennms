/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;

/**
 * <p>SyslogConnection class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @version $Id: $
 */
public class SyslogConnection implements Runnable {

    private DatagramPacket _packet;

    private String m_logPrefix;

    private String _matchPattern;

    private int _hostGroup;

    private int _messageGroup;

    private String _discardUei;

    private UeiList _ueiList;

    private HideMessage _hideMessages;

    private static final String LOG4J_CATEGORY = "OpenNMS.Syslogd";

    /**
     * <p>Constructor for SyslogConnection.</p>
     *
     * @param packet a {@link java.net.DatagramPacket} object.
     * @param matchPattern a {@link java.lang.String} object.
     * @param hostGroup a int.
     * @param messageGroup a int.
     * @param ueiList a {@link org.opennms.netmgt.config.syslogd.UeiList} object.
     * @param hideMessages a {@link org.opennms.netmgt.config.syslogd.HideMessage} object.
     * @param discardUei a {@link java.lang.String} object.
     */
    public SyslogConnection(final DatagramPacket packet, final String matchPattern, final int hostGroup, final int messageGroup, final UeiList ueiList, final HideMessage hideMessages, final String discardUei) {
        _packet = copyPacket(packet);
        _matchPattern = matchPattern;
        _hostGroup = hostGroup;
        _messageGroup = messageGroup;
        _discardUei = discardUei;
        _ueiList = ueiList;
        _hideMessages = hideMessages;

        m_logPrefix = LOG4J_CATEGORY;
    }

    /**
     * <p>run</p>
     */
    public void run() {
        ThreadCategory.setPrefix(m_logPrefix);
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        ConvertToEvent re = null;
        try {
            re = ConvertToEvent.make(_packet, _matchPattern, _hostGroup,  _messageGroup, _ueiList, _hideMessages, _discardUei);
        } catch (final UnsupportedEncodingException e1) {
            log.debug("Failure to convert package", e1);
        } catch (final MessageDiscardedException e) {
            log.debug("Message discarded, returning without enqueueing event.", e);
            return;
        }

        log.debug("Sending received packet to the queue");

        SyslogHandler.queueManager.putInQueue(re);
        // delay a random period of time
        try {
            Thread.sleep((new Random()).nextInt(100));
        } catch (final InterruptedException e) {
            log.debug("Syslogd: Interruption ", e);
        }

    }

    void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }

    private DatagramPacket copyPacket(final DatagramPacket packet) {
        byte[] message = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(packet.getAddress().getHostName(), packet.getAddress().getAddress());
            DatagramPacket retPacket = new DatagramPacket(
                                                          message,
                                                          packet.getOffset(),
                                                          packet.getLength(),
                                                          addr,
                                                          packet.getPort()
                                                      );
                                                      return retPacket;
        } catch (UnknownHostException e) {
            ThreadCategory.getInstance(getClass()).warn("unable to clone InetAddress object for " + packet.getAddress());
        }
        return null;
    }
}
