/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
