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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.protocols.xml.config.XmlGroup;

import net.sf.json.JSONObject;

/**
 * Tests the JSON collector against a document whose keys are not valid XML names.
 * <p>JSON object keys may start with a digit or contain characters that XML does not allow in an
 * element name. Those keys have to be usable in the xpath expressions of the data collection
 * configuration; see {@link JsonXpathRewriter}.</p>
 *
 * @author <a href="mailto:christian@opennms.com">Christian Pape</a>
 */
public class NMS15222IT extends JsonCollectorITCase {

    @Override
    public String getConfigFileName() {
        return "src/test/resources/NMS-15222-xml-datacollection-config.xml";
    }

    @Override
    public String getSampleFileName() {
        return "src/test/resources/NMS-15222.json";
    }

    /**
     * Tests the JSON collector against keys that are not valid XML names.
     *
     * @throws Exception the exception
     */
    @Test
    public void testJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("collection", "invalid-json-names");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");

        // 1 node level resource, plus 2 resources for each of the two multi-instance groups
        executeCollectorTest(parameters, 5);

        // a key starting with a digit in the middle of an object xpath
        validateJrb(new File(getSnmpRootDirectory(), "1/http-stats.jrb"),
                new String[] { "ok", "failed" }, new Double[] { 10.0, 3.0 });

        // A key starting with a digit that holds an array has to produce one resource per element.
        // If this ever collapses to a single resource, the rewritten form in JsonXpathRewriter no
        // longer behaves like a plain location step.
        validateJrb(new File(getSnmpRootDirectory(), "1/invalidNameIf/eth0/if-stats.jrb"),
                new String[] { "oneMin", "obrien" }, new Double[] { 1.0, 11.0 });
        validateJrb(new File(getSnmpRootDirectory(), "1/invalidNameIf/eth1/if-stats.jrb"),
                new String[] { "oneMin", "obrien" }, new Double[] { 2.0, 22.0 });

        // the same array, addressed through the multiple-key code path, where one of the
        // key xpaths itself starts with a digit
        validateJrb(new File(getSnmpRootDirectory(), "1/invalidNameIf2/eth0_alpha/if-keyed.jrb"),
                new String[] { "oneMin" }, new Double[] { 1.0 });
        validateJrb(new File(getSnmpRootDirectory(), "1/invalidNameIf2/eth1_beta/if-keyed.jrb"),
                new String[] { "oneMin" }, new Double[] { 2.0 });
    }

    /**
     * Tests that a timestamp xpath pointing at a key starting with a digit is resolved.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTimestampXpath() throws Exception {
        JSONObject json = MockDocumentBuilder.getJSONDocument();
        JXPathContext context = JXPathContext.newContext(json);
        context.setLenient(true);

        XmlGroup group = new XmlGroup();
        group.setTimestampXpath("/collectedAt/2timestamp");
        group.setTimestampFormat("yyyy-MM-dd HH:mm:ss");

        Date timestamp = new DefaultJsonCollectionHandler().getTimeStamp(context, group);
        Assert.assertNotNull(timestamp);
        Assert.assertEquals("2024-01-02 03:04:05",
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp));
    }
}
