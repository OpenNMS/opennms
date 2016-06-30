/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.net.InetAddress;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AsyncDetectorRunner implements Async<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncDetectorRunner.class);
    
    private final InetAddress m_address;
    private final AsyncServiceDetector m_detector;
    
    /**
     * <p>Constructor for AsyncDetectorRunner.</p>
     *
     * @param ifaceScan a {@link org.opennms.netmgt.provision.service.IpInterfaceScan} object.
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public AsyncDetectorRunner(AsyncServiceDetector detector, InetAddress address) {
        m_detector = detector;
        m_address = address;
    }

    /** {@inheritDoc} */
    @Override
    public void supplyAsyncThenAccept(Callback<Boolean> cb) {
        try {
            LOG.info("Attemping to detect service {} asynchronously on address {}", m_detector.getServiceName(), getHostAddress());
            // Launch the async detector
            DetectFuture future = m_detector.isServiceDetected(m_address);
            // After completion, run the callback
            future.addListener(new RunCallbackListener(cb));
            // And dispose of the detector
            future.addListener(new DisposeDetectorListener(m_detector));
        } catch (Throwable e) {
            cb.handleException(e);
            // TODO: Is this necessary? Probably but not certain.
            m_detector.dispose();
        }
    }

	private String getHostAddress() {
		return InetAddressUtils.str(m_address);
	}
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("Run detector %s on address %s", m_detector.getServiceName(), getHostAddress());
    }

    private static class RunCallbackListener implements DetectFutureListener<DetectFuture> {

        private final Callback<Boolean> m_callback;

        public RunCallbackListener(final Callback<Boolean> cb) {
            m_callback = cb;
        }

        @Override
        public void operationComplete(final DetectFuture future) {
            if (future.getException() != null) {
                m_callback.handleException(future.getException());
            } else {
                m_callback.accept(future.isServiceDetected());
            }
        };
    }

    private static class DisposeDetectorListener implements DetectFutureListener<DetectFuture> {

        private final AsyncServiceDetector m_detector;

        public DisposeDetectorListener(final AsyncServiceDetector detector) {
            m_detector = detector;
        }

        @Override
        public void operationComplete(final DetectFuture future) {
           m_detector.dispose();
        };
    }
}
