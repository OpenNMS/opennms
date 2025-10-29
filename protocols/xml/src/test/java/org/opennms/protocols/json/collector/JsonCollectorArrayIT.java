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

import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonCollectorArrayIT extends JsonCollectorITCase {

    @Override
    public String getConfigFileName() {
        return "src/test/resources/json-array-datacollection-config.xml";
    }

    @Override
    public String getSampleFileName() {
        return "src/test/resources/array.json";
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testXpath() throws Exception {
        JSONObject json = MockDocumentBuilder.getJSONDocument();
        JXPathContext context = JXPathContext.newContext(json);

        Iterator<Pointer> itr = context.iteratePointers("/elements[4]/it");

        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(itr.next().getValue(), "works");

        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testJsonCollector() throws Exception {
        Map<String, Object> parameters = new HashMap();
        parameters.put("collection", "json-array");
        parameters.put("handler-class", "org.opennms.protocols.json.collector.MockDefaultJsonCollectionHandler");

        executeCollectorTest(parameters, 4);
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/foo/json-array-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/bar/json-array-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/baz/json-array-stats.rrd").exists());
        Assert.assertTrue(new File(getSnmpRootDirectory(), "1/jsonArrayStats/works/json-array-stats.rrd").exists());

        validateRrd(new File(getSnmpRootDirectory(), "1/jsonArrayStats/foo/json-array-stats.rrd"), new String[] {"val"}, new Double[] {0.0});
        validateRrd(new File(getSnmpRootDirectory(), "1/jsonArrayStats/bar/json-array-stats.rrd"), new String[] {"val"}, new Double[] {1.0});
        validateRrd(new File(getSnmpRootDirectory(), "1/jsonArrayStats/baz/json-array-stats.rrd"), new String[] {"val"}, new Double[] {2.0});
        validateRrd(new File(getSnmpRootDirectory(), "1/jsonArrayStats/works/json-array-stats.rrd"), new String[] {"val"}, new Double[] {1337.0});
    }
}
