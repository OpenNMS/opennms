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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * The Class NumericNodeLevelDataTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NumericNodeLevelDataTest extends JsonCollectorITCase {

    /* (non-Javadoc)
     * @see org.opennms.protocols.json.collector.AbstractJsonCollectorTest#getJSONConfigFileName()
     */
    @Override
    public String getConfigFileName() {
        return "src/test/resources/sample-node-level-data.xml";
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.json.collector.AbstractJsonCollectorTest#getJSONSampleFileName()
     */
    @Override
    public String getSampleFileName() {
        return "src/test/resources/sample-node-level-data.json";
    }

    /**
     * Test default JSON collector.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDefaultJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Jeff");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");
        executeCollectorTest(parameters, 1);
        File file = new File(getSnmpRootDirectory(), "1/natStats.rrd");
        Assert.assertTrue(file.exists());
        String[] dsnames = new String[] { "ariNatTotalConx", "ariNatConnLimit" };
        Double[] dsvalues = new Double[] { 10.0, 20.0 };
        validateRrd(file, dsnames, dsvalues);
    }

}
