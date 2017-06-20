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
