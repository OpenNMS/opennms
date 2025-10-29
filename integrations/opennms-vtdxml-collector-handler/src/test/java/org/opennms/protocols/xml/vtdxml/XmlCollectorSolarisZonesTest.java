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
package org.opennms.protocols.xml.vtdxml;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The Test class for XML Collector for Solaris Zones Statistics
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
public class XmlCollectorSolarisZonesTest extends AbstractVTDXmlCollectorTest {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getXmlConfigFileName() {
        return "../../protocols/xml/src/test/resources/solaris-zones-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getXmlSampleFileName() {
        return "../../protocols/xml/src/test/resources/solaris-zones.xml";
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Solaris");
        parameters.put("handler-class", "org.opennms.protocols.xml.vtdxml.MockDefaultVTDXmlCollectionHandler");
        // Files expected: one JRB for each zone: global, zone1 and zone2 (3 in total)
        executeCollectorTest(parameters, 3);
        Assert.assertTrue(new File(getSnmpRoot(), "1/solarisZoneStats/global/solaris-zone-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRoot(), "1/solarisZoneStats/zone1/solaris-zone-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRoot(), "1/solarisZoneStats/zone2/solaris-zone-stats.rrd").exists());
        // Checking data from Global Zone.
        File file = new File(getSnmpRoot(), "1/solarisZoneStats/global/solaris-zone-stats.rrd");
        String[] dsnames = new String[] { "nproc", "nlwp", "pr_size", "pr_rssize", "pctmem", "pctcpu" };
        Double[] dsvalues = new Double[] { 245.0, 1455.0, 2646864.0, 1851072.0, 0.7, 0.24 };
        validateRrd(file, dsnames, dsvalues);
    }

}
