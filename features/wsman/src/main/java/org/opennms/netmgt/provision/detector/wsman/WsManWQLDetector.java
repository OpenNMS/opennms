/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2018 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.wsman;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.List;

import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManConstants;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.support.DetectResultsImpl;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

/**
 * Detects a service from the result of a WQL query against a resource URI. 
 *
 * @author jwhite
 * @author dino2gnt
 */
public class WsManWQLDetector extends SyncAbstractDetector {
    public static final Logger LOG = LoggerFactory.getLogger(WsManWQLDetector.class);

    private static String PROTOCOL_NAME = "WSManWQL";
    private String resourceUri = "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*";
    private String wql;

    private WSManClientFactory m_factory;

    public WsManWQLDetector() {
        super(PROTOCOL_NAME, 0);
    }

    public WsManWQLDetector(final String serviceName) {
        super(serviceName, 0);
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

        // Issue the query!
        List<Node> nodes = Lists.newLinkedList();
        final WSManClient client = m_factory.getClient(endpoint);
        try {
            LOG.debug("Issuing an ENUM on '{}' with query '{}'", resourceUri, wql);
            client.enumerateAndPullUsingFilter(resourceUri, WSManConstants.XML_NS_WQL_DIALECT, wql, nodes, true);
        } catch (WSManException e) {
            LOG.debug("ENUM failed for address '{}' with endpoint '{}', resourceUri '{}', query '{}'", address, endpoint, resourceUri, wql, e);
        }
        return new DetectResultsImpl(nodes.size() > 0);
    }

    public void setClientFactory(WSManClientFactory factory) {
        m_factory = factory;
    }

    public void setresourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getresourceUri() {
        return resourceUri;
    }
    public void setwql(String wql) {
        this.wql = wql;
    }

    public String getwql() {
        return wql;
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

