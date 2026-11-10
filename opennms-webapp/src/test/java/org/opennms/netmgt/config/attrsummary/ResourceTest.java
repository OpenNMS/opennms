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

public class ResourceTest extends XmlTestNoCastor<Resource> {

    public ResourceTest(final Resource sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Resource emptyResource = new Resource("foo");
        final Attribute emptyAttr = new Attribute("afoo");
        final Attribute attrWithStuff = new Attribute("abar", 1.0d, 2.0d, 3.0d);

        final Resource withAttribute = new Resource("bar");
        withAttribute.addAttribute(emptyAttr);

        final Resource withMultipleAttributes = new Resource("baz");
        withMultipleAttributes.addAttribute(emptyAttr);
        withMultipleAttributes.addAttribute(attrWithStuff);

        final Resource withSubResource = new Resource("quux");
        withSubResource.addAttribute(emptyAttr);
        withSubResource.addResource(emptyResource);
        withSubResource.addResource(withMultipleAttributes);

        return Arrays.asList(new Object[][] {
            {
                emptyResource,
                "<resource>"
                + "<name>foo</name>"
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withAttribute,
                "<resource>"
                + "<name>bar</name>"
                + "<attribute>"
                +   "<name>afoo</name>"
                + "</attribute>"
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withMultipleAttributes,
                "<resource>"
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
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            },
            {
                withSubResource,
                "<resource>"
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
                + "</resource>",
                "src/main/resources/xsds/attr-summary.xsd"
            }
        });
    }
}