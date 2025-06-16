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