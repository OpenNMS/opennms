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
package org.opennms.features.namecutter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.*;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
public class NameCutterTest {

    private static Map<String, String> dictionary = new HashMap<String, String>();

    private NameCutter nameCutter = new NameCutter();

    @BeforeClass
    public static void setUpClass() throws Exception {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/dictionary.properties")));
        properties.load(stream);
        stream.close();
        for (Object key : properties.keySet()) {
            dictionary.put(key.toString(), properties.get(key).toString());
            System.out.println(key.toString() + "\t" + properties.get(key).toString());
        }
    }

    @Before
    public void setUp() {
        nameCutter = new NameCutter();
        nameCutter.setDictionary(dictionary);
    }

    @Test
    public void testTrimByDictionary() {
        Assert.assertEquals("Blo", nameCutter.trimByDictionary("Bloom"));
        Assert.assertEquals("Tok", nameCutter.trimByDictionary("Token"));

        Assert.assertEquals("CommitVirtMemSize", nameCutter.trimByDictionary("CommittedVirtualMemorySize"));
        Assert.assertEquals("AvgCompRatio" , nameCutter.trimByDictionary("AverageCompressionRatio"));
        Assert.assertEquals("AllIdntToknzCnt" , nameCutter.trimByDictionary("AllIdentityTokenizedCount"));
    }

    @Test
    public void testTrimByCamelCase() {
        Assert.assertEquals("CommitteVirtMemSize", nameCutter.trimByCamelCase("CommittedVirtMemSize", 19));
        Assert.assertEquals("CommiVirtuMemorSize", nameCutter.trimByCamelCase("CommittedVirtualMemorySize", 19));
        Assert.assertEquals("AllIdentTokeniCount", nameCutter.trimByCamelCase("AllIdentityTokenizedCount", 19));
    }
}
