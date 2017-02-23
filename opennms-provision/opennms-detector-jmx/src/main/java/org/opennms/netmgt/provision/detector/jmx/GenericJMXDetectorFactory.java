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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.jmx;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.DetectRequestImpl;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericJMXDetectorFactory<T extends JMXDetector> extends GenericServiceDetectorFactory<JMXDetector> {

    @Autowired(required=false)
    protected JmxConfigDao jmxConfigDao;

    @SuppressWarnings("unchecked")
    public GenericJMXDetectorFactory(Class<T> clazz) {
        super((Class<JMXDetector>) clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createDetector() {
        return (T)super.createDetector();
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port) {
        return new DetectRequestImpl(address, port, getRuntimeAttributes(location, address, port));
    }

    public Map<String, String> getRuntimeAttributes(String location, InetAddress address, Integer port) {
        String ipAddress = address.getHostAddress();
        if (port == null) {
            throw new IllegalArgumentException(" Port number needs to be specified in the form of port=number ");
        }

        if (jmxConfigDao == null) {
            return Collections.emptyMap();
        } else {
            MBeanServer serverConfig = jmxConfigDao.getConfig().lookupMBeanServer(ipAddress, port);
            if (serverConfig == null) {
                return Collections.emptyMap();
            } else {
                return serverConfig.getParameterMap();
            }
        }
    }

    public void setJmxConfigDao(JmxConfigDao jmxConfigDao) {
        this.jmxConfigDao = jmxConfigDao;
    }
}

