/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

import java.io.IOException;

import org.opennms.protocols.rt.Messenger;
import org.opennms.protocols.rt.RequestTracker;
import org.smslib.OutboundMessage;
import org.smslib.USSDRequest;
import org.springframework.beans.factory.InitializingBean;

/**
 * MobileMsgTracker
 *
 * @author brozow
 * @version $Id: $
 */
public class MobileMsgTrackerImpl extends RequestTracker<MobileMsgRequest, MobileMsgResponse> implements MobileMsgTracker, InitializingBean {

    /**
     * <p>Constructor for MobileMsgTrackerImpl.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param messenger a {@link org.opennms.protocols.rt.Messenger} object.
     * @throws java.io.IOException if any.
     */
    public MobileMsgTrackerImpl(String name,  Messenger<MobileMsgRequest, MobileMsgResponse> messenger)
            throws IOException {
        super(name, messenger, new MatchingRequestLocator());

    }
    
    
    /** {@inheritDoc} */
    @Override
    public MobileMsgRequest sendSmsRequest(OutboundMessage msg, long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher matcher) throws Exception {
        SmsRequest request = new SmsRequest(msg, timeout, retries, cb, matcher);
        sendRequest(request);
        return request;
    }

    /** {@inheritDoc} */
    @Override
    public MobileMsgRequest sendUssdRequest(USSDRequest msg, long timeout, int retries, MobileMsgResponseCallback cb, MobileMsgResponseMatcher matcher) throws Exception {
        UssdRequest request = new UssdRequest(msg, timeout, retries, cb, matcher);
        sendRequest(request);
        return request;
    }


    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

}
