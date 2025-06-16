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
package org.opennms.netmgt.config.snmpAsset.adapter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SnmpAssetAdapterConfigurationTest extends XmlTestNoCastor<SnmpAssetAdapterConfiguration> {

    public SnmpAssetAdapterConfigurationTest(SnmpAssetAdapterConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/snmp-asset-adapter-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<snmp-asset-adapter-configuration>\n" + 
                "  <package name=\"PJs\">\n" +
                "    <sysoid>.1.3</sysoid>\n" +
                "    <!-- IP address filtering not supported yet -->\n" + 
                "    <!-- <include-range begin=\"0.0.0.0\" end=\"255.255.255.254\" /> -->\n" + 
                "    <assetField name=\"comment\" formatString=\"OS Type: ${osType}\">\n" + 
                "      <mibObjs>\n" + 
                "        <mibObj oid=\".1.3.6.1.4.1.33347.255.1\" alias=\"osType\"/>\n" + 
                "      </mibObjs>\n" + 
                "    </assetField>\n" + 
                "  </package>\n" + 
                "</snmp-asset-adapter-configuration>"
            }
        });
    }

    private static SnmpAssetAdapterConfiguration getConfig() {
        SnmpAssetAdapterConfiguration config = new SnmpAssetAdapterConfiguration();
        
        Package pkg = new Package();
        pkg.setName("PJs");
        pkg.setSysoid(".1.3");
        config.addPackage(pkg);

        AssetField field = new AssetField();
        field.setName("comment");
        field.setFormatString("OS Type: ${osType}");
        pkg.addAssetField(field);

        MibObj obj = new MibObj();
        obj.setOid(".1.3.6.1.4.1.33347.255.1");
        obj.setAlias("osType");
        field.addMibObj(obj);

        return config;
    }
}
