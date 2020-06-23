/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.client.rpc;

import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectRequest;

public class ExceptionalAsyncServiceDetector implements AsyncServiceDetector {

    @Override
    public DetectFuture detect(DetectRequest request) {
        throw new IllegalArgumentException("Failure on async detection.");
    }

    @Override
    public void init() {
        // pass
    }

    @Override
    public String getServiceName() {
        return "EXCEPTIONAL-ASYNC";
    }

    @Override
    public void setServiceName(String serviceName) {
        // pass
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void setPort(int port) {
        // pass
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public void setTimeout(int timeout) {
        // pass
    }

    @Override
    public String getIpMatch() {
        return null;
    }

    @Override
    public void setIpMatch(String ipMatch) {
        // pass
    }

    @Override
    public void dispose() {
        // pass
    }
}
