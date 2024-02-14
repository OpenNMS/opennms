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
package org.opennms.netmgt.config.trend;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class TrendConfigurationTest extends XmlTestNoCastor<TrendConfiguration> {

    public TrendConfigurationTest(final TrendConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Override
    protected boolean ignoreNamespace(final String namespace) {
        return "http://xmlns.opennms.org/xsd/config/trend".equals(namespace);
    }
    
    protected String getSchemaFile() {
        return "target/classes/xsds/trend-configuration.xsd";
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getBasicCollectdConfiguration(),
                        "<?xml version=\"1.0\"?>\n" +
                            "<trend-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trend\">\n" +
                            "    <trend-definition name=\"name1\">\n" +
                            "        <title>title1</title>\n" +
                            "        <subtitle>subtitle1</subtitle>\n" +
                            "        <visible>true</visible>\n" +
                            "        <icon>icon1</icon>\n" +
                            "        <trend-attributes>\n" +
                            "            <trend-attribute key=\"key1\" value=\"value1\"/>\n" +
                            "            <trend-attribute key=\"key2\" value=\"value2\"/>\n" +
                            "        </trend-attributes>\n" +
                            "        <descriptionLink>descriptionLink1</descriptionLink>\n" +
                            "        <description>description1</description>\n" +
                            "        <query>query1</query>\n" +
                            "    </trend-definition>\n" +
                            "</trend-configuration>"
                }
        });
    }

    private static TrendConfiguration getBasicCollectdConfiguration() {
        TrendConfiguration trendConfiguration=new TrendConfiguration();

        TrendDefinition trendDefinition = new TrendDefinition();
        trendDefinition.setName("name1");
        trendDefinition.setTitle("title1");
        trendDefinition.setSubtitle("subtitle1");
        trendDefinition.setVisible(true);
        trendDefinition.setIcon("icon1");
        TrendAttribute trendAttribute1 = new TrendAttribute();
        trendAttribute1.setKey("key1");
        trendAttribute1.setValue("value1");
        TrendAttribute trendAttribute2 = new TrendAttribute();
        trendAttribute2.setKey("key2");
        trendAttribute2.setValue("value2");

        trendDefinition.getTrendAttributes().add(trendAttribute1);
        trendDefinition.getTrendAttributes().add(trendAttribute2);
        trendDefinition.setDescription("description1");
        trendDefinition.setDescriptionLink("descriptionLink1");
        trendDefinition.setQuery("query1");

        trendConfiguration.getTrendDefinitions().add(trendDefinition);

        return trendConfiguration;
    }
}
