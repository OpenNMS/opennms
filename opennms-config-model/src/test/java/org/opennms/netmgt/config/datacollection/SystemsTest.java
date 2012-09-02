/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.SystemDef;

public class SystemsTest extends XmlTest<Systems> {

    public SystemsTest(final Systems sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Systems systems = new Systems();

        final SystemDef riverbed = new SystemDef();
        riverbed.setName("Riverbed Steelhead WAN Accelerators");
        riverbed.setSysoid(".1.3.6.1.4.1.17163.1.1");
        final Collect riverbedCollect = new Collect();
        riverbedCollect.addIncludeGroup("mib2-X-interfaces");
        riverbedCollect.addIncludeGroup("riverbed-steelhead-scalars");
        riverbedCollect.addIncludeGroup("riverbed-steelhead-cpu-stats");
        riverbedCollect.addIncludeGroup("riverbed-steelhead-port-bandwidth");
        riverbed.setCollect(riverbedCollect);

        final SystemDef lexmark = new SystemDef();
        lexmark.setName("Lexmark / Dell Printers");
        lexmark.setSysoidMask(".1.3.6.1.4.1.641.");
        final Collect lexmarkCollect = new Collect();
        lexmarkCollect.addIncludeGroup("printer-usage");
        lexmarkCollect.addIncludeGroup("printer-mib-supplies");
        lexmark.setCollect(lexmarkCollect);
        
        systems.addSystemDef(riverbed);
        
        return Arrays.asList(new Object[][] {
            {
                systems,
                "<systems>\n" +
                "      <systemDef name=\"Riverbed Steelhead WAN Accelerators\">\n" + 
                "        <sysoid>.1.3.6.1.4.1.17163.1.1</sysoid>\n" + 
                "        <collect>\n" + 
                "          <includeGroup>mib2-X-interfaces</includeGroup>\n" + 
                "          <includeGroup>riverbed-steelhead-scalars</includeGroup>\n" + 
                "          <includeGroup>riverbed-steelhead-cpu-stats</includeGroup>\n" + 
                "          <includeGroup>riverbed-steelhead-port-bandwidth</includeGroup>\n" + 
                "        </collect>\n" + 
                "      </systemDef>\n" +
                "</systems>\n",
                "target/classes/xsds/datacollection-config.xsd"
            }
        });
    }


}
