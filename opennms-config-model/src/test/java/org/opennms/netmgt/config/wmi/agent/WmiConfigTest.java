/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wmi.agent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class WmiConfigTest extends XmlTestNoCastor<WmiConfig> {

    public WmiConfigTest(WmiConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/wmi-config.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getWmiConfig(),
                /** Example from Wiki **/
                "<wmi-config retry=\"2\" timeout=\"1500\"\n" + 
                "        username=\"monitor\" domain=\"enter-domain-name\" password=\"enter-account-password\">\n" + 
                "    \n" + 
                "    <!-- This definition shows how to specify a user for a range of IP addresses. -->\n" + 
                "    <definition username=\"DomainUserA\" domain=\"MYDOMAIN\" password=\"unsecurepwd\">\n" + 
                "        <range \n" + 
                "            begin=\"192.168.1.1\" end=\"192.168.1.10\"/>\n" + 
                "    </definition>\n" + 
                "    \n" + 
                "    <!-- This definition shows how to specify a user for a specific IP address -->\n" + 
                "    <definition  username=\"BobVilla\" domain=\"MYMACHINENAME\" password=\"buildahouse\">\n" + 
                "        <specific xmlns=\"\">192.168.1.12</specific>\n" + 
                "    </definition>        \n" + 
                "\n" + 
                "</wmi-config>"
            },
            {
                new WmiConfig(),
                "<wmi-config/>"
            }
        });
    }

    private static WmiConfig getWmiConfig() {
        WmiConfig config = new WmiConfig();
        config.setRetry(2);
        config.setTimeout(1500);
        config.setUsername("monitor");
        config.setDomain("enter-domain-name");
        config.setPassword("enter-account-password");

        Definition def = new Definition();
        def.setUsername("DomainUserA");
        def.setDomain("MYDOMAIN");
        def.setPassword("unsecurepwd");
        config.addDefinition(def);

        Range range = new Range();
        range.setBegin("192.168.1.1");
        range.setEnd("192.168.1.10");
        def.addRange(range);

        def = new Definition();
        def.setUsername("BobVilla");
        def.setDomain("MYMACHINENAME");
        def.setPassword("buildahouse");
        def.addSpecific("192.168.1.12");
        config.addDefinition(def);

        return config;
    }
}
