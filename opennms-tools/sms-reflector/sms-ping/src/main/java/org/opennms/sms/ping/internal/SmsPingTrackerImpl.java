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

package org.opennms.sms.ping.internal;

import java.io.IOException;

import org.opennms.protocols.rt.IDBasedRequestLocator;
import org.opennms.protocols.rt.RequestTracker;
import org.opennms.sms.ping.PingRequestId;
import org.opennms.sms.ping.PingResponseCallback;
import org.opennms.sms.ping.SmsPingTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SmsPingTrackerImpl
 *
 * @author brozow
 * @version $Id: $
 */
public class SmsPingTrackerImpl extends RequestTracker<PingRequest, PingReply> implements SmsPingTracker {
    
    private static final Logger LOG = LoggerFactory.getLogger(SmsPingTrackerImpl.class);

    /**
     * <p>Constructor for SmsPingTrackerImpl.</p>
     *
     * @param smsMessenger a {@link org.opennms.sms.ping.internal.SmsPingMessenger} object.
     * @throws java.io.IOException if any.
     */
    public SmsPingTrackerImpl(SmsPingMessenger smsMessenger) throws IOException {
        super("SMS", smsMessenger, new IDBasedRequestLocator<PingRequestId, PingRequest, PingReply>());
        LOG.debug("Created SmsPingTrackerImpl");
    }

    /** {@inheritDoc} */
    @Override
    public void sendRequest(String phoneNumber, long timeout, int retries, PingResponseCallback cb) throws Exception {
        sendRequest(new PingRequest(new PingRequestId(phoneNumber), timeout, retries, cb));
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.rt.RequestTracker#start()
     */
    /** {@inheritDoc} */
    @Override
    public synchronized void start() {
        LOG.debug("Calling start()");
        super.start();
        LOG.debug("Called start()");
    }
    

    /**
     * <p>stop</p>
     */
    public void stop() {
        LOG.debug("Calling stop()");

        LOG.debug("Called stop()");
    }
    
    

}
