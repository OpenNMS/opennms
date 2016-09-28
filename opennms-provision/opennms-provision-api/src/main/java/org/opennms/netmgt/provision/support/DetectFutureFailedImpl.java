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
