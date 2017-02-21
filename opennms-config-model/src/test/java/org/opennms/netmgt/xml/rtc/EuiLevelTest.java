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

package org.opennms.netmgt.xml.rtc;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EuiLevelTest extends XmlTestNoCastor<EuiLevel> {

    public EuiLevelTest(EuiLevel sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/rtceui.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        EuiLevel euiLevel = new EuiLevel();
        Header header = new Header();
        header.setVer("1");
        header.setCreated("0");
        header.setMstation("Default");
        euiLevel.setHeader(header);

        Category category = new Category();
        category.setCatlabel("Servers");
        category.setCatvalue(99.99);

        Node node = new Node();
        node.setNodeid(1);
        node.setNodevalue(99.99);
        node.setNodesvccount(1);
        node.setNodesvcdowncount(0);
        category.getNode().add(node);
        euiLevel.getCategory().add(category);

        return Arrays.asList(new Object[][] {
            {
                euiLevel,
                    "<euiLevel xmlns=\"http://xmlns.opennms.org/xsd/rtceui\">\n" + 
                    "   <header>\n" + 
                    "      <ver>1</ver>\n" + 
                    "      <created>0</created>\n" + 
                    "      <mstation>Default</mstation>\n" + 
                    "   </header>\n" + 
                    "   <category>\n" + 
                    "      <catlabel>Servers</catlabel>\n" + 
                    "      <catvalue>99.99</catvalue>\n" + 
                    "      <node>\n" + 
                    "         <nodeid>1</nodeid>\n" + 
                    "         <nodevalue>99.99</nodevalue>\n" + 
                    "         <nodesvccount>1</nodesvccount>\n" + 
                    "         <nodesvcdowncount>0</nodesvcdowncount>\n" + 
                    "      </node>\n" + 
                    "   </category>\n" + 
                    "</euiLevel>" }
        });
    }
}
