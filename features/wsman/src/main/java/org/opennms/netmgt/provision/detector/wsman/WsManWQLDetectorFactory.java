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
