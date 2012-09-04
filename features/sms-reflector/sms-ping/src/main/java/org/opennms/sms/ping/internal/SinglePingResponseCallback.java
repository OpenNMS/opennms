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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.opennms.sms.ping.PingResponseCallback;
import org.smslib.Message;

/**
 * <p>SinglePingResponseCallback class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class SinglePingResponseCallback implements PingResponseCallback {
    private CountDownLatch bs = new CountDownLatch(1);
    @SuppressWarnings("unused")
    private Throwable error = null;
    private Long responseTime = null;
    private String m_phoneNumber;

    /**
     * <p>Constructor for SinglePingResponseCallback.</p>
     *
     * @param phoneNumber a {@link java.lang.String} object.
     */
    public SinglePingResponseCallback(String phoneNumber) {
        m_phoneNumber = phoneNumber;
    }

    /** {@inheritDoc} */
    public void handleResponse(PingRequest request, Message packet) {
        info("got response for request " + request + ", message = " + packet);
        responseTime = request.getRoundTripTime();
        bs.countDown();
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

    /** {@inheritDoc} */
    public void handleTimeout(PingRequest request, Message packet) {
        info("timed out pinging request " + request + ", message = " + packet);
        bs.countDown();
    }

    /** {@inheritDoc} */
    public void handleError(PingRequest request, Message pr, Throwable t) {
        info("an error occurred pinging " + request, t);
        error = t;
        bs.countDown();
    }

    /**
     * <p>waitFor</p>
     *
     * @param timeout a long.
     * @throws java.lang.InterruptedException if any.
     */
    public void waitFor(long timeout) throws InterruptedException {
        bs.await(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * <p>waitFor</p>
     *
     * @throws java.lang.InterruptedException if any.
     */
    public void waitFor() throws InterruptedException {
        info("waiting for ping to "+m_phoneNumber+" to finish");
        bs.await();
        info("finished waiting for ping to "+m_phoneNumber+" to finish");
    }

    /**
     * <p>Getter for the field <code>responseTime</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getResponseTime() {
        return responseTime;
    }

    /**
     * <p>info</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public void info(String msg) {
        log().info(msg);
    }
    /**
     * <p>info</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void info(String msg, Throwable t) {
        log().info(msg, t);
    }

}
