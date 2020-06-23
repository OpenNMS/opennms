/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DatacollectionConfigTest extends XmlTestNoCastor<DatacollectionConfig> {

    public DatacollectionConfigTest(final DatacollectionConfig sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final DatacollectionConfig config = new DatacollectionConfig();
        config.setRrdRepository("${install.share.dir}/rrd/snmp/");
        
        final SnmpCollection collection = new SnmpCollection();
        collection.setName("default");
        collection.setSnmpStorageFlag("select");
        
        final Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        rrd.addRra("RRA:AVERAGE:0.5:288:366");
        rrd.addRra("RRA:MAX:0.5:288:366");
        rrd.addRra("RRA:MIN:0.5:288:366");
        collection.setRrd(rrd);

        final IncludeCollection includeCollection = new IncludeCollection();
        includeCollection.setDataCollectionGroup("MIB2");
        
        collection.addIncludeCollection(includeCollection);

        config.addSnmpCollection(collection);

        return Arrays.asList(new Object[][] { {
                config,
                "<datacollection-config rrdRepository=\"${install.share.dir}/rrd/snmp/\">\n" + 
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
                "  </snmp-collection>\n" +
                "</datacollection-config>\n",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
