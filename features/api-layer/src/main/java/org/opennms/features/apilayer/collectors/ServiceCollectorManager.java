/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.collectors;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.integration.api.v1.collectors.ServiceCollectorFactory;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceCollectorManager extends InterfaceMapper<ServiceCollectorFactory, ServiceCollector> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCollectorManager.class);

    public ServiceCollectorManager(BundleContext bundleContext) {
        super(ServiceCollector.class, bundleContext);
    }

    @Override
    public ServiceCollector map(ServiceCollectorFactory ext) {
        return new ServiceCollectorImpl(ext);
    }

    // override as registry needs collector class name in properties.
    @Override
    public Map<String, Object> getServiceProperties(ServiceCollectorFactory extension) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", extension.getCollectorClassName());
        return properties;
    }
}
