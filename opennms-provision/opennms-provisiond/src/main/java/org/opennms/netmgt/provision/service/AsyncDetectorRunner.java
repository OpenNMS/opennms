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

package org.opennms.netmgt.provision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;

class AsyncDetectorRunner implements Async<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncDetectorRunner.class);
    
    private final IpInterfaceScan m_ifaceScan;
    private final AsyncServiceDetector m_detector;
    
    /**
     * <p>Constructor for AsyncDetectorRunner.</p>
     *
     * @param ifaceScan a {@link org.opennms.netmgt.provision.service.IpInterfaceScan} object.
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public AsyncDetectorRunner(IpInterfaceScan ifaceScan, AsyncServiceDetector detector) {
        m_ifaceScan = ifaceScan;
        m_detector = detector;
    }

    /** {@inheritDoc} */
    @Override
    public void submit(Callback<Boolean> cb) {
        try {
            LOG.info("Attemping to detect service {} on address {}", m_detector.getServiceName(), getHostAddress());
            DetectFuture future = m_detector.isServiceDetected(m_ifaceScan.getAddress());
            future.addListener(listener(cb));
        } catch (Throwable e) {
            cb.handleException(e);
        }
    }

	private String getHostAddress() {
		return InetAddressUtils.str(m_ifaceScan.getAddress());
	}
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("Run detector %s on address %s", m_detector.getServiceName(), getHostAddress());
    }

    private DetectFutureListener<DetectFuture> listener(final Callback<Boolean> cb) {
        return new DetectFutureListener<DetectFuture>() {
            @Override
            public void operationComplete(DetectFuture future) {
                try {
                    if (future.getException() != null) {
                        cb.handleException(future.getException());
                    } else {
                        cb.complete(future.isServiceDetected());
                    }
                } finally{
                   m_detector.dispose();
                }
            }
        };
    }
    
}
