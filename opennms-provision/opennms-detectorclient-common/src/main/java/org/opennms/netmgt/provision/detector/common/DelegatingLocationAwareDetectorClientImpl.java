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

package org.opennms.netmgt.provision.detector.common;

import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DelegatingLocationAwareDetectorClientImpl implements LocationAwareDetectorClient {

    @Autowired
    @Qualifier("localDetectorExecutor")
    DetectorRequestExecutor localDetectorExecutor;

    @Autowired
    @Qualifier("remoteDetectorExecutor")
    DetectorRequestExecutor remoteDetectorExecutor;

    @Autowired(required = false)
    private OnmsDistPoller identity;

    @Autowired
    private ServiceDetectorRegistry registry;

    @Override
    public DetectorRequestBuilder detect() {
        return new DetectorRequestBuilderImpl(this, registry);
    }

    protected DetectorRequestExecutor getDetectorRequestExecutor(String location) {
        if (location == null || (identity != null && identity.getLocation().equals(location))) {
            return localDetectorExecutor;
        } else {
            return remoteDetectorExecutor;
        }
    }

    public void setLocalDetectorExecutor(DetectorRequestExecutor localDetectorExecutor) {
        this.localDetectorExecutor = localDetectorExecutor;
    }

    public void setRemoteDetectorExecutor(DetectorRequestExecutor remoteDetectorExecutor) {
        this.remoteDetectorExecutor = remoteDetectorExecutor;
    }

    public void setIdentity(OnmsDistPoller identity) {
        this.identity = identity;
    }

    public void setRegistry(ServiceDetectorRegistry registry) {
        this.registry = registry;
    }
}
