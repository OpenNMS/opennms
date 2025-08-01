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
