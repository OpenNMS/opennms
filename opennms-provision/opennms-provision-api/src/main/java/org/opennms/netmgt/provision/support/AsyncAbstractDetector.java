/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectRequest;

/**
 * <p>AsyncAbstractDetector class.</p>
 *
 * @author thedesloge
 */
public abstract class AsyncAbstractDetector extends AbstractDetector implements AsyncServiceDetector {

    /**
     * <p>Constructor for AsyncAbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected AsyncAbstractDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
    }

    /**
     * <p>Constructor for AsyncAbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected AsyncAbstractDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    public abstract DetectFuture isServiceDetected(final InetAddress address);

    @Override
    public DetectFuture detect(DetectRequest request) {
        return isServiceDetected(request.getAddress());
    }

}
