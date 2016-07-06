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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;

public class ServiceDetectorFactoryProvider {

    @SuppressWarnings("rawtypes")
    private ServiceLoader<ServiceDetectorFactory> loader;

    public ServiceDetectorFactoryProvider() {
        loader = ServiceLoader.load(ServiceDetectorFactory.class);
    }

    @SuppressWarnings("rawtypes")
    public ServiceDetector getDetector(String detectorType) {

        Iterator<ServiceDetectorFactory> detectorFactory = loader.iterator();
        while (detectorFactory.hasNext()) {
            ServiceDetectorFactory serviceDetectorFactory = detectorFactory.next();
            ServiceDetector detector = serviceDetectorFactory.createDetector();
            if (detector.getServiceName().equals(detectorType)) {
                return detector;
            }
        }
        return null;
    }

}
