/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.support;

import java.util.Map;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;

/**
 * <p>DetectFutureNettyImpl class.</p>
 *
 * CAUTION: This class is unused. This implementation has never been in production.
 *
 * @author Seth
 */
public class DetectFutureNettyImpl implements DetectFuture {

    public static class ServiceDetectionFailedException extends Exception {
        private static final long serialVersionUID = -3784608501286028523L;
    }

    private final AsyncBasicDetectorNettyImpl<?,?> m_detector;
    private final ChannelFuture m_future;

    /**
     * <p>Constructor for DefaultDetectFuture.</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public DetectFutureNettyImpl(final AsyncBasicDetectorNettyImpl<?,?> detector, final ChannelFuture future) {
        m_detector = detector;
        m_future = future;
    }

    /**
     * <p>isServiceDetected</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isServiceDetected() {
        return m_future.isSuccess();
    }

    /**
     * <p>getServiceAttributes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, String> getServiceAttributes() {
        return null;
    }

    /**
     * <p>getException</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    @Override
    public Throwable getException() {
        return m_future.getCause();
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceDetected(final boolean serviceDetected) {
        if (serviceDetected) {
            m_future.setSuccess();
        } else {
            m_future.setFailure(new ServiceDetectionFailedException());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setException(final Throwable throwable) {
        m_future.setFailure(throwable);
    }

    @Override
    public void awaitFor() throws InterruptedException {
        m_future.await();
    }

    @Override
    public void awaitForUninterruptibly() {
        m_future.awaitUninterruptibly();
    }

    @Override
    public boolean isDone() {
        return m_future.isDone();
    }

    @Override
    public DetectFuture addListener(final DetectFutureListener<DetectFuture> listener) {
        final DetectFuture thisFuture = this;
        m_future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) {
                listener.operationComplete(thisFuture);
            }

        });
        return this;
    }
}
