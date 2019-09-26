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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;

public class PollerConfigReloadIT {

    private PollerConfigManager pollerConfigManager;

    private File includeUrlFile;


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        includeUrlFile = tempFolder.newFile("poller-config-include-url.txt");
        fillInitialData(includeUrlFile);
        InputStream configStream = setIncludeUrlFileInConfig(PollerConfigReloadIT.class.getResource("/poller-configuration.xml"));;
        FilterDao mockFilterDao = Mockito.mock(FilterDao.class);
        List<InetAddress> inetAddressList = new ArrayList<>();
        inetAddressList.add(InetAddressUtils.addr("127.0.0.5"));
        inetAddressList.add(InetAddressUtils.addr("128.0.1.10"));
        inetAddressList.add(InetAddressUtils.addr("128.0.1.9"));
        inetAddressList.add(InetAddressUtils.addr("128.0.1.12"));
        when(mockFilterDao.getActiveIPAddressList(Mockito.anyString())).thenReturn(inetAddressList);
        FilterDaoFactory.setInstance(mockFilterDao);
        pollerConfigManager = new TestPollerConfigFactory(configStream);
    }

    private static class TestPollerConfigFactory extends PollerConfigManager {

        private TestPollerConfigFactory(InputStream stream) {
            super(stream);
        }

        @Override
        protected void saveXml(String xml) throws IOException {
            //pass
        }
    }


    @Test
    public void testPollerConfigReloadForIncludeUrls() throws IOException {
        Package aPackage = pollerConfigManager.getPackage("example1");
        //Verify that ipAddress from url file exists in package.
        assertTrue(pollerConfigManager.isInterfaceInPackage("128.0.1.10", aPackage));
        assertFalse(pollerConfigManager.isInterfaceInPackage("128.0.1.12", aPackage));
        // Include 128.0.1.12 in url file
        updateIpAddressesInUrlFile(includeUrlFile);
        //Call update. Reload Event calls update on the config.
        pollerConfigManager.update();
        assertTrue(pollerConfigManager.isInterfaceInPackage("128.0.1.12", aPackage));
    }


    private void fillInitialData(File includeUrlFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(includeUrlFile.toPath())) {
            writer.write("127.0.0.5");
            writer.newLine();
            writer.write("128.0.1.10");
            writer.newLine();
        }
    }


    private void updateIpAddressesInUrlFile(File includeUrlFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(includeUrlFile.toPath())) {
            writer.write("128.0.1.12");
            writer.newLine();
            writer.write("128.0.1.9");
            writer.newLine();
        }
    }

    private InputStream setIncludeUrlFileInConfig(URL configUrl) throws IOException {
        String marshalledString = IOUtils.toString(configUrl, Charset.defaultCharset());
        String modifiedString = marshalledString.replace("${INCLUDE_URL_FILE}", includeUrlFile.getAbsolutePath());
        return IOUtils.toInputStream(modifiedString, Charset.defaultCharset());
    }
}
