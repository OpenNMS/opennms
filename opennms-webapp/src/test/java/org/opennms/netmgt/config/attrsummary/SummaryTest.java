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
package org.opennms.netmgt.config.attrsummary;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SummaryTest extends XmlTestNoCastor<Summary> {

    public SummaryTest(final Summary sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Resource emptyResource = new Resource("foo");
        final Attribute emptyAttr = new Attribute("afoo");
        final Attribute attrWithStuff = new Attribute("abar", 1.0d, 2.0d, 3.0d);

        final Resource resourceWithAttribute = new Resource("bar");
        resourceWithAttribute.addAttribute(emptyAttr);

        final Resource resourceWithMultipleAttributes = new Resource("baz");
        resourceWithMultipleAttributes.addAttribute(emptyAttr);
        resourceWithMultipleAttributes.addAttribute(attrWithStuff);

        final Resource resourceWithSubResource = new Resource("quux");
        resourceWithSubResource.addAttribute(emptyAttr);
        resourceWithSubResource.addResource(emptyResource);
        resourceWithSubResource.addResource(resourceWithMultipleAttributes);

        final Summary empty = new Summary();
        final Summary withEmptyResource = new Summary(emptyResource);
        final Summary withResourceWithAttribute = new Summary(resourceWithAttribute);
        final Summary withResourceWithMultipleAttributes = new Summary(resourceWithMultipleAttributes);
        final Summary withResourceWithSubResource = new Summary(resourceWithSubResource);
        
        return Arrays.asList(new Object[][] {
            {
                empty,
                "<summary></summary>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withEmptyResource,
                "<summary>"
                + "<resource>"
                + "<name>foo</name>"
                + "</resource>"
                + "</summary>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withResourceWithAttribute,
                "<summary>"
                + "<resource>"
                + "<name>bar</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "</resource>"
                + "</summary>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withResourceWithMultipleAttributes,
                "<summary>"
                + "<resource>"
                + "<name>baz</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "<attribute>"
                +   "<name>abar</name>"
                +   "<min>1.0</min>"
                +   "<average>2.0</average>"
                +   "<max>3.0</max>"
                + "</attribute>"
                + "</resource>"
                + "</summary>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withResourceWithSubResource,
                "<summary>"
                + "<resource>"
                + "<name>quux</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "<resource>"
                +   "<name>foo</name>"
                + "</resource>"
                + "<resource>"
                +   "<name>baz</name>"
                +   "<attribute>"
                +     "<name>afoo</name>"
                +   "</attribute>"
                +   "<attribute>"
                +     "<name>abar</name>"
                +     "<min>1.0</min>"
                +     "<average>2.0</average>"
                +     "<max>3.0</max>"
                +   "</attribute>"
                + "</resource>"
                + "</resource>"
                + "</summary>",
                "src/main/resources/xsds/attr-summary.xsd"
            }
        });
    }
}