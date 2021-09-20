/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.wsman;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.DetectRequestImpl;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WsManWQLDetectorFactory extends GenericServiceDetectorFactory<WsManWQLDetector> {
    private static final Logger LOG = LoggerFactory.getLogger(WsManWQLDetectorFactory.class);

    private final WSManClientFactory m_factory = new CXFWSManClientFactory();

    @Autowired
    private WSManConfigDao m_wsmanConfigDao;

    @Autowired
    private NodeDao m_nodeDao;

    public WsManWQLDetectorFactory() {
        super(WsManWQLDetector.class);
    }

    @Override
    public WsManWQLDetector createDetector(Map<String, String> properties) {
        final WsManWQLDetector detector = new WsManWQLDetector();
        setBeanProperties(detector, properties);
        detector.setClientFactory(m_factory);
        return detector;
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        return new DetectRequestImpl(address, port, WsmanEndpointUtils.toMap(m_wsmanConfigDao.getEndpoint(address)));
    }
}
