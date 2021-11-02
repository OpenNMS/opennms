/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.geoip;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.model.OnmsAssetRecord;

import com.google.common.collect.Lists;

public class GeoIpConfigTest  extends XmlTestNoCastor<GeoIpConfig> {

    public GeoIpConfigTest(GeoIpConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/geoip-adapter-configuration.xsd");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                {
                        getConfig(),
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                "<geoip-config enabled=\"true\" overwrite=\"false\" database=\"/foo/bar\" resolve=\"public\">\n" +
                                "    <location name=\"Default\">\n" +
                                "        <subnet cidr=\"192.168.0.0/16\"/>\n" +
                                "        <subnet cidr=\"172.16.0.0/16\">\n" +
                                "            <asset name=\"city\" value=\"Fulda\"/>\n" +
                                "            <asset name=\"building\" value=\"51\"/>\n" +
                                "        </subnet>\n" +
                                "    </location>\n" +
                                "</geoip-config>"
                }
        });
    }

    private static GeoIpConfig getConfig() {
        final GeoIpConfig geoIpConfig = new GeoIpConfig();
        geoIpConfig.setDatabase("/foo/bar");
        geoIpConfig.setEnabled(true);
        geoIpConfig.setResolve(GeoIpConfig.Resolve.PUBLIC);

        final Location location = new Location();

        location.setName("Default");

        final Subnet entry1 = new Subnet();
        entry1.setCidr("192.168.0.0/16");

        final Subnet entry2 = new Subnet();
        entry2.setCidr("172.16.0.0/16");
        entry2.getAssets().add(new Asset("city", "Fulda"));
        entry2.getAssets().add(new Asset("building", "51"));


        location.setSubnets(Lists.newArrayList(entry1, entry2));

        geoIpConfig.getLocations().add(location);

        return geoIpConfig;
    }
}