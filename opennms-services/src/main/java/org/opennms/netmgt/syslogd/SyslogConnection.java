/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 30, 2006
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2006-2009 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. Modifications: Created: May 30, 2006
 * 2009 Mar 23: Add support for discarding messages. - jeffg@opennms.org
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.syslogd;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
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

    public void run() {
        ThreadCategory.setPrefix(m_logPrefix);
        Category log = ThreadCategory.getInstance(getClass());

        ConvertToEvent re = null;
        try {
            re = ConvertToEvent.make(_packet, _matchPattern, _hostGroup,  _messageGroup, _ueiList, _hideMessages, _discardUei);
        } catch (UnsupportedEncodingException e1) {
            log.debug("Failure to convert package");
        } catch (MessageDiscardedException e) {
            log.debug("Message discarded, returning without enqueueing event.");
            return;
        }

        log.debug("Sending received packet to the queue");

        SyslogHandler.queueManager.putInQueue(re);
        // delay a random period of time
        try {
            Thread.sleep((new Random()).nextInt(100));
        } catch (InterruptedException e) {
            log.debug("Syslogd: Interruption " + e);
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
// END OF CLASS
