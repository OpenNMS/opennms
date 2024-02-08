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

import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;

/**
 * <p>DetectFutureNettyImpl class.</p>
 *
 * @author Seth
 */
public class DetectFutureFailedImpl implements DetectFuture {

    final AsyncServiceDetector m_detector;
    final Throwable m_cause;

    /**
     * <p>Constructor for DefaultDetectFuture.</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     * @param e 
     */
    public DetectFutureFailedImpl(final AsyncServiceDetector detector, Throwable e) {
        m_detector = detector;
        m_cause = e;
    }

    /**
     * <p>isServiceDetected</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isServiceDetected() {
        return false;
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
        return m_cause;
    }

    @Override
    public void setServiceDetected(final boolean serviceDetected) {
        throw new UnsupportedOperationException(getClass().getName() + ".setServiceDetected() not supported");
    }

    @Override
    public void setException(final Throwable throwable) {
        throw new UnsupportedOperationException(getClass().getName() + ".setException() not supported");
    }

    @Override
    public void awaitFor() throws InterruptedException {
    }

    @Override
    public void awaitForUninterruptibly() {
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public DetectFuture addListener(final DetectFutureListener<DetectFuture> listener) {
        throw new UnsupportedOperationException(getClass().getName() + ".addListener() not supported");
    }
}
