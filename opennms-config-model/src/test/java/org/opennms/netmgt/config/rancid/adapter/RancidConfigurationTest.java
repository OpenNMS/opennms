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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.rancid.adapter;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class RancidConfigurationTest extends XmlTestNoCastor<RancidConfiguration> {

    public RancidConfigurationTest(RancidConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/rancid-adapter-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getConfig(),
                    "<rancid-configuration delay=\"60\" retries=\"1\">\n" + 
                    "  <mapping sysoid-mask=\".1.3.6.1.4.1.9.1\" type=\"cisco\"/>\n" + 
                    "</rancid-configuration>"
                }
        });
    }

    private static RancidConfiguration getConfig() {
        RancidConfiguration config = new RancidConfiguration();
        config.setDelay(60L);
        config.setRetries(1);

        Mapping mapping = new Mapping();
        mapping.setSysoidMask(".1.3.6.1.4.1.9.1");
        mapping.setType("cisco");
        config.addMapping(mapping);
        
        return config;
    }
}
