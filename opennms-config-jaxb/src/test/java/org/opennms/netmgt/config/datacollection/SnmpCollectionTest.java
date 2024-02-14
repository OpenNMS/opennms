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

public class SnmpCollectionTest extends XmlTestNoCastor<SnmpCollection> {

    public SnmpCollectionTest(final SnmpCollection sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final SnmpCollection snmpc = new SnmpCollection();
        snmpc.setName("default");
        snmpc.setSnmpStorageFlag("select");
        
        final Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        rrd.addRra("RRA:AVERAGE:0.5:288:366");
        rrd.addRra("RRA:MAX:0.5:288:366");
        rrd.addRra("RRA:MIN:0.5:288:366");
        snmpc.setRrd(rrd);
        
        final IncludeCollection ic = new IncludeCollection();
        ic.setDataCollectionGroup("MIB2");
        snmpc.addIncludeCollection(ic);

        return Arrays.asList(new Object[][] { {
                snmpc,
                "  <snmp-collection name=\"default\" snmpStorageFlag=\"select\">\n" + 
                "    <rrd step=\"300\">\n" + 
                "      <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" + 
                "      <rra>RRA:AVERAGE:0.5:12:1488</rra>\n" + 
                "      <rra>RRA:AVERAGE:0.5:288:366</rra>\n" + 
                "      <rra>RRA:MAX:0.5:288:366</rra>\n" + 
                "      <rra>RRA:MIN:0.5:288:366</rra>\n" + 
                "    </rrd>\n" + 
                "\n" + 
                "    <include-collection dataCollectionGroup=\"MIB2\"/>\n" + 
                "  </snmp-collection>\n",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
