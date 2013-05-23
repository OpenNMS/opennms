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

package org.opennms.sms.reflector.smsservice;

import org.smslib.InboundMessage;

/**
 * <p>SmsResponse class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class SmsResponse extends MobileMsgResponse {
    
    private InboundMessage m_msg;
    
    /**
     * <p>Constructor for SmsResponse.</p>
     *
     * @param msg a {@link org.smslib.InboundMessage} object.
     * @param receiveTime a long.
     */
    public SmsResponse(InboundMessage msg, long receiveTime) {
    	super(receiveTime);
        m_msg = msg;
    }

    /**
     * <p>getOriginator</p>
     *
     * @return the originator
     */
    public String getOriginator() {
        return m_msg.getOriginator();
    }

    /**
     * <p>getText</p>
     *
     * @return the text
     */
    @Override
    public String getText() {
        return m_msg.getText();
    }

    /**
     * <p>getMessage</p>
     *
     * @return a {@link org.smslib.InboundMessage} object.
     */
    public InboundMessage getMessage() {
        return m_msg;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "" + m_msg;
    }


}
