/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.eifadapter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

import java.util.List;

public class EifPacketProcessor implements Processor {

    StringBuilder m_buff = new StringBuilder();

    @Override
    public void process(Exchange exchange) throws Exception {
        final String bytes = exchange.getIn().getBody(String.class);
        System.err.println("Packet processor called with "+bytes);
        m_buff.append(bytes);
        if ( m_buff.toString().contains("<START>>") && m_buff.toString().contains(";END") ) {
            int eifStart = m_buff.indexOf("<START>>");
            int eifEnd = m_buff.lastIndexOf(";END");
            StringBuilder eif = new StringBuilder(m_buff.substring(eifStart,eifEnd+4));
            m_buff.delete(eifStart,eifEnd+4);
            List<Event> e = EifParser.translateEifToOpenNMS(eif);
            if(e == null) {
                exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
            } else {
                Log eifEvents = new Log();
                e.forEach(event -> eifEvents.addEvent(event));
                exchange.getIn().setBody(eifEvents, Log.class);
            }
        } else {
            // message is incomplete, wait for more
            exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        }

        /*
        boolean close = false;
        if (close) {
            exchange.getOut().setHeader(NettyConstants.NETTY_CLOSE_CHANNEL_WHEN_COMPLETE, true);
        }
         */
    }

}
