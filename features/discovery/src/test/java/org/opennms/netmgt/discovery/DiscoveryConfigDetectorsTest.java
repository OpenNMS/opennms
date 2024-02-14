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
package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.Detector;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeUrl;

public class DiscoveryConfigDetectorsTest {


    @Test
    public void testGetDetectorsFromDefinitions() throws IOException {

        String resourcePath = DiscoveryConfigDetectorsTest.class.getResource("/mock/etc/discovery-configuration.xml").getPath();
        Path etcPath = Paths.get(resourcePath).getParent().getParent();
        System.setProperty("opennms.home", etcPath.toString());
        DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory();
        DiscoveryConfiguration config = configFactory.getConfiguration();
        // Check that configuration loaded properly from the test resources
        assertEquals(2, config.getDefinitions().size());
        // Get first definition and add include-url
        URL url = DiscoveryConfigDetectorsTest.class.getResource("/mock/include-url/include-url-example.txt");
        IncludeUrl includeUrl = new IncludeUrl();
        includeUrl.setUrl(url.toString());
        config.getDefinitions().get(0).addIncludeUrl(includeUrl);
        configFactory.saveConfiguration(config);
        configFactory.reload();

        //Test specifics from definition.
        List<Detector> detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("10.0.0.3"), "Minion");
        assertEquals(1, detectors.size());

        //Test range from definition.
        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("192.168.0.98"), "Minion");
        assertEquals(1, detectors.size());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("192.168.0.98"), null);
        assertEquals(0, detectors.size());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("192.165.0.98"), null);
        assertEquals(1, detectors.size());

        //Test include urls.
        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("fe80:0000:0000:0000:ffff:eeee:dddd:cccc"), "Minion");
        assertEquals(1, detectors.size());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("8.8.8.8"), "Minion");
        assertEquals(1, detectors.size());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("::1"), "Minion");
        assertEquals(1, detectors.size());
    }
}
