/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.LogUtils.infof;

import org.apache.mina.core.future.IoFutureListener;
import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;

class AsyncDetectorRunner implements Async<Boolean> {
    
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
    public void submit(Callback<Boolean> cb) {
        try {
            infof(this, "Attemping to detect service %s on address %s", m_detector.getServiceName(), m_ifaceScan.getAddress().getHostAddress());
            DetectFuture future = m_detector.isServiceDetected(m_ifaceScan.getAddress(), new NullDetectorMonitor());
            future.addListener(listener(cb));
        } catch (Throwable e) {
            cb.handleException(e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("Run detector %s on address %s", m_detector.getServiceName(), m_ifaceScan.getAddress().getHostAddress());
    }

    private IoFutureListener<DetectFuture> listener(final Callback<Boolean> cb) {
        return new IoFutureListener<DetectFuture>() {
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
