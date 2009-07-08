/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.provision.support;

import org.apache.mina.core.future.DefaultIoFuture;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;

/**
 * @author brozow
 *
 */
public class DefaultDetectFuture extends DefaultIoFuture implements DetectFuture {
    
    private final AsyncServiceDetector m_detector;

    /**
     * @param asyncBasicDetector
     */
    public DefaultDetectFuture(AsyncServiceDetector detector) {
        super(null);
        m_detector = detector;
    }

    public AsyncServiceDetector getServiceDetector() {
        return m_detector;
    }

    public boolean isServiceDetected() {
        return Boolean.TRUE.equals(getValue());
    }
    
    public Throwable getException() {
        Object val = getValue();
        if (val instanceof Throwable) {
            return (Throwable)val;
        }
        return null;
    }

    public void setServiceDetected(boolean serviceDetected) {
        setValue(serviceDetected);
    }

    public void setException(Throwable throwable) {
        setValue(throwable);
    }
    
}
