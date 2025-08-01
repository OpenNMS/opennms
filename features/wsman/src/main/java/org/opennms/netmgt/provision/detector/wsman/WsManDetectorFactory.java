/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    public WsManDetector createDetector(Map<String, String> properties) {
        final WsManDetector detector = new WsManDetector();
        setBeanProperties(detector, properties);
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
