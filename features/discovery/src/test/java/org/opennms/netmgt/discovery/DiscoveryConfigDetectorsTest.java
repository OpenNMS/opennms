/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

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
        Optional<List<Detector>> detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("10.0.0.3"), "Minion");
        assertTrue(detectors.isPresent());
        assertEquals(1, detectors.get().size());

        //Test range from definition.
        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("192.168.0.98"), "Minion");
        assertTrue(detectors.isPresent());
        assertEquals(1, detectors.get().size());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("192.168.0.98"), null);
        assertFalse(detectors.isPresent());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("192.165.0.98"), null);
        assertTrue(detectors.isPresent());
        assertEquals(1, detectors.get().size());

        //Test include urls.
        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("fe80:0000:0000:0000:ffff:eeee:dddd:cccc"), "Minion");
        assertTrue(detectors.isPresent());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("8.8.8.8"), "Minion");
        assertTrue(detectors.isPresent());

        detectors = configFactory.getListOfDetectors(InetAddressUtils.addr("::1"), "Minion");
        assertTrue(detectors.isPresent());
    }
}
