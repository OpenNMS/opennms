/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.support;

import org.apache.mina.core.future.DefaultIoFuture;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;

/**
 * <p>DefaultDetectFuture class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultDetectFuture extends DefaultIoFuture implements DetectFuture {
    
    private final AsyncServiceDetector m_detector;

    /**
     * <p>Constructor for DefaultDetectFuture.</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public DefaultDetectFuture(final AsyncServiceDetector detector) {
        super(null);
        m_detector = detector;
    }

    /**
     * <p>getServiceDetector</p>
     *
     * @return a {@link org.opennms.netmgt.provision.AsyncServiceDetector} object.
     */
    public AsyncServiceDetector getServiceDetector() {
        return m_detector;
    }

    /**
     * <p>isServiceDetected</p>
     *
     * @return a boolean.
     */
    public boolean isServiceDetected() {
        return Boolean.TRUE.equals(getValue());
    }
    
    /**
     * <p>getException</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getException() {
        final Object val = getValue();
        if (val instanceof Throwable) {
            return (Throwable)val;
        }
        return null;
    }

    /** {@inheritDoc} */
    public void setServiceDetected(final boolean serviceDetected) {
        setValue(serviceDetected);
    }

    /** {@inheritDoc} */
    public void setException(final Throwable throwable) {
//        System.err.println("setting exeception to " + throwable);
        setValue(throwable);
    }

    /**
     * <p>getObjectValue</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getObjectValue() {
        return super.getValue();
    }
}
