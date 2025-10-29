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
package org.opennms.protocols.json.collector;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.protocols.json.collector.JsonCollectorITCase;
import org.opennms.protocols.json.collector.MockDocumentBuilder;

/**
 * The Test class for JSON Collector for Solaris Zones Statistics
 * 
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JsonCollectorSolarisZonesIT extends JsonCollectorITCase {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getConfigFileName() {
        return "src/test/resources/solaris-zones-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getSampleFileName() {
        return "src/test/resources/solaris-zones.json";
    }

    /**
     * Test to verify XPath content.
     *
     * @throws Exception the exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testXpath() throws Exception {
        JSONObject json = MockDocumentBuilder.getJSONDocument();
        JXPathContext context = JXPathContext.newContext(json);
        Iterator<Pointer> itr = context.iteratePointers("/zones/zone");
        while (itr.hasNext()) {
            Pointer resPtr = itr.next();
            JXPathContext relativeContext = context.getRelativeContext(resPtr);
            String resourceName = (String) relativeContext.getValue("@name");
            Assert.assertNotNull(resourceName);
            String value = (String) relativeContext.getValue("parameter[@key='nproc']/@value");
            Assert.assertNotNull(Integer.valueOf(value));
        }
    }

    /**
     * Test JSON collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Solaris");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");
        // Files expected: one JRB for each zone: global, zone1 and zone2 (3 in total)
        executeCollectorTest(parameters, 3);
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/solarisZoneStats/global/solaris-zone-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/solarisZoneStats/zone1/solaris-zone-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/solarisZoneStats/zone2/solaris-zone-stats.rrd").exists());
        // Checking data from Global Zone.
        File file = new File(getSnmpRootDirectory(), "1/solarisZoneStats/global/solaris-zone-stats.rrd");
        String[] dsnames = new String[] { "nproc", "nlwp", "pr_size", "pr_rssize", "pctmem", "pctcpu" };
        Double[] dsvalues = new Double[] { 245.0, 1455.0, 2646864.0, 1851072.0, 0.7, 0.24 };
        validateRrd(file, dsnames, dsvalues);
        // Checking data from Zone 1
        file = new File(getSnmpRootDirectory(), "1/solarisZoneStats/zone1/solaris-zone-stats.rrd");
        dsnames = new String[] { "nproc", "nlwp", "pr_size", "pr_rssize", "pctmem", "pctcpu" };
        dsvalues = new Double[] { 24.0, 328.0, 1671128.0, 1193240.0, 0.4, 0.07 };
        validateRrd(file, dsnames, dsvalues);
        // Checking data from Zone 2
        file = new File(getSnmpRootDirectory(), "1/solarisZoneStats/zone2/solaris-zone-stats.rrd");
        dsnames = new String[] { "nproc", "nlwp", "pr_size", "pr_rssize", "pctmem", "pctcpu" };
        dsvalues = new Double[] { 124.0, 1328.0, 1571128.0, 193240.0, 0.5, 0.06 };
        validateRrd(file, dsnames, dsvalues);
    }

}
