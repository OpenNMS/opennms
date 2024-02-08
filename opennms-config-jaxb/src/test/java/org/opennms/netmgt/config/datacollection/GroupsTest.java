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

public class GroupsTest extends XmlTestNoCastor<Groups> {

    public GroupsTest(final Groups sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Groups groups = new Groups();
        
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

        groups.addGroup(group);

        return Arrays.asList(new Object[][] { {
                groups,
                "    <groups>\n" +
                "      <group name=\"windows-host\" ifType=\"ignore\">\n" + 
                "        <mibObj oid=\".1.3.6.1.2.1.25.3.3.1.2\" instance=\"1\" alias=\"cpuPercentBusy\" type=\"integer\" />\n" + 
                "        <mibObj oid=\".1.3.6.1.2.1.25.2.2\"     instance=\"0\" alias=\"memorySize\"     type=\"integer\" />\n" + 
                "        <includeGroup>foo</includeGroup>\n" +
                "      </group>\n" +
                "    </groups>\n",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
