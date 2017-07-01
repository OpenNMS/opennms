/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.categories;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class CategoriesTest extends XmlTestNoCastor<Catinfo> {

    public CategoriesTest(final Catinfo sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Catinfo ci = new Catinfo("1.3", "Wednesday, February 6, 2002 10:10:00 AM EST", "checkers");

        final CategoryGroup cg = new CategoryGroup("WebConsole");
        cg.setComment("Service Level Availability by Functional Group");
        cg.setCommonRule("IPADDR != '0.0.0.0'");

        final List<Category> cats = new ArrayList<>();
        cats.add(new Category("Overall Service Availability", "This category reflects availability of all services currently being monitored by OpenNMS.", 99.99d, 97d, "IPADDR != '0.0.0.0'"));
        cats.add(new Category("Network Interfaces", "This category reflects the ability to 'ping' managed devices and SNMP agents.  'Ping', using the ICMP protocol, tests a devices network connectivity/availability.", 99.99d, 97d, "(isICMP | isSNMP) & (IPADDR != '0.0.0.0')", "ICMP", "SNMP"));
        cg.setCategories(cats);

        ci.addCategoryGroup(cg);

        return Arrays.asList(new Object[][] {
            {
                ci,
                "<catinfo>\n" + 
                        "    <header>\n" + 
                        "        <rev>1.3</rev>\n" + 
                        "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                        "        <mstation>checkers</mstation>\n" + 
                        "    </header>\n" + 
                        "    <categorygroup>\n" + 
                        "        <name>WebConsole</name>\n" + 
                        "        <comment>Service Level Availability by Functional Group</comment>\n" + 
                        "        <common>\n" + 
                        "            <rule><![CDATA[IPADDR != '0.0.0.0']]></rule>\n" + 
                        "        </common>\n" + 
                        "        <categories>\n" + 
                        "            <category>\n" + 
                        "                <label><![CDATA[Overall Service Availability]]></label>\n" + 
                        "                <comment>This category reflects availability of all services currently being monitored by OpenNMS.</comment>\n" + 
                        "                <normal>99.99</normal>\n" + 
                        "                <warning>97.0</warning>\n" + 
                        "                <rule><![CDATA[IPADDR != '0.0.0.0']]></rule>\n" + 
                        "            </category>\n" + 
                        "            <category>\n" + 
                        "                <label><![CDATA[Network Interfaces]]></label>\n" + 
                        "                <comment>This category reflects the ability to 'ping' managed devices and SNMP agents.  'Ping', using the ICMP protocol, tests a devices network connectivity/availability.</comment>\n" + 
                        "                <normal>99.99</normal>\n" + 
                        "                <warning>97.0</warning>\n" + 
                        "                <service>ICMP</service>\n" + 
                        "                <service>SNMP</service>\n" + 
                        "                <rule><![CDATA[(isICMP | isSNMP) & (IPADDR != '0.0.0.0')]]></rule>\n" + 
                        "            </category>\n" + 
                        "        </categories>\n" + 
                        "    </categorygroup>\n" + 
                        "</catinfo>",
                        "src/main/resources/xsds/categories.xsd",
            }
        });
    }
}
