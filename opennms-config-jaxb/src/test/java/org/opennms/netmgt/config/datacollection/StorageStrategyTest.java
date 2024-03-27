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
package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class StorageStrategyTest extends XmlTestNoCastor<StorageStrategy> {

    public StorageStrategyTest(final StorageStrategy sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final StorageStrategy strategy = new StorageStrategy();
        strategy.setClazz("org.opennms.netmgt.collection.support.IndexStorageStrategy");
        strategy.addParameter(new Parameter("foo", "bar"));

        return Arrays.asList(new Object[][] { {
                strategy,
                "<storageStrategy class=\"org.opennms.netmgt.collection.support.IndexStorageStrategy\"><parameter key=\"foo\" value=\"bar\" /></storageStrategy>",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
