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

public class NMS14559IT extends JsonCollectorITCase {

    @Override
    public String getConfigFileName() {
        return "src/test/resources/NMS-14559-xml-datacollection-config.xml";
    }

    @Override
    public String getSampleFileName() {
        return "src/test/resources/NMS-14559.json";
    }

    @Test
    public void testDefaultXmlCollector() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("collection", "json-mapping-test");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");

        executeCollectorTest(parameters, 4);

        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/blupp/json-mapping-test.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/bar/json-mapping-test.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/foo/json-mapping-test.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/input/bla/json-mapping-test.rrd").exists());

        validateRrd(new File(getSnmpRootDirectory(), "1/input/foo/json-mapping-test.rrd"), new String[]{"input", "read", "write"}, new Double[]{100.0, 10.0, 10.0});
        validateRrd(new File(getSnmpRootDirectory(), "1/input/bar/json-mapping-test.rrd"), new String[]{"input", "read", "write"}, new Double[]{200.0, 20.0, 20.0});
        validateRrd(new File(getSnmpRootDirectory(), "1/input/blupp/json-mapping-test.rrd"), new String[]{"input", "read", "write"}, new Double[]{300.0, 30.0, 30.0});
        validateRrd(new File(getSnmpRootDirectory(), "1/input/bla/json-mapping-test.rrd"), new String[]{"input", "read", "write"}, new Double[]{400.0, 40.0, 40.0});
    }

}
