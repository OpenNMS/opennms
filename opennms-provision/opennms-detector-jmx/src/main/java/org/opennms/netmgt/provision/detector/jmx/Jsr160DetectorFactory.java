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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.provision.AbstractServiceDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Jsr160DetectorFactory extends AbstractServiceDetectorFactory<Jsr160Detector> {

    @Autowired
    protected JmxConfigDao jmxConfigDao;

    @Override
    public Jsr160Detector createDetector() {
        return new Jsr160Detector();
    }

    @Override
    public Map<String, String> getRuntimeAttributes(String location, InetAddress address, String port) {
        String ipAddress = address.getHostAddress();
        if (StringUtils.isBlank(port)) {
            throw new IllegalArgumentException("Need to specify port number in the form of port=number for Jsr160Detector");
        }
        return jmxConfigDao.getConfig().lookupMBeanServer(ipAddress, port).getParameterMap();
    }

    public void setJmxConfigDao(JmxConfigDao jmxConfigDao) {
        this.jmxConfigDao = jmxConfigDao;
    }
}
