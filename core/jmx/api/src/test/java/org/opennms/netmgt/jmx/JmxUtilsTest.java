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
package org.opennms.netmgt.jmx;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JmxUtilsTest {

    @Test
    public void testConvert() {
        Map<String, Object> input = new HashMap<>();
        input.put("1", "1 Value");
        input.put("2", "2 Value");
        input.put("3", 99);

        Map<String, String> output = JmxUtils.convertToUnmodifiableStringMap(input);
        Assert.assertNotNull(output);
        Assert.assertEquals(3, output.size());

        Assert.assertEquals("1 Value", output.get("1"));
        Assert.assertEquals("2 Value", output.get("2"));

        Assert.assertEquals("99", output.get("3"));
    }

    @Test
    public void testNotModifiable() {
        Map<String, Object> input = new HashMap<>();
        input.put("A", "VALUE");

        Map<String, String> output = JmxUtils.convertToUnmodifiableStringMap(input);

        try {
            output.put("4", "4 Value");
            Assert.fail("The converted output map should not be modifiable");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void testNullInput() {
        Map<String, String> output = JmxUtils.convertToUnmodifiableStringMap(null);
        Assert.assertNull(output);
    }

    @Test
    public void testGetCollectionDirectory() {
        Map<String, String> input = new HashMap<>();
        input.put("port", "100");

        String collectionDir = JmxUtils.getCollectionDirectory(input, "ulf", "alf");
        Assert.assertEquals("ulf", collectionDir);

        String collectionDir2 = JmxUtils.getCollectionDirectory(input, null, null);
        Assert.assertEquals("100", collectionDir2);

        String collectionDir3 = JmxUtils.getCollectionDirectory(input, null, "alf");
        Assert.assertEquals("alf", collectionDir3);

        String collectionDir4 = JmxUtils.getCollectionDirectory(input, "ulf", null);
        Assert.assertEquals("ulf", collectionDir4);

        try {
            JmxUtils.getCollectionDirectory(null, null, null);
            Assert.fail("NullPointerException should have been thrown.");
        } catch (NullPointerException npe) {

        }

        String collectionDir5 = JmxUtils.getCollectionDirectory(new HashMap<String, String>(), null, null);
        Assert.assertEquals(null, collectionDir5);
    }

}
