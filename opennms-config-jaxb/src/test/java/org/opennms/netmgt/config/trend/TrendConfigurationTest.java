/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
