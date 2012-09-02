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
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;

public class GroupTest extends XmlTest<Group> {

    public GroupTest(final Group sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Group group = new Group();
        group.setName("windows-host");
        group.setIfType("ignore");
        group.addIncludeGroup("foo");

        MibObj obj = new MibObj();
        obj.setOid(".1.3.6.1.2.1.25.3.3.1.2");
        obj.setInstance("1");
        obj.setAlias("cpuPercentBusy");
        obj.setType("integer");
        group.addMibObj(obj);
        
        obj = new MibObj();
        obj.setOid(".1.3.6.1.2.1.25.2.2");
        obj.setInstance("0");
        obj.setAlias("memorySize");
        obj.setType("integer");
        group.addMibObj(obj);

        return Arrays.asList(new Object[][] { {
                group,
                "      <group name=\"windows-host\" ifType=\"ignore\">\n" + 
                "        <mibObj oid=\".1.3.6.1.2.1.25.3.3.1.2\" instance=\"1\" alias=\"cpuPercentBusy\" type=\"integer\" />\n" + 
                "        <mibObj oid=\".1.3.6.1.2.1.25.2.2\"     instance=\"0\" alias=\"memorySize\"     type=\"integer\" />\n" + 
                "        <includeGroup>foo</includeGroup>\n" +
                "      </group>",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
