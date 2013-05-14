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

import org.smslib.USSDResponse;
import org.smslib.USSDSessionStatus;

/**
 * <p>UssdResponse class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class UssdResponse extends MobileMsgResponse {

    private String m_gatewayId;
    private USSDResponse m_msg;
    
    /**
     * <p>Constructor for UssdResponse.</p>
     *
     * @param gatewayId a {@link java.lang.String} object.
     * @param msg a {@link org.smslib.USSDResponse} object.
     * @param receiveTime a long.
     */
    public UssdResponse(String gatewayId, USSDResponse msg, long receiveTime) {
    	super(receiveTime);
        m_gatewayId = gatewayId;
        m_msg = msg;
    }

    /**
     * <p>getGatewayId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGatewayId() {
        return m_gatewayId;
    }

    /**
     * <p>getText</p>
     *
     * @return the text
     */
    @Override
    public String getText() {
        return m_msg.getContent();
    }
    
    
    /**
     * <p>getSessionStatus</p>
     *
     * @return a {@link org.smslib.USSDSessionStatus} object.
     */
    public USSDSessionStatus getSessionStatus() {
        return m_msg.getSessionStatus();
    }

    /**
     * <p>getMessage</p>
     *
     * @return a {@link org.smslib.USSDResponse} object.
     */
    public USSDResponse getMessage() {
        return m_msg;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "" + m_msg;
    }



}
