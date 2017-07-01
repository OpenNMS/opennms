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

package org.opennms.netmgt.config.threshd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ThreshdConfigurationTest extends XmlTestNoCastor<ThreshdConfiguration> {

    public ThreshdConfigurationTest(ThreshdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/thresholding.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        ThreshdConfiguration threshdConfiguration = new ThreshdConfiguration();
        threshdConfiguration.setThreads(5);
        
        Package pkg = new Package();
        pkg.setName("mib2");
        threshdConfiguration.addPackage(pkg);

        Filter filter = new Filter();
        filter.setContent("IPADDR != '0.0.0.0'");
        pkg.setFilter(filter);

        return Arrays.asList(new Object[][] {
            {
                threshdConfiguration,
                "<threshd-configuration threads=\"5\">\n" + 
                "   <package name=\"mib2\">\n" + 
                "      <filter>IPADDR != '0.0.0.0'</filter>\n" + 
                "   </package>\n" + 
                "</threshd-configuration>"
            }
        });
    }
}
