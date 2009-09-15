/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *
 */

package org.opennms.sms.reflector.smsservice;

import org.smslib.USSDRequest;
import org.springframework.core.style.ToStringCreator;

/**
 * @author brozow
 *
 */
public class UssdRequest extends MobileMsgRequest {

    private USSDRequest m_msg;

    /**
     * @param msg
     * @param timeout
     * @param retries
     * @param cb
     * @param responseMatcher
     */
    public UssdRequest(USSDRequest msg, long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher responseMatcher) {
        super(timeout, retries, cb, responseMatcher);
        m_msg = msg;
        
    }
    
    public String getGatewayId() {
    	return m_msg.getGatewayId();
    }

    /**
     * @return the text
     */
    public String getContent() {
        return m_msg.getContent();
    }

    @Override
    public String getId() {
        return "1";
    }

    /* (non-Javadoc)
     * @see org.opennms.sms.reflector.smsservice.MobileMsgRequest#createNextRetry()
     */
    @Override
    public MobileMsgRequest createNextRetry() {
        if (getRetries() > 0) {
            return new UssdRequest(m_msg, getTimeout(), getRetries()-1, getCb(), getResponseMatcher());
        } else {
            return null;
        }
        
    }

    public USSDRequest getMessage() {
        return m_msg;
    }

    public String toString() {
    	return new ToStringCreator(this)
    		.append("id", getId())
    		.append("gatewayId", getGatewayId())
    		.append("content", getContent())
    		.append("message", getMessage())
    		.toString();
    }
}
