/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmpmetadata;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

import com.google.common.collect.Lists;

public class SnmpMetadataConfigTest  extends XmlTestNoCastor<SnmpMetadataConfig> {

    public SnmpMetadataConfigTest(SnmpMetadataConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/snmp-metadata-adapter-configuration.xsd");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                {
                        getConfig(),
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                "<snmp-metadata-config>\n" +
                                "    <config name=\"rootLevel\" sysObjectId=\".4.3.2.1\" tree=\".1.2.3.4\">\n" +
                                "        <entry tree=\".1\" name=\"entry1\" index=\"false\" exact=\"false\"/>\n" +
                                "        <entry tree=\".2\" name=\"entry2\" index=\"false\" exact=\"false\">\n" +
                                "            <entry tree=\".1\" name=\"entry2_1\" index=\"true\" exact=\"false\"/>\n" +
                                "            <entry tree=\".2\" name=\"entry2_2\" index=\"false\" exact=\"true\"/>\n" +
                                "        </entry>\n" +
                                "    </config>\n" +
                                "    <config name=\"another\" sysObjectId=\".5.4.3.2.1\" tree=\".1.2.3.4.5\">\n" +
                                "        <entry tree=\".1\" name=\"another1\" index=\"false\" exact=\"false\"/>\n" +
                                "    </config>\n" +
                                "</snmp-metadata-config>"
                }
        });
    }

    private static SnmpMetadataConfig getConfig() {
        final SnmpMetadataConfig snmpMetadataConfig = new SnmpMetadataConfig();
        final Config config = new Config();

        config.setTree(".1.2.3.4");
        config.setSysObjectId(".4.3.2.1");
        config.setName("rootLevel");

        final Entry entry1 = new Entry();
        entry1.setName("entry1");
        entry1.setTree(".1");
        entry1.setIndex(false);

        final Entry entry2 = new Entry();
        entry2.setName("entry2");
        entry2.setTree(".2");
        entry2.setIndex(false);

        final Entry entry3 = new Entry();
        entry3.setName("entry2_1");
        entry3.setTree(".1");
        entry3.setIndex(true);

        final Entry entry4 = new Entry();
        entry4.setName("entry2_2");
        entry4.setTree(".2");
        entry4.setExact(true);
        entry4.setIndex(false);

        entry2.setEntries(Lists.newArrayList(entry3, entry4));
        config.setEntries(Lists.newArrayList(entry1, entry2));

        snmpMetadataConfig.getConfigs().add(config);

        final Config anotherConfig = new Config();
        anotherConfig.setTree(".1.2.3.4.5");
        anotherConfig.setSysObjectId(".5.4.3.2.1");
        anotherConfig.setName("another");

        final Entry anotherEntry = new Entry();
        anotherEntry.setName("another1");
        anotherEntry.setTree(".1");
        anotherEntry.setIndex(false);

        anotherConfig.setEntries(Lists.newArrayList(anotherEntry));

        snmpMetadataConfig.getConfigs().add(anotherConfig);

        return snmpMetadataConfig;
    }
}
