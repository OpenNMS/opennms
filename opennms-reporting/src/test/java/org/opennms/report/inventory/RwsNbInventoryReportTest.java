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

package org.opennms.report.inventory;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class RwsNbInventoryReportTest extends XmlTestNoCastor<RwsNbinventoryreport> {

    public RwsNbInventoryReportTest(RwsNbinventoryreport sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/rws-nbinventoryreport.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getMinimalReport(),
                    "<rws-nbinventoryreport/>"
                },
                {
                    getReport(),
                    "<rws-nbinventoryreport xmlns=\"http://xmlns.opennms.org/xsd/report/inventory\">\n" + 
                    "   <groupSet>\n" + 
                    "      <groupSetName>some-name</groupSetName>" + 
                    "      <nbisinglenode>\n" + 
                    "         <inventoryElement2RP>\n" + 
                    "            <inventoryMemoryRP/>\n" + 
                    "            <inventorySoftwareRP/>\n" + 
                    "            <tupleRP/>\n" + 
                    "         </inventoryElement2RP>\n" + 
                    "      </nbisinglenode>\n" + 
                    "   </groupSet>\n" + 
                    "</rws-nbinventoryreport>"
                }
        });
    }

    private static RwsNbinventoryreport getMinimalReport() {
        return new RwsNbinventoryreport();
    }

    private static RwsNbinventoryreport getReport() {
        RwsNbinventoryreport report = new RwsNbinventoryreport();

        GroupSet groupSet = new GroupSet();
        groupSet.setGroupSetName("some-name");
        report.addGroupSet(groupSet);

        Nbisinglenode node = new Nbisinglenode();
        groupSet.addNbisinglenode(node);

        InventoryElement2RP element = new InventoryElement2RP();
        node.addInventoryElement2RP(element);

        InventoryMemoryRP memory = new InventoryMemoryRP();
        element.addInventoryMemoryRP(memory);

        InventorySoftwareRP software = new InventorySoftwareRP();
        element.addInventorySoftwareRP(software);

        TupleRP tuple = new TupleRP();
        element.addTupleRP(tuple);

        return report;
    }
}
