/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.support;

import java.util.Map;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;

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
    private final ChannelPromise m_future;

    /**
     * <p>Constructor for DefaultDetectFuture.</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public DetectFutureNettyImpl(final AsyncBasicDetectorNettyImpl<?,?> detector, final ChannelPromise future) {
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
        return m_future.cause();
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
