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

public class MibObjTest extends XmlTestNoCastor<MibObj> {

    public MibObjTest(final MibObj sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final MibObj obj = new MibObj();
        obj.setAlias("cyPMSerialPortNum");
        obj.setInstance("cyPMSerialPortNum");
        obj.setOid(".1.3.6.1.4.1.2925.4.5.2.1.1");
        obj.setType("string");
        obj.setMaxval("4294967295");
        obj.setMinval("0");

        return Arrays.asList(new Object[][] { {
                obj,
                "<mibObj oid=\".1.3.6.1.4.1.2925.4.5.2.1.1\" instance=\"cyPMSerialPortNum\" alias=\"cyPMSerialPortNum\" type=\"string\" maxval=\"4294967295\" minval=\"0\" />",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
