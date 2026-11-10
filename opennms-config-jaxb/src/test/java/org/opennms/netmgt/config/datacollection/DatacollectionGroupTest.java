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
package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DatacollectionGroupTest extends XmlTestNoCastor<DatacollectionGroup> {

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
