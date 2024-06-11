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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.wsman.Identity;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.support.DetectResultsImpl;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects the presence of the WS-Man service by verifying that the
 * endpoint responds to the Identify command. 
 *
 * @author jwhite
 */
public class WsManDetector extends SyncAbstractDetector {
    public static final Logger LOG = LoggerFactory.getLogger(WsManDetector.class);

    private static final String PROTOCOL_NAME = "WS-Man";
    protected static final String UPDATE_ASSETS = "update-assests";
    protected static final String PRODUCT_VENDOR = "product-vendor";
    protected static final String PRODUCT_VERSION = "product-version";

    private WSManClientFactory m_factory;

    private boolean m_updateAssets = true;

    public WsManDetector() {
        super(PROTOCOL_NAME, 0);
    }

    @Override
    public DetectResults detect(DetectRequest request) {
        try {
            final WSManEndpoint endpoint = WsmanEndpointUtils.fromMap(request.getRuntimeAttributes());
            return isServiceDetected(request.getAddress(), endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isServiceDetected(InetAddress address) {
        throw new UnsupportedOperationException("WSManEndpoint is required.");
    }

    public DetectResults isServiceDetected(InetAddress address, WSManEndpoint endpoint) {
        // Issue the "Identify" request
        final WSManClient client = m_factory.getClient(endpoint);
        Identity identity = null;
        final Map<String, String> attributes = new HashMap<>();
        try {
            identity = client.identify();
            LOG.info("Identify succeeded for address {} with product vendor '{}' and product version '{}'.", address, identity.getProductVendor(), identity.getProductVersion());
            attributes.put(UPDATE_ASSETS, Boolean.toString(m_updateAssets));
            attributes.put(PRODUCT_VENDOR, identity.getProductVendor());
            attributes.put(PRODUCT_VERSION, identity.getProductVersion());
        } catch (WSManException e) {
            LOG.info("Identify failed for address {} with endpoint {}.", address, endpoint, e);
        }
        return new DetectResultsImpl(identity != null, attributes);
    }

    public void setClientFactory(WSManClientFactory factory) {
        m_factory = factory;
    }

    public void setUpdateAssets(boolean updateAssets) {
        m_updateAssets = updateAssets;
    }

    public boolean getUpdateAssets() {
        return m_updateAssets;
    }

    @Override
    protected void onInit() {
        // pass
    }

    @Override
    public void dispose() {
        // pass
    }
}
