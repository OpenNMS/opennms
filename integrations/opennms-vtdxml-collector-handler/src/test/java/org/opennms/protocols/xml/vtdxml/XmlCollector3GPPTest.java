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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 * The Test class for XML Collector for 3GPP Statistics
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
public class XmlCollector3GPPTest extends AbstractVTDXmlCollectorTest {

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlConfigFileName()
     */
    @Override
    public String getXmlConfigFileName() {
        return "../../protocols/xml/src/test/resources/3gpp-xml-datacollection-config.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbcstractXmlCollectorTest#getXmlSampleFileName()
     */
    @Override
    public String getXmlSampleFileName() {
        return "../../protocols/xml/src/test/resources/A20111025.0030-0500-0045-0500_MME00001.xml";
    }

    /**
     * Test time parser.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTimeParser() throws Exception {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        String value = "2011-10-25T00:45:00-05:00";
        long expectedTimestamp = 1319521500000l;
        DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = dtf.parseDateTime(value);
        Date date = dateTime.toDate();
        Assert.assertEquals(expectedTimestamp, date.getTime());

        MockDefaultVTDXmlCollectionHandler handler = new MockDefaultVTDXmlCollectionHandler();
        date = handler.getTimeStamp(MockDocumentBuilder.getVTDXmlDocument(), getConfigDao().getDataCollectionByName("3GPP").getXmlSources().get(0).getXmlGroups().get(0));
        Assert.assertEquals(expectedTimestamp, date.getTime());
    }

    /**
     * Test XML collector with Standard handler.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "3GPP");
        parameters.put("handler-class", "org.opennms.protocols.xml.vtdxml.MockDefaultVTDXmlCollectionHandler");
        executeCollectorTest(parameters, 147);
        // Test a JRB.
        File file = new File(getSnmpRoot(), "1/platformSystemResource/processor_v1_frame0_shelf0_slot4_sub-slot1/platform-system-resource.jrb");
        String[] dsnames = new String[] { "cpuUtilization", "memoryUtilization" };
        Double[] dsvalues = new Double[] { 1.0, 18.0 };
        validateJrb(file, dsnames, dsvalues);
    }

}
