/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.utils;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>XmlrpcUtil class.</p>
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @version $Id: $
 */
public final class XmlrpcUtil {

	private static final Logger LOG = LoggerFactory.getLogger(XmlrpcUtil.class);

	/**
     * This method is responsible for generating an xmlrpcNotification event and
     * sending it to eventd..
     *
     * @param txNo
     *            the transaction no.
     * @param sourceUei
     *            The uei of the source event that this event to report for.
     * @param message
     *            The message for external xmlrpc server.
     * @param status
     *            flag to indicate the type of this notification.
     * @param generator
     *            openNMS daemon where this event is produced.
     */
    public static void createAndSendXmlrpcNotificationEvent(final long txNo, final String sourceUei, final String message, final int status, final String generator) {

    	LOG.debug("createAndSendXmlrpcNotificationEvent:  txNo= {}\n uei = {}\n message = {}\n status = {}", txNo, sourceUei, message, status);

        String hostAddress = InetAddressUtils.getLocalHostAddressAsString();

        EventBuilder bldr = new EventBuilder(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI, generator);
        bldr.setHost(hostAddress);
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);
        bldr.addParam(EventConstants.PARM_SOURCE_EVENT_UEI, sourceUei);
        bldr.addParam(EventConstants.PARM_SOURCE_EVENT_MESSAGE, message);
        bldr.addParam(EventConstants.PARM_SOURCE_EVENT_STATUS, status);

        // Send event to Eventd
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());

            LOG.debug("createdAndSendXmlrpcNotificationEvent: successfully sent XMLRPC notification event for txno: {} / {} {}", txNo ,sourceUei, status);
        } catch (Throwable t) {
            LOG.warn("run: unexpected throwable exception caught during send to middleware", t);

            int failureFlag = 2;

            EventBuilder bldr2 = new EventBuilder(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI, generator);
            bldr2.setHost(hostAddress);
            bldr2.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);
            bldr2.addParam(EventConstants.PARM_SOURCE_EVENT_UEI, sourceUei);
            bldr2.addParam(EventConstants.PARM_SOURCE_EVENT_MESSAGE, message);
            bldr2.addParam(EventConstants.PARM_SOURCE_EVENT_STATUS, failureFlag);

            try {
                EventIpcManagerFactory.getIpcManager().sendNow(bldr2.getEvent());

                LOG.debug("createdAndSendXmlrpcNotificationEvent: successfully sent XMLRPC notification event for txno: {} / {} {}", txNo, sourceUei, failureFlag);
            } catch (Throwable te) {
                LOG.warn("run: unexpected throwable exception caught during send to middleware", te);
            }
        }
    }
}
