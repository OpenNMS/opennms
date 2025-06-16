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

