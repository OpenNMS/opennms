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
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.SystemDef;

public class DatacollectionGroupTest extends XmlTest<DatacollectionGroup> {

    public DatacollectionGroupTest(final DatacollectionGroup sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final DatacollectionGroup dcg = new DatacollectionGroup();
        dcg.setName("Trango");
        
        final Group group = new Group("trangolink45-rssi");
        group.setIfType("ignore");
        group.addMibObj(new MibObj(".1.3.6.1.4.1.5454.1.40.2.12", "0", "trangoRssi", "integer"));
        dcg.addGroup(group);

        final SystemDef systemDef = new SystemDef("TrangoLink-45");
        systemDef.setSysoid(".1.3.6.1.4.1.5454.1.40");
        final Collect collect = new Collect();
        collect.addIncludeGroup("trangolink45-rssi");
        systemDef.setCollect(collect);
        dcg.addSystemDef(systemDef);
        
        return Arrays.asList(new Object[][] { {
                dcg,
                "<datacollection-group name=\"Trango\">\n" + 
                "\n" + 
                "      <group  name=\"trangolink45-rssi\" ifType=\"ignore\">\n" + 
                "        <mibObj oid=\".1.3.6.1.4.1.5454.1.40.2.12\" instance=\"0\" alias=\"trangoRssi\" type=\"integer\" />\n" + 
                "      </group>\n" + 
                "      \n" + 
                "      <systemDef name = \"TrangoLink-45\">\n" + 
                "        <sysoid>.1.3.6.1.4.1.5454.1.40</sysoid>\n" + 
                "        <collect>\n" + 
                "          <includeGroup>trangolink45-rssi</includeGroup>\n" + 
                "        </collect>\n" + 
                "      </systemDef>\n" + 
                "\n" + 
                "</datacollection-group>",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
