/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.enlinkd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EnlinkdConfigurationTest extends XmlTestNoCastor<EnlinkdConfiguration> {

    public EnlinkdConfigurationTest(EnlinkdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/enlinkd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<enlinkd-configuration threads=\"5\" \n" + 
                "                     initial_sleep_time=\"60000\"\n" + 
                "                     rescan_interval=\"86400000\" \n" + 
                "                     use-cdp-discovery=\"true\"\n" + 
                "                     use-bridge-discovery=\"true\"\n" + 
                "                     use-lldp-discovery=\"true\"\n" + 
                "                     use-ospf-discovery=\"true\"\n" + 
                "                     use-isis-discovery=\"true\"\n" + 
                "                     />"
            }
        });
    }

    private static EnlinkdConfiguration getConfig() {
        EnlinkdConfiguration config = new EnlinkdConfiguration();
        config.setThreads(5);
        config.setInitialSleepTime(60000L);
        config.setRescanInterval(86400000L);
        config.setUseCdpDiscovery(true);
        config.setUseBridgeDiscovery(true);
        config.setUseLldpDiscovery(true);
        config.setUseOspfDiscovery(true);
        config.setUseIsisDiscovery(true);
        return config;
    }
}
