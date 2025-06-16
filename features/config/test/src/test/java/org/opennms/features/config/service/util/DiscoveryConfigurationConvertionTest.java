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
package org.opennms.features.config.service.util;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryConfigurationConvertionTest {
    private String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<discovery-configuration\n" +
            "        xmlns=\"http://xmlns.opennms.org/xsd/config/discovery\"\n" +
            "        location=\"localhost\" packets-per-second=\"1.0\"\n" +
            "        initial-sleep-time=\"30000\" restart-sleep-time=\"86400000\">\n" +
            "    <specific location=\"localhost\" retries=\"1\" timeout=\"2000\">10.10.10.1</specific>\n" +
            "    <specific location=\"pittsboro\" retries=\"1\" timeout=\"2000\">10.10.20.1</specific>\n" +
            "    <specific>10.10.30.1</specific>\n" +
            "    <include-range location=\"localhost\" retries=\"1\" timeout=\"2000\">\n" +
            "        <begin>10.10.10.10</begin>\n" +
            "        <end>10.10.10.20</end>\n" +
            "    </include-range>\n" +
            "    <include-range location=\"pittsboro\" retries=\"1\" timeout=\"2000\">\n" +
            "        <begin>10.10.20.10</begin>\n" +
            "        <end>10.10.20.20</end>\n" +
            "    </include-range>\n" +
            "    <include-range>\n" +
            "        <begin>10.10.30.10</begin>\n" +
            "        <end>10.10.30.20</end>\n" +
            "    </include-range>\n" +
            "    <exclude-range>\n" +
            "        <begin>192.168.10.1</begin>\n" +
            "        <end>192.168.10.10</end>\n" +
            "    </exclude-range>\n" +
            "    <exclude-range>\n" +
            "        <begin>192.168.20.1</begin>\n" +
            "        <end>192.168.20.10</end>\n" +
            "    </exclude-range>\n" +
            "    <exclude-range>\n" +
            "        <begin>192.168.30.1</begin>\n" +
            "        <end>192.168.30.10</end>\n" +
            "    </exclude-range>\n" +
            "    <include-url location=\"localhost\" retries=\"1\" timeout=\"2000\">10.10.10.1/foo.html</include-url>\n" +
            "    <include-url location=\"pittsboro\" retries=\"1\" timeout=\"2000\">10.10.20.1/bar.html</include-url>\n" +
            "    <include-url>10.10.30.1/bar.html</include-url>\n" +
            "</discovery-configuration>";

    private String expectedJson = "{\"initial-sleep-time\":30000,\"include-range\":[{\"retries\":1,\"location\":\"localhost\",\"end\":\"10.10.10.20\",\"begin\":\"10.10.10.10\",\"timeout\":2000},{\"retries\":1,\"location\":\"pittsboro\",\"end\":\"10.10.20.20\",\"begin\":\"10.10.20.10\",\"timeout\":2000},{\"end\":\"10.10.30.20\",\"begin\":\"10.10.30.10\"}],\"exclude-range\":[{\"end\":\"192.168.10.10\",\"begin\":\"192.168.10.1\"},{\"end\":\"192.168.20.10\",\"begin\":\"192.168.20.1\"},{\"end\":\"192.168.30.10\",\"begin\":\"192.168.30.1\"}],\"location\":\"localhost\",\"restart-sleep-time\":86400000,\"specific\":[{\"retries\":1,\"address\":\"10.10.10.1\",\"location\":\"localhost\",\"timeout\":2000},{\"retries\":1,\"address\":\"10.10.20.1\",\"location\":\"pittsboro\",\"timeout\":2000},{\"address\":\"10.10.30.1\"}],\"include-url\":[{\"retries\":1,\"location\":\"localhost\",\"timeout\":2000,\"url\":\"10.10.10.1/foo.html\"},{\"retries\":1,\"location\":\"pittsboro\",\"timeout\":2000,\"url\":\"10.10.20.1/bar.html\"},{\"url\":\"10.10.30.1/bar.html\"}],\"packets-per-second\":1}";

    @Test
    public void testConvert() throws IOException {
        ConfigDefinition def = XsdHelper.buildConfigDefinition("discovery", "discovery-configuration.xsd",
                "discovery-configuration", ConfigurationManagerService.BASE_PATH, false);
        ConfigConverter converter = XsdHelper.getConverter(def);
        String jsonStr = converter.xmlToJson(xmlStr);

        JSONAssert.assertEquals(expectedJson, jsonStr, true);
    }

    @Test
    public void testObjectToJson() {
        DiscoveryConfiguration discoveryConfiguration = new DiscoveryConfiguration();
        discoveryConfiguration.setInitialSleepTime(100L);
        List<IncludeUrl> includeUrls = new ArrayList<>();
        IncludeUrl iurl = new IncludeUrl();
        iurl.setUrl("url");
        iurl.setLocation("location-range");
        includeUrls.add(iurl);
        discoveryConfiguration.setIncludeUrls(includeUrls);
        Specific specific = new Specific();
        specific.setAddress("address");
        specific.setLocation("location");
        specific.setForeignSource("foreign");
        discoveryConfiguration.addSpecific(specific);

        String json = ConfigConvertUtil.objectToJson(discoveryConfiguration);

        DiscoveryConfiguration d2 = ConfigConvertUtil.jsonToObject(json, DiscoveryConfiguration.class);
        Assert.assertEquals(100L, (long) d2.getInitialSleepTime().get());
        Assert.assertEquals("address", d2.getSpecifics().get(0).getAddress());
        Assert.assertEquals("location", d2.getSpecifics().get(0).getLocation().get());
        Assert.assertEquals("url", d2.getIncludeUrls().get(0).getUrl().get());
    }
}
