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

package org.opennms.netmgt.provision.detector.wsman;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.support.DetectRequestImpl;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WsManDetectorFactory extends GenericServiceDetectorFactory<WsManDetector> {
    private static final Logger LOG = LoggerFactory.getLogger(WsManDetectorFactory.class);

    private final WSManClientFactory m_factory = new CXFWSManClientFactory();

    @Autowired
    private WSManConfigDao m_wsmanConfigDao;

    @Autowired
    private NodeDao m_nodeDao;

    public WsManDetectorFactory() {
        super(WsManDetector.class);
    }

    @Override
    public WsManDetector createDetector() {
        final WsManDetector detector = new WsManDetector();
        detector.setClientFactory(m_factory);
        return detector;
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        return new DetectRequestImpl(address, port, WsmanEndpointUtils.toMap(m_wsmanConfigDao.getEndpoint(address)));
    }

    /**
     * Stores the product vendor and product version in the node assets table
     * after the service was successfully detected.
     */
    @Override
    @Transactional
    public void afterDetect(DetectRequest request, DetectResults results, Integer nodeId) {
        if (!results.isServiceDetected() || nodeId == null) {
            return;
        }

        final boolean updateAssets = Boolean.parseBoolean(results.getServiceAttributes().getOrDefault(WsManDetector.UPDATE_ASSETS, "false"));
        final String productVendor = results.getServiceAttributes().get(WsManDetector.PRODUCT_VENDOR);
        final String productVersion = results.getServiceAttributes().get(WsManDetector.PRODUCT_VERSION);

        if (!updateAssets) {
            LOG.info("Asset updates disabled.");
            return;
        }

        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            LOG.warn("No node was found with id: {}", nodeId);
            return;
        }

        LOG.debug("Updating vendor and modelNumber assets on node[{}] with '{}' and '{}'",
                nodeId, productVendor, productVersion);
        node.getAssetRecord().setVendor(productVendor);
        node.getAssetRecord().setModelNumber(productVersion);
        m_nodeDao.update(node);
    }
}
